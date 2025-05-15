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
    var fieldsModel: FieldsModel? by mutableStateOf(null)
        private set
    var errorValue by mutableStateOf("")
    var color by mutableStateOf(Color.Black)
        private set

    private val detailCall by lazy { CallingHelper<DetailResponse?>() }
    fun getDetail(id: Int) {
        detailCall.launchCall(
            {
                repository.getDetail(
                    DetailModel(
                        id.toString()
                    )
                )
            },
            object : NetworkCallback<DetailResponse?> {
                override fun noInternetAvailable() {
                    viewModelScope.launch {
//                        errorMessage.emit("Please Check your Internet Connection")
                    }
                }

                override fun unKnownErrorFound(error: String) {
                    viewModelScope.launch {
//                        errorMessage.emit(error)
                    }

                }

                override fun onProgressing(value: Boolean) {
                    if (value) {
//                        showCircularProgress = true
                    }
                }

                override fun onRequestAgainRestarted() {
                }

                override fun onSuccess(result: DetailResponse?) {

                    result?.fields?.let {
                        color = Color.Black
                        fieldsModel = it
                        Log.e("onSuccess", ": $fieldsModel")
                    } ?: run {
                        color = Color.Red
                        errorValue = "Something went wrong may be the Id was wrong"
                    }
                }


            }
        )
    }

}