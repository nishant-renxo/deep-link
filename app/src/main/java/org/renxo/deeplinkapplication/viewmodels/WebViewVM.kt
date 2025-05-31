package org.renxo.deeplinkapplication.viewmodels

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import org.renxo.deeplinkapplication.models.DetailResponse
import org.renxo.deeplinkapplication.models.ParamModel
import org.renxo.deeplinkapplication.models.ResponseModel
import org.renxo.deeplinkapplication.models.User
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.networking.NetworkCallback
import org.renxo.deeplinkapplication.utils.AppConstants
import org.renxo.deeplinkapplication.utils.ContactInfo
import org.renxo.deeplinkapplication.utils.json
import javax.inject.Inject

@HiltViewModel
class WebViewVM @Inject constructor(private val repository: ApiRepository) : BaseViewModel() {
    var fieldsModel: DetailResponse? by mutableStateOf(null)
        private set

    private fun hasWriteContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun saveContact2(context: Context) {
        val fields = fieldsModel?.fields
        context.saveContactComprehensive(
            // Basic information
            name = fields?.name.toString(),
            company = fields?.company_name,
            jobTitle = fields?.job_title,
            primaryPostal = fields?.address?.getOrNull(0)?.address ?: "",
            primaryEmail = fields?.emails?.getOrNull(0)?.email,
            primaryPhone = fields?.phone_numbers?.getOrNull(0)?.phone_no,
            websiteUrl = fields?.urls?.getOrNull(0)?.url,

            secondaryPostal = fields?.address?.getOrNull(1)?.address ?: "",
            secondaryEmail = fields?.emails?.getOrNull(1)?.email,
            secondaryPhone = fields?.phone_numbers?.getOrNull(1)?.phone_no,
            blogWebsite = fields?.urls?.getOrNull(1)?.url,


            tertiaryPostal = fields?.address?.getOrNull(2)?.address ?: "",
            tertiaryEmail = fields?.emails?.getOrNull(2)?.email,
            tertiaryPhone = fields?.phone_numbers?.getOrNull(2)?.phone_no,
            workWebsite = fields?.urls?.getOrNull(2)?.url,

            otherWebsite = fields?.urls?.getOrNull(3)?.url,
        )

    }



    fun saveContact(context: Context) {
        ContactInfo(context)
    }

