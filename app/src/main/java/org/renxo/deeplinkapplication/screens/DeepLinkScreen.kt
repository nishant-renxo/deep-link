package org.renxo.deeplinkapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.renxo.deeplinkapplication.networking.FieldsModel
import org.renxo.deeplinkapplication.utils.GetOneTimeBlock
import org.renxo.deeplinkapplication.viewmodels.DeepLinkVM

@Composable
fun DeepLinkScreen(productId: String, viewmodel: DeepLinkVM = hiltViewModel(), finish: () -> Unit) {
    BackHandler {
        finish()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        viewmodel.fieldsModel?.let {
            FieldsInfoCard(it)
        }?:run {
            if (viewmodel.errorValue.isNotEmpty()) {
                Text(
                    viewmodel.errorValue,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(10.dp),
                    fontWeight = FontWeight.Bold,
                    color = viewmodel.color,
                    textAlign = TextAlign.Center
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
    GetOneTimeBlock {
        productId.toIntOrNull()?.let {
            viewmodel.getDetail(it)
        }
    }
}


@Composable
fun FieldsInfoCard(model: FieldsModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            model.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (model.job_title != null && model.company != null) {
                Text(
                    text = "${model.job_title} at ${model.company}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            HorizontalDivider()

            model.email?.let {
                InfoRow(label = "📧 Email", value = it)
            }

            model.phone_no?.let {
                InfoRow(label = "📞 Phone", value = it)
            }

            model.address?.let {
                InfoRow(label = "📍 Address", value = it)
            }

            model.website?.let {
                InfoRow(label = "🌐 Website", value = it)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(text = value, fontSize = 15.sp, color = Color.DarkGray)
    }
}
