package org.renxo.deeplinkapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.networking.NetworkConnectivityManager
import org.renxo.deeplinkapplication.utils.AppConstants
import org.renxo.deeplinkapplication.utils.PreferenceManager
import org.renxo.deeplinkapplication.utils.getRandomSessionId


@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        private lateinit var _connectivityManager: NetworkConnectivityManager
        val connectivityManager get() = _connectivityManager
        private lateinit var _preferenceManager: PreferenceManager
        val preferenceManager get() = _preferenceManager
    }

    override fun onCreate() {
        super.onCreate()
        _connectivityManager = NetworkConnectivityManager(this)
        // Check for install referrer on first launch
        _preferenceManager =
            PreferenceManager(filesDir.resolve(AppConstants.Preferences.APP_PREFERENCES).absolutePath)
        val deepLinkHandler = DeepLinkHandler(this)
        deepLinkHandler.checkForInstallReferrer()
        checkSessionId()
    }

    private fun checkSessionId() {
        CoroutineScope(Dispatchers.IO).launch {
            if (preferenceManager.getSessionId().isNullOrEmpty()) {
                preferenceManager.setSessionId(getRandomSessionId())
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        _connectivityManager.cleanup()
    }
}