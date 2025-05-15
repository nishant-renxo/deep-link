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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.DetailModel
import org.renxo.deeplinkapplication.networking.DetailResponse
import org.renxo.deeplinkapplication.networking.FieldsModel
import org.renxo.deeplinkapplication.networking.NetworkCallback
import org.renxo.deeplinkapplication.utils.ImageAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class ScanningVM @Inject constructor(private val repository: ApiRepository) : BaseViewModel() {
    // Used to set up a link between the Camera and your UI.
    var color by mutableStateOf(Color.Black)
        private set

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
//            scanAgain = false
            _showScanResult.value = true // Show the result overlay
            checkIfUrlCorrect(it)
            Log.e("ImageAnalyzer", ": $it")
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

    private val path = "https://ronil-renxo.github.io/deep-link?product="
    private fun checkIfUrlCorrect(url: String) {

        if (url.contains(path)) {
            val id = url.replace(path, "").toIntOrNull()
            if (id != null) {
                getDetail(
                    id.toString(), success = {
                        _showScanResult.value = true // Show the result overlay
                        it?.fields?.let {
                            color = Color.Black
                            fieldsModel = it
                        } ?: run {
                            color = Color.Red
                            errorValue = "Something went wrong may be the Id was wrong"
                        }
                    })
            }
        } else {
            color = Color.Red
            errorValue = "Invalid Url"
        }
    }

    private val detailCall by lazy { CallingHelper<DetailResponse?>() }

    private fun getDetail(id: String, success: (DetailResponse?) -> Unit) {
        detailCall.launchCall({
            repository.getDetail(
                DetailModel(
                    id
                )
            )
        }, object : NetworkCallback<DetailResponse?> {
            override fun noInternetAvailable() {
                viewModelScope.launch {
//                        errorMessage.emit("Please Check your Internet Connection")
                }
            }

            override fun unKnownErrorFound(error: String) {
                viewModelScope.launch {
//                        errorMessage.emit(error)
                }

            }

            override fun onProgressing(value: Boolean) {
                if (value) {
//                        showCircularProgress = true
                }
            }

            override fun onRequestAgainRestarted() {
            }

            override fun onSuccess(result: DetailResponse?) {
                result.let {
                    viewModelScope.launch {
                        success(result)

                    }
                }
            }

        })
    }
}
