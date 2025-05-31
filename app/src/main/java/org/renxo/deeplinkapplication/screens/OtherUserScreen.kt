package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.PictureDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caverock.androidsvg.SVG
import org.renxo.deeplinkapplication.viewmodels.OtherUserInfoVM


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OtherUserScreen(
    id: String,
    onBackPressed: (() -> Unit),
) {
    val viewModel: OtherUserInfoVM = hiltViewModel<OtherUserInfoVM>()
    viewModel.svg?.let { ShowSvgCard(it,viewModel) }
}

@Composable
fun ShowSvgCard(
    svgXml: String,
    viewModel: OtherUserInfoVM
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx().toInt() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx().toInt() }

    LaunchedEffect(svgXml, screenWidthPx, screenHeightPx) {
        imageBitmap = try {
            val svg = SVG.getFromString(svgXml)

            // Get original SVG dimensions
            val svgWidth = svg.documentWidth
            val svgHeight = svg.documentHeight

            // For 16:9 image, after rotation it becomes 9:16
            // Calculate dimensions to fit screen width
            val targetWidth = screenWidthPx
            val targetHeight = (targetWidth * svgHeight / svgWidth).toInt()

            svg.setDocumentWidth(targetWidth.toFloat())
            svg.setDocumentHeight(targetHeight.toFloat())

            val drawable = PictureDrawable(svg.renderToPicture())
            val originalBitmap = drawable.toBitmap(targetWidth, targetHeight)

            // Rotate the bitmap by 90 degrees
            val matrix = Matrix().apply {
                postRotate(90f)
            }

            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0, 0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )

            rotatedBitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        imageBitmap?.let { bitmap ->
            val imageWidth = with(density) { bitmap.width.toDp() }
            val imageHeight = with(density) { bitmap.height.toDp() }

            Card(
//                colors = CardDefaults.cardColors(containerColor = Color.Red),
                modifier = Modifier
                    .padding(horizontal = 85.dp)
                    .size(width = imageWidth, height = imageHeight)
                    .clip(RoundedCornerShape(10.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        if (viewModel.contact != null) {
val context= LocalContext.current
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveContact(context)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF2196F3) // Material Blue
            ) {
                // You can use Person icon for contacts or Add icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Add to Contact", color = Color.White)
                    Icon(
                        imageVector = Icons.Default.Person, // Or Icons.Default.Add
                        contentDescription = "Add Contact",
                        tint = Color.White
                    )
                }
            }
        }

    }
}