    fun saveContact3(context: Context) {

        context.saveContactComprehensive(
            // Basic information
            name = "RonilTheGreat",//1
            namePrefix = "Dr.",
            nameSuffix = "Ph.D.",
            firstName = "Ronil",
            middleName = "Kumar",
            lastName = "Sharma",
            phoneticName = "Ronil My Friend",//2
            phoneticFirstName = "Ro-nil",
            phoneticMiddleName = "Ku-mar",
            phoneticLastName = "Shar-ma",
            nickname = "Roni",//5
            company = "Awesome Technologies Inc.",//3
            jobTitle = "Chief Innovation Officer",//4
            // Phone number [Work ,Home,Main,Custom]
            // Email [Work, Home,Custom]
            // Address [Work ,Home,Other,Custom]
            //Date     [Birthday Anniversary,Custom]
            //single RelationShip   [Assistant,Brother,Child,Domestic Partner,Father,/friend,Manager,Mother,Parent,Partner,Relative,Spouse,Sister,Custom]

            notes = "Met at the tech conference in Mumbai. Interested in AI and mobile development.",//4

            // Phone numbers
            primaryPhone = "+91 98765 43210",
            primaryPhoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            primaryPhoneIsPrimary = true,
            secondaryPhone = "+91 2345 678901",
            secondaryPhoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME,
            tertiaryPhone = "+91 87654 32109",
            tertiaryPhoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK,
            workPhone = "+91 44556 67788",
            homePhone = "+91 22334 45566",
            mobilePhone = "+91 99887 76655",
            faxWorkPhone = "+91 11223 34455",
            faxHomePhone = "+91 55667 78899",
            pagerPhone = "+91 66778 89900",
            otherPhone = "+91 77889 90011",
            callbackPhone = "+91 88990 01122",
            carPhone = "+91 99001 12233",
            companyMainPhone = "+91 11224 45566",
            internetCallPhone = "ronil.thegreat@sip.example.com",

            // Email addresses
            primaryEmail = "ronil.personal@email.com",
            primaryEmailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME,
            primaryEmailIsPrimary = true,
            secondaryEmail = "ronil.work@awesome-tech.com",
            secondaryEmailType = ContactsContract.CommonDataKinds.Email.TYPE_WORK,
            tertiaryEmail = "ronil.other@email.com",
            tertiaryEmailType = ContactsContract.CommonDataKinds.Email.TYPE_OTHER,
            workEmail = "cio@awesome-tech.com",
            homeEmail = "ronil.home@email.com",
            otherEmail = "ronil.backup@email.com",
            mobileEmail = "ronil.mobile@email.com",

            // Postal addresses
            primaryPostal = "123 Green Street, Model Town, Panipat, Haryana 132103",
            primaryPostalType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME,
            primaryPostalIsPrimary = true,
            secondaryPostal = "Awesome Tech Building, IT Park, Cyber City, Gurugram, Haryana 122002",
            secondaryPostalType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK,
            tertiaryPostal = "456 Lake View Apartments, Sector 14, Rohtak, Haryana 124001",
            tertiaryPostalType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER,

            // Structured postal components
            homeStreet = "123 Green Street",
            homeCity = "Model Town, Panipat",
            homeRegion = "Haryana",
            homePostcode = "132103",
            homeCountry = "India",
            workStreet = "Awesome Tech Building, IT Park",
            workCity = "Gurugram",
            workRegion = "Haryana",
            workPostcode = "122002",
            workCountry = "India",

            // IM information
            imHandle = "ronil.awesome",
            imProtocol = ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK,
            imIsPrimary = true,
            aimHandle = "ronilAIM",
            msnHandle = "ronilMSN",
            yahooHandle = "ronilYahoo",
            skypeHandle = "ronil.thegreat",
            qqHandle = "ronilQQ",
            icqHandle = "ronilICQ",
            jabberHandle = "ronil@jabber.org",

            // Website
            websiteUrl = "https://www.ronilthegreat.com",
            websiteType = ContactsContract.CommonDataKinds.Website.TYPE_HOME,
            workWebsite = "https://www.awesome-tech.com",
            homeWebsite = "https://www.ronilpersonal.com",
            blogWebsite = "https://blog.ronilthegreat.com",
            profileWebsite = "https://linkedin.com/in/ronilthegreat",
            otherWebsite = "https://github.com/ronilthegreat",

            // Events / Dates
            birthday = "1985-05-15",
            anniversary = "2010-11-20",
            customDate1 = "2015-07-10",
            customDate1Label = "Joined Awesome Tech",
            customDate2 = "2020-03-25",
            customDate2Label = "Promotion to CIO",

            // Relationships
            spouse = "Priya Sharma",
            child = "Anika Sharma",
            mother = "Sunita Sharma",
            father = "Rajesh Sharma",
            parent = "Ramesh Sharma",
            brother = "Sunil Sharma",
            sister = "Anjali Sharma",
            friend = "Vikram Patel",
            relative = "Pankaj Kumar",
            manager = "Amit Verma",
            assistant = "Neha Singh",
            referredBy = "Rahul Gupta",
            partner = "Sanjay Mehta",
            domesticPartner = "N/A",
            customRelation = "Mentor",
            customRelationLabel = "Career Mentor",
            customTypeList = listOf(Pair("Sir", "DOM Don"), Pair("Sir22", "DOM 4556464"))
        )

    }


