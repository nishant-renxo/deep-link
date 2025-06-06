package org.renxo.deeplinkapplication.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.renxo.deeplinkapplication.models.DetailResponse
import org.renxo.deeplinkapplication.models.FieldsModel
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
        val emails: List<String>,
    )

    // Result class for duplicate check
    data class DuplicateCheckResult(
        val existingContacts: List<ExistingContact>,
        val hasMatches: Boolean,
    )

    private suspend fun checkForDuplicateContacts(
        phoneNumbers: List<PhoneNumber>,
        emails: List<Email>,
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
            val contactIdIndex =
                it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getLong(contactIdIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                val cleanStoredNumber = number.replace(Regex("[^\\d+]"), "")

                if (cleanStoredNumber.contains(cleanPhoneNumber) || cleanPhoneNumber.contains(
                        cleanStoredNumber
                    )
                ) {
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
            val contactIdIndex =
                it.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
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

    private suspend fun mergeWithExistingContact(
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
            flags=FLAG_ACTIVITY_NEW_TASK
            data = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_URI,
                existingContactId.toString()
            )

            val dataList = ArrayList<ContentValues>()

            // Add all new information as additional data
            // Preferred name (nickname)
            preferredName?.let {
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

            // Add phone numbers
            phoneNumbers.forEach { phoneNumber ->
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
                    put(
                        ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                        address.fullAddress
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Relation.NAME, relationship.name)
                    put(ContactsContract.CommonDataKinds.Relation.TYPE, relationship.type.value)
                    if (relationship.type == RelationType.CUSTOM && relationship.customLabel != null) {
                        put(
                            ContactsContract.CommonDataKinds.Relation.LABEL,
                            relationship.customLabel
                        )
                    }
                    dataList.add(this)
                }
            }

            // Add photo/logo if provided
            logo?.let {
                getByteArrayFromImageUrl(it)?.let { array ->
                    ContentValues().apply {
                        put(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        )
                        put(ContactsContract.CommonDataKinds.Photo.PHOTO, array)
                        dataList.add(this)
                    }
                }
            }

            // Add company and job title if provided
            company?.let {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Organization.COMPANY, it)
                    jobTitle?.let { title ->
                        put(ContactsContract.CommonDataKinds.Organization.TITLE, title)
                    }
                    put(
                        ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.TYPE_WORK
                    )
                    dataList.add(this)
                }
            }

            // Add notes if provided
            notes?.let {
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                    )
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
            flags=FLAG_ACTIVITY_NEW_TASK
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

            // Add phone numbers
            phoneNumbers.forEachIndexed { index, phoneNumber ->
                if (index == 0) {
                    putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber.number)
                    putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phoneNumber.type.value)
                    putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, true)
                } else {
                    ContentValues().apply {
                        put(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
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
                        put(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                        )
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
                        put(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                        )
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                            address.fullAddress
                        )
                        put(
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                            address.type.value
                        )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                    )
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
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Relation.NAME, relationship.name)
                    put(ContactsContract.CommonDataKinds.Relation.TYPE, relationship.type.value)
                    if (relationship.type == RelationType.CUSTOM && relationship.customLabel != null) {
                        put(
                            ContactsContract.CommonDataKinds.Relation.LABEL,
                            relationship.customLabel
                        )
                    }
                    dataList.add(this)
                }
            }

            // Add photo/logo if provided
            logo?.let {
                getByteArrayFromImageUrl(it)?.let { array ->
                    ContentValues().apply {
                        put(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        )
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
    data class Relationship(
        val name: String,
        val type: RelationType,
        val customLabel: String? = null,
    )

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


    data class PendingContactData(
        val name: String,
        val phoneticName: String? = null,
        val preferredName: String? = null,
        val company: String? = null,
        val jobTitle: String? = null,
        val notes: String? = null,
        val phoneNumbers: List<PhoneNumber> = emptyList(),
        val addresses: List<Address> = emptyList(),
        val emails: List<Email> = emptyList(),
        val websites: List<Website> = emptyList(),
        val dates: List<ImportantDate> = emptyList(),
        val relationships: List<Relationship> = emptyList(),
        val logo: String? = null,
    )

    var showDuplicateSheet by mutableStateOf(false)
    var duplicateContacts by mutableStateOf<List<ExistingContact>>(emptyList())
    private var pendingContactData by mutableStateOf<PendingContactData?>(null)

    suspend fun saveContact(contact: FieldsModel?) {

        // Extract contact data
        val contactData = extractContactData(contact)
        pendingContactData = contactData

        // Check for duplicates
        val duplicateResult = checkForDuplicateContacts(
            phoneNumbers = contactData.phoneNumbers,
            emails = contactData.emails
        )

        if (duplicateResult.hasMatches) {
            // Show bottom sheet with duplicate contacts
            duplicateContacts = duplicateResult.existingContacts
            showDuplicateSheet = true
        } else {
            // No duplicates, save directly
            saveAsNewContact()
        }

    }

    suspend fun saveAsNewContact() {
        pendingContactData?.let { data ->
            saveContact(
                name = data.name,
                phoneticName = data.phoneticName,
                preferredName = data.preferredName,
                company = data.company,
                jobTitle = data.jobTitle,
                notes = data.notes,
                phoneNumbers = data.phoneNumbers,
                addresses = data.addresses,
                emails = data.emails,
                websites = data.websites,
                dates = data.dates,
                relationships = data.relationships,
                logo = data.logo
            )
        }
        hideDuplicateSheet()

    }

    suspend fun mergeWithExistingContact(existingContactId: Long) {
        pendingContactData?.let { data ->
            mergeWithExistingContact(
                existingContactId = existingContactId,
                name = data.name,
                phoneticName = data.phoneticName,
                preferredName = data.preferredName,
                company = data.company,
                jobTitle = data.jobTitle,
                notes = data.notes,
                phoneNumbers = data.phoneNumbers,
                addresses = data.addresses,
                emails = data.emails,
                websites = data.websites,
                dates = data.dates,
                relationships = data.relationships,
                logo = data.logo
            )
        }
        hideDuplicateSheet()

    }

    fun hideDuplicateSheet() {
        showDuplicateSheet = false
        duplicateContacts = emptyList()
        pendingContactData = null
    }

    private fun extractContactData(contact: FieldsModel?): PendingContactData {
        return PendingContactData(
            name = contact?.name ?: "Unknown",
            phoneticName = contact?.name,
            preferredName = contact?.name,
            company = contact?.company_name,
            jobTitle = contact?.job_title,
            notes = contact?.tag_line,
            phoneNumbers = extractPhoneNumbers(contact),
            addresses = extractAddresses(contact),
            emails = extractEmails(contact),
            websites = extractWebsites(contact),
            dates = extractDates(contact),
            relationships = extractRelationships(contact),
            logo = null
        )
    }

    private fun extractPhoneNumbers(contact: FieldsModel?): List<PhoneNumber> {
        return contact?.phone_numbers?.map { phone ->
            PhoneNumber(
                number = phone.phone_no ?: "",
                type = when (phone.phone_no?.lowercase()) {
                    "work" -> ContactInfo.PhoneType.WORK
                    "home" -> ContactInfo.PhoneType.HOME
                    "main" -> ContactInfo.PhoneType.MAIN
                    else -> ContactInfo.PhoneType.HOME
                }
            )
        } ?: emptyList()
    }

    private fun extractEmails(contact: FieldsModel?): List<Email> {
        return contact?.emails?.map { email ->
            Email(
                address = email?.email ?: "",
                type = when (email?.email?.lowercase()) {
                    "work" -> ContactInfo.EmailType.WORK
                    "home" -> ContactInfo.EmailType.HOME
                    else -> ContactInfo.EmailType.HOME
                }
            )
        } ?: emptyList()
    }

    private fun extractAddresses(contact: FieldsModel?): List<Address> {
        return contact?.address?.map { address ->
            Address(
                fullAddress = address?.address ?: "",
                type = when (address?.address?.lowercase()) {
                    "work" -> ContactInfo.AddressType.WORK
                    "home" -> ContactInfo.AddressType.HOME
                    else -> ContactInfo.AddressType.HOME
                }
            )
        } ?: emptyList()
    }

    private fun extractWebsites(contact: FieldsModel?): List<Website> {
        return contact?.urls?.map { website ->
            Website(
                url = website.url ?: "",
                type = when (website.url?.lowercase()) {
                    "work" -> ContactInfo.WebsiteType.WORK
                    "home" -> ContactInfo.WebsiteType.HOME
                    "blog" -> ContactInfo.WebsiteType.BLOG
                    "profile" -> ContactInfo.WebsiteType.PROFILE
                    else -> ContactInfo.WebsiteType.OTHER
                },
                customLabel = website.url
            )
        } ?: emptyList()
    }

    private fun extractDates(contact: FieldsModel?): List<ImportantDate> {
        return contact?.dates?.map { date ->
            ImportantDate(
                date = date.toString() ?: "",
                type = when (date.toString().lowercase()) {
                    "birthday" -> ContactInfo.DateType.BIRTHDAY
                    "anniversary" -> ContactInfo.DateType.ANNIVERSARY
                    else -> ContactInfo.DateType.CUSTOM
                },
                customLabel = date
            )
        } ?: emptyList()
    }

    private fun extractRelationships(contact: FieldsModel?): List<Relationship> {
        return contact?.relationships?.map { relationship ->
            Relationship(
                name = relationship ?: "",
                type = when (relationship?.lowercase()) {
                    "spouse" -> ContactInfo.RelationType.SPOUSE
                    "parent" -> ContactInfo.RelationType.PARENT
                    "child" -> ContactInfo.RelationType.CHILD
                    "friend" -> ContactInfo.RelationType.FRIEND
                    "assistant" -> ContactInfo.RelationType.ASSISTANT
                    "manager" -> ContactInfo.RelationType.MANAGER
                    else -> ContactInfo.RelationType.RELATIVE
                },
                customLabel = relationship
            )
        } ?: emptyList()
    }

}
