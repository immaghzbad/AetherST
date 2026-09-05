package io.github.immaghzbad.aetherst.platform

import android.content.Context
import androidx.core.content.edit

class AndroidSettings(context: Context) : Settings {
    private val prefs = context.getSharedPreferences("aether_settings", Context.MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)
    override fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    override fun getLong(key: String, defaultValue: Long): Long = prefs.getLong(key, defaultValue)
    override fun putLong(key: String, value: Long) {
        prefs.edit { putLong(key, value) }
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> =
        prefs.getStringSet(key, defaultValue) ?: defaultValue

    override fun putStringSet(key: String, value: Set<String>) {
        prefs.edit {
            putStringSet(
                key,
                value
            )
        }
    }
}

actual fun getSettings(context: PlatformContext): Settings = AndroidSettings(context.context)
