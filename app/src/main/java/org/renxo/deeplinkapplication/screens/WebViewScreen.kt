package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    id: String,
    onBackPressed: (() -> Unit),
) {

    val context = LocalContext.current
    val webView = remember { WebView(context) }


    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackPressed.invoke()
        }
    }

    Box(Modifier.fillMaxWidth()) {

        AndroidView(
            factory = {
                webView.apply {
                    Log.e("WebViewScreen", ": $url", )
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(url)

                }
            },
            update = {
                if (it.url != url) {
                    it.loadUrl(it.url!!)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
//        if (viewModel.fieldsModel != null) {

            ExtendedFloatingActionButton(
                onClick = {
//                    viewModel.saveContact(context)
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
//            }
        }
    }



}
