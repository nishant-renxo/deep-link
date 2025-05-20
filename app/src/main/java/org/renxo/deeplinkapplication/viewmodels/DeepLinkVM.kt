package org.renxo.deeplinkapplication.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.renxo.deeplinkapplication.networking.ApiRepository
import javax.inject.Inject


class DeepLinkVM :ViewModel() {

    var errorValue by mutableStateOf("")
    var color by mutableStateOf(Color.Black)
        private set



}