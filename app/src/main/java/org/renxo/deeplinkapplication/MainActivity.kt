package org.renxo.deeplinkapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.navigation.AppNavGraph
import org.renxo.deeplinkapplication.navigation.AppRoutes
import org.renxo.deeplinkapplication.navigation.NavRouts
import org.renxo.deeplinkapplication.ui.theme.DeepLinkApplicationTheme
import org.renxo.deeplinkapplication.utils.ContactInfo
import org.renxo.deeplinkapplication.utils.LocalMainViewModelProvider


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        getPlayStoreUri(this, createDeepLinkUrl("92"))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        ContactInfo(this)
        setContent {
            App()
        }
//        checkDeeplink(intent)

    }

    @Composable
    fun App() {
        DeepLinkApplicationTheme {
            CompositionLocalProvider(LocalMainViewModelProvider provides viewModel()) {
                AppNavGraph()
            }
        }
    }

    private fun checkDeeplink(intent: Intent?) {
        Log.e("onCreate", ": ${intent?.data}")

        intent?.data?.let { uri ->
            processDeepLink(uri)
        }?.let {
            lifecycleScope.launch {
//                navigate.emit(it)
            }
        }
    }

    private fun processDeepLink(uri: Uri): NavRouts? {
        val id = uri.getQueryParameter("id")
        return if (!id.isNullOrEmpty()) {
            AppRoutes.DeepLinkPage(id)
        } else {
            null
        }
    }
}
