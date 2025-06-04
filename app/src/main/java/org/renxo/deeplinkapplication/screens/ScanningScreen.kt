package org.renxo.deeplinkapplication.screens

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
fun ScanningScreen(navigate: (Int?, Int?) -> Unit, navigateWithImage: (String) -> Unit = {}) {
    val viewModel: ScanningVM = hiltViewModel()
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    GetOneTimeBlock {
        viewModel.navEvents.collect {
            navigate(
                it.id.toIntOrNull(), it.templateId
            )
        }
    }

    GetOneTimeBlock {
        viewModel.imageNavEvents.collect { imageUri ->
            navigateWithImage(imageUri)
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
                    "Whoops! Looks like we need your camera to work our magic!" + "Don't worry, we just wanna see your pretty face (and maybe some cats).  " + "Grant us permission and let's get this party started!"
                } else {
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
    val isCaptureMode by viewModel.isCaptureMode.collectAsStateWithLifecycle()
    val capturedImage by viewModel.capturedImage.collectAsStateWithLifecycle()
    val showCapturedImage by viewModel.showCapturedImage.collectAsStateWithLifecycle()

    // Bind to camera once when the composable is first composed
    GetOneTimeBlock {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Camera preview (always visible)
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = modifier.then(
                    if (showScanResult && !isCaptureMode) Modifier.alpha(0f) else Modifier
                )
            )
        }

        // Mode Switch at top right (placed after camera to ensure it's on top)
        if (!showCapturedImage) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .zIndex(10f), // Ensure it's above camera
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCaptureMode) "Capture" else "Scan",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isCaptureMode,
                    onCheckedChange = { viewModel.toggleCaptureMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Green,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
        }

        // Capture button (always visible in capture mode)
        if (isCaptureMode && !showScanResult) {
            FloatingActionButton(
                onClick = { viewModel.captureImage(context) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .size(64.dp),
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Capture Image",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Show scan result overlay (only in scan mode)
        if (!isCaptureMode) {
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

        // Show captured image as overlay card
        AnimatedVisibility(
            visible = showCapturedImage,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxSize()
//                    .height(300.dp)
                    .zIndex(Float.MAX_VALUE), // 🔥 Ensure it always stays on top
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Image preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        capturedImage?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Captured Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Cancel button
                        Button(
                            onClick = { viewModel.cancelCapture() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Confirm button
                        Button(
                            onClick = { viewModel.confirmCapture() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Green,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}