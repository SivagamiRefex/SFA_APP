package com.example.sfa.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREF_NAME = "secure_prefs"
    private const val KEY_TOKEN = "access_token"
    private const val SFA_PREF_NAME = "sfa_prefs"


    fun initSecure(context: Context): SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(SFA_PREF_NAME, Context.MODE_PRIVATE)
    }


    fun saveToken(context: Context, token: String) {
        val prefs = initSecure(context)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    fun getToken(context: Context): String? {
        return initSecure(context).getString(KEY_TOKEN, null)
    }
    fun setString(context: Context, key: String, value: String) {
        getPrefs(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String): String? {
        return getPrefs(context).getString(key, null)
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String): Boolean {
        return getPrefs(context).getBoolean(key, false)
    }

    fun setInt(context: Context, key: String, value: Int) {
        getPrefs(context).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String): Int {
        return getPrefs(context).getInt(key, 0)
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
        initSecure(context).edit().clear().apply()
    }

    fun clearPref(context: Context){
        getPrefs(context).edit().clear().apply()

    }

    fun clearSecPref(context: Context){
        initSecure(context).edit().clear().apply()

    }


}