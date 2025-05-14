package org.renxo.deeplinkapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.renxo.deeplinkapplication.networking.NetworkConnectivityManager



@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        private lateinit var _connectivityManager: NetworkConnectivityManager
        val connectivityManager get() = _connectivityManager

    }
    override fun onCreate() {
        super.onCreate()
        _connectivityManager = NetworkConnectivityManager(this)
        // Check for install referrer on first launch
        val deepLinkHandler = DeepLinkHandler(this)
        deepLinkHandler.checkForInstallReferrer()
    }
    override fun onTerminate() {
        super.onTerminate()
        _connectivityManager.cleanup()
    }
}