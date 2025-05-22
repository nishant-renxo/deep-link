package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.renxo.deeplinkapplication.viewmodels.WebViewVM


@SuppressLint("HardwareIds", "SetJavaScriptEnabled")
@Composable
fun RegisterScreen(
    url: String,
    onBackPressed: (() -> Unit)
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }
    val androidID = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }
    val viewModel: WebViewVM = hiltViewModel<WebViewVM>()

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackPressed.invoke()
        }
    }

    Box(Modifier.fillMaxWidth()) {

        val postData = "androidID=$androidID&key2=value2"
        val encodedPostData = postData.toByteArray(Charsets.UTF_8)

        AndroidView(
            factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    postUrl(url, encodedPostData) // <-- POST request here
                }
            },
            update = {
                if (it.url != url) {
                    it.postUrl(url, encodedPostData)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (viewModel.fieldsModel != null) {

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


@Composable
fun FancyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    )
}


