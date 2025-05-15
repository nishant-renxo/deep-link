package org.renxo.deeplinkapplication.navigation

import kotlinx.serialization.Serializable


interface NavRouts

sealed class AppRoutes {
    @Serializable
    data object SplashPage : NavRouts

    @Serializable
    data object ScanningPage : NavRouts

    @Serializable
    data object RegisterPage : NavRouts
    @Serializable
    data object SelectionPage : NavRouts
    @Serializable
    data object WebViewPage : NavRouts

    @Serializable
    data class DeepLinkPage(val productId: String) : NavRouts


}