package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewScreen() {

}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    onBackPressed: (() -> Unit) ,
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

    AndroidView(
        factory = {
            webView.apply {
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
                it.loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
