package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.renxo.deeplinkapplication.utils.LocalMainViewModelProvider
import java.net.URLEncoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
@SuppressLint("HardwareIds", "SetJavaScriptEnabled")
@Composable
fun EditScreen(
    url: String,
    onBackPressed: (Boolean) -> Unit,
) {
    val token= LocalMainViewModelProvider.current.authToken
    val context = LocalContext.current
    val webView = remember { WebView(context) }
    val androidID = remember {
        Base64.encode(
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                .toByteArray()
        )
        //        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    val postParams = mapOf(
        "param1" to "value1",
        "param2" to "value2"
    )

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackPressed.invoke(false)
        }
    }

    Box(Modifier.fillMaxWidth()) {

//        val postData = "androidID=$androidID&key2=value2"
//        val encodedPostData = postData.toByteArray(Charsets.UTF_8)

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                    }

                    settings.javaScriptEnabled = true

                    // Prepare POST parameters
                    val postData = (postParams + mapOf("android_id" to androidID))
                        .map { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}" }
                        .joinToString("&")
                        .toByteArray()

                    // Inject Bearer token header using Cookie workaround
                    val cookieManager = android.webkit.CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setCookie(url, "Authorization=Bearer $token")

                    // Load the URL with POST data
                    postUrl(url, postData)
                }
            },
            modifier = Modifier.fillMaxSize()
        )




    }
}
