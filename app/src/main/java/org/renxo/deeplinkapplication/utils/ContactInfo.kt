package org.renxo.deeplinkapplication.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL



class ContactInfo(private val context: Context) {




    init {
/*
        CoroutineScope(Dispatchers.IO).launch {
            saveContact(
                name = "John Doe",
                phoneticName = "Jon Do",
                preferredName = "Johnny",
                company = "Tech Corp",
                jobTitle = "Developer",
                notes = "Met at the tech conference in Mumbai. Interested in AI and mobile development.",//4
                phoneNumbers = listOf(
                    PhoneNumber("123-456-7890", PhoneType.WORK),
                    PhoneNumber("098-765-4321", PhoneType.HOME)
                ),
                emails = listOf(
                    Email("john@work.com", EmailType.WORK),
                    Email("john@home.com", EmailType.HOME)
                ),
                websites = listOf(
                    Website("https://johndoe.com", WebsiteType.HOME),
                    Website("https://company.com/john", WebsiteType.WORK),
                    Website("https://johnsblog.com", WebsiteType.BLOG),
                    Website("https://linkedin.com/in/johndoe", WebsiteType.PROFILE),
                    Website("https://custom-site.com", WebsiteType.CUSTOM, "Portfolio")
                ),
                addresses = listOf(
                    Address("123 Work St, City, State", AddressType.HOME)
                ),
                dates = listOf(
                    ImportantDate("1990-01-15", DateType.BIRTHDAY)
                ),
                relationships = listOf(
                    Relationship("Jane Doe", RelationType.SPOUSE)
                ),
                logo = "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcR8ItSgne8I1vk75zL-AO-szl1H1mXRNe2VvNwRqZNZPq9lt-M3J-p5ukbwWF_XBM7lsE28Zts0oJLA1bOkHlVx-Q"
            )
        }
*/
    }


    // Data class for existing contact information
    data class ExistingContact(
        val id: Long,
        val name: String,
        val phoneNumbers: List<String>,
        val emails: List<String>
    )

    // Result class for duplicate check
    data class DuplicateCheckResult(
        val existingContacts: List<ExistingContact>,
        val hasMatches: Boolean
    )

    suspend fun checkForDuplicateContacts(
        phoneNumbers: List<PhoneNumber>,
        emails: List<Email>
    ): DuplicateCheckResult = withContext(Dispatchers.IO) {
        val existingContacts = mutableListOf<ExistingContact>()
        val phoneNumberStrings = phoneNumbers.map { it.number.replace(Regex("[^\\d+]"), "") }
        val emailStrings = emails.map { it.address.lowercase() }

        // Check for phone number matches
        phoneNumberStrings.forEach { phoneNumber ->
            val phoneMatches = findContactsByPhone(phoneNumber)
            existingContacts.addAll(phoneMatches)
        }

        // Check for email matches
        emailStrings.forEach { email ->
            val emailMatches = findContactsByEmail(email)
            existingContacts.addAll(emailMatches)
        }

        // Remove duplicates based on contact ID
        val uniqueContacts = existingContacts.distinctBy { it.id }

        DuplicateCheckResult(
            existingContacts = uniqueContacts,
            hasMatches = uniqueContacts.isNotEmpty()
        )
    }

    private fun findContactsByPhone(phoneNumber: String): List<ExistingContact> {
        val contacts = mutableListOf<ExistingContact>()
        val cleanPhoneNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getLong(contactIdIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                val cleanStoredNumber = number.replace(Regex("[^\\d+]"), "")

                if (cleanStoredNumber.contains(cleanPhoneNumber) || cleanPhoneNumber.contains(cleanStoredNumber)) {
                    val fullContact = getFullContactInfo(contactId, name)
                    if (contacts.none { contact -> contact.id == contactId }) {
                        contacts.add(fullContact)
                    }
                }
            }
        }

