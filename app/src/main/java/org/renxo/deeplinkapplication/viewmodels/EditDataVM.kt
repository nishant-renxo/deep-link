package org.renxo.deeplinkapplication.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.renxo.deeplinkapplication.models.Addresses
import org.renxo.deeplinkapplication.models.Emails
import org.renxo.deeplinkapplication.models.FieldsModel
import org.renxo.deeplinkapplication.models.PhoneNumbers
import org.renxo.deeplinkapplication.models.Urls
import org.renxo.deeplinkapplication.utils.ContactInfo
import javax.inject.Inject

@HiltViewModel
class EditDataVM @Inject constructor(
    val contactInfo: ContactInfo,
) : BaseViewModel() {
    //    private var initialData: FieldsModel? = null
    var name by mutableStateOf("")
    var showName by mutableStateOf(false)

    var companyName by mutableStateOf("")
    var showCompanyName by mutableStateOf(false)

    var companyLogo by mutableStateOf("")
    var showCompanyLogo by mutableStateOf(false)

    var designation by mutableStateOf("")
    var showDesignation by mutableStateOf(false)

    var jobTitle by mutableStateOf("")
    var showJobTitle by mutableStateOf(false)

    var tagLine by mutableStateOf("")
    var showTagLine by mutableStateOf(false)

    val addressList = mutableStateListOf<String>()
    val emailList = mutableStateListOf<String>()
    val phoneList = mutableStateListOf<String>()
    val urlList = mutableStateListOf<String>()
    val datesList = mutableStateListOf<String>()
    val relationsList = mutableStateListOf<String>()


    fun setData(initialData: FieldsModel?) {
//        initialData = model
        name = initialData?.name ?: ""
        showName = initialData?.name != null

        companyName = initialData?.company_name ?: ""
        showCompanyName = initialData?.company_name != null

        companyLogo = initialData?.company_logo ?: ""
        showCompanyLogo = initialData?.company_logo != null

        designation = initialData?.designation ?: ""
        showDesignation = initialData?.designation != null

        jobTitle = initialData?.job_title ?: ""
        showJobTitle = initialData?.job_title != null

        tagLine = initialData?.tag_line ?: ""
        showTagLine = initialData?.tag_line != null
        addressList.apply {
            clear()
            initialData?.address?.forEach { it?.address?.let(::add) }
        }
        emailList.apply {
            clear()
            initialData?.emails?.forEach { it?.email?.let(::add) }
        }
        phoneList.apply {
            clear()
            initialData?.phone_numbers?.forEach { it?.phone_no?.let(::add) }
        }
        urlList.apply {
            clear()
            initialData?.urls?.forEach { it?.url?.let(::add) }
        }
        datesList.apply {
            clear()
            initialData?.dates?.forEach { it?.let(::add) }
        }
        relationsList.apply {
            clear()
            initialData?.relationships?.forEach { it?.let(::add) }
        }


    }


    private val _submitResult = MutableSharedFlow<FieldsModel>()
    val submitResult = _submitResult.asSharedFlow()

    fun addField(type: FieldType) {
        when (type) {
            FieldType.Name -> showName = true
            FieldType.CompanyName -> showCompanyName = true
            FieldType.CompanyLogo -> showCompanyLogo = true
            FieldType.Designation -> showDesignation = true
            FieldType.JobTitle -> showJobTitle = true
            FieldType.TagLine -> showTagLine = true
        }
    }

    fun submit() {
        val cleanFields = FieldsModel(
            name = name.takeIf { it.isNotBlank() },
            company_name = companyName.takeIf { it.isNotBlank() },
            company_logo = companyLogo.takeIf { it.isNotBlank() },
            designation = designation.takeIf { it.isNotBlank() },
            job_title = jobTitle.takeIf { it.isNotBlank() },
            tag_line = tagLine.takeIf { it.isNotBlank() },
            emails = emailList.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
                ?.map { Emails(it) },
            phone_numbers = phoneList.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
                ?.map { PhoneNumbers(it) },
            urls = urlList.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }?.map { Urls(it) },
            address = addressList.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
                ?.map { Addresses(it) },
            dates = datesList.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() },
            relationships = relationsList.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
        )


        viewModelScope.launch {
            contactInfo.saveContact(cleanFields)


//            _submitResult.emit(cleanFields)
        }
    }

    enum class FieldType {
        Name, CompanyName, CompanyLogo, Designation, JobTitle, TagLine
    }
}
