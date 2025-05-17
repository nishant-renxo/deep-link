package org.renxo.deeplinkapplication.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.renxo.deeplinkapplication.networking.ApiException
import org.renxo.deeplinkapplication.networking.NetworkCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.renxo.deeplinkapplication.MyApplication.Companion.connectivityManager
import kotlin.coroutines.cancellation.CancellationException


abstract class BaseViewModel : ViewModel() {

    private val _exception = MutableSharedFlow<String>()
    val exception = _exception.asSharedFlow()

    private val _showToast = MutableSharedFlow<String>()
    val showToast = _showToast.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun showToast(value: String) {
        viewModelScope.launch {
            _showToast.emit(value)
        }
    }

    fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    fun setException(value: String) {
        viewModelScope.launch {
            _exception.emit(value)
        }
    }

    // Improved CallHelper with proper resource management
    inner class CallingHelper<T>(
        private val autoRetryOnConnectivity: Boolean = false
    ) {
        private var job: Job? = null
        private var connectivityJob: Job? = null

        // Cancel any ongoing operations when ViewModel is cleared
        init {
            viewModelScope.launch {
                @Suppress("DEPRECATION")
                addCloseable {
                    job?.cancel()
                    connectivityJob?.cancel()
                }
            }
        }

        fun launchCall(
            call: suspend () -> Result<T>,
            callback: NetworkCallback<T>
        ) {
            // Cancel previous job if exists
            job?.cancel()
            // Start new job
            job = viewModelScope.launch {
                setLoading(true)
                callback.onProgressing(true)

                try {
                    if (connectivityManager.isConnected.value) {
                        try {
                            val result = call()
                            result.fold(
                                onSuccess = {
                                    callback.onSuccess(it)
                                },
                                onFailure = { error ->
                                    when (error) {
                                        is ApiException -> {
                                            if (error.code == 401) {
                                                // Handle unauthorized access
                                                // logoutAndClearPreference()
                                            }
                                            callback.unKnownErrorFound(error.errorMessage)
                                        }
                                        else -> callback.unKnownErrorFound(error.message ?: "Unknown error")
                                    }
                                }
                            )
                        } catch (e: CancellationException) {
                            // Just propagate cancellation exceptions
                            callback.unKnownErrorFound(e.message ?: "Unknown error")

//                            throw e
                        } catch (e: Exception) {
                            callback.unKnownErrorFound(e.message ?: "Unknown error")
                        }
                    } else {
                        Log.e("CallingHelper", "No internet available")
                        callback.noInternetAvailable()
                        callback.unKnownErrorFound("No Internet Available")

                        if (autoRetryOnConnectivity) {
                            setupConnectivityObserver(call, callback)
                        }
                    }
                } finally {
                    setLoading(false)
                    callback.onProgressing(false)
                }
            }
        }

        private fun setupConnectivityObserver(
            call: suspend () -> Result<T>,
            callback: NetworkCallback<T>
        ) {
            connectivityJob?.cancel()
            connectivityJob = viewModelScope.launch {
                connectivityManager.isConnected.collectLatest { isConnected ->
                    if (isConnected) {
                        callback.onRequestAgainRestarted()
                        // Cancel current connectivity observation
                        connectivityJob?.cancel()
                        // Restart the API call
                        launchCall(call, callback)
                    }
                }
            }
        }
    }
}

