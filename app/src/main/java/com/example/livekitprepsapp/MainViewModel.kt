package io.livekit.android.sample

import android.app.Application
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import io.livekit.android.BuildConfig

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    fun getSavedUrl() = preferences.getString(PREFERENCES_KEY_URL, URL) as String
    fun getSavedToken() = preferences.getString(PREFERENCES_KEY_TOKEN, TOKEN) as String
    fun getE2EEOptionsOn() = preferences.getBoolean(PREFERENCES_KEY_E2EE_ON, false)
    fun getSavedE2EEKey() = preferences.getString(PREFERENCES_KEY_E2EE_KEY, E2EE_KEY) as String

    fun setSavedUrl(url: String) {
        preferences.edit {
            putString(PREFERENCES_KEY_URL, url)
        }
    }

    fun setSavedToken(token: String) {
        preferences.edit {
            putString(PREFERENCES_KEY_TOKEN, token)
        }
    }

    fun setSavedE2EEOn(yesno: Boolean) {
        preferences.edit {
            putBoolean(PREFERENCES_KEY_E2EE_ON, yesno)
        }
    }

    fun setSavedE2EEKey(key: String) {
        preferences.edit {
            putString(PREFERENCES_KEY_E2EE_KEY, key)
        }
    }

    fun reset() {
        preferences.edit { clear() }
    }

    companion object {
        private const val PREFERENCES_KEY_URL = "url"
        private const val PREFERENCES_KEY_TOKEN = "token"
        private const val PREFERENCES_KEY_E2EE_ON = "enable_e2ee"
        private const val PREFERENCES_KEY_E2EE_KEY = "e2ee_key"

        const val URL = "wss://emeet-app-pvosc3mg.livekit.cloud"
        const val TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDkwMTYzNjcsImlzcyI6IkFQSWYzenZGVGJBcUVkIiwibmFtZSI6InRlc3RfdXNlciIsIm5iZiI6MTc0ODkyOTk2Nywic3ViIjoidGVzdF91c2VyIiwidmlkZW8iOnsicm9vbSI6InRlc3Rfcm9vbSIsInJvb21Kb2luIjp0cnVlfX0.3IDMZjd3Krr5m2nmZGlyf7RAVum4hYgisE8THG9VENw"
        const val E2EE_KEY = "12345678"
    }
}