        return contacts
    }

    private fun findContactsByEmail(email: String): List<ExistingContact> {
        val contacts = mutableListOf<ExistingContact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Email.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.ADDRESS
        )

        val selection = "${ContactsContract.CommonDataKinds.Email.ADDRESS} = ?"
        val selectionArgs = arrayOf(email.lowercase())

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)

            while (it.moveToNext()) {
                val contactId = it.getLong(contactIdIndex)
                val name = it.getString(nameIndex) ?: "Unknown"

                val fullContact = getFullContactInfo(contactId, name)
                if (contacts.none { contact -> contact.id == contactId }) {
                    contacts.add(fullContact)
                }
            }
        }

        return contacts
    }

    private fun getFullContactInfo(contactId: Long, displayName: String): ExistingContact {
        val phoneNumbers = mutableListOf<String>()
        val emails = mutableListOf<String>()

        // Get phone numbers
        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val phoneSelection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val phoneSelectionArgs = arrayOf(contactId.toString())

        val phoneCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            phoneSelection,
            phoneSelectionArgs,
            null
        )

        phoneCursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex) ?: ""
                if (number.isNotEmpty()) {
                    phoneNumbers.add(number)
                }
            }
        }

        // Get emails
        val emailProjection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
        val emailSelection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        val emailSelectionArgs = arrayOf(contactId.toString())

        val emailCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            emailProjection,
            emailSelection,
            emailSelectionArgs,
            null
        )

        emailCursor?.use {
            val addressIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (it.moveToNext()) {
                val address = it.getString(addressIndex) ?: ""
                if (address.isNotEmpty()) {
                    emails.add(address)
                }
            }
        }

        return ExistingContact(
            id = contactId,
            name = displayName,
            phoneNumbers = phoneNumbers,
            emails = emails
        )
    }

    suspend fun mergeWithExistingContact(
        existingContactId: Long,
        name: String,
        phoneticName: String? = null,
        preferredName: String? = null,
        company: String? = null,
        jobTitle: String? = null,
        notes: String? = null,
        phoneNumbers: List<PhoneNumber> = emptyList(),
        addresses: List<Address> = emptyList(),
        emails: List<Email> = emptyList(),
        websites: List<Website> = emptyList(),
        dates: List<ImportantDate> = emptyList(),
        relationships: List<Relationship> = emptyList(),
        logo: String? = null,
    ) {
        // Create intent to edit existing contact
        val intent = Intent(Intent.ACTION_EDIT).apply {
            data = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, existingContactId.toString())

            val dataList = ArrayList<ContentValues>()

            // Add all new information as additional data
            // Preferred name (nickname)
            preferredName?.let {
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it)
                    put(ContactsContract.CommonDataKinds.Nickname.TYPE, ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT)
                    dataList.add(this)
                }
            }

            // Add phone numbers
            phoneNumbers.forEach { phoneNumber ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.number)
                    put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneNumber.type.value)
                    if (phoneNumber.type == PhoneType.CUSTOM) {
                        put(ContactsContract.CommonDataKinds.Phone.LABEL, "Custom")
                    }
                    dataList.add(this)
                }
            }

            // Add emails
            emails.forEach { email ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Email.ADDRESS, email.address)
                    put(ContactsContract.CommonDataKinds.Email.TYPE, email.type.value)
                    if (email.type == EmailType.CUSTOM) {
                        put(ContactsContract.CommonDataKinds.Email.LABEL, "Custom")
                    }
                    dataList.add(this)
                }
            }

            // Add addresses
            addresses.forEach { address ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address.fullAddress)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, address.type.value)
                    if (address.type == AddressType.CUSTOM) {
                        put(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, "Custom")
                    }
                    dataList.add(this)
                }
            }

            // Add websites
            websites.forEach { website ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Website.URL, website.url)
                    put(ContactsContract.CommonDataKinds.Website.TYPE, website.type.value)
                    if (website.type == WebsiteType.CUSTOM && website.customLabel != null) {
                        put(ContactsContract.CommonDataKinds.Website.LABEL, website.customLabel)
                    }
                    dataList.add(this)
                }
            }

            // Add dates
            dates.forEach { date ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Event.START_DATE, date.date)
                    put(ContactsContract.CommonDataKinds.Event.TYPE, date.type.value)
                    if (date.type == DateType.CUSTOM && date.customLabel != null) {
                        put(ContactsContract.CommonDataKinds.Event.LABEL, date.customLabel)
                    }
                    dataList.add(this)
                }
            }

            // Add relationships
            relationships.forEach { relationship ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Relation.NAME, relationship.name)
                    put(ContactsContract.CommonDataKinds.Relation.TYPE, relationship.type.value)
                    if (relationship.type == RelationType.CUSTOM && relationship.customLabel != null) {
                        put(ContactsContract.CommonDataKinds.Relation.LABEL, relationship.customLabel)
                    }
                    dataList.add(this)
                }
            }

            // Add photo/logo if provided
            logo?.let {
                getByteArrayFromImageUrl(it)?.let { array ->
                    ContentValues().apply {
                        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        put(ContactsContract.CommonDataKinds.Photo.PHOTO, array)
                        dataList.add(this)
                    }
                }
            }

            // Add company and job title if provided
            company?.let {
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Organization.COMPANY, it)
                    jobTitle?.let { title ->
                        put(ContactsContract.CommonDataKinds.Organization.TITLE, title)
                    }
                    put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    dataList.add(this)
                }
            }

            // Add notes if provided
            notes?.let {
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Note.NOTE, it)
                    dataList.add(this)
                }
            }

            // Add all the additional data if we have any
            if (dataList.isNotEmpty()) {
                putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, dataList)
            }
        }

        context.startActivity(intent)
    }

    suspend fun saveContact(
        name: String,
        phoneticName: String? = null,
        preferredName: String? = null,
        company: String? = null,
        jobTitle: String? = null,
        notes: String? = null,
        phoneNumbers: List<PhoneNumber> = emptyList(),
        addresses: List<Address> = emptyList(),
        emails: List<Email> = emptyList(),
        websites: List<Website> = emptyList(),
        dates: List<ImportantDate> = emptyList(),
        relationships: List<Relationship> = emptyList(),
        logo: String? = null,
    ) {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE

            // Basic information
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            phoneticName?.let { putExtra(ContactsContract.Intents.Insert.PHONETIC_NAME, it) }
            company?.let { putExtra(ContactsContract.Intents.Insert.COMPANY, it) }
            jobTitle?.let { putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it) }
            notes?.let { putExtra(ContactsContract.Intents.Insert.NOTES, it) }

            val dataList = ArrayList<ContentValues>()

            // Add preferred name (nickname) if provided
            preferredName?.let {
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it)
                    put(ContactsContract.CommonDataKinds.Nickname.TYPE, ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT)
                    dataList.add(this)
                }
            }

            // Add phone numbers
            phoneNumbers.forEachIndexed { index, phoneNumber ->
                if (index == 0) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber.number)
                    putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phoneNumber.type.value)
                    putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, true)
                } else {
                    ContentValues().apply {
                        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber.number)
                        put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneNumber.type.value)
                        if (phoneNumber.type == PhoneType.CUSTOM) {
                            put(ContactsContract.CommonDataKinds.Phone.LABEL, "Custom")
                        }
                        dataList.add(this)
                    }
                }
            }

            // Add email addresses
            emails.forEachIndexed { index, email ->
                if (index == 0) {
                    putExtra(ContactsContract.Intents.Insert.EMAIL, email.address)
                    putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, email.type.value)
                    putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY, true)
                } else {
                    ContentValues().apply {
                        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        put(ContactsContract.CommonDataKinds.Email.ADDRESS, email.address)
                        put(ContactsContract.CommonDataKinds.Email.TYPE, email.type.value)
                        if (email.type == EmailType.CUSTOM) {
                            put(ContactsContract.CommonDataKinds.Email.LABEL, "Custom")
                        }
                        dataList.add(this)
                    }
                }
            }

            // Add addresses
            addresses.forEachIndexed { index, address ->
                if (index == 0) {
                    putExtra(ContactsContract.Intents.Insert.POSTAL, address.fullAddress)
                    putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, address.type.value)
                    putExtra(ContactsContract.Intents.Insert.POSTAL_ISPRIMARY, true)
                } else {
                    ContentValues().apply {
                        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address.fullAddress)
                        put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, address.type.value)
                        if (address.type == AddressType.CUSTOM) {
                            put(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, "Custom")
                        }
                        dataList.add(this)
                    }
                }
            }

            // Add websites
            websites.forEach { website ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Website.URL, website.url)
                    put(ContactsContract.CommonDataKinds.Website.TYPE, website.type.value)
                    if (website.type == WebsiteType.CUSTOM && website.customLabel != null) {
                        put(ContactsContract.CommonDataKinds.Website.LABEL, website.customLabel)
                    }
                    dataList.add(this)
                }
            }

            // Add dates (birthday, anniversary, custom)
            dates.forEach { date ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Event.START_DATE, date.date)
                    put(ContactsContract.CommonDataKinds.Event.TYPE, date.type.value)
                    if (date.type == DateType.CUSTOM && date.customLabel != null) {
                        put(ContactsContract.CommonDataKinds.Event.LABEL, date.customLabel)
                    }
                    dataList.add(this)
                }
            }

            // Add relationships
            relationships.forEach { relationship ->
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Relation.NAME, relationship.name)
                    put(ContactsContract.CommonDataKinds.Relation.TYPE, relationship.type.value)
                    if (relationship.type == RelationType.CUSTOM && relationship.customLabel != null) {
                        put(ContactsContract.CommonDataKinds.Relation.LABEL, relationship.customLabel)
                    }
                    dataList.add(this)
                }
            }

            // Add photo/logo if provided
            logo?.let {
                getByteArrayFromImageUrl(it)?.let { array ->
                    ContentValues().apply {
                        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        put(ContactsContract.CommonDataKinds.Photo.PHOTO, array)
                        dataList.add(this)
                    }
                }
            }

            // Add all the additional data if we have any
            if (dataList.isNotEmpty()) {
                putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, dataList)
            }
        }

        context.startActivity(intent)
    }

    private suspend fun getByteArrayFromImageUrl(imageUrl: String?): ByteArray? {
        if (imageUrl == null) return null
        return try {
            val url = URL(imageUrl)
            val connection = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            connection.doInput = true
            withContext(Dispatchers.IO) {
                connection.connect()
            }
            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            withContext(Dispatchers.IO) {
                inputStream.close()
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Keep your existing data classes and enums...
    data class PhoneNumber(val number: String, val type: PhoneType)
    data class Address(val fullAddress: String, val type: AddressType)
    data class Email(val address: String, val type: EmailType)
    data class ImportantDate(val date: String, val type: DateType, val customLabel: String? = null)
    data class Relationship(val name: String, val type: RelationType, val customLabel: String? = null)
    data class Website(val url: String, val type: WebsiteType, val customLabel: String? = null)

    enum class PhoneType(val value: Int) {
        WORK(ContactsContract.CommonDataKinds.Phone.TYPE_WORK),
        HOME(ContactsContract.CommonDataKinds.Phone.TYPE_HOME),
        MAIN(ContactsContract.CommonDataKinds.Phone.TYPE_MAIN),
        CUSTOM(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
    }

    enum class AddressType(val value: Int) {
        WORK(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK),
        HOME(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME),
        OTHER(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER),
        CUSTOM(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM)
    }

    enum class EmailType(val value: Int) {
        WORK(ContactsContract.CommonDataKinds.Email.TYPE_WORK),
        HOME(ContactsContract.CommonDataKinds.Email.TYPE_HOME),
        CUSTOM(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM)
    }

    enum class DateType(val value: Int) {
        BIRTHDAY(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY),
        ANNIVERSARY(ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY),
        CUSTOM(ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM)
    }

    enum class RelationType(val value: Int) {
        ASSISTANT(ContactsContract.CommonDataKinds.Relation.TYPE_ASSISTANT),
        BROTHER(ContactsContract.CommonDataKinds.Relation.TYPE_BROTHER),
        CHILD(ContactsContract.CommonDataKinds.Relation.TYPE_CHILD),
        DOMESTIC_PARTNER(ContactsContract.CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER),
        FATHER(ContactsContract.CommonDataKinds.Relation.TYPE_FATHER),
        FRIEND(ContactsContract.CommonDataKinds.Relation.TYPE_FRIEND),
        MANAGER(ContactsContract.CommonDataKinds.Relation.TYPE_MANAGER),
        MOTHER(ContactsContract.CommonDataKinds.Relation.TYPE_MOTHER),
        PARENT(ContactsContract.CommonDataKinds.Relation.TYPE_PARENT),
        PARTNER(ContactsContract.CommonDataKinds.Relation.TYPE_PARTNER),
        RELATIVE(ContactsContract.CommonDataKinds.Relation.TYPE_RELATIVE),
        SPOUSE(ContactsContract.CommonDataKinds.Relation.TYPE_SPOUSE),
        SISTER(ContactsContract.CommonDataKinds.Relation.TYPE_SISTER),
        CUSTOM(ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM)
    }

    enum class WebsiteType(val value: Int) {
        HOME(ContactsContract.CommonDataKinds.Website.TYPE_HOME),
        WORK(ContactsContract.CommonDataKinds.Website.TYPE_WORK),
        BLOG(ContactsContract.CommonDataKinds.Website.TYPE_BLOG),
        PROFILE(ContactsContract.CommonDataKinds.Website.TYPE_PROFILE),
        OTHER(ContactsContract.CommonDataKinds.Website.TYPE_OTHER),
        CUSTOM(ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM)
    }
}

/*
*
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
                        ContactsContract.CommonDataKinds.Nickname .CONTENT_ITEM_TYPE
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
    }*/