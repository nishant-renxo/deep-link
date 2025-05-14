package org.renxo.deeplinkapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.renxo.deeplinkapplication.navigation.AppNavGraph
import org.renxo.deeplinkapplication.navigation.AppRoutes
import org.renxo.deeplinkapplication.navigation.NavRouts
import org.renxo.deeplinkapplication.ui.theme.DeepLinkApplicationTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        getPlayStoreUri(this, createDeepLinkUrl("92"))
        Log.e("TAG", ": onCreate")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            DeepLinkApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    AppNavigation(intent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e("onNewIntent", ":${intent.data} ")
        setIntent(intent)
        // Update the UI with the new intent
        setContent {
            DeepLinkApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    AppNavigation(intent)
                }
            }
        }
    }
}


@Composable
fun AppNavigation(intent: Intent) {
    val navController = rememberNavController()
    val startDestination = remember {
        intent.data?.let { uri ->
            processDeepLink(uri)
        } ?: AppRoutes.Splash
    }
    Log.e("TAG", ": $startDestination")
    AppNavGraph(navController, startDestination)

}

// Deep link processor
private fun processDeepLink(uri: Uri): NavRouts {

    val productId = uri.getQueryParameter("product")
    Log.e("processDeepLink", ": $uri->>>$productId")
    return if (!productId.isNullOrEmpty()) {
        AppRoutes.HomeScreen(productId)
    } else {
        AppRoutes.Splash
    }
}