    private fun Context.saveContactComprehensive(
        // Basic information
        name: String,
        namePrefix: String? = null,
        nameSuffix: String? = null,
        firstName: String? = null,
        middleName: String? = null,
        lastName: String? = null,
        phoneticName: String? = null,
        phoneticFirstName: String? = null,
        phoneticMiddleName: String? = null,
        phoneticLastName: String? = null,
        nickname: String? = null,
        company: String? = null,
        jobTitle: String? = null,
        notes: String? = null,

        // Phone numbers
        primaryPhone: String? = null,
        primaryPhoneType: Int = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
        primaryPhoneIsPrimary: Boolean = true,
        secondaryPhone: String? = null,
        secondaryPhoneType: Int = ContactsContract.CommonDataKinds.Phone.TYPE_HOME,
        tertiaryPhone: String? = null,
        tertiaryPhoneType: Int = ContactsContract.CommonDataKinds.Phone.TYPE_WORK,
        workPhone: String? = null,
        homePhone: String? = null,
        mobilePhone: String? = null,
        faxWorkPhone: String? = null,
        faxHomePhone: String? = null,
        pagerPhone: String? = null,
        otherPhone: String? = null,
        callbackPhone: String? = null,
        carPhone: String? = null,
        companyMainPhone: String? = null,
        isdnPhone: String? = null,
        mainPhone: String? = null,
        otherFaxPhone: String? = null,
        radioPhone: String? = null,
        telexPhone: String? = null,
        ttyTddPhone: String? = null,
        workMobilePhone: String? = null,
        workPagerPhone: String? = null,
        assistantPhone: String? = null,
        mmsPhone: String? = null,
        internetCallPhone: String? = null, // SIP address for internet calling

        // Email addresses
        primaryEmail: String? = null,
        primaryEmailType: Int = ContactsContract.CommonDataKinds.Email.TYPE_HOME,
        primaryEmailIsPrimary: Boolean = true,
        secondaryEmail: String? = null,
        secondaryEmailType: Int = ContactsContract.CommonDataKinds.Email.TYPE_WORK,
        tertiaryEmail: String? = null,
        tertiaryEmailType: Int = ContactsContract.CommonDataKinds.Email.TYPE_OTHER,
        workEmail: String? = null,
        homeEmail: String? = null,
        otherEmail: String? = null,
        mobileEmail: String? = null,

        // Postal addresses
        primaryPostal: String? = null,
        primaryPostalType: Int = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME,
        primaryPostalIsPrimary: Boolean = true,
        secondaryPostal: String? = null,
        secondaryPostalType: Int = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK,
        tertiaryPostal: String? = null,
        tertiaryPostalType: Int = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER,
        // Structured postal components
        homeStreet: String? = null,
        homeCity: String? = null,
        homeRegion: String? = null, // State/Province
        homePostcode: String? = null,
        homeCountry: String? = null,
        workStreet: String? = null,
        workCity: String? = null,
        workRegion: String? = null, // State/Province
        workPostcode: String? = null,
        workCountry: String? = null,

        // IM information
        imHandle: String? = null,
        imProtocol: Int = ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK,
        imIsPrimary: Boolean = true,
        aimHandle: String? = null,
        msnHandle: String? = null,
        yahooHandle: String? = null,
        skypeHandle: String? = null,
        qqHandle: String? = null,
        icqHandle: String? = null,
        jabberHandle: String? = null,

        // Website
        websiteUrl: String? = null,
        websiteType: Int = ContactsContract.CommonDataKinds.Website.TYPE_HOME,
        workWebsite: String? = null,
        homeWebsite: String? = null,
        blogWebsite: String? = null,
        profileWebsite: String? = null,
        otherWebsite: String? = null,

        // Events / Dates
        birthday: String? = null, // Format: yyyy-MM-dd
        anniversary: String? = null, // Format: yyyy-MM-dd
        customDate1: String? = null,
        customDate1Label: String? = null,
        customDate2: String? = null,
        customDate2Label: String? = null,

        // Relationships
        spouse: String? = null,
        child: String? = null,
        mother: String? = null,
        father: String? = null,
        parent: String? = null,
        brother: String? = null,
        sister: String? = null,
        friend: String? = null,
        relative: String? = null,
        manager: String? = null,
        assistant: String? = null,
        referredBy: String? = null,
        partner: String? = null,
        domesticPartner: String? = null,
        customRelation: String? = null,
        customRelationLabel: String? = null,
        customTypeList: List<Pair<String, String>> = emptyList(),
    ) {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE

            // Basic information
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            phoneticName?.let { putExtra(ContactsContract.Intents.Insert.PHONETIC_NAME, it) }
            company?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
            jobTitle?.let { putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it) }
            notes?.let { putExtra(ContactsContract.Intents.Insert.NOTES, it) }

