package org.renxo.deeplinkapplication.utils

interface AppConstants {

    object Preferences {
        const val APP_PREFERENCES = "prefs.preferences_pb"
        const val AUTH_TOKEN = "auth_token"
        const val SESSION_ID = "session_id"
        const val CONTACT = "contact"
        const val QR_CODE = "qrCode"
    }

    object SuccessCodes {
        const val SUCCESS200 = "SUCCESS200"

    }    object Params {
        const val user = "user"
        const val template = "template"
        const val contacts = "contacts"
        const val url = "url"

    }
}