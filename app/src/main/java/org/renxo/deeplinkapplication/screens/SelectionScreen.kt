package org.renxo.deeplinkapplication.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.renxo.deeplinkapplication.R
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
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Options",
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Please select an option",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        CustomButton(
            icon = R.drawable.qr_code_scan,
            label = "Scan",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF0053FF), Color(0xFF6391E0))
            ),
            onClick = onScanClick
        )

        if (!mainVM.authToken.isNullOrEmpty()) {
            CustomButton(
                icon = R.drawable.show_card,
                label = "Show",
                gradient = Brush.horizontalGradient(
                    listOf(Color(0xFFA508C9), Color(0xFF8815BA))
                ),
                onClick = onShowClick
            )
        }

        val editMode = viewModel.showButton
        CustomButton(
            icon = if (editMode) R.drawable.edit_card else R.drawable.register,
            label = if (editMode) "Edit" else "Register",
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFFAB2F52), Color(0xFFBB1C54))
            ),
            onClick = if (editMode) onEditClick else onRegisterClick
        )
    }
    GetOneTimeBlock {
        viewModel.checkStatus()
    }
}

@Composable
fun CustomButton(
    @DrawableRes icon: Int,
    label: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        modifier = Modifier.padding(10.dp)
            .height(70.dp)
            .width(200.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .background(gradient, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(
//            defaultElevation = 10.dp,
//            pressedElevation = 15.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource( icon),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

