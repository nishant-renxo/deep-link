package org.renxo.deeplinkapplication.networking

interface NetworkCallback<T> {
    fun noInternetAvailable()
    fun unKnownErrorFound(error: String)
    fun onSuccess(result: T)
    fun onProgressing(value: Boolean)
    fun onRequestAgainRestarted()
}