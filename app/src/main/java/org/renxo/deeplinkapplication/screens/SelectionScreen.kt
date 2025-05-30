package org.renxo.deeplinkapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.renxo.deeplinkapplication.utils.GetOneTimeBlock
import org.renxo.deeplinkapplication.utils.LocalMainViewModelProvider
import org.renxo.deeplinkapplication.viewmodels.SelectionVM

@Composable
fun SelectionScreen(
    onScanClick: () -> Unit,
    onShowClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: SelectionVM = viewModel(),
) {
    val mainVM= LocalMainViewModelProvider.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Please select an option",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Scan")
        }
        if (!mainVM.authToken.isNullOrEmpty()) {
            Button(
                onClick = onShowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Show")
            }
        }

        Button(
            onClick = if (viewModel.showButton) onEditClick else onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(if (viewModel.showButton) "Edit" else "Register")
        }
    }
    GetOneTimeBlock {
        viewModel.checkStatus()
    }
}

