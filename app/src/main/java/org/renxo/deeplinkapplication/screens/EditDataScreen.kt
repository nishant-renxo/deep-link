package org.renxo.deeplinkapplication.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.models.FieldsModel
import org.renxo.deeplinkapplication.utils.GetOneTimeBlock
import org.renxo.deeplinkapplication.viewmodels.EditDataVM


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditDataScreen(
    viewModel: EditDataVM,
    onSubmit: (FieldsModel) -> Unit,
) {
    val scope = rememberCoroutineScope()

    GetOneTimeBlock {
        viewModel.submitResult.collect { onSubmit(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DuplicateSheetHandling(viewModel)
        Column(
            modifier = Modifier
                .padding(bottom = 72.dp) // leave space for submit button
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Edit Data", style = MaterialTheme.typography.headlineSmall)

            EditableField("Name", viewModel.name, { viewModel.name = it }, viewModel.showName) {
                viewModel.addField(EditDataVM.FieldType.Name)
            }

            EditableField(
                "Company Name",
                viewModel.companyName,
                { viewModel.companyName = it },
                viewModel.showCompanyName
            ) {
                viewModel.addField(EditDataVM.FieldType.CompanyName)
            }

            EditableField(
                "Company Logo",
                viewModel.companyLogo,
                { viewModel.companyLogo = it },
                viewModel.showCompanyLogo
            ) {
                viewModel.addField(EditDataVM.FieldType.CompanyLogo)
            }

            EditableField(
                "Designation",
                viewModel.designation,
                { viewModel.designation = it },
                viewModel.showDesignation
            ) {
                viewModel.addField(EditDataVM.FieldType.Designation)
            }

            EditableField(
                "Job Title",
                viewModel.jobTitle,
                { viewModel.jobTitle = it },
                viewModel.showJobTitle
            ) {
                viewModel.addField(EditDataVM.FieldType.JobTitle)
            }

            EditableField(
                "Tagline",
                viewModel.tagLine,
                { viewModel.tagLine = it },
                viewModel.showTagLine
            ) {
                viewModel.addField(EditDataVM.FieldType.TagLine)
            }

            SectionList("Addresses", viewModel.addressList)
            SectionList("Emails", viewModel.emailList)
            SectionList("Phone Numbers", viewModel.phoneList)
            SectionList("URLs", viewModel.urlList)
            SectionList("Dates", viewModel.datesList)
            SectionList("Relationships", viewModel.relationsList)

            Spacer(Modifier.height(24.dp)) // space before bottom bar
        }

        val context = LocalContext.current
        val contactPermissionState =
            rememberPermissionState(android.Manifest.permission.READ_CONTACTS) {
                if (it) {
                    viewModel.submit()
                } else {
                    Toast.makeText(context, "Please Provide the Permission", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        // Fixed Submit Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (contactPermissionState.status.isGranted) {
                        viewModel.submit()
                    } else {
                        contactPermissionState.launchPermissionRequest()
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save to Contacts", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun DuplicateSheetHandling(viewModel: EditDataVM) {
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
}


@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showField: Boolean,
    onAddClick: () -> Unit,
) {


    if (showField) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = label,
//                    modifier = Modifier
//                        .size(24.dp)
//                        .padding(end = 8.dp)
//                )

                Column {
                    Text(text = label, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    } else {
        TextButton(
            onClick = onAddClick,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add $label")
            Spacer(Modifier.width(8.dp))
            Text("Add $label")
        }
    }
}

@Composable
private fun SectionList(
    label: String,
    list: SnapshotStateList<String>,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        list.forEachIndexed { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = item,
                        onValueChange = { list[index] = it },
                        label = { Text("$label ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    IconButton(
                        onClick = { list.removeAt(index) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove $label ${index + 1}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = { list.add("") },
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add $label")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add $label")
        }
    }
}
