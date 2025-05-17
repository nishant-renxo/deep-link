package org.renxo.deeplinkapplication.viewmodels

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.FieldsModel
import org.renxo.deeplinkapplication.utils.ImageAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class ScanningVM : ViewModel() {
    // Used to set up a link between the Camera and your UI.
    var color by mutableStateOf(Color.Black)
        private set
    val navEvents = MutableSharedFlow<Navigate>()

    var fieldsModel: FieldsModel? by mutableStateOf(null)
        private set

    var errorValue by mutableStateOf("")
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val _showScanResult = MutableStateFlow(false)
    val showScanResult = _showScanResult.asStateFlow()

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val analyzer: ImageAnalyzer by lazy {
        ImageAnalyzer {
            _showScanResult.value = true // Show the result overlay
            checkIfUrlCorrect(it)
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

        processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            DEFAULT_BACK_CAMERA,
            imageAnalysis,
            cameraPreviewUseCase
        )

        // Keep this coroutine active until the viewmodel is cleared
        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    // Instead of toggling a "scanAgain" flag, just hide the result overlay
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
            val id = url.replace(path, "").toIntOrNull()
            if (id != null) {
                viewModelScope.launch {
                    navEvents.emit(Navigate(id.toString()))
                }
            }else {
                color = Color.Red
                errorValue = "Invalid Url the ID is not Present"
            }
        } else {
            color = Color.Red
            errorValue = "Invalid Url"
        }
    }


    data class Navigate(val id: String)

}
