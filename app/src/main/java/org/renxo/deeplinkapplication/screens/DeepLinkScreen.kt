package org.renxo.deeplinkapplication.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.renxo.deeplinkapplication.utils.GetOneTimeBlock

@Composable
fun DeepLinkScreen(
    navigate: () -> Unit,
    finish: () -> Unit,
) {
    BackHandler {
        finish()
    }
    GetOneTimeBlock {
        navigate()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

}

