package io.github.immaghzbad.aetherst.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.immaghzbad.aetherst.shared.model.AppInfo

expect class PlatformContext

interface VpnController {
    fun startVpn()
    fun stopVpn()
    fun restartVpn()
    fun startProxy()
    fun stopProxy()
    fun restartProxy()
    fun prepareVpn(onPermissionRequired: () -> Unit): Boolean
    fun isVpnPrepared(): Boolean
    fun submitLoginCode(code: String)
}

interface TrafficProvider {
    fun getTxBytes(): Long
    fun getRxBytes(): Long
}

interface AppInfoProvider {
    suspend fun getInstalledApps(): List<AppInfo>
}

interface SystemUtils {
    fun isBatteryOptimized(): Boolean
    fun getFilesDir(): String
    fun getCacheDir(): String
    fun getPackageName(): String
    fun getAppVersion(): String
    fun getAppVersionCode(): Int
    fun exitApp()
    fun execPing(host: String, size: Int, timeoutMs: Int, dontFragment: Boolean = false): Boolean
    fun getInterfaceMtu(): Int
    fun isNetworkConnected(): Boolean
    fun readLastCrashLog(): String?
    fun clearCrashLog()
    fun copyToClipboard(text: String)
    fun requestNotificationPermission()
    fun isNotificationPermissionGranted(): Boolean
    fun requestBatteryOptimization()
    fun openVpnSettings()
    fun exportFile(fileName: String, content: String, onResult: (Boolean) -> Unit)
    fun importFile(onResult: (String?) -> Unit)
    fun shareFile(fileName: String, content: String)
    fun readInternalAsset(fileName: String): String?
    fun setSystemProxy(host: String, port: Int)
    fun clearSystemProxy()
    fun setSystemDns(dnsList: String)
    fun clearSystemDns()
    fun isAdministrator(): Boolean
    fun relaunchAsAdmin()
}

interface Settings {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
    fun getLong(key: String, defaultValue: Long): Long
    fun putLong(key: String, value: Long)
    fun getStringSet(key: String, defaultValue: Set<String>): Set<String>
    fun putStringSet(key: String, value: Set<String>)
}

expect fun getVpnController(context: PlatformContext): VpnController
expect fun getTrafficProvider(context: PlatformContext): TrafficProvider
expect fun getAppInfoProvider(context: PlatformContext): AppInfoProvider
expect fun getSystemUtils(context: PlatformContext): SystemUtils
expect fun getSettings(context: PlatformContext): Settings
expect fun getCurrentTimestamp(): String

expect val isDesktop: Boolean

expect val isWindows: Boolean

expect fun getDeviceModel(): String

expect fun getOsVersion(): String

@Composable
expect fun AppIcon(app: AppInfo, modifier: Modifier)
