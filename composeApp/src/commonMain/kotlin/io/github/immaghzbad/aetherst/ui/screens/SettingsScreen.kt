package io.github.immaghzbad.aetherst.shared.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.immaghzbad.aetherst.platform.isDesktop
import io.github.immaghzbad.aetherst.shared.core.NetworkUtils
import io.github.immaghzbad.aetherst.shared.model.AetherConfig
import io.github.immaghzbad.aetherst.shared.model.AetherIpMode
import io.github.immaghzbad.aetherst.shared.model.AetherLogLevel
import io.github.immaghzbad.aetherst.shared.model.AetherNoise
import io.github.immaghzbad.aetherst.shared.model.AetherPerfProfile
import io.github.immaghzbad.aetherst.shared.model.AetherProtocol
import io.github.immaghzbad.aetherst.shared.model.AetherScanMode
import io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings
import io.github.immaghzbad.aetherst.shared.model.ConnectionMode
import io.github.immaghzbad.aetherst.shared.model.TunnelEngine
import io.github.immaghzbad.aetherst.shared.ui.components.AppDivider
import io.github.immaghzbad.aetherst.shared.ui.components.IosActionRow
import io.github.immaghzbad.aetherst.shared.ui.components.IosConfirmationDialog
import io.github.immaghzbad.aetherst.shared.ui.components.IosGroupCard
import io.github.immaghzbad.aetherst.shared.ui.components.IosIconBadge
import io.github.immaghzbad.aetherst.shared.ui.components.IosInputField
import io.github.immaghzbad.aetherst.shared.ui.components.IosInputFieldRow
import io.github.immaghzbad.aetherst.shared.ui.components.IosPickerRow
import io.github.immaghzbad.aetherst.shared.ui.components.IosPresetItem
import io.github.immaghzbad.aetherst.shared.ui.components.IosSwitchRow
import io.github.immaghzbad.aetherst.shared.i18n.LocalAppStrings
import io.github.immaghzbad.aetherst.shared.ui.theme.AppPalette

private val IosCardBg = AppPalette.surfaceRaised
private val IosGroupBg = AppPalette.divider
private val IosSecondaryLabel = AppPalette.textSecondary
private val IosActiveBlue = AppPalette.accent
private val IosDividerColor = AppPalette.divider
private val IosActiveGreen = AppPalette.statusConnected

enum class SettingsPage(val title: String) {
    PRESETS("Configuration Profiles"),
    CONNECTION("Connection & Tunneling"),
    PROTOCOL("Protocol & Transport"),
    ZEROTRUST("Cloudflare Zero Trust"),
    NETWORK("Network Parameters"),
    SECURITY("Security & Reliability"),
    AUTO_CONNECT("Auto-Connect & Recovery"),
    DIAGNOSTICS("Diagnostics & Core"),
    SYSTEM("System & Maintenance"),
    HEV_ENGINE("HEV Engine"),
    INTERFACE("User Interface")
}

