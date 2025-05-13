package org.renxo.deeplinkapplication

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Check for install referrer on first launch
        val deepLinkHandler = DeepLinkHandler(this)
        deepLinkHandler.checkForInstallReferrer()
    }
}