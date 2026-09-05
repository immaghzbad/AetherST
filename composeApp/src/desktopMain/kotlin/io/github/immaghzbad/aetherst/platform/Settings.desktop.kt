package io.github.immaghzbad.aetherst.platform

import java.util.Properties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import kotlinx.coroutines.*

class DesktopSettings private constructor() : Settings {
    private val props = Properties()
    private val file: File
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val baseDir = if (isWindows) {
            System.getenv("AppData") ?: System.getProperty("user.home")
        } else {
            System.getProperty("user.home") + "/.config"
        }
        val dir = File(baseDir, "AetherY")
        if (!dir.exists()) dir.mkdirs()
        file = File(dir, "settings.properties")
    }

    init {
        if (file.exists()) {
            try {
                FileInputStream(file).use { props.load(it) }
            } catch (_: Exception) {
                
            }
        }
    }

    private fun save() {
        scope.launch {
            try {
                FileOutputStream(file).use { props.store(it, null) }
            } catch (_: Exception) {
                
            }
        }
    }

    override fun getString(key: String, defaultValue: String): String = props.getProperty(key, defaultValue) ?: defaultValue
    override fun putString(key: String, value: String) { props.setProperty(key, value); save() }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = props.getProperty(key, defaultValue.toString()).toBoolean()
    override fun putBoolean(key: String, value: Boolean) { props.setProperty(key, value.toString()); save() }
    override fun getInt(key: String, defaultValue: Int): Int = props.getProperty(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    override fun putInt(key: String, value: Int) { props.setProperty(key, value.toString()); save() }
    override fun getLong(key: String, defaultValue: Long): Long = props.getProperty(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    override fun putLong(key: String, value: Long) { props.setProperty(key, value.toString()); save() }
    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> {
        val value = props.getProperty(key) ?: return defaultValue
        return value.split(",").filter { it.isNotEmpty() }.toSet()
    }
    override fun putStringSet(key: String, value: Set<String>) { props.setProperty(key, value.joinToString(",")); save() }

    companion object {
        @Volatile
        private var instance: DesktopSettings? = null
        fun getInstance(): DesktopSettings =
            instance ?: synchronized(this) {
                instance ?: DesktopSettings().also { instance = it }
            }
    }
}

actual fun getSettings(context: PlatformContext): Settings = DesktopSettings.getInstance()
