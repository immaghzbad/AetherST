package io.github.immaghzbad.aetherst.shared.ui.screens
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.immaghzbad.aetherst.shared.ui.theme.AppPalette
import io.github.immaghzbad.aetherst.shared.ui.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.immaghzbad.aetherst.platform.PlatformContext
import io.github.immaghzbad.aetherst.platform.getSettings
import io.github.immaghzbad.aetherst.platform.getSystemUtils
import io.github.immaghzbad.aetherst.platform.isDesktop
import io.github.immaghzbad.aetherst.platform.isWindows
import io.github.immaghzbad.aetherst.shared.data.IpInfo
import io.github.immaghzbad.aetherst.shared.data.PingState
import io.github.immaghzbad.aetherst.shared.data.PsiphonEgressRegistry
import io.github.immaghzbad.aetherst.shared.model.AetherConfig
import io.github.immaghzbad.aetherst.shared.model.AetherProtocol
import io.github.immaghzbad.aetherst.shared.model.ConnectionMode
import io.github.immaghzbad.aetherst.shared.model.PsiphonChainMode
import io.github.immaghzbad.aetherst.shared.model.ConnectionStatus
import io.github.immaghzbad.aetherst.shared.model.SessionTraffic
import io.github.immaghzbad.aetherst.shared.ui.components.CountryFlag
import io.github.immaghzbad.aetherst.shared.i18n.LocalAppStrings
import io.github.immaghzbad.aetherst.shared.i18n.StringsFa
import io.github.immaghzbad.aetherst.shared.util.CountryNames
import kotlinx.coroutines.launch

private val IosCardBg = AppPalette.surfaceRaised
private val IosGroupBg = AppPalette.divider
private val IosSecondaryLabel = AppPalette.textSecondary
private val IosActiveGreen = AppPalette.statusConnected
private val IosActiveBlue = AppPalette.accent
private val IosScanningAmber = AppPalette.statusScanning
private val IosErrorRed = AppPalette.statusError

