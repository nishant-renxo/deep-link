package org.renxo.deeplinkapplication.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath
import org.renxo.deeplinkapplication.models.Contact

class PreferenceManager(preferenceName: String) {

    suspend fun clearAllPreferences() {
        preferenceManager.edit { preferences ->
            preferences.clear()
        }
    }

    private val preferenceManager: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(produceFile = { (if (preferenceName.contains(".preferences_pb")) preferenceName else "$preferenceName.preferences_pb").toPath() })
    }

    suspend fun setValue(key: String, value: Any) {
        preferenceManager.edit {
            when (value) {
                is String -> {
                    it[stringPreferencesKey(key)] = value
                }

                is Int -> {
                    it[intPreferencesKey(key)] = value
                }

                is Boolean -> {
                    it[booleanPreferencesKey(key)] = value
                }

                is Double -> {
                    it[doublePreferencesKey(key)] = value
                }

                else -> {
                    it[stringPreferencesKey(key)] = value.toString()
                }
            }
        }
    }


    suspend fun removeKey(key: String) {
        preferenceManager.edit {
            it.remove(stringPreferencesKey(key))
        }
    }

    suspend fun getString(key: String): String? {
        return preferenceManager.data.first()[stringPreferencesKey(key)]
    }

    suspend fun getInt(key: String, default: Int = 0): Int {
        return preferenceManager.data.first()[intPreferencesKey(key)] ?: default
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean {
        return preferenceManager.data.first()[booleanPreferencesKey(key)] ?: default
    }

    suspend fun getDouble(key: String, default: Double = 0.0): Double {
        return preferenceManager.data.first()[doublePreferencesKey(key)] ?: default
    }

    suspend fun setAuthToken(token: String) {
        setValue(AppConstants.Preferences.AUTH_TOKEN, token)
    }

    suspend fun setSessionId(token: String) {
        setValue(AppConstants.Preferences.SESSION_ID, token)
    }

    suspend fun getSessionId(): String? {
//        return "abc123456"
        return getString(AppConstants.Preferences.SESSION_ID)
    }


    suspend fun getAuthToken(): String? {
        return getString(AppConstants.Preferences.AUTH_TOKEN)
    }

    suspend fun setData(contact: Contact, qrCode: String?) {
        preferenceManager.edit {
            it[stringPreferencesKey(AppConstants.Preferences.CONTACT)] =
                json.encodeToString(contact)
            qrCode?.let { code ->
                it[stringPreferencesKey(AppConstants.Preferences.QR_CODE)] = code
            }
        }

    }

    suspend fun getContact(): Contact? {
        return preferenceManager.data.first()[stringPreferencesKey(AppConstants.Preferences.CONTACT)]?.let {
            json.decodeFromString<Contact>(it)
        }
    }

    suspend fun getQrCode(): String? {
        return preferenceManager.data.first()[stringPreferencesKey(AppConstants.Preferences.QR_CODE)]
    }


}


