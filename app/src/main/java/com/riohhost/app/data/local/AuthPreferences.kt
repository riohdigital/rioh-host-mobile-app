package com.riohhost.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUser(email: String, password: String) {
        sharedPreferences.edit()
            .putString("email", email)
            .putString("password", password)
            .putBoolean("remember_me", true)
            .apply()
    }

    fun getSavedUser(): Pair<String, String>? {
        if (!sharedPreferences.getBoolean("remember_me", false)) return null
        
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        return if (email != null && password != null) {
            Pair(email, password)
        } else {
            null
        }
    }

    fun clearUser() {
        sharedPreferences.edit()
            .remove("email")
            .remove("password")
            .remove("remember_me")
            .apply()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometric_enabled", false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("biometric_enabled", enabled)
            .apply()
    }
}
