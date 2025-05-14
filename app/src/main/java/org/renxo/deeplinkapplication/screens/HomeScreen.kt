package org.renxo.deeplinkapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.renxo.deeplinkapplication.viewmodels.DeepLinkVM

@Composable
fun HomeScreen(productId: String, viewmodel: DeepLinkVM = hiltViewModel()) {
    Box(
        Modifier
            .fillMaxSize().background(Color.White), contentAlignment = Alignment.Center
    ) {
        if (viewmodel.scannedValue.isNotEmpty()) {
            Text(
                viewmodel.scannedValue,
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
    LaunchedEffect(Unit) {
        productId.toIntOrNull()?.let {
            viewmodel.getDetail(it)
        }
    }
}