            // Phone numbers
            primaryPhone?.let {
                putExtra(ContactsContract.Intents.Insert.PHONE, it)
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, primaryPhoneType)
                putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, primaryPhoneIsPrimary)
            }

            secondaryPhone?.let {
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, it)
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, secondaryPhoneType)
            }

            tertiaryPhone?.let {
                putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, it)
                putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, tertiaryPhoneType)
            }

            // Email addresses
            primaryEmail?.let {
                putExtra(ContactsContract.Intents.Insert.EMAIL, it)
                putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, primaryEmailType)
                putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY, primaryEmailIsPrimary)
            }

            secondaryEmail?.let {
                putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, it)
                putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, secondaryEmailType)
            }

            tertiaryEmail?.let {
                putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, it)
                putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, tertiaryEmailType)
            }

            // Postal addresses
            primaryPostal?.let {
                putExtra(ContactsContract.Intents.Insert.POSTAL, it)
                putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, primaryPostalType)
                putExtra(ContactsContract.Intents.Insert.POSTAL_ISPRIMARY, primaryPostalIsPrimary)
            }

            /*  secondaryPostal?.let {
                  putExtra(ContactsContract.Intents.Insert.SECONDARY_POSTAL, it)
                  putExtra(ContactsContract.Intents.Insert.SECONDARY_POSTAL_TYPE, secondaryPostalType)
              }

              tertiaryPostal?.let {
                  putExtra(ContactsContract.Intents.Insert.TERTIARY_POSTAL, it)
                  putExtra(ContactsContract.Intents.Insert.TERTIARY_POSTAL_TYPE, tertiaryPostalType)
              }*/

            // IM information
            imHandle?.let {
                putExtra(ContactsContract.Intents.Insert.IM_HANDLE, it)
                putExtra(ContactsContract.Intents.Insert.IM_PROTOCOL, imProtocol)
                putExtra(ContactsContract.Intents.Insert.IM_ISPRIMARY, imIsPrimary)
            }

            // Create array list for additional data that's not directly supported by intent extras
            val dataList = ArrayList<ContentValues>()

            customTypeList.forEach {

                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it.first)
                    put(
                        ContactsContract.CommonDataKinds.Nickname.TYPE,
                        ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM
                    )
                    put(ContactsContract.CommonDataKinds.Nickname.LABEL, it.second)
                    dataList.add(this)
                }
            }


            // Add name components if provided
            if (namePrefix != null || firstName != null || middleName != null ||
                lastName != null || nameSuffix != null ||
                phoneticFirstName != null || phoneticMiddleName != null || phoneticLastName != null
            ) {

                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )

                    // Name components
                    namePrefix?.let {
                        put(ContactsContract.CommonDataKinds.StructuredName.PREFIX, it)
                    }
                    firstName?.let {
                        put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, it)
                    }
                    middleName?.let {
                        put(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, it)
                    }
                    lastName?.let {
                        put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, it)
                    }
                    nameSuffix?.let {
                        put(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, it)
                    }

                    // Phonetic name components
                    phoneticFirstName?.let {
                        put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, it)
                    }
                    phoneticMiddleName?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME,
                            it
                        )
                    }
                    phoneticLastName?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME,
                            it
                        )
                    }

                    dataList.add(this)
                }
            }

            // Add nickname if provided
            nickname?.let {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it)
                    put(
                        ContactsContract.CommonDataKinds.Nickname.TYPE,
                        ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT
                    )
                    dataList.add(this)
                }
            }

            // Add additional phone numbers if provided
            val additionalPhones = mutableListOf<Pair<String?, Int>>()
            if (workPhone != null) additionalPhones.add(
                Pair(
                    workPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                )
            )
            if (homePhone != null) additionalPhones.add(
                Pair(
                    homePhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME
                )
            )
            if (mobilePhone != null) additionalPhones.add(
                Pair(
                    mobilePhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
            )
            if (faxWorkPhone != null) additionalPhones.add(
                Pair(
                    faxWorkPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK
                )
            )
            if (faxHomePhone != null) additionalPhones.add(
                Pair(
                    faxHomePhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME
                )
            )
            if (pagerPhone != null) additionalPhones.add(
                Pair(
                    pagerPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_PAGER
                )
            )
            if (otherPhone != null) additionalPhones.add(
                Pair(
                    otherPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                )
            )
            if (callbackPhone != null) additionalPhones.add(
                Pair(
                    callbackPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK
                )
            )
            if (carPhone != null) additionalPhones.add(
                Pair(
                    carPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_CAR
                )
            )
            if (companyMainPhone != null) additionalPhones.add(
                Pair(
                    companyMainPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN
                )
            )
            if (isdnPhone != null) additionalPhones.add(
                Pair(
                    isdnPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_ISDN
                )
            )
            if (mainPhone != null) additionalPhones.add(
                Pair(
                    mainPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MAIN
                )
            )
            if (otherFaxPhone != null) additionalPhones.add(
                Pair(
                    otherFaxPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX
                )
            )
            if (radioPhone != null) additionalPhones.add(
                Pair(
                    radioPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_RADIO
                )
            )
            if (telexPhone != null) additionalPhones.add(
                Pair(
                    telexPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_TELEX
                )
            )
            if (ttyTddPhone != null) additionalPhones.add(
                Pair(
                    ttyTddPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD
                )
            )
            if (workMobilePhone != null) additionalPhones.add(
                Pair(
                    workMobilePhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE
                )
            )
            if (workPagerPhone != null) additionalPhones.add(
                Pair(
                    workPagerPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER
                )
            )
            if (assistantPhone != null) additionalPhones.add(
                Pair(
                    assistantPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT
                )
            )
            if (mmsPhone != null) additionalPhones.add(
                Pair(
                    mmsPhone,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MMS
                )
            )

            // Add SIP address for internet calling
            if (internetCallPhone != null) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS, internetCallPhone)
                    put(
                        ContactsContract.CommonDataKinds.SipAddress.TYPE,
                        ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME
                    )
                    dataList.add(this)
                }
            }

            // Add additional phone numbers to the data list
            for ((phone, type) in additionalPhones) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    put(ContactsContract.CommonDataKinds.Phone.TYPE, type)
                    dataList.add(this)
                }
            }

            // Add additional email addresses if provided
            val additionalEmails = mutableListOf<Pair<String?, Int>>()
            if (workEmail != null) additionalEmails.add(
                Pair(
                    workEmail,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                )
            )
            if (homeEmail != null) additionalEmails.add(
                Pair(
                    homeEmail,
                    ContactsContract.CommonDataKinds.Email.TYPE_HOME
                )
            )
            if (otherEmail != null) additionalEmails.add(
                Pair(
                    otherEmail,
                    ContactsContract.CommonDataKinds.Email.TYPE_OTHER
                )
            )
            if (mobileEmail != null) additionalEmails.add(
                Pair(
                    mobileEmail,
                    ContactsContract.CommonDataKinds.Email.TYPE_MOBILE
                )
            )

            // Add additional email addresses to the data list
            for ((email, type) in additionalEmails) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                    put(ContactsContract.CommonDataKinds.Email.TYPE, type)
                    dataList.add(this)
                }
            }

            // Add structured postal addresses if components are provided
            if (homeStreet != null || homeCity != null || homeRegion != null || homePostcode != null || homeCountry != null) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
                    put(
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
                    )
                    homeStreet?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                            it
                        )
                    }
                    homeCity?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                            it
                        )
                    }
                    homeRegion?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                            it
                        )
                    }
                    homePostcode?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                            it
                        )
                    }
                    homeCountry?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                            it
                        )
                    }
                    dataList.add(this)
                }
            }

            if (workStreet != null || workCity != null || workRegion != null || workPostcode != null || workCountry != null) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
                    put(
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
                    )
                    workStreet?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                            it
                        )
                    }
                    workCity?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                            it
                        )
                    }
                    workRegion?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                            it
                        )
                    }
                    workPostcode?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                            it
                        )
                    }
                    workCountry?.let {
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                            it
                        )
                    }
                    dataList.add(this)
                }
            }

            // Add additional IM handles if provided
            val additionalImHandles = mutableListOf<Pair<String?, Int>>()
            if (aimHandle != null) additionalImHandles.add(
                Pair(
                    aimHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM
                )
            )
            if (msnHandle != null) additionalImHandles.add(
                Pair(
                    msnHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN
                )
            )
            if (yahooHandle != null) additionalImHandles.add(
                Pair(
                    yahooHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO
                )
            )
            if (skypeHandle != null) additionalImHandles.add(
                Pair(
                    skypeHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE
                )
            )
            if (qqHandle != null) additionalImHandles.add(
                Pair(
                    qqHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ
                )
            )
            if (icqHandle != null) additionalImHandles.add(
                Pair(
                    icqHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ
                )
            )
            if (jabberHandle != null) additionalImHandles.add(
                Pair(
                    jabberHandle,
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER
                )
            )

            // Add additional IM handles to the data list
            for ((handle, protocol) in additionalImHandles) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Im.DATA, handle)
                    put(ContactsContract.CommonDataKinds.Im.PROTOCOL, protocol)
                    dataList.add(this)
                }
            }

            // Add website data
            val websites = mutableListOf<Pair<String?, Int>>()
            if (websiteUrl != null) websites.add(Pair(websiteUrl, websiteType))
            if (workWebsite != null) websites.add(
                Pair(
                    workWebsite,
                    ContactsContract.CommonDataKinds.Website.TYPE_WORK
                )
            )
            if (homeWebsite != null) websites.add(
                Pair(
                    homeWebsite,
                    ContactsContract.CommonDataKinds.Website.TYPE_HOME
                )
            )
            if (blogWebsite != null) websites.add(
                Pair(
                    blogWebsite,
                    ContactsContract.CommonDataKinds.Website.TYPE_BLOG
                )
            )
            if (profileWebsite != null) websites.add(
                Pair(
                    profileWebsite,
                    ContactsContract.CommonDataKinds.Website.TYPE_PROFILE
                )
            )
            if (otherWebsite != null) websites.add(
                Pair(
                    otherWebsite,
                    ContactsContract.CommonDataKinds.Website.TYPE_OTHER
                )
            )

            for ((url, type) in websites) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Website.URL, url)
                    put(ContactsContract.CommonDataKinds.Website.TYPE, type)
                    dataList.add(this)
                }
            }

            // Add event dates (birthday, anniversary)
            val events = mutableListOf<Triple<String?, Int, String?>>()
            if (birthday != null) events.add(
                Triple(
                    birthday,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY,
                    null
                )
            )
            if (anniversary != null) events.add(
                Triple(
                    anniversary,
                    ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY,
                    null
                )
            )
            if (customDate1 != null) events.add(
                Triple(
                    customDate1,
                    ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM,
                    customDate1Label
                )
            )
            if (customDate2 != null) events.add(
                Triple(
                    customDate2,
                    ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM,
                    customDate2Label
                )
            )

            for ((date, type, label) in events) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Event.START_DATE, date)
                    put(ContactsContract.CommonDataKinds.Event.TYPE, type)
                    if (type == ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM && label != null) {
                        put(ContactsContract.CommonDataKinds.Event.LABEL, label)
                    }
                    dataList.add(this)
                }
            }

            // Add relationship data
            val relationships = mutableListOf<Pair<String?, Int>>()
            if (spouse != null) relationships.add(
                Pair(
                    spouse,
                    ContactsContract.CommonDataKinds.Relation.TYPE_SPOUSE
                )
            )
            if (child != null) relationships.add(
                Pair(
                    child,
                    ContactsContract.CommonDataKinds.Relation.TYPE_CHILD
                )
            )
            if (mother != null) relationships.add(
                Pair(
                    mother,
                    ContactsContract.CommonDataKinds.Relation.TYPE_MOTHER
                )
            )
            if (father != null) relationships.add(
                Pair(
                    father,
                    ContactsContract.CommonDataKinds.Relation.TYPE_FATHER
                )
            )
            if (parent != null) relationships.add(
                Pair(
                    parent,
                    ContactsContract.CommonDataKinds.Relation.TYPE_PARENT
                )
            )
            if (brother != null) relationships.add(
                Pair(
                    brother,
                    ContactsContract.CommonDataKinds.Relation.TYPE_BROTHER
                )
            )
            if (sister != null) relationships.add(
                Pair(
                    sister,
                    ContactsContract.CommonDataKinds.Relation.TYPE_SISTER
                )
            )
            if (friend != null) relationships.add(
                Pair(
                    friend,
                    ContactsContract.CommonDataKinds.Relation.TYPE_FRIEND
                )
            )
            if (relative != null) relationships.add(
                Pair(
                    relative,
                    ContactsContract.CommonDataKinds.Relation.TYPE_RELATIVE
                )
            )
            if (manager != null) relationships.add(
                Pair(
                    manager,
                    ContactsContract.CommonDataKinds.Relation.TYPE_MANAGER
                )
            )
            if (assistant != null) relationships.add(
                Pair(
                    assistant,
                    ContactsContract.CommonDataKinds.Relation.TYPE_ASSISTANT
                )
            )
            if (referredBy != null) relationships.add(
                Pair(
                    referredBy,
                    ContactsContract.CommonDataKinds.Relation.TYPE_REFERRED_BY
                )
            )
            if (partner != null) relationships.add(
                Pair(
                    partner,
                    ContactsContract.CommonDataKinds.Relation.TYPE_PARTNER
                )
            )
            if (domesticPartner != null) relationships.add(
                Pair(
                    domesticPartner,
                    ContactsContract.CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER
                )
            )

            // Add custom relationship if provided
            if (customRelation != null) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Relation.NAME, customRelation)
                    put(
                        ContactsContract.CommonDataKinds.Relation.TYPE,
                        ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM
                    )
                    customRelationLabel?.let {
                        put(ContactsContract.CommonDataKinds.Relation.LABEL, it)
                    }
                    dataList.add(this)
                }
            }

            // Add all relationship data to the data list
            for ((name, type) in relationships) {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Relation.NAME, name)
                    put(ContactsContract.CommonDataKinds.Relation.TYPE, type)
                    dataList.add(this)
                }
            }

            // Add all the additional data if we have any
            if (dataList.isNotEmpty()) {
                putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, dataList)
            }
        }

        startActivity(intent)
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




