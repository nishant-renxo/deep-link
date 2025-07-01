package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.PictureDrawable
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.caverock.androidsvg.SVG
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.utils.ContactInfo
import org.renxo.deeplinkapplication.viewmodels.OtherUserInfoVM


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OtherUserScreen(
    viewModel: OtherUserInfoVM
) {
//    val viewModel: OtherUserInfoVM = hiltViewModel<OtherUserInfoVM>()
    val context = LocalContext.current

    // Show duplicate contacts bottom sheet
    if (viewModel.contactInfo.showDuplicateSheet) {
        DuplicateContactsBottomSheet(
            existingContacts = viewModel.contactInfo.duplicateContacts,
            onMergeContact = { contactId ->
                viewModel.viewModelScope.launch {

                    viewModel.contactInfo.mergeWithExistingContact(contactId)
                }
            },
            onAddAsNew = {
                viewModel.viewModelScope.launch {

                    viewModel.contactInfo.saveAsNewContact()
                }
            },
            onDismiss = {
                viewModel.viewModelScope.launch {
                    viewModel.contactInfo.hideDuplicateSheet()
                }
            }
        )
    }

    ShowSvgCard(viewModel.svg, viewModel)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowSvgCard(
    svgXml: String?,
    viewModel: OtherUserInfoVM,
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
    val context= LocalContext.current
    val contactPermissionState = rememberPermissionState(android.Manifest.permission.READ_CONTACTS){
        if (it){
            viewModel.viewModelScope.launch {
                viewModel.contactInfo.saveContact(viewModel.contact?.fields)
            }
        }else{
            Toast.makeText(context, "Please Provide the Permission", Toast.LENGTH_SHORT).show()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        imageBitmap?.let { bitmap ->
            val imageWidth = with(density) { bitmap.width.toDp() }
            val imageHeight = with(density) { bitmap.height.toDp() }

            Card(
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
            ExtendedFloatingActionButton(
                onClick = {
                    if (contactPermissionState.status.isGranted) {
                        viewModel.viewModelScope.launch {
                            viewModel.contactInfo.saveContact(viewModel.contact?.fields)
                        }
                    } else {
                        contactPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF2196F3)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Add to Contact", color = Color.White)
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add Contact",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateContactsBottomSheet(
    existingContacts: List<ContactInfo.ExistingContact>,
    onMergeContact: (Long) -> Unit,
    onAddAsNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Duplicate Contacts Found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "We found existing contacts with matching phone numbers or emails. Choose to merge with an existing contact or add as new.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // List of existing contacts
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(existingContacts) { contact ->
                    ExistingContactItem(
                        contact = contact,
                        onMergeClick = { onMergeContact(contact.id) }
                    )
                }
            }

            // Bottom buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Add as New Contact button
                Button(
                    onClick = onAddAsNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Add as New Contact",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ExistingContactItem(
    contact: ContactInfo.ExistingContact,
    onMergeClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMergeClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Contact name and merge button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contact.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onMergeClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Merge",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone numbers
            if (contact.phoneNumbers.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contact.phoneNumbers.joinToString(", "),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Email addresses
            if (contact.emails.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contact.emails.joinToString(", "),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}