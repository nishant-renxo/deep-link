package org.renxo.deeplinkapplication.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.GenerateTokenRequest
import org.renxo.deeplinkapplication.networking.GenerateTokenResponse
import org.renxo.deeplinkapplication.networking.NetworkCallback
import org.renxo.deeplinkapplication.utils.preferenceManager
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(private val repository: ApiRepository) :
    BaseViewModel() {
    var authToken: String? = null
        private set

    init {
        checkNeedForFetchingDetails()
    }

    private fun checkNeedForFetchingDetails() {
        viewModelScope.launch {
            preferenceManager.getAuthToken().let {

                if (it.isNullOrEmpty()) {
                    preferenceManager.getSessionId()?.let { getToken(it) }
                } else {
                    authToken = it
                }
            }
        }

    }

    private val detailCall by lazy { CallingHelper<GenerateTokenResponse>() }
     fun getToken(id: String) {
        detailCall.launchCall(
            call = {
                repository.getTokenUsingSessionID(
                    GenerateTokenRequest(
                        id,
                    )
                )
            },
            callback = object : NetworkCallback<GenerateTokenResponse> {
                override fun noInternetAvailable() {
                    viewModelScope.launch {
//                        _errorMessage.value = "Please Check your Internet Connection"
                    }
                }

                override fun unKnownErrorFound(error: String) {
                    viewModelScope.launch {
//                        _errorMessage.value = error
                    }
                }

                override fun onProgressing(value: Boolean) {
                    // BaseViewModel already handles loading state
                }

                override fun onRequestAgainRestarted() {
                    // Optional: notify UI that request is being retried
                }

                override fun onSuccess(result: GenerateTokenResponse) {
                    result.token?.let {
                        viewModelScope.launch {
                            preferenceManager.setSessionId(it)
                            authToken = it

                        }
                    }
                }
            }
        )
    }


}