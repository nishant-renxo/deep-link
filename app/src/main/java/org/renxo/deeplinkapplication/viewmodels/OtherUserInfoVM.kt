package org.renxo.deeplinkapplication.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.models.DetailResponse
import org.renxo.deeplinkapplication.models.Emails
import org.renxo.deeplinkapplication.models.FieldsModel
import org.renxo.deeplinkapplication.models.ParamModel
import org.renxo.deeplinkapplication.models.PhoneNumbers
import org.renxo.deeplinkapplication.models.ResponseModel
import org.renxo.deeplinkapplication.models.TemplateData
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.NetworkCallback
import org.renxo.deeplinkapplication.screens.svgTect
import org.renxo.deeplinkapplication.utils.AppConstants
import org.renxo.deeplinkapplication.utils.ContactInfo
import org.renxo.deeplinkapplication.utils.getMap
import org.renxo.deeplinkapplication.utils.json
import javax.inject.Inject


@HiltViewModel
class OtherUserInfoVM @Inject constructor(
    private val repository: ApiRepository,
    val contactInfo: ContactInfo,
) : BaseViewModel() {
    var contact: DetailResponse? by mutableStateOf(null)
    var svg: String? by mutableStateOf(null)


    private val detailCall by lazy { CallingHelper<ResponseModel>() }
    private val authToken =
        "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkZXZpY2VfaWQiOiIiLCJleHAiOjE3NDg3NTI1NjUsImlhdCI6MTc0ODY2NjE2NSwibGFuZ3VhZ2VfaWQiOiJlbi1VUyIsInN1YiI6ImNkZmVmZDhmLTU2NDktNDA5ZC05NDE2LWEyZDAyMmRiMzQyZSIsIndhcmVob3VzZSI6IiJ9.BNoTFyIo7ci2YSLKt4TaYAR_YEFPkjOvvyT3S9Mx3JWeHAuopqeB_S45Nuo4w4vmVayM6VlSM77ZUHxBi1VtyO-7VZm3_Sjho_mhhxGJJWnmmSeBP01jz2-d74eiHs7GrMWp2kDR6F8w5sFDdG8D-4Pnd1_LMZcmEup1j9GCloitZMKKvlBbmDcJXoTYTOlImts0HzaLr4dTCiKUu9AEo_JoLOzog0KscwSv2yaErIH44zKqKtL3rMeKx6lqP-DO1ZPJm5JjOAPIS9pcAmfwPgtfIVi73OSCKq2W20TlkFOSMDCCmK44nJ14490H7RUTZArTceoHNR54VVBRfLfsSW"

    // Data class to hold pending contact information

    fun getContactDetails(id: String, telexPhone: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            contact = DetailResponse(
                fields = FieldsModel(
                    name = "Kartik",
                    phone_numbers = listOf(
                        PhoneNumbers(phone_no = "7014989502"),
                        PhoneNumbers(phone_no = "451554545"),
                        PhoneNumbers(phone_no = "54545454545"),
                        PhoneNumbers(phone_no = "845454545454"),

                        ),
                    emails = listOf(
                        Emails(email = "ronil@gmail.com"),

                        )
                )
            )

            Log.e("unKnownErrorFound", ": $id ${json.encodeToString(contact)}")
            svg = svgTect
        }

        /*
               detailCall.launchCall(call = {
                     repository.getDetail(
                         ParamModel(action = "GetContacts", params = getMap {
                             put("id", id)
                         }), authToken
                     )
                 }, callback = object : NetworkCallback<ResponseModel> {
                     override fun noInternetAvailable() {
                         viewModelScope.launch {
                             // Handle no internet
                         }
                     }

                     override fun unKnownErrorFound(error: String) {
                         Log.e("unKnownErrorFound", ": $error")
                         viewModelScope.launch {
                             // Handle error
                         }
                     }

                     override fun onProgressing(value: Boolean) {
                         // BaseViewModel already handles loading state
                     }

                     override fun onRequestAgainRestarted() {
                         // Optional: notify UI that request is being retried
                     }

                     override fun onSuccess(result: ResponseModel) {
                         Log.e("unKnownErrorFound", ": ${result.params}")
                         if (result.result?.code == AppConstants.SuccessCodes.SUCCESS200) {
                             result.params?.let { params ->
                                 params[AppConstants.Params.contacts]?.let {
                                     json.decodeFromString<DetailResponse>(it).let { con ->
                                         contact = con
                                     }
                                 }
                                 params[AppConstants.Params.template]?.let {
                                     json.decodeFromString<TemplateData>(it).let { con ->
                                         svg = con.svg
                                     }
                                 }
                             }
                         }
                     }
                 })*/
    }


}
/*    fun Context.insertContact(

    ) {
        val ops = ArrayList<ContentProviderOperation>()

        val rawContactInsertIndex = ops.size
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    dataFields?.name
                )
                .build()
        )

        // Phone
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, dataFields?.phone_no)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )

        // Email (optional)
        dataFields?.email?.let {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, it)
                    .withValue(
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK
                    )
                    .build()
            )
        }

        // Company (optional)
        dataFields?.company?.let {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, it)
                    .withValue(
                        ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.TYPE_WORK
                    )
                    .build()
            )
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save contact", Toast.LENGTH_SHORT).show()
        }
    }*/




