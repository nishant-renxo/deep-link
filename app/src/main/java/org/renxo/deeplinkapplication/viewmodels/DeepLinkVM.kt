package org.renxo.deeplinkapplication.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.DetailModel
import org.renxo.deeplinkapplication.networking.DetailResponse
import org.renxo.deeplinkapplication.networking.FieldsModel
import org.renxo.deeplinkapplication.networking.NetworkCallback
import javax.inject.Inject


@HiltViewModel
class DeepLinkVM @Inject constructor(private val repository: ApiRepository) : BaseViewModel() {

    var errorValue by mutableStateOf("")
    var color by mutableStateOf(Color.Black)
        private set



}