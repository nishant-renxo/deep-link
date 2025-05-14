package org.renxo.deeplinkapplication.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import org.renxo.deeplinkapplication.screens.HomeScreen
import org.renxo.deeplinkapplication.screens.SplashScreen


@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: NavRouts,
) {
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // This ensures proper padding when the keyboard is visible
            .navigationBarsPadding() // Respect bottom nav bar
            .statusBarsPadding(),

        navController = navController,
        startDestination = startDestination,
        ) {

        composable<AppRoutes.Splash> {
            SplashScreen()
        }
        composable<AppRoutes.HomeScreen>(
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
            val productId = backStackEntry.toRoute<AppRoutes.HomeScreen>().productId
            HomeScreen(productId)
        }

    }


}