@Composable
fun DashboardScreen(
    config: AetherConfig,
    connectionStatus: ConnectionStatus,
    elapsedSeconds: Long,
    sessionTraffic: SessionTraffic,
    ipInfo: IpInfo = IpInfo(),
    pingState: PingState = PingState(),
    appVersion: String = "1.0.0",
    onToggleVpn: () -> Unit,
    onForceStop: () -> Unit = {},
    onUpdateConfig: (AetherConfig) -> Unit = {},
    onUpdateProtocol: (AetherProtocol) -> Unit,
    onTogglePsiphon: (Boolean) -> Unit = {},
    onRefreshIpInfo: () -> Unit = {},
    onRefreshPing: () -> Unit = {},
    onCopy: (String) -> Unit = {},
    onOpenSettingsToZeroTrust: () -> Unit = {},
    bottomContentPadding: Dp = 0.dp,
    platformContext: PlatformContext? = null,
    onSwipeDragging: (Boolean) -> Unit = {}
) {
    var showProxyOverlay by remember { mutableStateOf(true) }
    var showAdminRequiredDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var supportDialogAuto by remember { mutableStateOf(true) }
    var showPsiphonSheet by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val uriHandler = LocalUriHandler.current
    val settings = platformContext?.let { getSettings(it) }

    LaunchedEffect(Unit) {
        if (settings != null && !settings.getBoolean("support_dialog_dismissed", false)) {
            supportDialogAuto = true
            showSupportDialog = true
        }
    }
    val systemUtils = platformContext?.let { getSystemUtils(it) }

    LaunchedEffect(connectionStatus) {
        if (connectionStatus != ConnectionStatus.RUNNING) {
            showProxyOverlay = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = this.maxWidth
        val screenHeight = this.maxHeight
        val baseScale = screenWidth.value / 411f
        val scaleFactor = if (isDesktop) (baseScale * 0.82f).coerceIn(0.65f, 0.90f) else baseScale.coerceIn(0.7f, 1.1f)
        val isCompactHeight = screenHeight < 640.dp
        val isVeryCompactHeight = screenHeight < 580.dp
        val horizontalPadding = when {
            isDesktop -> 12.dp
            screenWidth < 360.dp -> 12.dp
            else -> 16.dp
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomContentPadding + 10.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(top = if (isDesktop) 8.dp else 36.dp),
                verticalArrangement = Arrangement.spacedBy(if (isDesktop) (10 * scaleFactor).dp else (14 * scaleFactor).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = strings.APP_TITLE,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (26 * scaleFactor).sp,
                            lineHeight = (30 * scaleFactor).sp
                        )
                        Text(
                            text = if (config.connectionMode == ConnectionMode.TUNNEL) strings.SUBTITLE_TUNNEL else strings.SUBTITLE_PROXY,
                            style = MaterialTheme.typography.bodySmall,
                            color = IosSecondaryLabel,
                            fontSize = (12 * scaleFactor).sp,
                            lineHeight = (16 * scaleFactor).sp
                        )
                        if (config.protocol == AetherProtocol.ZERO_TRUST && connectionStatus == ConnectionStatus.RUNNING && config.teamName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = IosActiveGreen, modifier = Modifier.size((14 * scaleFactor).dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = buildString {
                                        append(config.teamName)
                                        val who = config.accessEmail.ifBlank { config.accessId.ifBlank { config.accessToken.takeIf { it.isNotBlank() }?.let { "token" } } }
                                        if (!who.isNullOrBlank()) append(" • $who")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IosActiveGreen,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (12 * scaleFactor).sp
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (config.connectionMode == ConnectionMode.PROXY_ONLY && connectionStatus == ConnectionStatus.RUNNING) {
                            IconButton(
                                onClick = { showProxyOverlay = true },
                                modifier = Modifier.size((32 * scaleFactor).dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = strings.PROXY_INFO,
                                    tint = IosActiveBlue,
                                    modifier = Modifier.size((22 * scaleFactor).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width((8 * scaleFactor).dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = IosGroupBg,
                            modifier = Modifier.clickable {
                                supportDialogAuto = false
                                showSupportDialog = true
                            }
                        ) {
                            Text(
                                text = "v$appVersion",
                                modifier = Modifier.padding(horizontal = (12 * scaleFactor).dp, vertical = (6 * scaleFactor).dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IosActiveBlue,
                                fontSize = (10 * scaleFactor).sp
                            )
                        }
                    }
                }

                IosStatusHeroCard(
                    connectionStatus = connectionStatus,
                    elapsedSeconds = elapsedSeconds,
                    sessionTraffic = sessionTraffic,
                    config = config,
                    ipInfo = ipInfo,
                    pingState = pingState,
                    onRefreshIpInfo = onRefreshIpInfo,
                    onRefreshPing = onRefreshPing,
                    onCopy = onCopy,
                    hideConfigChips = isCompactHeight,
                    scaleFactor = scaleFactor
                )

                if (isWindows && (connectionStatus == ConnectionStatus.RUNNING || connectionStatus == ConnectionStatus.TUN_ACTIVE)) {
                    WindowsProxyPortsCard(
                        config = config,
                        onCopy = onCopy,
                        scaleFactor = scaleFactor
                    )
                }

                if (!isVeryCompactHeight && (connectionStatus == ConnectionStatus.ERROR || connectionStatus == ConnectionStatus.RECONNECTING)) {
                    val isReconnecting = connectionStatus == ConnectionStatus.RECONNECTING
                    val bg = if (isReconnecting) IosScanningAmber.copy(alpha = 0.12f) else IosErrorRed.copy(alpha = 0.1f)
                    val tint = if (isReconnecting) IosScanningAmber else IosErrorRed
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bg)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isReconnecting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = tint, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, null, tint = tint, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isReconnecting) {
                                    if (config.smartReconnect) "${strings.RECONNECTING_AUTO} (${config.reconnectRetryLimit} ${strings.LABEL_COUNTED} • ${config.reconnectSecs}s)" else strings.STATUS_RECONNECTING
                                } else {
                                    if (config.smartReconnect) strings.CONNECTION_FAILED_RETRY else strings.CONNECTION_FAILED_TRY
                                },
                                color = tint,
                                fontSize = (11 * scaleFactor).sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy((12 * scaleFactor).dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isWindows = remember { try { System.getProperty("os.name")?.lowercase()?.contains("win") == true } catch (_: Throwable) { false } }
                    val isAndroid = remember { !isDesktop }
                    val handleToggle: () -> Boolean = {
                        if (connectionStatus == ConnectionStatus.STOPPING) {
                            onForceStop()
                            true
                        } else if (config.protocol == AetherProtocol.ZERO_TRUST && connectionStatus == ConnectionStatus.STOPPED) {
                            if (config.zeroTrustError() != null) {
                                onOpenSettingsToZeroTrust()
                                false
                            } else {
                                onToggleVpn()
                                true
                            }
                        } else if (isWindows && config.connectionMode == ConnectionMode.TUNNEL && systemUtils?.isAdministrator() == false) {
                            showAdminRequiredDialog = true
                            false
                        } else {
                            onToggleVpn()
                            true
                        }
                    }
                    if (config.connectButtonStyle == "capsule") {
                        CapsuleConnectButton(
                            connectionStatus = connectionStatus,
                            onToggle = handleToggle,
                            onRecover = onForceStop,
                            modifier = Modifier.fillMaxWidth(),
                            scaleFactor = scaleFactor
                        )
                    } else if ((isDesktop && isWindows) || isAndroid) {
                        WindowsSwipeSwitch(
                            connectionStatus = connectionStatus,
                            onToggle = handleToggle,
                            onRecover = onForceStop,
                            onAdminCancelResetKey = if (showAdminRequiredDialog) 1 else 0,
                            modifier = Modifier.fillMaxWidth(),
                            scaleFactor = scaleFactor,
                            onDraggingChanged = onSwipeDragging,
                            isSwipeMode = config.connectButtonStyle != "capsule"
                        )
                    } else {
                        val minDim = if (screenWidth < screenHeight) screenWidth else screenHeight
                        val buttonSize = (minDim * 0.28f).coerceIn(90.dp, 140.dp)
                        IosPowerButton(
                            connectionStatus = connectionStatus,
                            onToggle = { handleToggle().let {} },
                            onRecover = onForceStop,
                            size = buttonSize
                        )
                    }
                }

                if (!isVeryCompactHeight) {
                    if (!isDesktop) {
                        val psiphonAllowed = config.protocol != AetherProtocol.ZERO_TRUST
                        val psiphonOn = config.psiphonEnabled && psiphonAllowed
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = if (psiphonOn) RoundedCornerShape(20.dp) else RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(containerColor = IosCardBg)
                        ) {
                            Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AppPalette.accentVariant), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(strings.PSIPHON_CHAIN, fontWeight = FontWeight.Bold, color = Color.White, fontSize = (13 * scaleFactor).sp)
                                        Text(if (!psiphonAllowed) strings.PSIPHON_NOT_AVAILABLE_ZT else if (config.psiphonEnabled) when (config.protocol) { AetherProtocol.MASQUE -> strings.PSIPHON_OVER_MASQUE ; AetherProtocol.WG -> strings.PSIPHON_OVER_WG ; AetherProtocol.GOOL -> strings.PSIPHON_OVER_GOOL ; AetherProtocol.ZERO_TRUST -> strings.PSIPHON_ROUTE_VIA } else strings.PSIPHON_ROUTE_VIA, color = IosSecondaryLabel, fontSize = (10 * scaleFactor).sp)
                                    }
                                }
                                Switch(
                                    checked = config.psiphonEnabled && psiphonAllowed,
                                    onCheckedChange = { onTogglePsiphon(it) },
                                    enabled = psiphonAllowed && (connectionStatus == ConnectionStatus.STOPPED || connectionStatus == ConnectionStatus.ERROR),
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosActiveGreen, checkedBorderColor = Color.Transparent, uncheckedThumbColor = Color.White, uncheckedTrackColor = AppPalette.inactiveTrack, uncheckedBorderColor = Color.Transparent, disabledCheckedTrackColor = IosActiveGreen.copy(alpha = 0.4f), disabledCheckedThumbColor = Color.White.copy(alpha = 0.9f), disabledCheckedBorderColor = Color.Transparent,                                     disabledUncheckedTrackColor = AppPalette.inactiveTrack.copy(alpha = 0.6f), disabledUncheckedThumbColor = Color.White.copy(alpha = 0.7f), disabledUncheckedBorderColor = Color.Transparent)
                                )
                            }
                                if (psiphonOn) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { showPsiphonSheet = true }.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AppPalette.accentVariant.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(strings.SHOW_MORE_PSIPHON, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = (12 * scaleFactor).sp)
                                            Text(strings.SHOW_MORE_SUBTITLE, color = IosSecondaryLabel, fontSize = (10 * scaleFactor).sp)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = IosSecondaryLabel, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (isDesktop && isWindows) {
                        IosConnectionModeSegmentedControl(
                            selectedMode = config.connectionMode,
                            onModeSelected = { onUpdateConfig(config.copy(connectionMode = it)) },
                            enabled = connectionStatus == ConnectionStatus.STOPPED || connectionStatus == ConnectionStatus.ERROR,
                            scaleFactor = scaleFactor
                        )
                    }
                    IosProtocolSegmentedControl(
                        selectedProtocol = config.protocol,
                        onProtocolSelected = onUpdateProtocol,
                        enabled = connectionStatus == ConnectionStatus.STOPPED || connectionStatus == ConnectionStatus.ERROR,
                        allowedProtocols = if (config.psiphonEnabled) setOf(AetherProtocol.MASQUE, AetherProtocol.WG, AetherProtocol.GOOL) else null,
                        scaleFactor = scaleFactor
                    )
                }
            }
        }

        val offsetY = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(showProxyOverlay) {
            if (showProxyOverlay) {
                offsetY.snapTo(0f)
            }
        }

        AnimatedVisibility(
            visible = (config.connectionMode == ConnectionMode.PROXY_ONLY || config.connectionMode == ConnectionMode.SYSTEM_PROXY) && connectionStatus == ConnectionStatus.RUNNING && showProxyOverlay && !isWindows,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 36.dp)
                .graphicsLayer { translationY = offsetY.value }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetY.value < -100f) {
                                    showProxyOverlay = false
                                } else {
                                    offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetY.snapTo((offsetY.value + dragAmount).coerceAtMost(20f))
                            }
                        }
                    )
                }
        ) {
            ProxyOverlayPill(
                host = config.socksHost,
                socksPort = config.socksPort,
                httpPort = config.httpPort,
                onHide = { showProxyOverlay = false },
                onCopy = onCopy,
                scaleFactor = scaleFactor,
                psiphonEnabled = config.psiphonEnabled,
                psiphonPort = config.psiphonSocksPort
            )
        }

        if (showAdminRequiredDialog) {
            AdminRequiredDialog(
                onRelaunch = {
                    showAdminRequiredDialog = false
                    systemUtils?.relaunchAsAdmin()
                },
                onDismiss = { showAdminRequiredDialog = false },
                scaleFactor = scaleFactor
            )
        }

        if (showSupportDialog) {
            SupportDialog(
                autoShow = supportDialogAuto,
                onJoin = {
                    settings?.putBoolean("support_dialog_dismissed", true)
                    showSupportDialog = false
                    uriHandler.openUri(TelegramChannelUrl)
                },
                onSkip = {
                    settings?.putBoolean("support_dialog_dismissed", true)
                    showSupportDialog = false
                },
                onCancel = { showSupportDialog = false },
                scaleFactor = scaleFactor
            )
        }
        if (showPsiphonSheet) {
            PsiphonOptionsSheet(
                config = config,
                onUpdateConfig = onUpdateConfig,
                onDismiss = { showPsiphonSheet = false },
                scaleFactor = scaleFactor
            )
        }
    }
}

