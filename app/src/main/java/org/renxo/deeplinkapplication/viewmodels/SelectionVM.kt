package org.renxo.deeplinkapplication.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.MyApplication.Companion.preferenceManager


class SelectionVM : ViewModel() {
    fun checkStatus() {
        viewModelScope.launch {
            preferenceManager.getAuthToken()?.let {
                showButton = true
            }
        }
    }

    var showButton by mutableStateOf(false)

}