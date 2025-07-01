package org.renxo.deeplinkapplication.screens

import android.annotation.SuppressLint
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.renxo.deeplinkapplication.viewmodels.RegisterWebViewVM
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
@SuppressLint("HardwareIds", "SetJavaScriptEnabled")
@Composable
fun RegisterScreen(
    url: String,
    session: String,
    onBackPressed: (Boolean) -> Unit,
) {
    val viewModel: RegisterWebViewVM = hiltViewModel<RegisterWebViewVM>()

    val context = LocalContext.current
    val webView = remember { WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webViewClient = WebViewClient()
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true;
//      settings.allowFileAccess = true;
//      settings.allowContentAccess = true;
//      settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
        loadUrl("$url?session_id=${session}")
    } }
    val androidID = remember {
        Base64.encode(
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                .toByteArray()
        )
        //        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }



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
            factory = {
                webView
            },
            update = {
                val fullUrl = "$url?session_id=$session"
                if (it.url != fullUrl) {
                    webView.loadUrl(fullUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        FloatingActionButton(
            containerColor = Color(0xFF2196F3) // Material Blue
            ,
            modifier = Modifier

                .align(Alignment.BottomEnd)
                .padding(16.dp), onClick = {
                onBackPressed(true)
            }) {
            Icon(
                imageVector = Icons.Default.Home, // Or Icons.Default.Add
                contentDescription = "Home",
                tint = Color.White
            )
        }
/*
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
*/
    }

}