private const val TelegramChannelUrl = ""

@Composable
private fun SupportDialog(
    autoShow: Boolean,
    onJoin: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    scaleFactor: Float
) {
    Dialog(
        onDismissRequest = { if (autoShow) onSkip() else onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val strings = LocalAppStrings.current
        val isRtl = strings is StringsFa
        CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IosCardBg),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding((20 * scaleFactor).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        strings.SUPPORT_AETHERST,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (18 * scaleFactor).sp,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(
                            textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                        )
                    )
                    Spacer(modifier = Modifier.height((10 * scaleFactor).dp))
                    Text(
                        strings.SUPPORT_DIALOG_DESC,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = (13 * scaleFactor).sp,
                        lineHeight = (18 * scaleFactor).sp,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(
                            textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                        )
                    )
                    Spacer(modifier = Modifier.height((20 * scaleFactor).dp))
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.fillMaxWidth().height((48 * scaleFactor).dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosActiveBlue, contentColor = Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            strings.JOIN_TELEGRAM,
                            fontWeight = FontWeight.Bold,
                            fontSize = (14 * scaleFactor).sp,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.LocalTextStyle.current.copy(
                                textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height((8 * scaleFactor).dp))
                    TextButton(
                        onClick = { if (autoShow) onSkip() else onCancel() },
                        modifier = Modifier.fillMaxWidth().height((42 * scaleFactor).dp)
                    ) {
                        Text(
                            if (autoShow) strings.SKIP else strings.CANCEL,
                            color = IosSecondaryLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = (13 * scaleFactor).sp,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.LocalTextStyle.current.copy(
                                textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                            )
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun AdminRequiredDialog(
    onRelaunch: () -> Unit,
    onDismiss: () -> Unit,
    scaleFactor: Float = 1f
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val strings = LocalAppStrings.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = (24 * scaleFactor).dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = (340 * scaleFactor).dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppPalette.surfaceRaised),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding((24 * scaleFactor).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size((64 * scaleFactor).dp)
                            .clip(CircleShape)
                            .background(AppPalette.statusError.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AppPalette.statusError,
                            modifier = Modifier.size((32 * scaleFactor).dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height((20 * scaleFactor).dp))
                    
                    Text(
                        text = strings.ADMIN_REQUIRED,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = (20 * scaleFactor).sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height((12 * scaleFactor).dp))
                    
                    Text(
                        text = strings.ADMIN_REQUIRED_DESC,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = (14 * scaleFactor).sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height((32 * scaleFactor).dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy((12 * scaleFactor).dp)
                    ) {
                        Button(
                            onClick = onRelaunch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((52 * scaleFactor).dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppPalette.accent,
                                contentColor = Color.White
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.RELAUNCH_AS_ADMIN,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (15 * scaleFactor).sp
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((52 * scaleFactor).dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = strings.CANCEL,
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium,
                                fontSize = (15 * scaleFactor).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProxyOverlayPill(
    host: String,
    socksPort: String,
    httpPort: String,
    onHide: () -> Unit,
    onCopy: (String) -> Unit,
    scaleFactor: Float,
    psiphonEnabled: Boolean = false,
    psiphonPort: String = "3080"
) {
    val socksAddress = "$host:$socksPort"
    val httpAddress = "$host:$httpPort"
    val psiphonAddress = "$host:$psiphonPort"

    Surface(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .padding(horizontal = 8.dp)
            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = IosActiveBlue.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp),
        color = AppPalette.surfaceRaised.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IosActiveBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Dns, null, tint = IosActiveBlue, modifier = Modifier.size(20.dp))
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ProxyCopyRow(
                    label = "SOCKS5",
                    address = socksAddress,
                    onCopy = {
                        onCopy(socksAddress)
                    },
                    scaleFactor = scaleFactor
                )
                ProxyCopyRow(
                    label = "HTTP",
                    address = httpAddress,
                    onCopy = {
                        onCopy(httpAddress)
                    },
                    scaleFactor = scaleFactor
                )
                if (psiphonEnabled) {
                    ProxyCopyRow(
                        label = "Psiphon",
                        address = psiphonAddress,
                        onCopy = {
                            onCopy(psiphonAddress)
                        },
                        scaleFactor = scaleFactor
                    )
                }
            }

            VerticalDivider(modifier = Modifier.height(36.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.1f))

            IconButton(
                onClick = onHide,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = IosSecondaryLabel, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ProxyCopyRow(
    label: String,
    address: String,
    onCopy: () -> Unit,
    scaleFactor: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCopy() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = IosActiveBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (9 * scaleFactor).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (12 * scaleFactor).sp,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size((14 * scaleFactor).dp)
        )
    }
}

@Composable
fun WindowsProxyPortsCard(
    config: AetherConfig,
    onCopy: (String) -> Unit,
    scaleFactor: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCardBg)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = (12 * scaleFactor).dp, vertical = (10 * scaleFactor).dp),
            verticalArrangement = Arrangement.spacedBy((6 * scaleFactor).dp)
        ) {
            Text(
                text = "PROXY PORTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = IosSecondaryLabel,
                fontSize = (8.5 * scaleFactor).sp
            )
            ProxyCopyRow(
                label = "Counted",
                address = "127.0.0.1:10808 / 127.0.0.1:10809",
                onCopy = { onCopy("127.0.0.1:10808") },
                scaleFactor = scaleFactor
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            ProxyCopyRow(
                label = "Core",
                address = "${config.socksHost}:${config.socksPort} / ${config.socksHost}:${config.httpPort}",
                onCopy = { onCopy("${config.socksHost}:${config.socksPort}") },
                scaleFactor = scaleFactor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IosStatusHeroCard(
    connectionStatus: ConnectionStatus,
    elapsedSeconds: Long,
    sessionTraffic: SessionTraffic,
    config: AetherConfig,
    ipInfo: IpInfo = IpInfo(),
    pingState: PingState = PingState(),
    onRefreshIpInfo: () -> Unit = {},
    onRefreshPing: () -> Unit = {},
    onCopy: (String) -> Unit = {},
    hideConfigChips: Boolean = false,
    scaleFactor: Float = 1f
) {
    val strings = LocalAppStrings.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val statusColor by animateColorAsState(
        targetValue = when (connectionStatus) {
            ConnectionStatus.RUNNING, ConnectionStatus.TUN_ACTIVE -> IosActiveGreen
            ConnectionStatus.STARTING, ConnectionStatus.VALIDATING, ConnectionStatus.DATAPLANE_VALIDATED, ConnectionStatus.SOCKS_READY, ConnectionStatus.RECONNECTING, ConnectionStatus.STOPPING -> IosScanningAmber
            ConnectionStatus.ERROR, ConnectionStatus.FAILED -> IosErrorRed
            ConnectionStatus.STOPPED -> IosSecondaryLabel
        },
        label = "statusColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("status_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IosCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding((14 * scaleFactor).dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size((7 * scaleFactor).dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width((5 * scaleFactor).dp))
                                Text(
                                    text = when (connectionStatus) {
                                        ConnectionStatus.RUNNING, ConnectionStatus.TUN_ACTIVE -> if (config.connectionMode == ConnectionMode.TUNNEL) strings.STATUS_PROTECTED_CONNECTED else strings.STATUS_PROXY_ACTIVE
                                        ConnectionStatus.STARTING -> strings.STATUS_FINDING_SERVERS
                                        ConnectionStatus.VALIDATING, ConnectionStatus.DATAPLANE_VALIDATED -> strings.STATUS_ESTABLISHING_LINK
                                        ConnectionStatus.SOCKS_READY -> strings.STATUS_CONNECTING
                                        ConnectionStatus.RECONNECTING -> strings.STATUS_RECONNECTING
            ConnectionStatus.STOPPING -> strings.STATUS_SWIPE_FORCE_STOP
                                        ConnectionStatus.ERROR, ConnectionStatus.FAILED -> strings.STATUS_CONNECTION_ERROR
                                        ConnectionStatus.STOPPED -> strings.STATUS_READY_TO_CONNECT
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = statusColor,
                                    fontSize = (8.5 * scaleFactor).sp
                                )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IosGroupBg
                    ) {
                        val protocolText = if (config.protocol == AetherProtocol.MASQUE) {
                            if (config.h2Mode) "MASQUE (H2)" else "MASQUE (H3)"
                        } else {
                            config.protocol.displayName
                        }
                        Text(
                            text = protocolText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IosActiveBlue,
                            fontSize = (8.5 * scaleFactor).sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height((10 * scaleFactor).dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = formatTime(elapsedSeconds),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = (28 * scaleFactor).sp
                        )
                    }

                    if (connectionStatus == ConnectionStatus.RUNNING) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onRefreshPing() }
                                .padding(2.dp)
                        ) {
                            if (pingState.isPinging) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size((11 * scaleFactor).dp),
                                    color = IosActiveBlue,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Ping",
                                    tint = if (pingState.error != null) IosErrorRed else IosActiveBlue,
                                    modifier = Modifier.size((15 * scaleFactor).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = when {
                                    pingState.isPinging -> "..."
                                    pingState.error != null -> "TIMEOUT"
                                    pingState.ms >= 0 -> "${pingState.ms}ms"
                                    else -> "PING"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pingState.error != null) IosErrorRed else IosActiveBlue,
                                fontSize = (12 * scaleFactor).sp
                            )
                        }
                    } else {
                        Text(
                            text = if (connectionStatus == ConnectionStatus.RECONNECTING) strings.DASHBOARD_RETRY else strings.DASHBOARD_NO_UPLINK,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (connectionStatus == ConnectionStatus.RECONNECTING) IosScanningAmber else IosSecondaryLabel,
                            modifier = Modifier.clickable { onRefreshPing() },
                            fontSize = (10 * scaleFactor).sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height((8 * scaleFactor).dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                if (connectionStatus == ConnectionStatus.RUNNING) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IosGroupBg)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrafficValue(
                            label = "UPLOAD",
                            value = formatTrafficBytes(sessionTraffic.uploadedBytes),
                            speed = sessionTraffic.uploadSpeedBps,
                            color = IosActiveBlue,
                            alignment = Alignment.Start,
                            modifier = Modifier.weight(1f),
                            scaleFactor = scaleFactor
                        )
                        TrafficValue(
                            label = "DOWNLOAD",
                            value = formatTrafficBytes(sessionTraffic.downloadedBytes),
                            speed = sessionTraffic.downloadSpeedBps,
                            color = IosActiveGreen,
                            alignment = Alignment.End,
                            modifier = Modifier.weight(1f),
                            scaleFactor = scaleFactor
                        )
                    }
                }

                Spacer(modifier = Modifier.height((8 * scaleFactor).dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            onClick = { onRefreshIpInfo() },
                            onLongClick = {
                                if (ipInfo.ip.isNotEmpty()) {
                                    onCopy(ipInfo.ip)
                                }
                            }
                        ),
                    color = IosGroupBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (ipInfo.countryCode.isNotEmpty()) {
                                CountryFlag(
                                    countryCode = ipInfo.countryCode,
                                    size = (20 * scaleFactor).dp
                                )
                            } else {
                                Text(
                                    text = "🌐",
                                    fontSize = (16 * scaleFactor).sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = when {
                                        ipInfo.country.isNotEmpty() -> if (ipInfo.countryCode.isNotEmpty()) "${ipInfo.country} (${ipInfo.countryCode})" else ipInfo.country
                                        ipInfo.isLoading -> strings.DASHBOARD_IP_WAIT
                                        ipInfo.error != null -> strings.DASHBOARD_IP_ERROR
                                        else -> strings.DASHBOARD_IP_UNKNOWN
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        textDirection = if (ipInfo.country.isNotEmpty()) TextDirection.Ltr else if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = (11 * scaleFactor).sp
                                )
                            Text(
                                text = when {
                                    ipInfo.ip.isNotEmpty() -> ipInfo.ip
                                    ipInfo.isLoading -> strings.DASHBOARD_IP_LOCATING
                                    ipInfo.error != null -> strings.DASHBOARD_IP_NOT_FOUND
                                    else -> strings.DASHBOARD_IP_SHOW
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDirection = if (ipInfo.ip.isNotEmpty()) TextDirection.Ltr else if (isRtl) TextDirection.Rtl else TextDirection.Ltr
                                ),
                                color = when {
                                    ipInfo.error != null -> IosErrorRed
                                    ipInfo.isLoading -> IosScanningAmber
                                    else -> IosSecondaryLabel
                                },
                                fontSize = (9 * scaleFactor).sp
                            )
                            }
                        }

                        if (ipInfo.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size((12 * scaleFactor).dp),
                                color = IosActiveBlue,
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = IosSecondaryLabel,
                                modifier = Modifier.size((12 * scaleFactor).dp)
                            )
                        }
                    }
                }
                }

                if (!hideConfigChips) {
                    Spacer(modifier = Modifier.height((10 * scaleFactor).dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IosGroupBg)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IosConfigChip(label = "BYPASS", value = config.noise.displayName.split(" ")[0], scaleFactor = scaleFactor)
                        IosConfigChip(label = "SPEED", value = config.scanMode.name.take(6), scaleFactor = scaleFactor)
                        IosConfigChip(label = "NETWORK", value = config.ipMode.rawValue, scaleFactor = scaleFactor)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrafficValue(label: String, value: String, color: Color, alignment: Alignment.Horizontal, modifier: Modifier = Modifier, speed: Double = 0.0, scaleFactor: Float = 1f) {
    Column(modifier = modifier.heightIn(min = 38.dp), horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = IosSecondaryLabel,
            fontSize = (8 * scaleFactor).sp,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = (12 * scaleFactor).sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = if (speed > 0) formatSpeedValue(speed) else "0 B/s",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (speed > 0) color.copy(alpha = 0.7f) else IosSecondaryLabel.copy(alpha = 0.55f),
            fontSize = (8 * scaleFactor).sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IosConfigChip(label: String, value: String, scaleFactor: Float = 1f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = IosSecondaryLabel, fontSize = (8 * scaleFactor).sp, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White, fontSize = (10 * scaleFactor).sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IosPowerButton(
    connectionStatus: ConnectionStatus,
    onToggle: () -> Unit,
    onRecover: () -> Unit = {},
    size: Dp = 140.dp
) {
    val isConnected = connectionStatus == ConnectionStatus.RUNNING
    val isWorking = connectionStatus == ConnectionStatus.STARTING ||
                    connectionStatus == ConnectionStatus.VALIDATING ||
                    connectionStatus == ConnectionStatus.RECONNECTING ||
                    connectionStatus == ConnectionStatus.STOPPING
    val isError = connectionStatus == ConnectionStatus.ERROR
    val canToggle = true

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "refinedGlow")

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isWorking) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathingScale"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (isWorking) breathingScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )

    val cornerRadiusPercent by animateFloatAsState(
        targetValue = if (isConnected || isWorking) 0.28f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cornerRadius"
    )

    val buttonColor by animateColorAsState(
        targetValue = when {
            isConnected -> IosActiveGreen
            isWorking -> IosScanningAmber
            isError -> IosErrorRed
            else -> IosActiveBlue
        },
        animationSpec = tween(durationMillis = 600),
        label = "buttonColor"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = if (isConnected) 1.8f else 1.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .size(size * 2.5f),
        contentAlignment = Alignment.Center
    ) {
        if (isWorking || isConnected) {
            val pulseColor = buttonColor.copy(alpha = 0.45f)
            val glowShape = RoundedCornerShape(size * cornerRadiusPercent)

            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = glowScale
                        scaleY = glowScale
                        alpha = glowAlpha
                    }
                    .background(pulseColor, glowShape)
            )

            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(size)
                        .graphicsLayer {
                            scaleX = glowScale * 0.75f
                            scaleY = glowScale * 0.75f
                            alpha = glowAlpha * 1.8f
                        }
                        .background(pulseColor, glowShape)
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .shadow(
                    elevation = if (isPressed) 6.dp else 24.dp,
                    shape = RoundedCornerShape(size * cornerRadiusPercent),
                    ambientColor = buttonColor.copy(alpha = 0.6f),
                    spotColor = buttonColor
                )
                .clip(RoundedCornerShape(size * cornerRadiusPercent))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = canToggle,
                    onClick = {
                        scope.launch {
                            if (connectionStatus == ConnectionStatus.STOPPING) onRecover() else onToggle()
                        }
                    }
                ),
            color = buttonColor,
            tonalElevation = 14.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
    }
}

@Composable
fun CapsuleConnectButton(
    connectionStatus: ConnectionStatus,
    onToggle: () -> Boolean,
    modifier: Modifier = Modifier,
    scaleFactor: Float = 1f,
    onRecover: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val sf = scaleFactor.coerceIn(0.7f, 1.1f)
    val isConnected = connectionStatus == ConnectionStatus.RUNNING
    val isWorking = connectionStatus in setOf(
        ConnectionStatus.STARTING, ConnectionStatus.VALIDATING, ConnectionStatus.DATAPLANE_VALIDATED,
        ConnectionStatus.SOCKS_READY, ConnectionStatus.TUN_ACTIVE, ConnectionStatus.RECONNECTING, ConnectionStatus.STOPPING
    )
    val isError = connectionStatus == ConnectionStatus.ERROR
    val trackColor = when {
        isConnected -> IosActiveGreen
        isWorking -> IosScanningAmber
        isError -> IosErrorRed
        else -> IosGroupBg
    }
    val label = when {
        connectionStatus == ConnectionStatus.STOPPING -> strings.FORCE_STOP
        isWorking -> strings.CONNECTING_DOTS
        isConnected -> strings.DISCONNECT
        isError -> strings.RECONNECT
        else -> strings.CONNECT
    }
    Box(
        modifier = modifier
            .height((56 * sf).dp.coerceIn(48.dp, 64.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(trackColor)
            .clickable { if (connectionStatus == ConnectionStatus.STOPPING) onRecover() else onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = (16 * sf).dp)
        ) {
            if (isWorking) {
                CircularProgressIndicator(modifier = Modifier.size((18 * sf).dp), color = Color.White, strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.width((8 * sf).dp))
            }
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if (strings is StringsFa) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = ((if (strings is StringsFa) 13 else 14) * sf).coerceIn(11f, 15f).sp,
                    letterSpacing = (if (strings is StringsFa) 0.1f else 0.6f * sf).sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WindowsSwipeSwitch(
    connectionStatus: ConnectionStatus,
    onToggle: () -> Boolean,
    modifier: Modifier = Modifier,
    scaleFactor: Float = 1f,
    onAdminCancelResetKey: Int = 0,
    onRecover: () -> Unit = {},
    onDraggingChanged: (Boolean) -> Unit = {},
    isSwipeMode: Boolean = true
) {
    val strings = LocalAppStrings.current
    val isConnected = connectionStatus == ConnectionStatus.RUNNING
    val isWorking = connectionStatus == ConnectionStatus.STARTING ||
            connectionStatus == ConnectionStatus.VALIDATING ||
            connectionStatus == ConnectionStatus.DATAPLANE_VALIDATED ||
            connectionStatus == ConnectionStatus.SOCKS_READY ||
            connectionStatus == ConnectionStatus.TUN_ACTIVE ||
            connectionStatus == ConnectionStatus.RECONNECTING ||
            connectionStatus == ConnectionStatus.STOPPING
    val isError = connectionStatus == ConnectionStatus.ERROR
    val canSwipe = true
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val latestOnToggle by rememberUpdatedState(onToggle)
    val latestOnRecover by rememberUpdatedState(onRecover)
    val latestConnectionStatus by rememberUpdatedState(connectionStatus)
    val latestIsConnected by rememberUpdatedState(isConnected)
    val latestIsWorking by rememberUpdatedState(isWorking)
    val trackColor by animateColorAsState(
        targetValue = when {
            isConnected -> IosActiveGreen
            isWorking -> IosScanningAmber
            isError -> IosErrorRed
            else -> IosGroupBg
        }, label = "trackColor"
    )
    val text = when (connectionStatus) {
        ConnectionStatus.STARTING -> strings.FINDING_SERVERS
        ConnectionStatus.VALIDATING -> strings.VALIDATING
        ConnectionStatus.DATAPLANE_VALIDATED, ConnectionStatus.SOCKS_READY, ConnectionStatus.TUN_ACTIVE -> strings.CONNECTING_DOTS
        ConnectionStatus.RECONNECTING -> strings.STATUS_RECONNECTING
        ConnectionStatus.STOPPING -> strings.STATUS_SWIPE_FORCE_STOP
        ConnectionStatus.RUNNING -> if (isSwipeMode) strings.SWIPE_TO_DISCONNECT else strings.TAP_TO_DISCONNECT
        ConnectionStatus.ERROR, ConnectionStatus.FAILED -> if (isSwipeMode) strings.SWIPE_TO_RECONNECT else strings.TAP_TO_RECONNECT
        ConnectionStatus.STOPPED -> if (isSwipeMode) strings.SWIPE_TO_CONNECT else strings.TAP_TO_CONNECT
    }
    val hintTransition = rememberInfiniteTransition(label = "hint")
    val hintShift by hintTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hintShift"
    )
    val dotTransition = rememberInfiniteTransition(label = "dots")
    val dotPhase by dotTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "dotPhase"
    )
    val sf = scaleFactor.coerceIn(0.7f, 1.1f)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    BoxWithConstraints(
        modifier = modifier
            .widthIn(min = (280 * sf).dp, max = (360 * sf).dp)
            .height((64 * sf).dp.coerceIn(52.dp, 72.dp))
            .shadow(12.dp, RoundedCornerShape(36.dp), spotColor = trackColor.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(36.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toPx() }
        val thumbSize = (48 * sf).dp.coerceIn(42.dp, 56.dp)
        val thumbPx = with(androidx.compose.ui.platform.LocalDensity.current) { thumbSize.toPx() }
        val horizontalPadding = (8 * sf).dp
        val paddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { horizontalPadding.toPx() }
        val maxDrag = (maxWidthPx - thumbPx - paddingPx * 2).coerceAtLeast(0f)
        val dragFraction = when {
            !canSwipe || maxDrag == 0f -> 0f
            isConnected -> (1f - offsetX.value / maxDrag).coerceIn(0f, 1f)
            isWorking -> (offsetX.value / maxDrag).coerceIn(0f, 1f)
            else -> 0f
        }
        val isDisconnectDrag = (isConnected || isWorking) && isDragging && dragFraction > 0.05f
        val effectiveTrackColor = if (isDisconnectDrag) lerp(trackColor, IosErrorRed, dragFraction) else trackColor

        LaunchedEffect(isConnected, isWorking, maxDrag) {
            if (isDragging) return@LaunchedEffect
            if (isWorking) {
                offsetX.snapTo(if (isConnected) maxDrag else 0f)
            } else {
                offsetX.animateTo(
                    targetValue = if (isConnected) maxDrag else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }
        LaunchedEffect(onAdminCancelResetKey) {
            if (!isConnected && !isWorking && offsetX.value != 0f && !isDragging) {
                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(36.dp))
                .background(effectiveTrackColor.copy(alpha = if (isConnected || isDisconnectDrag) 1f else 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if (strings is StringsFa) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) {
                Text(
                    text = if (isDisconnectDrag) strings.RELEASE_TO_DISCONNECT else text,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Bold,
                    fontSize = ((if (strings is StringsFa) 10 else 11) * sf).coerceIn(9f, 12f).sp,
                    letterSpacing = (if (strings is StringsFa) 0.15f else 0.6f * sf).sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = ((if (strings is StringsFa) 50 else 56) * sf).dp)
                )
            }
        }

        val hintOffset = if (!isDragging && !isWorking) {
            if (!isConnected) hintShift else -hintShift
        } else 0f
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = horizontalPadding)
                .offset { androidx.compose.ui.unit.IntOffset((offsetX.value + hintOffset).toInt(), 0) }
                .size(thumbSize)
                .shadow(8.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White)
                .pointerInput(canSwipe, maxDrag) {
                        if (!canSwipe) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true; onDraggingChanged(true) },
                            onDragEnd = {
                                isDragging = false
                                onDraggingChanged(false)
                                scope.launch {
                                    val threshold = if (latestIsWorking) maxDrag * 0.25f else maxDrag * 0.5f
                                    val shouldTrigger = if (latestIsWorking) {
                                        if (!latestIsConnected) offsetX.value > threshold else offsetX.value < maxDrag - threshold
                                    } else {
                                        if (!latestIsConnected) offsetX.value > threshold else offsetX.value < threshold
                                    }
                                    if (shouldTrigger) {
                                        val success = if (latestConnectionStatus == ConnectionStatus.STOPPING) {
                                            latestOnRecover()
                                            true
                                        } else latestOnToggle()
                                        if (success) {
                                            if (latestIsWorking) {
                                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                            } else {
                                                offsetX.animateTo(
                                                    if (!latestIsConnected) maxDrag else 0f,
                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                                )
                                            }
                                        } else {
                                            offsetX.animateTo(
                                                if (latestIsConnected) maxDrag else 0f,
                                                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                            )
                                        }
                                    } else {
                                        offsetX.animateTo(
                                            if (latestIsConnected) maxDrag else 0f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                onDraggingChanged(false)
                                scope.launch {
                                    offsetX.animateTo(
                                        if (latestIsConnected) maxDrag else 0f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val next = (offsetX.value + dragAmount).coerceIn(0f, maxDrag)
                                    offsetX.snapTo(next)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isWorking && !isDragging) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = effectiveTrackColor,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isConnected || isWorking) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = IosActiveBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        if (!isWorking && !isConnected) {
            val connectFraction = if (maxDrag > 0f) (offsetX.value / maxDrag).coerceIn(0f, 1f) else 0f
            val rightAlpha = if (isDragging) (1f - connectFraction).coerceIn(0f, 1f) else 1f
            val rightShift = if (isDragging) connectFraction * 40f else hintShift * 0.6f
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .graphicsLayer { translationX = rightShift; alpha = rightAlpha },
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { idx ->
                    val alpha = 0.3f + ((dotPhase + idx * 0.33f) % 1f) * 0.7f
                    Box(
                        modifier = Modifier
                            .padding(start = if (idx == 0) 0.dp else 3.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = alpha.coerceIn(0.3f, 1f)))
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp).padding(start = 4.dp)
                )
            }
        }
    }
    }
}

@Composable
fun IosConnectionModeSegmentedControl(
    selectedMode: ConnectionMode,
    onModeSelected: (ConnectionMode) -> Unit,
    enabled: Boolean = true,
    scaleFactor: Float = 1f
) {
    val strings = LocalAppStrings.current
    val modes = listOf(
        ConnectionMode.TUNNEL to strings.TUN_MODE,
        ConnectionMode.SYSTEM_PROXY to strings.SYSTEM_PROXY,
        ConnectionMode.PROXY_ONLY to strings.PROXY_ONLY
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = IosCardBg,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IosCardBg)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEach { (mode, label) ->
                val selected = mode == selectedMode
                val bg by animateColorAsState(
                    targetValue = if (selected) IosActiveBlue else Color.Transparent,
                    animationSpec = tween(250), label = "modeBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) Color.White else IosSecondaryLabel,
                    animationSpec = tween(250), label = "modeText"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((36 * scaleFactor).dp)
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .shadow(
                            elevation = if (selected) 10.dp else 0.dp,
                            shape = RoundedCornerShape(50),
                            spotColor = IosActiveBlue.copy(alpha = 0.4f),
                            ambientColor = IosActiveBlue.copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = enabled) { onModeSelected(mode) }
                        .graphicsLayer { alpha = if (enabled || selected) 1f else 0.45f }
                        .testTag("connection_mode_${mode.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.22f),
                                            Color.White.copy(alpha = 0.06f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = textColor,
                        fontSize = (10 * scaleFactor).sp,
                        letterSpacing = 0.3.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun IosProtocolSegmentedControl(
    selectedProtocol: AetherProtocol,
    onProtocolSelected: (AetherProtocol) -> Unit,
    enabled: Boolean = true,
    allowedProtocols: Set<AetherProtocol>? = null,
    scaleFactor: Float = 1f
) {
    Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = IosCardBg,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IosCardBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AetherProtocol.entries.forEach { proto ->
                    val selected = proto == selectedProtocol
                    val itemEnabled = enabled && (allowedProtocols == null || proto in allowedProtocols)
                    val bg by animateColorAsState(
                        targetValue = if (selected) IosActiveBlue else Color.Transparent,
                        animationSpec = tween(250), label = "protoBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (selected) Color.White else IosSecondaryLabel,
                        animationSpec = tween(250), label = "protoText"
                    )
                    val label = if (proto == AetherProtocol.ZERO_TRUST) "Z-TRUST" else proto.displayName.split(" ")[0].uppercase()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((36 * scaleFactor).dp)
                            .clip(RoundedCornerShape(50))
                            .background(bg)
                            .shadow(
                                elevation = if (selected) 10.dp else 0.dp,
                                shape = RoundedCornerShape(50),
                                spotColor = IosActiveBlue.copy(alpha = 0.4f),
                                ambientColor = IosActiveBlue.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = itemEnabled) { onProtocolSelected(proto) }
                            .graphicsLayer { alpha = if (itemEnabled || selected) 1f else 0.45f }
                            .testTag("protocol_${proto.rawValue}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.22f),
                                                Color.White.copy(alpha = 0.06f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = textColor,
                            fontSize = (10 * scaleFactor).sp,
                            letterSpacing = 0.3.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    
    fun pad(n: Long) = if (n < 10) "0$n" else n.toString()
    return "${pad(h)}:${pad(m)}:${pad(s)}"
}

private fun formatTrafficBytes(bytes: Long): String {
    val safeBytes = bytes.coerceAtLeast(0).coerceAtMost(9_000_000_000_000_000L)
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    var value = safeBytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    val roundedValue = (value * 100).toLong() / 100.0
    return if (unitIndex == 0) {
        "$safeBytes ${units[unitIndex]}"
    } else {
        val formatted = if (roundedValue >= 100) "${roundedValue.toLong()}" else "$roundedValue"
        "$formatted ${units[unitIndex]}"
    }
}

private fun formatSpeedValue(bytesPerSec: Double): String {
    return when {
        bytesPerSec >= 1024.0 * 1024.0 * 1024.0 * 1024.0 -> "${"%.1f".format(bytesPerSec / (1024.0 * 1024.0 * 1024.0 * 1024.0))} TB/s"
        bytesPerSec >= 1024.0 * 1024.0 * 1024.0 -> "${"%.1f".format(bytesPerSec / (1024.0 * 1024.0 * 1024.0))} GB/s"
        bytesPerSec >= 1024.0 * 1024.0 -> "${"%.1f".format(bytesPerSec / (1024.0 * 1024.0))} MB/s"
        bytesPerSec >= 1024.0 -> "${"%.0f".format(bytesPerSec / 1024.0)} KB/s"
        else -> "${"%.0f".format(bytesPerSec)} B/s"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PsiphonOptionsSheet(
    config: AetherConfig,
    onUpdateConfig: (AetherConfig) -> Unit,
    onDismiss: () -> Unit,
    scaleFactor: Float
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IosCardBg,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(strings.PSIPHON_OPTIONS_TITLE, fontWeight = FontWeight.Bold, fontSize = (18 * scaleFactor).sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when (config.protocol) {
                        AetherProtocol.MASQUE -> strings.PSIPHON_OPTIONS_SUBTITLE_MASQUE
                        else -> strings.PSIPHON_OPTIONS_SUBTITLE_WG
                    },
                    color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp
                )
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black)) { Column {
                val outerOptions = listOf("MASQUE", "WireGuard", "Gool")
                val outerValues = listOf("masque", "wg", "gool")
                val currentOuter = when (config.psiphonChainOuter) { "wg" -> "WireGuard"; "gool" -> "Gool"; else -> "MASQUE" }
                IosPickerRow(icon = Icons.Default.VpnLock, iconBg = AppPalette.statusConnected, title = strings.OUTER_PROTOCOL, value = currentOuter, options = outerOptions, onOptionSelected = { idx -> val outer = outerValues[idx]; val proto = when (outer) { "wg" -> AetherProtocol.WG; "gool" -> AetherProtocol.GOOL; else -> AetherProtocol.MASQUE }; onUpdateConfig(config.copy(psiphonChainOuter = outer, protocol = proto)) })
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(when (config.psiphonChainOuter) { "wg" -> strings.PSIPHON_SHEET_OUTER_DESC_WG ; "gool" -> strings.PSIPHON_SHEET_OUTER_DESC_GOOL ; else -> strings.PSIPHON_SHEET_OUTER_DESC_MASQUE }, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                }
                if (config.protocol == AetherProtocol.MASQUE && config.psiphonEnabled) {
                    AppDivider()
                    val orderOptions = listOf("Psiphon first", "MASQUE first", "Auto")
                    val orderValues = listOf("psiphon_first", "masque_first", "auto")
                    val currentOrder = when (config.psiphonMasqueOrder) { "masque_first" -> "MASQUE first"; "auto" -> "Auto"; else -> "Psiphon first" }
                    IosPickerRow(icon = Icons.Default.SwapHoriz, iconBg = Color(0xFF30B0C7), title = strings.MASQUE_ORDER, value = currentOrder, options = orderOptions, onOptionSelected = { idx -> onUpdateConfig(config.copy(psiphonMasqueOrder = orderValues[idx])) })
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(when (config.psiphonMasqueOrder) { "masque_first" -> strings.PSIPHON_SHEET_ORDER_DESC_MASQUE_FIRST ; "auto" -> strings.PSIPHON_SHEET_ORDER_DESC_AUTO ; else -> strings.PSIPHON_SHEET_ORDER_DESC_PSIPHON_FIRST }, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                    }
                }
                val isWgFamily = config.protocol == AetherProtocol.WG || config.protocol == AetherProtocol.GOOL
                if (!isWgFamily && config.protocol != AetherProtocol.MASQUE) {
                    AppDivider()
                    val chainModes = listOf(PsiphonChainMode.AUTO, PsiphonChainMode.FALLBACK, PsiphonChainMode.ALWAYS)
                    val chainLabels = mapOf(PsiphonChainMode.AUTO to "Auto", PsiphonChainMode.FALLBACK to "Fallback", PsiphonChainMode.ALWAYS to "Always")
                    IosPickerRow(icon = Icons.Default.Sync, iconBg = AppPalette.accent, title = strings.PSIPHON_CHAIN_MODE, value = chainLabels[config.psiphonChainMode] ?: strings.CHAIN_MODE_AUTO, options = chainModes.map { chainLabels[it]!! }, onOptionSelected = { idx -> onUpdateConfig(config.copy(psiphonChainMode = chainModes[idx])) })
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        val modeDesc = when (config.psiphonChainMode) {
                            PsiphonChainMode.AUTO -> strings.PSIPHON_SHEET_CHAIN_DESC_AUTO
                            PsiphonChainMode.FALLBACK -> strings.PSIPHON_SHEET_CHAIN_DESC_FALLBACK
                            PsiphonChainMode.ALWAYS -> strings.PSIPHON_SHEET_CHAIN_DESC_ALWAYS
                        }
                        Text(modeDesc, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(strings.PSIPHON_SHEET_WG_ALWAYS_VIA, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                    }
                }
                AppDivider()
                val availableRegions by PsiphonEgressRegistry.availableRegions.collectAsStateWithLifecycle()
                val selectedRegion = config.psiphonEgressRegion.trim().uppercase()
                val regionCodes = buildList {
                    add("")
                    addAll(availableRegions)
                    if (selectedRegion.isNotEmpty() && selectedRegion !in availableRegions) add(selectedRegion)
                }
                val regionOptions = regionCodes.map { CountryNames.label(it) }
                IosPickerRow(icon = Icons.Default.Public, iconBg = Color(0xFF30B0C7), title = strings.EXIT_COUNTRY, value = CountryNames.label(selectedRegion), options = regionOptions, onOptionSelected = { idx -> onUpdateConfig(config.copy(psiphonEgressRegion = regionCodes[idx])) })
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(strings.PSIPHON_SHEET_EXIT_AUTO, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (16 * scaleFactor).sp)
                }
                if (isWgFamily) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(strings.PSIPHON_SHEET_EGRESS_WARN_WG, color = Color(0xFFFFCC00), fontSize = (11 * scaleFactor).sp, lineHeight = (15 * scaleFactor).sp)
                    }
                }
            } }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(strings.HOW_IT_WORKS, fontWeight = FontWeight.Bold, color = Color.White, fontSize = (14 * scaleFactor).sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(when (config.psiphonChainOuter) { "wg" -> strings.PSIPHON_SHEET_HOW_WG ; "gool" -> strings.PSIPHON_SHEET_HOW_GOOL ; else -> strings.PSIPHON_SHEET_HOW_MASQUE }, color = IosSecondaryLabel, fontSize = (12 * scaleFactor).sp, lineHeight = (17 * scaleFactor).sp)
                }
            }
        }
        }
    }
}
