package org.renxo.deeplinkapplication.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import android.view.Surface
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.models.Addresses
import org.renxo.deeplinkapplication.models.DetailResponse
import org.renxo.deeplinkapplication.models.Emails
import org.renxo.deeplinkapplication.models.FieldsModel
import org.renxo.deeplinkapplication.models.PhoneNumbers
import org.renxo.deeplinkapplication.models.Urls
import org.renxo.deeplinkapplication.utils.ImageAnalyzer
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanningVM : ViewModel() {
    // Existing properties
    var color by mutableStateOf(Color.Black)
        private set
    val navEvents = MutableSharedFlow<Navigate>()
    val imageNavEvents = MutableSharedFlow<DetailResponse>() // New: for image navigation
    var showProcessing by mutableStateOf(false)
    var errorValue by mutableStateOf("")
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val _showScanResult = MutableStateFlow(false)
    val showScanResult = _showScanResult.asStateFlow()

    // New: Capture mode properties
    private val _isCaptureMode = MutableStateFlow(false)
    val isCaptureMode = _isCaptureMode.asStateFlow()

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage = _capturedImage.asStateFlow()

    private val _showCapturedImage = MutableStateFlow(false)
    val showCapturedImage = _showCapturedImage.asStateFlow()

    private var savedImageUri: String? = null

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    // New: Image capture use case with proper rotation
    private val imageCapture = ImageCapture.Builder()
        .setTargetRotation(Surface.ROTATION_0)
        .build()

    private val analyzer: ImageAnalyzer by lazy {
        ImageAnalyzer {
            if (!_isCaptureMode.value) { // Only analyze in scan mode
                _showScanResult.value = true
                checkIfUrlCorrect(it)
            }
        }
    }

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        // Function to bind camera with current mode
        fun bindCamera() {
            try {
                processCameraProvider.unbindAll()
                if (_isCaptureMode.value) {
                    processCameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        DEFAULT_BACK_CAMERA,
                        cameraPreviewUseCase,
                        imageCapture
                    )
                } else {
                    processCameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        DEFAULT_BACK_CAMERA,
                        cameraPreviewUseCase,
                        imageAnalysis
                    )
                }
            } catch (e: Exception) {
                // Handle rebinding errors
                println("Camera binding error: ${e.message}")
            }
        }

        // Initial binding
        bindCamera()

        // Listen for mode changes and rebind camera
        val modeJob = viewModelScope.launch {
            _isCaptureMode.collect {
                bindCamera()
            }
        }

        // Keep this coroutine active until the viewmodel is cleared
        try {
            awaitCancellation()
        } finally {
            modeJob.cancel()
            processCameraProvider.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    fun toggleCaptureMode() {
        _isCaptureMode.value = !_isCaptureMode.value
        // Reset states when switching modes
        resetStates()
    }

    fun captureImage(context: Context) {
        // Ensure we're in capture mode
        if (!_isCaptureMode.value) {
            errorValue = "Switch to capture mode first"
            color = Color.Red
            _showScanResult.value = true
            return
        }

        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "captured_${System.currentTimeMillis()}.jpg"
        )

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        try {
            imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        savedImageUri = imageFile.absolutePath
                        // Load and correct the captured image orientation
                        val bitmap = loadAndFixImageOrientation(imageFile.absolutePath)
                        _capturedImage.value = bitmap
                        _showCapturedImage.value = true
                    }

                    override fun onError(exception: ImageCaptureException) {
                        // Handle error - you might want to show an error message
                        errorValue = "Failed to capture image: ${exception.message}"
                        color = Color.Red
                        _showScanResult.value = true
                        println("ImageCapture error: ${exception.message}")
                    }
                }
            )
        } catch (e: Exception) {
            errorValue = "Camera not ready: ${e.message}"
            color = Color.Red
            _showScanResult.value = true
            println("Capture exception: ${e.message}")
        }
    }

    fun cancelCapture() {
        // Delete the saved image file if it exists
        savedImageUri?.let { uri ->
            File(uri).delete()
        }
        resetCaptureStates()
    }

    fun confirmCapture() {
        savedImageUri?.let { uri ->
            viewModelScope.launch {
                sendImageToServer(File(uri)) {
                    imageNavEvents.emit(it)
                    resetCaptureStates()
                }
            }
        }
    }

    private suspend fun sendImageToServer(file: File, callback: suspend (DetailResponse) -> Unit) {
        showProcessing = true
        delay(1000)
        val dummyDetailResponse = DetailResponse(
            contact_id = 101,
            fields = FieldsModel(
                name = "Ron Sharma",
                company_name = "TechDev Pvt Ltd",
                company_logo = "https://example.com/logo.png",
                designation = "Senior Android Developer",
                job_title = "Mobile Architect",
                tag_line = "Crafting pixel-perfect Android experiences",
                address = listOf(
                    Addresses("221B Baker Street, London"),
                    Addresses("Tech Park, Bengaluru")
                ),
                emails = listOf(
                    Emails("ron@example.com"),
                    Emails("dev.ron@techdev.com")
                ),
                phone_numbers = listOf(
                    PhoneNumbers("+91-9876543210"),
                    PhoneNumbers("+44-20-7946-0958")
                ),
                urls = listOf(
                    Urls("https://ronsharma.dev"),
                    Urls("https://github.com/ronsharma")
                ),
                dates = listOf(
                    "2024-12-01",
                    "2023-05-21"
                ),
                relationships = listOf(
                    "Colleague",
                    "Friend"
                )
            ),

            )

        callback(
            dummyDetailResponse
        )
    }

    private fun resetCaptureStates() {
        _capturedImage.value = null
        _showCapturedImage.value = false
        savedImageUri = null
        showProcessing = false

    }

    private fun resetStates() {
        resetCaptureStates()
        errorValue = ""
        color = Color.Black
        _showScanResult.value = false
        analyzer.isAnalyzeCompleted = false
    }

    fun resumeScanning() {
        errorValue = ""
        color = Color.Black
        analyzer.isAnalyzeCompleted = false
        _showScanResult.value = false
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }

    private val path = "https://ronil-renxo.github.io/deep-link?id="
    private fun checkIfUrlCorrect(url: String) {
        if (url.contains(path)) {
            url.replace(path, "").split("&").apply {
                val id = this[0].toIntOrNull()
                val templateId =
                    if (this.size > 1) this[1].replace("template_id=", "").toIntOrNull() else null
                if (id != null) {
                    viewModelScope.launch {
                        navEvents.emit(Navigate(id.toString(), templateId))
                    }
                } else {
                    color = Color.Red
                    errorValue = "Invalid Url the ID is not Present"
                }
            }
        } else {
            color = Color.Red
            errorValue = "Invalid Url"
        }
    }

    private fun loadAndFixImageOrientation(imagePath: String): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            println("Error loading/rotating image: ${e.message}")
            BitmapFactory.decodeFile(imagePath)
        }
    }

    data class Navigate(val id: String, val templateId: Int? = null)
}