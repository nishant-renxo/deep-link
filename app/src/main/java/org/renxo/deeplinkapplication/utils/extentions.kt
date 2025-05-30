package org.renxo.deeplinkapplication.utils

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.renxo.deeplinkapplication.MyApplication
import org.renxo.deeplinkapplication.ui.theme.AppColors
import org.renxo.deeplinkapplication.viewmodels.MainVM


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetAlertDialogue(onDismissRequest: () -> Unit = {}, content: @Composable () -> Unit) {
    BasicAlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(20.dp))
            .background(AppColors.backgroundColor)
            .padding(vertical = 30.dp, horizontal = 15.dp)
    ) {
        content()
    }
}


@Composable
inline fun GetOneTimeBlock(crossinline block: suspend CoroutineScope.() -> Unit) =
    LaunchedEffect(Unit) {
        block()
    }



val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}


@Composable
fun LockScreenOrientation(orientation: Int) {

    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context as Activity
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // Restore original orientation when navigating away
//            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity.requestedOrientation = originalOrientation
        }
    }
}


inline fun getMap(
    crossinline init: HashMap<String, Any?>.() -> Unit = {}
): HashMap<String, Any?> {
    return HashMap<String, Any?>().apply {
        init()
    }
}

fun getRandomSessionId(length: Int=22): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
val LocalMainViewModelProvider = compositionLocalOf<MainVM> {
    error("No MainVM provided")
}
val preferenceManager = MyApplication.preferenceManager
