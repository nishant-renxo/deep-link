package org.renxo.deeplinkapplication.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import org.renxo.deeplinkapplication.R
import java.io.ByteArrayOutputStream

class ContactInfo(context: Context) {

    init {
        context.saveContactSimplified(
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
            ), logo = context.drawableToByteArray(R.drawable.logo)
        )
    }

    private fun Context.drawableToByteArray(drawableRes: Int): ByteArray? {
        return try {
            val drawable = ContextCompat.getDrawable(this, drawableRes)
            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                // Convert vector or other drawable types to bitmap
                val bitmap = Bitmap.createBitmap(
                    drawable?.intrinsicWidth ?: 100,
                    drawable?.intrinsicHeight ?: 100,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable?.setBounds(0, 0, canvas.width, canvas.height)
                drawable?.draw(canvas)
                bitmap
            }

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

data class PhoneNumber(
    val number: String,
    val type: PhoneType
)

data class Address(
    val fullAddress: String,
    val type: AddressType
)

data class Email(
    val address: String,
    val type: EmailType
)

data class ImportantDate(
    val date: String, // Format: yyyy-MM-dd
    val type: DateType,
    val customLabel: String? = null
)

data class Relationship(
    val name: String,
    val type: RelationType,
    val customLabel: String? = null
)

data class Website(
    val url: String,
    val type: WebsiteType,
    val customLabel: String? = null
)

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

private fun Context.saveContactSimplified(
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
    logo: ByteArray? = null
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
                // Use the primary phone extras for the first phone number
                putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber.number)
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phoneNumber.type.value)
                putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, true)
            } else {
                // Add additional phone numbers to data list
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
                // Use the primary email extras for the first email
                putExtra(ContactsContract.Intents.Insert.EMAIL, email.address)
                putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, email.type.value)
                putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY, true)
            } else {
                // Add additional emails to data list
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
                // Use the primary postal extras for the first address
                putExtra(ContactsContract.Intents.Insert.POSTAL, address.fullAddress)
                putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, address.type.value)
                putExtra(ContactsContract.Intents.Insert.POSTAL_ISPRIMARY, true)
            } else {
                // Add additional addresses to data list
                ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
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
                    put(ContactsContract.CommonDataKinds.Relation.LABEL, relationship.customLabel)
                }
                dataList.add(this)
            }
        }

        // Add photo/logo if provided
        logo?.let {
            ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                )
                put(ContactsContract.CommonDataKinds.Photo.PHOTO, it)
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