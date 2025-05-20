package org.renxo.deeplinkapplication.screens

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.renxo.deeplinkapplication.utils.GetOneTimeBlock
import org.renxo.deeplinkapplication.viewmodels.ScanningVM


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanningScreen(navigate: (Int?,Int?) -> Unit) {
    val viewModel: ScanningVM = hiltViewModel()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    GetOneTimeBlock {
        viewModel.navEvents.collect {
            navigate(
                it.id.toIntOrNull(),it.templateId
            )
        }
    }
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
    ) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreviewContent(viewModel)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
                    .widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // then gently explain why the app requires this permission
                    "Whoops! Looks like we need your camera to work our magic!" + "Don't worry, we just wanna see your pretty face (and maybe some cats).  " + "Grant us permission and let's get this party started!"
                } else {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    "Hi there! We need your camera to work our magic! ✨\n" + "Grant us permission and let's get this party started! \uD83C\uDF89"
                }
                Text(textToShow, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Unleash the Camera!")
                }
            }
        }
    }

}

@Composable
fun CameraPreviewContent(
    viewModel: ScanningVM,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val showScanResult by viewModel.showScanResult.collectAsStateWithLifecycle(false)

    // Bind to camera once when the composable is first composed
    GetOneTimeBlock {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Always keep the camera preview in composition, just control visibility
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = modifier.then(
                    if (showScanResult) Modifier.alpha(0f) else Modifier
                )
            )
        }

        // Show scan result as an overlay when available
        AnimatedVisibility(
            visible = showScanResult,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                    if (viewModel.errorValue.isNotEmpty()) {
                        Text(
                            viewModel.errorValue,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            color = viewModel.color,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = {
                            viewModel.resumeScanning()
                        }) {
                            Text("Scan Again")
                        }
                    } else {
                        CircularProgressIndicator()
                    }
                }



        }
    }
}