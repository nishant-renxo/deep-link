package org.renxo.deeplinkapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import androidx.core.net.toUri


class DeepLinkHandler(private val context: Context) {
    private lateinit var referrerClient: InstallReferrerClient

    fun checkForInstallReferrer() {
        referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established
                        processReferrer()
                    }
                    // Handle other response codes as needed
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Handle service disconnection
            }
        })
    }

    private fun processReferrer() {
        try {
            val response = referrerClient.installReferrer
            val referrerUrl = response.installReferrer

            // Check if referrer contains a deep link
            if (referrerUrl.contains("deep_link")) {
                // Extract the deep link from the referrer
                val deepLinkParam = "deep_link="
                val startIndex = referrerUrl.indexOf(deepLinkParam)
                if (startIndex != -1) {
                    val deepLinkRaw = referrerUrl.substring(startIndex + deepLinkParam.length)
                    val deepLink = Uri.decode(deepLinkRaw)

                    // Launch the app with the deep link
                    val intent = Intent(Intent.ACTION_VIEW, deepLink.toUri())
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }

            // Close the connection
            referrerClient.endConnection()
        } catch (e: Exception) {
            // Handle any exceptions
        }
    }
}


// Step 9: Create URLs for your app that work with or without the app installed
// This is an example function that generates a URL that works for both web and deep linking
fun createDeepLinkUrl(productId: String): String {
    return "https://ronil-renxo.github.io/product/$productId".also {
        Log.e("createDeepLinkUrl", ":$it ", )
    }
}

// Step 10: Add this function to generate Play Store URI with deep link parameter
fun getPlayStoreUri(context: Context, deepLink: String): Uri {
    val packageName = context.packageName
    val encodedDeepLink = Uri.encode(deepLink)
    return "https://play.google.com/store/apps/details?id=$packageName&referrer=deep_link=$encodedDeepLink".toUri().also {
        Log.e("getPlayStoreUri", ":$it ", )

    }
}