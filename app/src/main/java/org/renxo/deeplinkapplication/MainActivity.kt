package org.renxo.deeplinkapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.renxo.deeplinkapplication.navigation.AppNavGraph
import org.renxo.deeplinkapplication.navigation.AppRoutes
import org.renxo.deeplinkapplication.navigation.NavRouts
import org.renxo.deeplinkapplication.ui.theme.DeepLinkApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getPlayStoreUri(this, createDeepLinkUrl("92"))
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


// Step 4: Define the Navigation Component
@Composable
fun AppNavigation(intent: Intent) {
    val navController = rememberNavController()
    val startDestination = remember {
        intent.data?.let { uri ->
            processDeepLink(uri)
        } ?: AppRoutes.Splash
    }
    AppNavGraph(navController, startDestination)

}

// Step 5: Deep link processor
private fun processDeepLink(uri: Uri): NavRouts {
    val path = uri.path
    if (path?.startsWith("/product/") == true) {
        val productId = path.removePrefix("/product/")
        return AppRoutes.HomeScreen(productId)
    }
    return AppRoutes.Splash
}