@Composable
fun SettingsScreen(
    config: AetherConfig,
    isBatteryOptimized: Boolean,
    onUpdateConfig: (AetherConfig) -> Unit,
    onUpdateTunnelEngine: (TunnelEngine) -> Unit,
    onApplyPreset: (String) -> Unit,
    onOpenSplitTunneling: () -> Unit,
    onOpenRoutingRules: () -> Unit,
    onOpenAutoDetect: () -> Unit = {},
    onOpenDnsOptimizer: () -> Unit = {},
    onOpenSpeedTest: () -> Unit = {},
    onRequestBatteryOptimization: () -> Unit,
    onOpenVpnSettings: () -> Unit = {},
    onResetAll: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onOptimizeMtu: () -> Unit,
    isOptimizingMtu: Boolean = false,
    onShowToast: (String, Boolean) -> Unit = { _, _ -> },
    initialPage: SettingsPage? = null,
    onSubPageClosed: () -> Unit = {},
    bottomContentPadding: Dp = 0.dp,
    loadAutoConnectSettings: () -> AutoConnectSettings = { AutoConnectSettings() },
    saveAutoConnectSettings: (AutoConnectSettings) -> Unit = {},
) {
    val strings = LocalAppStrings.current
    var currentPage by remember { mutableStateOf(initialPage) }
    LaunchedEffect(initialPage) {
        if (initialPage != null) currentPage = initialPage
    }

    if (currentPage != null) {
        SettingsSubPage(
            page = currentPage!!,
            config = config,
            isBatteryOptimized = isBatteryOptimized,
            onBack = {
                currentPage = null
                onSubPageClosed()
            },
            onUpdateConfig = onUpdateConfig,
            onUpdateTunnelEngine = onUpdateTunnelEngine,
            onApplyPreset = onApplyPreset,
            onOpenSplitTunneling = onOpenSplitTunneling,
            onOpenRoutingRules = onOpenRoutingRules,
            onRequestBatteryOptimization = onRequestBatteryOptimization,
            onOpenVpnSettings = onOpenVpnSettings,
            onResetAll = onResetAll,
            onExportBackup = onExportBackup,
            onImportBackup = onImportBackup,
            onOptimizeMtu = onOptimizeMtu,
            isOptimizingMtu = isOptimizingMtu,
            onShowToast = onShowToast,
            bottomContentPadding = bottomContentPadding,
            onOpenDnsOptimizer = onOpenDnsOptimizer,
            loadAutoConnectSettings = loadAutoConnectSettings,
            saveAutoConnectSettings = saveAutoConnectSettings
        )
        return
    }

    val isAndroid = remember { try { Class.forName("android.os.Build"); true } catch(_: Throwable) { false } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomContentPadding + 12.dp, top = if (isDesktop) 12.dp else 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(strings.SETTINGS_TITLE, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 26.sp, lineHeight = 30.sp)
                Text(strings.SETTINGS_SUBTITLE, color = IosSecondaryLabel, fontSize = 12.sp)
            }
        }
        item {
            IosGroupCard {
                Column {
                    IosActionRow(icon = Icons.Default.Speed, iconBg = AppPalette.statusScanning, title = strings.SPEED_TEST_TITLE, subtitle = strings.SPEED_TEST_SUBTITLE, onClick = onOpenSpeedTest)
                    HorizontalDivider(color = IosDividerColor, thickness = 0.5.dp)
                    IosActionRow(icon = Icons.Default.Radar, iconBg = AppPalette.accent, title = strings.AUTO_DETECT_TITLE, subtitle = strings.AUTO_DETECT_SUBTITLE, onClick = onOpenAutoDetect)
                }
            }
        }
        item { CategoryCard(icon = Icons.Default.Tune, iconBg = AppPalette.textSecondary, title = strings.CAT_CONFIGURATION_PROFILES, subtitle = strings.CAT_CONFIGURATION_PROFILES_SUB, onClick = { currentPage = SettingsPage.PRESETS }) }
        item { CategoryCard(icon = Icons.Default.VpnLock, iconBg = AppPalette.statusConnected, title = strings.CAT_CONNECTION_TUNNELING, subtitle = strings.CAT_CONNECTION_TUNNELING_SUB, onClick = { currentPage = SettingsPage.CONNECTION }) }
        item { CategoryCard(icon = Icons.Default.Shield, iconBg = IosActiveBlue, title = strings.CAT_PROTOCOL_TRANSPORT, subtitle = strings.CAT_PROTOCOL_TRANSPORT_SUB, onClick = { currentPage = SettingsPage.PROTOCOL }) }
        if (config.protocol == AetherProtocol.ZERO_TRUST) {
            item { CategoryCard(icon = Icons.Default.Business, iconBg = AppPalette.accentVariant, title = strings.CAT_ZEROTRUST, subtitle = strings.CAT_ZEROTRUST_SUB, onClick = { currentPage = SettingsPage.ZEROTRUST }) }
        }
        item { CategoryCard(icon = Icons.Default.Language, iconBg = IosActiveBlue, title = strings.CAT_NETWORK_PARAMETERS, subtitle = strings.CAT_NETWORK_PARAMETERS_SUB, onClick = { currentPage = SettingsPage.NETWORK }) }
        item { CategoryCard(icon = Icons.Default.Lock, iconBg = AppPalette.statusError, title = strings.CAT_SECURITY_RELIABILITY, subtitle = strings.CAT_SECURITY_RELIABILITY_SUB, onClick = { currentPage = SettingsPage.SECURITY }) }
        item { CategoryCard(icon = Icons.Default.Repeat, iconBg = AppPalette.statusConnected, title = strings.CAT_AUTO_CONNECT, subtitle = strings.CAT_AUTO_CONNECT_SUB, onClick = { currentPage = SettingsPage.AUTO_CONNECT }) }
        item { CategoryCard(icon = Icons.Default.BugReport, iconBg = AppPalette.debugCyan, title = strings.CAT_DIAGNOSTICS_CORE, subtitle = strings.CAT_DIAGNOSTICS_CORE_SUB, onClick = { currentPage = SettingsPage.DIAGNOSTICS }) }
        if (isAndroid) {
            item { CategoryCard(icon = Icons.Default.Memory, iconBg = AppPalette.accentVariantAlt, title = strings.CAT_HEV_ENGINE, subtitle = strings.CAT_HEV_ENGINE_SUB, onClick = { currentPage = SettingsPage.HEV_ENGINE }) }
        }
        item { CategoryCard(icon = Icons.Default.Settings, iconBg = AppPalette.textSecondary, title = strings.CAT_SYSTEM_MAINTENANCE, subtitle = strings.CAT_SYSTEM_MAINTENANCE_SUB, onClick = { currentPage = SettingsPage.SYSTEM }) }
        item { CategoryCard(icon = Icons.Default.PhoneAndroid, iconBg = AppPalette.accentVariant, title = strings.CAT_USER_INTERFACE, subtitle = strings.CAT_USER_INTERFACE_SUB, onClick = { currentPage = SettingsPage.INTERFACE }) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CategoryCard(icon: ImageVector, iconBg: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = IosCardBg)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IosIconBadge(icon = icon, backgroundColor = iconBg)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp); Text(subtitle, color = IosSecondaryLabel, fontSize = 12.sp) }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = IosSecondaryLabel, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsSubPage(page: SettingsPage, config: AetherConfig, isBatteryOptimized: Boolean, onBack: () -> Unit, onUpdateConfig: (AetherConfig) -> Unit, onUpdateTunnelEngine: (TunnelEngine) -> Unit, onApplyPreset: (String) -> Unit, onOpenSplitTunneling: () -> Unit, onOpenRoutingRules: () -> Unit, onRequestBatteryOptimization: () -> Unit, onOpenVpnSettings: () -> Unit, onResetAll: () -> Unit, onExportBackup: () -> Unit, onImportBackup: () -> Unit, onOptimizeMtu: () -> Unit, isOptimizingMtu: Boolean, onShowToast: (String, Boolean) -> Unit, bottomContentPadding: Dp, onOpenDnsOptimizer: () -> Unit = {}, loadAutoConnectSettings: () -> AutoConnectSettings = { AutoConnectSettings() }, saveAutoConnectSettings: (AutoConnectSettings) -> Unit = {}) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showAdvancedZt by remember { mutableStateOf(false) }
    val isAndroid = remember { try { Class.forName("android.os.Build"); true } catch(_: Throwable) { false } }
    val focusManager = LocalFocusManager.current

    io.github.immaghzbad.aetherst.shared.ui.components.PlatformBackHandler(enabled = true, onBack = onBack)

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }) {
        val strings = LocalAppStrings.current
        Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = if (isDesktop) 12.dp else 36.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
            Text(
                when (page) {
                    SettingsPage.PRESETS -> strings.CAT_CONFIGURATION_PROFILES
                    SettingsPage.CONNECTION -> strings.CAT_CONNECTION_TUNNELING
                    SettingsPage.PROTOCOL -> strings.CAT_PROTOCOL_TRANSPORT
                    SettingsPage.ZEROTRUST -> strings.CAT_ZEROTRUST
                    SettingsPage.NETWORK -> strings.CAT_NETWORK_PARAMETERS
                SettingsPage.SECURITY -> strings.CAT_SECURITY_RELIABILITY
                SettingsPage.AUTO_CONNECT -> strings.CAT_AUTO_CONNECT
                SettingsPage.DIAGNOSTICS -> strings.CAT_DIAGNOSTICS_CORE
                    SettingsPage.HEV_ENGINE -> strings.CAT_HEV_ENGINE
                    SettingsPage.SYSTEM -> strings.CAT_SYSTEM_MAINTENANCE
                    SettingsPage.INTERFACE -> strings.CAT_USER_INTERFACE
                }, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomContentPadding + 12.dp, top = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (page) {
                SettingsPage.PRESETS -> item { PresetPage(config, onApplyPreset, onShowToast) }
                SettingsPage.CONNECTION -> item { ConnectionPage(config, isAndroid, onUpdateConfig, onUpdateTunnelEngine, onOpenSplitTunneling, onOpenRoutingRules) }
                SettingsPage.PROTOCOL -> item { ProtocolPage(config, onUpdateConfig, onOptimizeMtu, isOptimizingMtu) }
                SettingsPage.ZEROTRUST -> item { ZeroTrustPage(config, showAdvancedZt, onUpdateConfig) { showAdvancedZt = it } }
                SettingsPage.NETWORK -> item { NetworkPage(config, onUpdateConfig, onShowToast, onOpenDnsOptimizer) }
                SettingsPage.SECURITY -> item { SecurityPage(config, isAndroid, isBatteryOptimized, onUpdateConfig, onRequestBatteryOptimization) }
                SettingsPage.AUTO_CONNECT -> item { AutoConnectPage(isAndroid, loadAutoConnectSettings, saveAutoConnectSettings) }
                SettingsPage.DIAGNOSTICS -> item { DiagnosticsPage(config, onUpdateConfig) }
                SettingsPage.HEV_ENGINE -> item { HevEnginePage(config, onUpdateConfig) }
                SettingsPage.SYSTEM -> item { SystemPage(isAndroid, onExportBackup, onImportBackup, onOpenVpnSettings) { showResetDialog = true } }
                SettingsPage.INTERFACE -> item { InterfacePage(config, onUpdateConfig) }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
    val strings = LocalAppStrings.current
    if (showResetDialog) IosConfirmationDialog(title = strings.SETTINGS_TITLE, message = strings.RESET_DEFAULTS_SUB, confirmText = strings.RESET_DEFAULTS, confirmColor = AppPalette.statusError, onConfirm = { onResetAll(); showResetDialog = false; onShowToast(strings.RESET_DEFAULTS_SUB, false) }, onDismiss = { showResetDialog = false })
}

@Composable private fun PresetPage(config: AetherConfig, onApplyPreset: (String) -> Unit, onShowToast: (String, Boolean) -> Unit) {
    val strings = LocalAppStrings.current
    IosGroupCard { Column {
        IosPresetItem(icon = Icons.Default.Bolt, iconBg = AppPalette.statusScanning, title = strings.PRESETS_TURBO, subtitle = strings.PRESETS_TURBO_SUB, isActive = config.presetId == "turbo", onClick = { onApplyPreset("turbo"); onShowToast(strings.PRESETS_TURBO, false) })
        Row(modifier = Modifier.fillMaxWidth().padding(start = 58.dp, end = 16.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(AppPalette.statusScanning.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(strings.PRESET_CHIP_SPEED, color = AppPalette.statusScanning, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(IosGroupBg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("MTU 1320", color = IosSecondaryLabel, fontSize = 10.sp) }
            if (config.presetId == "turbo") androidx.compose.foundation.layout.Box(modifier = Modifier.background(Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("Recommended", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
        AppDivider(); IosPresetItem(icon = Icons.Default.Search, iconBg = AppPalette.accent, title = strings.PRESETS_THOROUGH, subtitle = strings.PRESETS_THOROUGH_SUB, isActive = config.presetId == "thorough", onClick = { onApplyPreset("thorough"); onShowToast(strings.PRESETS_THOROUGH, false) })
        Row(modifier = Modifier.fillMaxWidth().padding(start = 58.dp, end = 16.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(AppPalette.accent.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(strings.PRESET_CHIP_STABLE, color = AppPalette.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(IosGroupBg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("MTU 1320", color = IosSecondaryLabel, fontSize = 10.sp) }
        }
        AppDivider(); IosPresetItem(icon = Icons.Default.VisibilityOff, iconBg = AppPalette.accentVariant, title = strings.PRESETS_STEALTH, subtitle = strings.PRESETS_STEALTH_SUB, isActive = config.presetId == "stealth", onClick = { onApplyPreset("stealth"); onShowToast(strings.PRESETS_STEALTH, false) })
        Row(modifier = Modifier.fillMaxWidth().padding(start = 58.dp, end = 16.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(AppPalette.accentVariant.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(strings.PRESET_CHIP_STEALTH, color = AppPalette.accentVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(IosGroupBg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("MTU 1330", color = IosSecondaryLabel, fontSize = 10.sp) }
        }
        AppDivider(); IosPresetItem(icon = Icons.Default.Shield, iconBg = IosActiveBlue, title = strings.PRESETS_IRONCLAD, subtitle = strings.PRESETS_IRONCLAD_SUB, isActive = config.presetId == "ironclad", onClick = { onApplyPreset("ironclad"); onShowToast(strings.PRESETS_IRONCLAD, false) })
        Row(modifier = Modifier.fillMaxWidth().padding(start = 58.dp, end = 16.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(AppPalette.statusError.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(strings.PRESET_CHIP_IRONCLAD, color = AppPalette.statusError, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            androidx.compose.foundation.layout.Box(modifier = Modifier.background(IosGroupBg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("MTU 1330", color = IosSecondaryLabel, fontSize = 10.sp) }
        }
        AppDivider(); IosPresetItem(icon = Icons.Default.Tune, iconBg = AppPalette.textSecondary, title = strings.PRESETS_CUSTOM, subtitle = strings.PRESETS_CUSTOM_SUB, isActive = config.presetId == "custom", onClick = { onApplyPreset("custom"); onShowToast(strings.PRESETS_CUSTOM, false) })
    } }
}

@Composable private fun ConnectionPage(config: AetherConfig, isAndroid: Boolean, onUpdateConfig: (AetherConfig) -> Unit, onUpdateTunnelEngine: (TunnelEngine) -> Unit, onOpenSplitTunneling: () -> Unit, onOpenRoutingRules: () -> Unit) {
    val strings = LocalAppStrings.current
    IosGroupCard { Column {
        val opts = if (isAndroid) listOf(strings.TUN_MODE, strings.PROXY_ONLY) else if (isDesktop) listOf(strings.TUN_MODE_GLOBAL, strings.SYSTEM_PROXY, strings.PROXY_ONLY) else listOf(strings.TUN_MODE_GLOBAL, strings.SYSTEM_PROXY, strings.PROXY_ONLY)
        IosPickerRow(icon = Icons.Default.VpnLock, iconBg = AppPalette.statusConnected,             title = strings.CONNECTION_MODE, value = when (config.connectionMode) { ConnectionMode.TUNNEL -> if (isAndroid) strings.TUN_MODE else strings.TUN_MODE_GLOBAL; ConnectionMode.SYSTEM_PROXY -> strings.SYSTEM_PROXY; else -> strings.PROXY_ONLY }, options = opts, onOptionSelected = { val m = if (isAndroid) { if (it == 0) ConnectionMode.TUNNEL else ConnectionMode.PROXY_ONLY } else if (isDesktop) { when (it) { 0 -> ConnectionMode.TUNNEL; 1 -> ConnectionMode.SYSTEM_PROXY; else -> ConnectionMode.PROXY_ONLY } } else { when (it) { 0 -> ConnectionMode.TUNNEL; 1 -> ConnectionMode.SYSTEM_PROXY; else -> ConnectionMode.PROXY_ONLY } }; onUpdateConfig(config.copy(connectionMode = m)) })
        if (config.connectionMode == ConnectionMode.TUNNEL) {
            AppDivider(); IosPickerRow(icon = Icons.Default.VpnLock, iconBg = AppPalette.accentVariant, title = strings.TUNNEL_ENGINE, value = config.tunnelEngine.displayName, options = TunnelEngine.entries.map { it.displayName }, onOptionSelected = { onUpdateTunnelEngine(TunnelEngine.entries[it]) })
            if (!isDesktop) {
                AppDivider(); IosSwitchRow(icon = Icons.Default.AllInclusive, iconBg = IosActiveBlue, title = strings.TUNNEL_WHOLE_DEVICE, subtitle = strings.TUNNEL_WHOLE_DEVICE_SUB, checked = config.tunnelAllApps, onCheckedChange = { onUpdateConfig(config.copy(tunnelAllApps = it)) }, testTag = "switch_tunnel_all"); AppDivider(); IosPickerRow(icon = Icons.Default.Tune, iconBg = AppPalette.accentVariant, title = strings.SPLIT_TUNNELING, value = if (config.tunnelAllApps) strings.SPLIT_TUNNELING_ALL else "${config.tunneledPackages.size + config.blockedPackages.size} ${strings.SPLIT_TUNNELING_CUSTOM}", options = emptyList(), onOptionSelected = {}, onClickOverride = onOpenSplitTunneling, enabled = !config.tunnelAllApps)
            }
            AppDivider()
        }
        IosPickerRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = IosActiveBlue, title = strings.DOMAIN_IP_ROUTING, value = "${config.routingRules.size} ${strings.ROUTING_RULES_TITLE}", options = emptyList(), onOptionSelected = {}, onClickOverride = onOpenRoutingRules)
        if (isAndroid) { AppDivider(); IosSwitchRow(icon = Icons.Default.Share, iconBg = AppPalette.accentVariantAlt, title = strings.SHARE_HOTSPOT, subtitle = strings.SHARE_HOTSPOT_SUB, checked = config.shareHotspot, onCheckedChange = { onUpdateConfig(config.copy(shareHotspot = it)) }, testTag = "switch_share_hotspot"); if (config.shareHotspot) HotspotInfo(config) }
    } }
}

@Composable private fun InterfacePage(config: AetherConfig, onUpdateConfig: (AetherConfig) -> Unit) {
    val strings = LocalAppStrings.current
    IosGroupCard {
        IosPickerRow(
            icon = Icons.Default.TouchApp,
            iconBg = IosActiveBlue,
            title = strings.INTERFACE_CONNECT_BUTTON,
            value = if (config.connectButtonStyle == "capsule") strings.CONNECT_BUTTON_CAPSULE else strings.CONNECT_BUTTON_SWIPE,
            options = listOf(strings.CONNECT_BUTTON_SWIPE, strings.CONNECT_BUTTON_CAPSULE),
            onOptionSelected = { idx -> onUpdateConfig(config.copy(connectButtonStyle = if (idx == 0) "swipe" else "capsule")) }
        )
        AppDivider()
        val langOptions = listOf(strings.LANGUAGE_AUTO, strings.LANGUAGE_ENGLISH, strings.LANGUAGE_PERSIAN)
        val langCodes = listOf("auto", "en", "fa")
        val currentLangIdx = langCodes.indexOf(config.appLanguage).coerceAtLeast(0)
        IosPickerRow(
            icon = Icons.Default.Language,
            iconBg = AppPalette.accentVariant,
            title = strings.APP_LANGUAGE,
            value = langOptions[currentLangIdx],
            options = langOptions,
            onOptionSelected = { idx -> onUpdateConfig(config.copy(appLanguage = langCodes[idx])) }
        )
    }
}

@Composable private fun HotspotInfo(config: AetherConfig) {
    val strings = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxWidth().background(IosGroupBg.copy(alpha = 0.4f)).padding(14.dp)) {
        var localIp by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) { localIp = NetworkUtils.getLocalIpAddress() }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { Icon(if (localIp != null) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (localIp != null) IosActiveGreen else AppPalette.statusScanning, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(if (localIp != null) strings.HOTSPOT_ACTIVE else strings.HOTSPOT_INACTIVE, color = if (localIp != null) IosActiveGreen else AppPalette.statusScanning, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            IconButton(onClick = { localIp = NetworkUtils.getLocalIpAddress() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Refresh, null, tint = IosActiveBlue, modifier = Modifier.size(18.dp)) }
        }
        if (localIp != null) { Spacer(modifier = Modifier.height(10.dp)); Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color.Black.copy(alpha = 0.3f)) { Column(modifier = Modifier.padding(12.dp)) { Text(strings.PROXY_ADDRESS, color = IosSecondaryLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp); Spacer(modifier = Modifier.height(6.dp)); Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("$localIp:${config.socksPort}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) } } } }
    }
}

@Composable private fun ProtocolPage(config: AetherConfig, onUpdateConfig: (AetherConfig) -> Unit, onOptimizeMtu: () -> Unit, isOptimizingMtu: Boolean) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IosGroupCard { Column {
            IosPickerRow(icon = Icons.Default.VpnLock, iconBg = IosActiveBlue, title = strings.TRANSPORT_PROTOCOL, value = config.protocol.displayName, options = AetherProtocol.entries.map { it.displayName }, onOptionSelected = { onUpdateConfig(config.copy(protocol = AetherProtocol.entries[it])) })
            if (config.protocol == AetherProtocol.MASQUE) { AppDivider(); IosSwitchRow(icon = Icons.Default.Http, iconBg = IosActiveBlue, title = strings.HTTP2_FALLBACK, subtitle = strings.HTTP2_FALLBACK_SUB, checked = config.h2Mode, onCheckedChange = { onUpdateConfig(config.copy(h2Mode = it)) }, testTag = "switch_h2_mode"); AppDivider(); IosSwitchRow(icon = Icons.Default.VerticalSplit, iconBg = AppPalette.accentVariant, title = strings.PACKET_FRAGMENTATION, subtitle = strings.PACKET_FRAGMENTATION_SUB, checked = config.h2Fragment, onCheckedChange = { onUpdateConfig(config.copy(h2Fragment = it)) }, testTag = "switch_fragment"); if (config.h2Fragment) { IosInputFieldRow(icon = Icons.Default.Straighten, iconBg = IosSecondaryLabel, label = strings.FRAGMENT_SIZE, value = config.fragmentSize, onValueChange = { onUpdateConfig(config.copy(fragmentSize = it)) }, placeholder = "16-32", testTag = "fragment_size_input"); AppDivider(); IosInputFieldRow(icon = Icons.Default.Timer, iconBg = IosSecondaryLabel, label = strings.FRAGMENT_DELAY, value = config.fragmentDelay, onValueChange = { onUpdateConfig(config.copy(fragmentDelay = it)) }, placeholder = "2-10", testTag = "fragment_delay_input"); AppDivider() }; IosSwitchRow(icon = Icons.Default.EnhancedEncryption, iconBg = IosActiveGreen, title = strings.ECH, subtitle = strings.ECH_SUB, checked = config.echEnabled, onCheckedChange = { onUpdateConfig(config.copy(echEnabled = it)) }, testTag = "switch_ech_enabled"); AppDivider(); IosInputFieldRow(icon = Icons.Default.Straighten, iconBg = IosSecondaryLabel, label = strings.MASQUE_INNER_MTU, value = if (config.masqueMtu > 0) config.masqueMtu.toString() else "", onValueChange = { onUpdateConfig(config.copy(masqueMtu = it.toIntOrNull()?.coerceIn(0, 9000) ?: 0)) }, placeholder = "Auto", keyboardType = KeyboardType.Number, testTag = "masque_mtu_input"); AppDivider() }
            IosSwitchRow(icon = Icons.Default.DataUsage, iconBg = AppPalette.statusScanning, title = strings.DISABLE_DATA_VERIFICATION, subtitle = strings.DISABLE_DATA_VERIFICATION_SUB, checked = config.noDataCheck, onCheckedChange = { onUpdateConfig(config.copy(noDataCheck = it)) }, testTag = "switch_no_data_check"); AppDivider()
            val availNoise = if (config.protocol == AetherProtocol.MASQUE) listOf(AetherNoise.FIREWALL, AetherNoise.GFW, AetherNoise.OFF) else listOf(AetherNoise.BALANCED, AetherNoise.AGGRESSIVE, AetherNoise.LIGHT, AetherNoise.OFF)
            IosPickerRow(icon = Icons.Default.Tune, iconBg = AppPalette.accentVariantAlt, title = strings.BYPASS_OBFUSCATION, value = config.noise.displayName.substringBefore(" ("), options = availNoise.map { it.displayName }, onOptionSelected = { onUpdateConfig(config.copy(noise = availNoise[it])) }); AppDivider()
            IosPickerRow(icon = Icons.Default.NetworkCheck, iconBg = AppPalette.statusScanning, title = strings.SPEED_STRATEGY, value = config.scanMode.name.lowercase().replaceFirstChar { it.uppercase() }, options = AetherScanMode.entries.map { "${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }} (${it.description})" }, onOptionSelected = { onUpdateConfig(config.copy(scanMode = AetherScanMode.entries[it])) }); AppDivider()
            IosPickerRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = AppPalette.accentVariant, title = strings.NETWORK_STACK, value = config.ipMode.rawValue, options = AetherIpMode.entries.map { it.displayName }, onOptionSelected = { onUpdateConfig(config.copy(ipMode = AetherIpMode.entries[it])) }); AppDivider()
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.Bottom) { Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.Default.Tune, backgroundColor = IosActiveGreen); Spacer(modifier = Modifier.width(12.dp)); IosInputField(label = strings.CUSTOM_MTU, value = config.mtu.toString(), onValueChange = { onUpdateConfig(config.copy(mtu = it.toIntOrNull() ?: 1100)) }, modifier = Modifier.weight(1f), placeholder = "1100", keyboardType = KeyboardType.Number, testTag = "mtu_input") }; Spacer(modifier = Modifier.width(8.dp)); Button(onClick = onOptimizeMtu, enabled = !isOptimizingMtu, modifier = Modifier.height(46.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = IosActiveBlue.copy(alpha = 0.15f), contentColor = IosActiveBlue, disabledContainerColor = IosActiveBlue.copy(alpha = 0.05f), disabledContentColor = IosActiveBlue.copy(alpha = 0.3f)), contentPadding = PaddingValues(horizontal = 16.dp)) { if (isOptimizingMtu) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = IosActiveBlue, strokeWidth = 2.dp) else Text(strings.OPTIMIZE, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }
        } }
        Text(strings.CLOAK_OBFUSCATION, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = IosSecondaryLabel, fontSize = 11.sp, letterSpacing = 0.5.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        IosGroupCard { Column {
            IosSwitchRow(icon = Icons.Default.Security, iconBg = AppPalette.accentVariant, title = strings.CLOAK_DECOY, subtitle = if (config.cloakEnabled) strings.CLOAK_DECOY_ON else if (isDesktop) strings.CLOAK_DECOY_OFF_WIN else strings.CLOAK_DECOY_OFF_ANDROID, checked = config.cloakEnabled, onCheckedChange = { onUpdateConfig(config.copy(cloakEnabled = it)) }, testTag = "switch_cloak_enabled")
            if (config.cloakEnabled) {
                AppDivider()
                if (isDesktop) {
                    Row(modifier = Modifier.fillMaxWidth().background(AppPalette.statusConnected.copy(alpha = 0.12f)).padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, null, tint = AppPalette.statusConnected, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.CLOAK_WINDOWS_INFO, color = AppPalette.statusConnected, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                    AppDivider()
                }
                if (config.protocol != AetherProtocol.MASQUE || !config.h2Mode) {
                    Row(modifier = Modifier.fillMaxWidth().background(AppPalette.statusScanning.copy(alpha = 0.12f)).padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, null, tint = AppPalette.statusScanning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${strings.CLOAK_H2_ONLY_INFO} ${config.protocol.displayName} ${if (config.h2Mode) "H2" else "H3"}", color = AppPalette.statusScanning, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                    AppDivider()
                }
                IosInputFieldRow(icon = Icons.Default.Public, iconBg = IosActiveGreen, label = strings.CLOAK_DECOY_SNI_LIST, value = config.cloakSniList, onValueChange = { onUpdateConfig(config.copy(cloakSniList = it)) }, placeholder = "www.bing.com,www.hcaptcha.com", testTag = "cloak_sni_input"); AppDivider()
                IosInputFieldRow(icon = Icons.Default.Timer, iconBg = AppPalette.statusScanning, label = strings.CLOAK_TTL_LIST, value = config.cloakTtlList, onValueChange = { onUpdateConfig(config.copy(cloakTtlList = it)) }, placeholder = "4,5,6,8", testTag = "cloak_ttl_input"); AppDivider()
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.Default.Timer, backgroundColor = IosSecondaryLabel); Spacer(modifier = Modifier.width(12.dp)); IosInputField(label = strings.CLOAK_JITTER_MIN, value = config.cloakJitterMin.toString(), onValueChange = { onUpdateConfig(config.copy(cloakJitterMin = it.toIntOrNull() ?: 20)) }, placeholder = "20", keyboardType = KeyboardType.Number, testTag = "cloak_jitter_min") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IosInputField(label = strings.CLOAK_JITTER_MAX, value = config.cloakJitterMax.toString(), onValueChange = { onUpdateConfig(config.copy(cloakJitterMax = it.toIntOrNull() ?: 80)) }, placeholder = "80", keyboardType = KeyboardType.Number, testTag = "cloak_jitter_max") }
                }; AppDivider()
                IosSwitchRow(icon = Icons.Default.VerticalSplit, iconBg = AppPalette.accentVariant, title = strings.CLOAK_FRAGMENT_REAL_HELLO, subtitle = strings.CLOAK_FRAGMENT_REAL_HELLO_SUB, checked = config.cloakFragment, onCheckedChange = { onUpdateConfig(config.copy(cloakFragment = it)) }, testTag = "switch_cloak_fragment"); AppDivider()
                IosSwitchRow(icon = Icons.Default.Refresh, iconBg = IosActiveGreen, title = strings.CLOAK_ADAPTIVE_STATS, subtitle = strings.CLOAK_ADAPTIVE_STATS_SUB, checked = config.cloakAdaptive, onCheckedChange = { onUpdateConfig(config.copy(cloakAdaptive = it)) }, testTag = "switch_cloak_adaptive"); AppDivider()
                IosSwitchRow(icon = Icons.Default.FontDownload, iconBg = IosSecondaryLabel, title = strings.CLOAK_RANDOMIZE_SNI_CASE, subtitle = strings.CLOAK_RANDOMIZE_SNI_CASE_SUB, checked = config.cloakRandomizeSniCase, onCheckedChange = { onUpdateConfig(config.copy(cloakRandomizeSniCase = it)) }, testTag = "switch_cloak_randomize"); AppDivider()
                IosInputFieldRow(icon = Icons.Default.Dns, iconBg = IosActiveBlue, label = strings.CLOAK_FALLBACK_PORTS, value = config.cloakFallbackPorts, onValueChange = { onUpdateConfig(config.copy(cloakFallbackPorts = it)) }, placeholder = "443,2053,2083,2087,2096,8443", testTag = "cloak_fallback_input"); AppDivider()
                IosPickerRow(icon = Icons.Default.BugReport, iconBg = IosSecondaryLabel, title = strings.CLOAK_LOG_LEVEL_TITLE, value = config.cloakLogLevel, options = listOf("error", "warn", "info", "debug"), onOptionSelected = { idx -> val lvl = listOf("error", "warn", "info", "debug")[idx]; onUpdateConfig(config.copy(cloakLogLevel = lvl)) })
            }
        } }
        IosGroupCard {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(strings.ABOUT_CLOAK, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(strings.CLOAK_ABOUT_DESC, color = IosSecondaryLabel, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable private fun ZeroTrustPage(config: AetherConfig, showAdvanced: Boolean, onUpdateConfig: (AetherConfig) -> Unit, onToggleAdvanced: (Boolean) -> Unit) {
    val strings = LocalAppStrings.current
    val isZt = config.protocol == AetherProtocol.ZERO_TRUST
    val ztError = if (isZt) config.zeroTrustError() else null
    val hasAuth = config.teamName.isNotBlank() &&
        (config.accessEmail.isNotBlank() || config.accessId.isNotBlank() || config.accessSecret.isNotBlank() || config.accessToken.isNotBlank())
    IosGroupCard { Column {
        IosInputFieldRow(icon = Icons.Default.Business, iconBg = AppPalette.accentVariant, label = if (isZt) strings.ZT_TEAM_NAME_REQUIRED else strings.ZT_TEAM_NAME, value = config.teamName, onValueChange = { onUpdateConfig(config.copy(teamName = it)) }, placeholder = "e.g. my-org", testTag = "zt_team_input"); AppDivider()
        IosInputFieldRow(icon = Icons.Default.Language, iconBg = IosActiveBlue, label = strings.ZT_ACCESS_EMAIL, value = config.accessEmail, onValueChange = { onUpdateConfig(config.copy(accessEmail = it)) }, placeholder = "user@example.com", testTag = "zt_email_input"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Shield, iconBg = IosActiveGreen, title = strings.ZT_GATEWAY, subtitle = strings.ZT_GATEWAY_SUB, checked = config.useGateway, onCheckedChange = { onUpdateConfig(config.copy(useGateway = it)) }, testTag = "switch_zt_gateway"); AppDivider()
        IosSwitchRow(icon = Icons.Default.CheckCircle, iconBg = IosActiveBlue, title = strings.ZT_STAY_SIGNED_IN, subtitle = strings.ZT_STAY_SIGNED_IN_SUB, checked = config.ztStaySignedIn, onCheckedChange = { onUpdateConfig(config.copy(ztStaySignedIn = it)) }, testTag = "switch_zt_stay_signed_in"); AppDivider()
        if (hasAuth) {
            Row(modifier = Modifier.fillMaxWidth().clickable { onUpdateConfig(config.copy(teamName = "", accessEmail = "", accessId = "", accessSecret = "", accessToken = "", ztTokenExpiry = 0, ztStaySignedIn = false)) }.padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.AutoMirrored.Filled.Logout, backgroundColor = AppPalette.statusError); Spacer(modifier = Modifier.width(12.dp)); Text(strings.ZT_SIGN_OUT, fontWeight = FontWeight.Medium, color = AppPalette.statusError, fontSize = 15.sp) } }
        }
        Row(modifier = Modifier.fillMaxWidth().clickable { onToggleAdvanced(!showAdvanced) }.padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.Default.Lock, backgroundColor = IosSecondaryLabel); Spacer(modifier = Modifier.width(12.dp)); Text(strings.ZT_ADVANCED_AUTH, fontWeight = FontWeight.Medium, color = Color.White, fontSize = 15.sp) }; Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = IosSecondaryLabel, modifier = Modifier.size(18.dp)) }
        AnimatedVisibility(visible = showAdvanced, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column(modifier = Modifier.fillMaxWidth().background(IosGroupBg.copy(alpha = 0.4f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(strings.ZT_CHOOSE_ONE_METHOD, color = IosSecondaryLabel, fontSize = 12.sp); IosInputField(label = strings.ZT_ACCESS_ID, value = config.accessId, onValueChange = { onUpdateConfig(config.copy(accessId = it)) }, placeholder = "Required for Service Tokens", testTag = "zt_access_id"); IosInputField(label = strings.ZT_ACCESS_SECRET, value = config.accessSecret, onValueChange = { onUpdateConfig(config.copy(accessSecret = it)) }, placeholder = "Required for Service Tokens", testTag = "zt_access_secret"); IosInputField(label = strings.ZT_ACCESS_TOKEN, value = config.accessToken, onValueChange = { onUpdateConfig(config.copy(accessToken = it, ztTokenExpiry = config.parseJwtExpiry(it))) }, placeholder = "Existing token you already hold", testTag = "zt_access_token") } }
    } }
    if (ztError != null) {
        Text(
            text = ztError,
            color = AppPalette.statusError,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
        )
    }
}

@Composable private fun NetworkPage(config: AetherConfig, onUpdateConfig: (AetherConfig) -> Unit, onShowToast: (String, Boolean) -> Unit = { _, _ -> }, onOpenDnsOptimizer: () -> Unit = {}) {
    val strings = LocalAppStrings.current
    val httpLocked = config.psiphonEnabled
    IosGroupCard { Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.Default.Language, backgroundColor = IosActiveBlue); Spacer(modifier = Modifier.width(12.dp)); IosInputField(label = strings.SOCKS5_HOST, value = config.socksHost, onValueChange = { onUpdateConfig(config.copy(socksHost = it)) }, modifier = Modifier.weight(1f), placeholder = "127.0.0.1", testTag = "socks_host_input"); Spacer(modifier = Modifier.width(10.dp)); IosInputField(label = strings.SOCKS_PORT, value = config.socksPort, onValueChange = { onUpdateConfig(config.copy(socksPort = it)) }, modifier = Modifier.width(75.dp), placeholder = "1819", keyboardType = KeyboardType.Number, testTag = "socks_port_input"); Spacer(modifier = Modifier.width(8.dp)); IosInputField(label = strings.HTTP_PORT, value = config.httpPort, onValueChange = { onUpdateConfig(config.copy(httpPort = it)) }, modifier = Modifier.width(75.dp), placeholder = "1820", keyboardType = KeyboardType.Number, testTag = "http_port_input") }
        AppDivider(); androidx.compose.foundation.layout.Box(modifier = if (httpLocked) Modifier.fillMaxWidth().clickable { onShowToast(strings.TOAST_DISABLE_PSIPHON_FIRST, true) } else Modifier.fillMaxWidth()) { IosSwitchRow(icon = Icons.Default.Http, iconBg = IosActiveBlue, title = strings.INTERNAL_HTTP_PROXY, subtitle = if (httpLocked) strings.INTERNAL_HTTP_PROXY_LOCKED_BY_PSIPHON else strings.INTERNAL_HTTP_PROXY_SUB, checked = config.httpProxyEnabled, enabled = !httpLocked, onCheckedChange = { if (httpLocked && !it) { onShowToast(strings.TOAST_DISABLE_PSIPHON_FIRST, true); return@IosSwitchRow }; onUpdateConfig(config.copy(httpProxyEnabled = it)) }, testTag = "switch_http_proxy_enabled") }; AppDivider()
        IosInputFieldRow(icon = Icons.Default.Code, iconBg = IosSecondaryLabel, label = strings.TLS_KEY_GROUPS, value = config.tlsGroups, onValueChange = { onUpdateConfig(config.copy(tlsGroups = it)) }, placeholder = "P-256:X25519:P-384",         testTag = "tls_groups_input"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Dns, iconBg = IosActiveBlue, title = strings.CUSTOM_DNS, subtitle = strings.CUSTOM_DNS_SUB, checked = config.dnsEnabled, onCheckedChange = { onUpdateConfig(config.copy(dnsEnabled = it, dnsList = if (it) config.dnsList.ifBlank { "1.1.1.1,1.0.0.1" } else config.dnsList)) }, testTag = "switch_custom_dns"); AppDivider()
        if (config.dnsEnabled) { Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.Bottom) { Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.Default.Dns, backgroundColor = IosActiveBlue); Spacer(modifier = Modifier.width(12.dp)); IosInputField(label = if (isDesktop) strings.TUNNEL_DNS_DESKTOP else strings.TUNNEL_DNS, value = config.dnsList, onValueChange = { onUpdateConfig(config.copy(dnsList = it.replace(Regex("\\s*,\\s*"), ","))) }, modifier = Modifier.weight(1f), placeholder = "1.1.1.1,1.0.0.1", testTag = "dns_list_input") }; Spacer(modifier = Modifier.width(8.dp)); Button(onClick = onOpenDnsOptimizer, modifier = Modifier.height(46.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = IosActiveBlue.copy(alpha = 0.15f), contentColor = IosActiveBlue), contentPadding = PaddingValues(horizontal = 16.dp)) { Text(strings.DNS_OPTIMIZE, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }; AppDivider() }
        if (isDesktop) {
            Row(modifier = Modifier.fillMaxWidth().background(AppPalette.statusConnected.copy(alpha = 0.08f)).padding(10.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = AppPalette.statusConnected, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Windows: Uses 127.0.0.1 local DNS relay via SOCKS. Requires admin to bind port 53. If not admin, DNS will fallback to ${config.dnsList}. Test on whoer.com.", color = AppPalette.statusConnected, fontSize = 11.sp, lineHeight = 14.sp)
            }
            AppDivider()
        }
        IosInputFieldRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = AppPalette.accentVariant, label = strings.FORCED_PEER_IP, value = config.peer, onValueChange = { onUpdateConfig(config.copy(peer = it)) }, placeholder = "e.g. 1.2.3.4:443", testTag = "peer_input"); AppDivider()
        if (config.protocol == AetherProtocol.WG || config.protocol == AetherProtocol.GOOL) {
            IosInputFieldRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = AppPalette.accentVariant, label = strings.WG_PEER, value = config.wgPeer, onValueChange = { onUpdateConfig(config.copy(wgPeer = it)) }, placeholder = "e.g. 162.159.192.1:2408", testTag = "wg_peer_input"); AppDivider()
        }
        if (config.protocol == AetherProtocol.GOOL) {
            IosInputFieldRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = AppPalette.accentVariant, label = strings.WIW_OUTER, value = config.wiwOuter, onValueChange = { onUpdateConfig(config.copy(wiwOuter = it)) }, placeholder = "e.g. 162.159.192.1:2408", testTag = "wiw_outer_input"); AppDivider()
            IosInputFieldRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = AppPalette.accentVariant, label = strings.WIW_INNER, value = config.wiwInner, onValueChange = { onUpdateConfig(config.copy(wiwInner = it)) }, placeholder = "e.g. 188.114.96.1:2408", testTag = "wiw_inner_input"); AppDivider()
            IosSwitchRow(icon = Icons.Default.Radar, iconBg = AppPalette.statusScanning, title = strings.WIW_SCAN, subtitle = strings.WIW_SCAN_SUB, checked = config.wiwScan, onCheckedChange = { onUpdateConfig(config.copy(wiwScan = it)) }, testTag = "switch_wiw_scan"); AppDivider()
        }
        if (config.protocol == AetherProtocol.WG || config.protocol == AetherProtocol.GOOL) {
            IosSwitchRow(icon = Icons.Default.Bolt, iconBg = AppPalette.statusScanning, title = strings.KEEPALIVE_PACKETS, subtitle = if (config.keepaliveEnabled) strings.KEEPALIVE_ON_SUB else strings.KEEPALIVE_OFF_SUB, checked = config.keepaliveEnabled, onCheckedChange = { onUpdateConfig(config.copy(keepaliveEnabled = it)) }, testTag = "switch_keepalive_enabled"); AppDivider()
            AnimatedVisibility(visible = config.keepaliveEnabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { Column { IosInputFieldRow(icon = Icons.Default.Bolt, iconBg = AppPalette.statusScanning, label = strings.KEEPALIVE_INTERVAL, value = config.keepalive.toString(), onValueChange = { onUpdateConfig(config.copy(keepalive = it.toIntOrNull()?.coerceIn(1, 300) ?: 5)) }, placeholder = "5", keyboardType = KeyboardType.Number, testTag = "keepalive_input"); AppDivider() } }
        }
        IosInputFieldRow(icon = Icons.Default.Timer, iconBg = IosSecondaryLabel, label = strings.VALIDATION_INTERVAL, value = config.validateSecs.toString(), onValueChange = { onUpdateConfig(config.copy(validateSecs = it.toIntOrNull()?.coerceIn(1, 300) ?: 10)) }, placeholder = "10", keyboardType = KeyboardType.Number, testTag = "validate_secs_input"); AppDivider()
        IosInputFieldRow(icon = Icons.Default.Timer, iconBg = IosSecondaryLabel, label = "Reconnect Interval", value = config.reconnectSecs.toString(), onValueChange = { onUpdateConfig(config.copy(reconnectSecs = it.toIntOrNull()?.coerceIn(1, 300) ?: 2)) }, placeholder = "2", keyboardType = KeyboardType.Number, testTag = "reconnect_secs_input"); AppDivider()
        if (config.protocol == AetherProtocol.WG || config.protocol == AetherProtocol.GOOL) {
            IosInputFieldRow(icon = Icons.Default.Timer, iconBg = IosSecondaryLabel, label = "Endpoint Cooldown", value = config.wgEndpointCooldownSecs.toString(), onValueChange = { onUpdateConfig(config.copy(wgEndpointCooldownSecs = it.toIntOrNull()?.coerceIn(30, 3600) ?: 300)) }, placeholder = "300", keyboardType = KeyboardType.Number, testTag = "wg_cooldown_input"); AppDivider()
        }
        IosInputFieldRow(icon = Icons.Default.Memory, iconBg = IosSecondaryLabel, label = strings.NETSTACK_TCP_RX, value = if (config.netstackTcpRx > 0) config.netstackTcpRx.toString() else "", onValueChange = { onUpdateConfig(config.copy(netstackTcpRx = it.toIntOrNull()?.coerceIn(0, 67108864) ?: 0)) }, placeholder = "Auto (bytes)", keyboardType = KeyboardType.Number, testTag = "netstack_rx_input"); AppDivider()
        IosInputFieldRow(icon = Icons.Default.Memory, iconBg = IosSecondaryLabel, label = strings.NETSTACK_TCP_TX, value = if (config.netstackTcpTx > 0) config.netstackTcpTx.toString() else "", onValueChange = { onUpdateConfig(config.copy(netstackTcpTx = it.toIntOrNull()?.coerceIn(0, 67108864) ?: 0)) }, placeholder = "Auto (bytes)", keyboardType = KeyboardType.Number, testTag = "netstack_tx_input"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Block, iconBg = AppPalette.statusError, title = "No Profile Retry", subtitle = "Disable retry with next profile on failure", checked = config.noProfileRetry, onCheckedChange = { onUpdateConfig(config.copy(noProfileRetry = it)) }, testTag = "switch_no_profile_retry")
    } }
}

@Composable private fun SecurityPage(config: AetherConfig, isAndroid: Boolean, isBatteryOptimized: Boolean, onUpdateConfig: (AetherConfig) -> Unit, onRequestBatteryOptimization: () -> Unit) {
    val strings = LocalAppStrings.current
    IosGroupCard { Column {
        IosSwitchRow(icon = Icons.Default.VpnLock, iconBg = AppPalette.accentVariant, title = strings.STRICT_KILL_SWITCH, subtitle = strings.STRICT_KILL_SWITCH_SUB, checked = config.strictKillSwitch, onCheckedChange = { onUpdateConfig(config.copy(strictKillSwitch = it)) }, testTag = "switch_strict_kill_switch"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Lock, iconBg = AppPalette.statusError, title = strings.KILL_SWITCH, subtitle = strings.KILL_SWITCH_SUB, checked = config.killSwitch, onCheckedChange = { onUpdateConfig(config.copy(killSwitch = it)) }, testTag = "switch_kill_switch"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Security, iconBg = AppPalette.accentVariant, title = strings.IPV6_LEAK, subtitle = strings.IPV6_LEAK_SUB, checked = config.ipv6Leak, onCheckedChange = { onUpdateConfig(config.copy(ipv6Leak = it)) }, testTag = "switch_ipv6_leak"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Restore, iconBg = IosActiveGreen, title = strings.SMART_RECONNECT, subtitle = strings.SMART_RECONNECT_SUB, checked = config.smartReconnect, onCheckedChange = { onUpdateConfig(config.copy(smartReconnect = it)) }, testTag = "switch_smart_reconnect")
        if (config.smartReconnect) { AppDivider(); Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IosIconBadge(icon = Icons.Default.Repeat, backgroundColor = IosSecondaryLabel); Spacer(modifier = Modifier.width(12.dp)); IosInputField(label = strings.MAX_RETRIES, value = config.reconnectRetryLimit.toString(), onValueChange = { onUpdateConfig(config.copy(reconnectRetryLimit = it.toIntOrNull() ?: 10)) }, placeholder = "10", keyboardType = KeyboardType.Number, testTag = "reconnect_limit_input") }; Spacer(modifier = Modifier.width(12.dp)); Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { IosInputField(label = strings.DELAY_SECS, value = config.reconnectSecs.toString(), onValueChange = { onUpdateConfig(config.copy(reconnectSecs = it.toIntOrNull() ?: 2)) }, placeholder = "2", keyboardType = KeyboardType.Number, testTag = "reconnect_secs_input") } } }
        AppDivider(); IosSwitchRow(icon = Icons.Default.Sync, iconBg = IosActiveGreen, title = strings.REPROVISION, subtitle = strings.REPROVISION_SUB, checked = config.reprovision, onCheckedChange = { onUpdateConfig(config.copy(reprovision = it)) }, testTag = "switch_reprovision")
        if (isAndroid) { AppDivider(); IosSwitchRow(icon = Icons.Default.BatteryAlert, iconBg = AppPalette.statusError, title = strings.BATTERY_OPTIMIZATION, subtitle = strings.BATTERY_OPTIMIZATION_SUB, checked = isBatteryOptimized, enabled = !isBatteryOptimized, onCheckedChange = { if (it) onRequestBatteryOptimization() }, testTag = "switch_battery_opt") }
    } }
}

private data class ParsedUpstreamProxy(val scheme: String, val host: String, val port: String, val user: String, val pass: String)

private fun parseUpstreamProxy(raw: String): ParsedUpstreamProxy {
    if (raw.isBlank()) return ParsedUpstreamProxy("socks5", "127.0.0.1", "", "", "")
    val scheme = if (raw.startsWith("http://", ignoreCase = true)) "http" else "socks5"
    val body = raw.removePrefix("socks5://").removePrefix("http://")
    val authHost = if (body.contains("@")) body.substringBefore("@") to body.substringAfter("@") else "" to body
    val (userRaw, passRaw) = if (authHost.first.contains(":")) authHost.first.substringBefore(":") to authHost.first.substringAfter(":") else authHost.first to ""
    val hostPort = authHost.second
    val (host, port) = if (hostPort.startsWith("[")) {
        val end = hostPort.indexOf("]")
        hostPort.substring(0, end + 1) to hostPort.substringAfter("]:").substringBefore("/")
    } else {
        val i = hostPort.lastIndexOf(":")
        if (i < 0) hostPort to "" else hostPort.substring(0, i) to hostPort.substring(i + 1)
    }
    return ParsedUpstreamProxy(scheme, host, port, decodeUpstreamCredential(userRaw), decodeUpstreamCredential(passRaw))
}

private fun buildUpstreamProxy(p: ParsedUpstreamProxy): String {
    if (p.host.isBlank() || p.port.isBlank()) return ""
    val auth = if (p.user.isBlank()) "" else "${encodeUpstreamCredential(p.user)}:${encodeUpstreamCredential(p.pass)}@"
    return "${p.scheme}://$auth${p.host}:${p.port}"
}

private fun decodeUpstreamCredential(s: String): String = s.replace("%40", "@").replace("%3A", ":")
private fun encodeUpstreamCredential(s: String): String = s.replace("@", "%40").replace(":", "%3A")

@Composable private fun DiagnosticsPage(config: AetherConfig, onUpdateConfig: (AetherConfig) -> Unit) {
    val strings = LocalAppStrings.current
    IosGroupCard { Column {
        IosInputFieldRow(icon = Icons.Default.Speed, iconBg = AppPalette.statusScanning, label = strings.PING_URL_LABEL, value = config.pingUrl, onValueChange = { onUpdateConfig(config.copy(pingUrl = it)) }, placeholder = "https://www.gstatic.com/generate_204", testTag = "ping_url_input"); AppDivider()
        val logLevelOptions = AetherLogLevel.entries.map { it.displayName }
        val logLevelLocalized = mapOf("off" to strings.LOG_LEVEL_OFF, "error" to strings.LOG_LEVEL_ERROR, "warn" to strings.LOG_LEVEL_WARN, "info" to strings.LOG_LEVEL_INFO, "debug" to strings.LOG_LEVEL_DEBUG)
        IosPickerRow(icon = Icons.Default.BugReport, iconBg = AppPalette.debugCyan, title = strings.APP_SYSTEM_LOGGING, value = logLevelLocalized[config.appLogLevel.rawValue] ?: config.appLogLevel.displayName.substringBefore(" ("), options = logLevelLocalized.values.toList(), onOptionSelected = { idx -> val key = logLevelLocalized.keys.toList()[idx]; val level = AetherLogLevel.entries.find { it.rawValue == key } ?: AetherLogLevel.INFO; onUpdateConfig(config.copy(appLogLevel = level)) }); AppDivider()
        IosPickerRow(icon = Icons.Default.VpnLock, iconBg = IosSecondaryLabel, title = strings.AETHER_CORE_LOGGING, value = logLevelLocalized[config.coreLogLevel.rawValue] ?: config.coreLogLevel.displayName.substringBefore(" ("), options = logLevelLocalized.values.toList(), onOptionSelected = { idx -> val key = logLevelLocalized.keys.toList()[idx]; val level = AetherLogLevel.entries.find { it.rawValue == key } ?: AetherLogLevel.INFO; onUpdateConfig(config.copy(coreLogLevel = level)) }); AppDivider()
        IosPickerRow(icon = Icons.Default.Speed, iconBg = IosActiveGreen, title = strings.PERFORMANCE_PROFILE, value = config.perfProfile.displayName, options = AetherPerfProfile.entries.map { it.displayName }, onOptionSelected = { onUpdateConfig(config.copy(perfProfile = AetherPerfProfile.entries[it])) }); AppDivider()
        Column {
            IosSwitchRow(icon = Icons.AutoMirrored.Filled.AltRoute, iconBg = AppPalette.accentVariantAlt, title = strings.CHAIN_EXTERNAL_PROXY, subtitle = strings.CHAIN_EXTERNAL_PROXY_SUB, checked = config.upstreamProxyEnabled, onCheckedChange = { onUpdateConfig(config.copy(upstreamProxyEnabled = it, upstreamProxy = if (it) config.upstreamProxy.ifBlank { "socks5://127.0.0.1:1080" } else "")) }, testTag = "switch_upstream_proxy")
            if (config.upstreamProxyEnabled) {
                AppDivider()
                val up = remember(config.upstreamProxy) { parseUpstreamProxy(config.upstreamProxy) }
                val updateUpstream: (ParsedUpstreamProxy) -> Unit = { onUpdateConfig(config.copy(upstreamProxy = buildUpstreamProxy(it))) }
                IosPickerRow(icon = Icons.Default.Shuffle, iconBg = AppPalette.accentVariantAlt, title = strings.PROXY_TYPE, value = up.scheme.uppercase(), options = listOf(strings.PROXY_TYPE_SOCKS5, strings.PROXY_TYPE_HTTP), onOptionSelected = { idx -> updateUpstream(up.copy(scheme = if (idx == 0) "socks5" else "http")) })
                AppDivider()
                IosInputFieldRow(icon = Icons.Default.Dns, iconBg = AppPalette.textSecondary, label = strings.PROXY_HOST, value = up.host, onValueChange = { updateUpstream(up.copy(host = it)) }, placeholder = "127.0.0.1", testTag = "upstream_proxy_host")
                AppDivider()
                IosInputFieldRow(icon = Icons.Default.Numbers, iconBg = AppPalette.textSecondary, label = strings.PROXY_PORT_LABEL, value = up.port, onValueChange = { updateUpstream(up.copy(port = it.filter { c -> c.isDigit() }.take(5))) }, placeholder = "1080", keyboardType = KeyboardType.Number, testTag = "upstream_proxy_port")
                AppDivider()
                IosInputFieldRow(icon = Icons.Default.Person, iconBg = AppPalette.textSecondary, label = strings.PROXY_USERNAME, value = up.user, onValueChange = { updateUpstream(up.copy(user = it)) }, placeholder = "user", testTag = "upstream_proxy_user")
                AppDivider()
                IosInputFieldRow(icon = Icons.Default.Lock, iconBg = AppPalette.textSecondary, label = strings.PROXY_PASSWORD, value = up.pass, onValueChange = { updateUpstream(up.copy(pass = it)) }, placeholder = "password", testTag = "upstream_proxy_pass")
            }
        }; AppDivider()
        IosSwitchRow(icon = Icons.AutoMirrored.Filled.Rule, iconBg = IosActiveBlue, title = strings.DOMAIN_SNIFFING, subtitle = strings.DOMAIN_SNIFFING_SUB, checked = config.routeSniffing, onCheckedChange = { onUpdateConfig(config.copy(routeSniffing = it)) }, testTag = "switch_route_sniffing")
        if (config.routeSniffing) { AppDivider(); IosInputFieldRow(icon = Icons.Default.Timer, iconBg = IosSecondaryLabel, label = strings.SNIFFING_TIMEOUT_LABEL, value = config.sniffingTimeoutMs.toString(), onValueChange = { onUpdateConfig(config.copy(sniffingTimeoutMs = it.toIntOrNull() ?: 100)) }, placeholder = "100", keyboardType = KeyboardType.Number, testTag = "sniffing_timeout_input") }
        AppDivider(); IosSwitchRow(icon = Icons.Default.Restore, iconBg = IosActiveGreen, title = strings.QUICK_RECONNECT_STRATEGY, subtitle = strings.QUICK_RECONNECT_STRATEGY_SUB, checked = config.quickReconnect, onCheckedChange = { onUpdateConfig(config.copy(quickReconnect = it)) }, testTag = "switch_quick_reconnect"); AppDivider()
        IosSwitchRow(icon = Icons.Default.Block, iconBg = AppPalette.statusError, title = strings.STRICT_PROFILE_LOCK, subtitle = strings.STRICT_PROFILE_LOCK_SUB, checked = config.noProfileRetry, onCheckedChange = { onUpdateConfig(config.copy(noProfileRetry = it)) }, testTag = "switch_no_profile_retry")
    } }
}

@Composable private fun HevEnginePage(config: AetherConfig, onUpdateConfig: (AetherConfig) -> Unit) {
    val strings = LocalAppStrings.current
    val hevLevels = listOf("error", "warn", "info", "debug")
    val levelLabels = mapOf("error" to strings.LOG_LEVEL_ERROR, "warn" to strings.HEV_LOG_WARN_DEFAULT, "info" to strings.HEV_LOG_INFO, "debug" to strings.HEV_LOG_DEBUG)
    val currentLevel = if (config.hevLogLevel in hevLevels) config.hevLogLevel else "warn"
    val hevUdpOptions = listOf("udp", "tcp", "off")
    val hevUdpLabels = mapOf(
        "udp" to strings.HEV_UDP_ASSOCIATE,
        "tcp" to strings.HEV_UDP_ICMP_TCP,
        "off" to strings.HEV_UDP_DISABLED
    )
    val rawMode = config.hevUdpMode.lowercase().trim().let { if (it == "icmp" || it == "true") "tcp" else it }
    val hevUdpMode = if (rawMode in hevUdpOptions) rawMode else "tcp"

    IosGroupCard { Column {
        IosPickerRow(
            icon = Icons.Default.BugReport,
            iconBg = AppPalette.accentVariantAlt,
            title = strings.HEV_LOG_LEVEL,
            value = levelLabels[currentLevel] ?: strings.HEV_LOG_WARN_DEFAULT,
            options = hevLevels.map { levelLabels[it]!! },
            onOptionSelected = { index -> onUpdateConfig(config.copy(hevLogLevel = hevLevels[index])) }
        ); AppDivider()
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                IosIconBadge(icon = Icons.Default.Timer, backgroundColor = IosActiveBlue); Spacer(modifier = Modifier.width(12.dp))
                IosInputField(label = strings.HEV_CONNECT_TIMEOUT_MS, value = config.hevConnectTimeoutMs.toString(), onValueChange = { onUpdateConfig(config.copy(hevConnectTimeoutMs = it.toIntOrNull()?.coerceIn(500, 120000) ?: 5000)) }, modifier = Modifier.weight(1f), placeholder = "5000", keyboardType = KeyboardType.Number, testTag = "hev_connect_timeout_input")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                IosIconBadge(icon = Icons.Default.SwapHoriz, backgroundColor = IosActiveGreen); Spacer(modifier = Modifier.width(12.dp))
                IosInputField(label = strings.HEV_RW_TIMEOUT_MS, value = config.hevReadWriteTimeoutMs.toString(), onValueChange = { onUpdateConfig(config.copy(hevReadWriteTimeoutMs = it.toIntOrNull()?.coerceIn(1000, 600000) ?: 60000)) }, modifier = Modifier.weight(1f), placeholder = "60000", keyboardType = KeyboardType.Number, testTag = "hev_rw_timeout_input")
            }
        }
        AppDivider()
        IosInputFieldRow(icon = Icons.Default.Layers, iconBg = AppPalette.accentVariant, label = strings.HEV_MAX_SESSIONS_LABEL, value = config.hevMaxSessionCount.toString(), onValueChange = { onUpdateConfig(config.copy(hevMaxSessionCount = it.toIntOrNull()?.coerceIn(0, 200000) ?: 0)) }, placeholder = "0", keyboardType = KeyboardType.Number, testTag = "hev_max_sessions_input"); AppDivider()
        IosInputFieldRow(icon = Icons.Default.Storage, iconBg = AppPalette.statusScanning, label = strings.HEV_MAPDNS_CACHE_SIZE_LABEL, value = config.hevMapdnsCacheSize.toString(), onValueChange = { onUpdateConfig(config.copy(hevMapdnsCacheSize = it.toIntOrNull()?.coerceIn(100, 1000000) ?: 10000)) }, placeholder = "10000", keyboardType = KeyboardType.Number, testTag = "hev_mapdns_cache_input")
        AppDivider()
        IosPickerRow(
            icon = Icons.Default.SwapVert,
            iconBg = Color(0xFF30B0C7),
            title = strings.HEV_UDP_FORWARDING_MODE,
            value = hevUdpLabels[hevUdpMode] ?: strings.HEV_UDP_ASSOCIATE,
            options = hevUdpOptions.map { hevUdpLabels[it]!! },
            onOptionSelected = { index -> onUpdateConfig(config.copy(hevUdpMode = hevUdpOptions[index])) }
        )
    } }
    Spacer(modifier = Modifier.height(8.dp))
    IosGroupCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(strings.ABOUT_HEV_ENGINE, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(strings.HEV_ABOUT_DESC, color = IosSecondaryLabel, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable private fun AutoConnectPage(isAndroid: Boolean, loadAutoConnectSettings: () -> AutoConnectSettings, saveAutoConnectSettings: (AutoConnectSettings) -> Unit) {
    val strings = LocalAppStrings.current
    var settings by remember { mutableStateOf(loadAutoConnectSettings()) }

    IosGroupCard { Column {
        IosSwitchRow(
            icon = Icons.Default.PlayArrow,
            iconBg = IosActiveGreen,
            title = strings.AUTO_CONNECT_ON_START,
            subtitle = strings.AUTO_CONNECT_ON_START_SUB,
            checked = settings.autoConnectOnStart,
            onCheckedChange = {
                settings = settings.copy(autoConnectOnStart = it)
                saveAutoConnectSettings(settings)
            },
            testTag = "switch_auto_connect_start"
        )
        AppDivider()
        IosSwitchRow(
            icon = Icons.Default.Restore,
            iconBg = IosActiveBlue,
            title = strings.AUTO_CONNECT_ON_BOOT,
            subtitle = if (isAndroid) strings.AUTO_CONNECT_ON_BOOT_SUB else "Only available on Android",
            checked = settings.autoConnectOnBoot,
            enabled = isAndroid,
            onCheckedChange = {
                if (isAndroid) {
                    settings = settings.copy(autoConnectOnBoot = it)
                    saveAutoConnectSettings(settings)
                }
            },
            testTag = "switch_auto_connect_boot"
        )
        AppDivider()
        IosSwitchRow(
            icon = Icons.Default.Wifi,
            iconBg = AppPalette.accentVariant,
            title = strings.AUTO_CONNECT_ON_NETWORK,
            subtitle = if (isAndroid) strings.AUTO_CONNECT_ON_NETWORK_SUB else "Only available on Android",
            checked = settings.autoConnectOnNetwork,
            enabled = isAndroid,
            onCheckedChange = {
                if (isAndroid) {
                    settings = settings.copy(autoConnectOnNetwork = it)
                    saveAutoConnectSettings(settings)
                }
            },
            testTag = "switch_auto_connect_network"
        )
        AppDivider()
        IosSwitchRow(
            icon = Icons.Default.Refresh,
            iconBg = AppPalette.statusScanning,
            title = strings.AUTO_RESTART_ON_CRASH,
            subtitle = if (isAndroid) strings.AUTO_RESTART_ON_CRASH_SUB else "Only available on Android",
            checked = settings.autoRestartOnCrash,
            enabled = isAndroid,
            onCheckedChange = {
                if (isAndroid) {
                    settings = settings.copy(autoRestartOnCrash = it)
                    saveAutoConnectSettings(settings)
                }
            },
            testTag = "switch_auto_restart_crash"
        )
        AppDivider()
        IosSwitchRow(
            icon = Icons.Default.Repeat,
            iconBg = AppPalette.statusConnected,
            title = strings.AUTO_CONNECT_AFTER_CRASH,
            subtitle = if (isAndroid) strings.AUTO_CONNECT_AFTER_CRASH_SUB else "Only available on Android",
            checked = settings.autoConnectAfterCrash,
            enabled = isAndroid,
            onCheckedChange = {
                if (isAndroid) {
                    settings = settings.copy(autoConnectAfterCrash = it)
                    saveAutoConnectSettings(settings)
                }
            },
            testTag = "switch_auto_connect_crash"
        )
    } }
    Spacer(modifier = Modifier.height(8.dp))
    IosGroupCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(strings.CAT_AUTO_CONNECT, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Auto-Connect: Restores your last VPN connection automatically.\n\n" +
                "Crash Recovery: If the app crashes more than 3 times in 60 seconds, auto-restart is disabled to prevent crash loops.\n\n" +
                "Manual Disconnect: If you tap Disconnect, auto-connect is paused until you connect again.",
                color = IosSecondaryLabel, fontSize = 12.sp, lineHeight = 17.sp
            )
        }
    }
}

@Composable private fun SystemPage(isAndroid: Boolean, onExportBackup: () -> Unit, onImportBackup: () -> Unit, onOpenVpnSettings: () -> Unit, onResetClick: () -> Unit) {
    IosGroupCard { Column {
        val strings = LocalAppStrings.current
        if (isAndroid) {
            IosActionRow(icon = Icons.Default.Lock, iconBg = Color(0xFF0A84FF), title = strings.ALWAYS_ON_VPN, subtitle = strings.ALWAYS_ON_VPN_SUB, onClick = onOpenVpnSettings); AppDivider()
        }
        IosActionRow(icon = Icons.Default.CloudUpload, iconBg = AppPalette.accentVariant, title = strings.FULL_BACKUP_TITLE, subtitle = strings.FULL_BACKUP_SUB, onClick = onExportBackup); AppDivider()
        IosActionRow(icon = Icons.Default.CloudDownload, iconBg = IosActiveGreen, title = strings.RESTORE_BACKUP_TITLE, subtitle = strings.RESTORE_BACKUP_SUB, onClick = onImportBackup); AppDivider()
        IosActionRow(icon = Icons.Default.DeleteForever, iconBg = AppPalette.statusError, title = strings.RESET_DEFAULTS, subtitle = strings.RESET_DEFAULTS_SUB, onClick = onResetClick, titleColor = AppPalette.statusError)
    } }
}
