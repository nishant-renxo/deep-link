package org.renxo.deeplinkapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import org.renxo.deeplinkapplication.ui.theme.DeepLinkApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
       val link= getPlayStoreUri(this, createDeepLinkUrl("123"))
        Log.e("onCreate", ":$link ", )
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
    var deepLinkProcessed by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }

        // Define deep link destination
        composable(
            route = "p/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://ronildeveloper.in/p/{productId}"
                },
                navDeepLink {
                    uriPattern = "http://ronildeveloper.in/p/{productId}"
                },
                navDeepLink {
                    uriPattern = "ronildeveloper://p/{productId}"
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(productId = productId, navController = navController)
        }

        // Other destinations
        composable("settings") {
            SettingsScreen(navController)
        }
    }

    // Process deep link if it exists
    if (!deepLinkProcessed) {
        Log.e("processDeepLink", ": ${intent.data}")

        intent.data?.let { uri ->
            // Handle the deep link
            processDeepLink(uri, navController)
            deepLinkProcessed = true
        }
    }
}

// Step 5: Deep link processor
private fun processDeepLink(uri: Uri, navController: androidx.navigation.NavController) {
    val path = uri.path ?: return
    // Extract path components
    if (path.startsWith("/p/")) {
        val productId = path.removePrefix("/p/")
        navController.navigate("p/$productId")
    }
    // Add other deep link paths as needed
}

// Step 6: Screen composables
@Composable
fun HomeScreen(navController: androidx.navigation.NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Red)
    )
}

@Composable
fun ProductDetailScreen(productId: String?, navController: androidx.navigation.NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Green), contentAlignment = Alignment.Center
    ) {
        Text("productID=$productId")
    }
}

@Composable
fun SettingsScreen(navController: androidx.navigation.NavController) {
    // Settings screen
    // ...
}


