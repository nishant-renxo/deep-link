package org.renxo.deeplinkapplication.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import org.renxo.deeplinkapplication.screens.DeepLinkScreen
import org.renxo.deeplinkapplication.screens.RegisterScreen
import org.renxo.deeplinkapplication.screens.ScanningScreen
import org.renxo.deeplinkapplication.screens.SelectionScreen
import org.renxo.deeplinkapplication.screens.SplashScreen
import org.renxo.deeplinkapplication.screens.WebViewScreen
import org.renxo.deeplinkapplication.utils.MyAnimation


@Composable
fun AppNavGraph(
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // This ensures proper padding when the keyboard is visible
            .navigationBarsPadding() // Respect bottom nav bar
            .statusBarsPadding()
    ) { innerPadding ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            navController = navController,
            startDestination = AppRoutes.SplashPage,
            enterTransition = { MyAnimation.myEnterAnimation() },
            exitTransition = { MyAnimation.myExitAnimation() },
            popEnterTransition = { MyAnimation.myEnterAnimation() },
            popExitTransition = { MyAnimation.myExitAnimation() },
        ) {

            composable<AppRoutes.SplashPage> {
                SplashScreen {
                    navController.navigateTo(AppRoutes.SelectionPage, finish = true)
                }
            }
            composable<AppRoutes.ScanningPage> {
                ScanningScreen()
            }
            composable<AppRoutes.RegisterPage> {
                RegisterScreen()
            }
            composable<AppRoutes.WebViewPage> {
                WebViewScreen("http://192.168.31.171:5173/app?screen=template") {
                    navController.finish()
                }
            }
            composable<AppRoutes.SelectionPage> {
                SelectionScreen(onScanClick = {
                    navController.navigateTo(AppRoutes.ScanningPage)
                }, onRegisterClick = {
                    navController.navigateTo(AppRoutes.RegisterPage)
                }, onOpenWebView = {
                    navController.navigateTo(AppRoutes.WebViewPage)
                })
            }
            composable<AppRoutes.DeepLinkPage>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://ronil-renxo.github.io/deep-link?product={productId}"
                    },
                    navDeepLink {
                        uriPattern = "http://ronil-renxo.github.io/deep-link?product={productId}"
                    },
                    navDeepLink {
                        uriPattern = "ronil-renxo.github.io://deep-link?product={productId}"
                    }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.toRoute<AppRoutes.DeepLinkPage>().productId
                DeepLinkScreen(productId) {
                    navController.navigateTo(AppRoutes.SelectionPage, finishAll = true)
                }
            }
        }


    }
}

