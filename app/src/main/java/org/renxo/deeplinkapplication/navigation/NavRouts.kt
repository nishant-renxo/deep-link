package org.renxo.deeplinkapplication.navigation

import kotlinx.serialization.Serializable


interface NavRouts

sealed class AppRoutes {

    @Serializable
    data object Splash : NavRouts

    @Serializable
    data class HomeScreen(val productId:String) : NavRouts


}