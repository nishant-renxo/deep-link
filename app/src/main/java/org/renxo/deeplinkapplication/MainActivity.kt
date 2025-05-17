package org.renxo.deeplinkapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.navigation.AppNavGraph
import org.renxo.deeplinkapplication.navigation.AppRoutes
import org.renxo.deeplinkapplication.navigation.NavRouts
import org.renxo.deeplinkapplication.navigation.navigateTo
import org.renxo.deeplinkapplication.ui.theme.DeepLinkApplicationTheme
import org.renxo.deeplinkapplication.utils.GetOneTimeBlock


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private val navigate: MutableSharedFlow<NavRouts?> = MutableSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        getPlayStoreUri(this, createDeepLinkUrl("92"))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            DeepLinkApplicationTheme {
                    AppNavGraph()

            }
//            GetOneTimeBlock {
//                navigate.collect {
//                    it?.let {
//                        navController.navigateTo(it, finishAll = true)
//                    }
//                }
//            }
        }
//        checkDeeplink(intent)

    }

    private fun checkDeeplink(intent: Intent?) {
        Log.e("onCreate", ": ${intent?.data}", )

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
