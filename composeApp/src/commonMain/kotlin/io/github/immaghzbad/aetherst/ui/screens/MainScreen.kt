package io.github.immaghzbad.aetherst.shared.ui.screens

import io.github.immaghzbad.aetherst.shared.ui.theme.AppPalette
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.immaghzbad.aetherst.platform.PlatformContext
import io.github.immaghzbad.aetherst.platform.isDesktop
import io.github.immaghzbad.aetherst.platform.getSystemUtils
import io.github.immaghzbad.aetherst.platform.getDeviceModel
import io.github.immaghzbad.aetherst.platform.getOsVersion
import io.github.immaghzbad.aetherst.platform.getSettings
import io.github.immaghzbad.aetherst.shared.desktop.TrayState
import io.github.immaghzbad.aetherst.shared.ui.AetherViewModel
import io.github.immaghzbad.aetherst.shared.ui.OnboardingViewModel
import io.github.immaghzbad.aetherst.shared.ui.components.IosToast
import io.github.immaghzbad.aetherst.shared.ui.components.PlatformBackHandler
import io.github.immaghzbad.aetherst.shared.model.AetherProtocol
import io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings
import kotlin.math.roundToInt

private val IosNavBackground = AppPalette.surfaceRaised
private val IosNavActiveBlue = AppPalette.accent
private val IosNavInactiveGrey = AppPalette.textSecondary
private val BarContentHeight = 90.dp
private val ButtonSize = 56.dp
private val ButtonCenterY = 20.dp
private val CircleGap = 6.dp
private val BarTopY = 20.dp
private val ItemBottomPadding = 12.dp

private sealed class Screen(val route: String, val tabIndex: Int?) {
    object Dashboard : Screen("dashboard", 0)
    object Settings : Screen("settings", 1)
    object Logs : Screen("logs", 2)
    object About : Screen("about", 3)
    object None : Screen("none", null)
    object SplitTunneling : Screen("split", null)
    object RoutingRules : Screen("routing", null)
    object AutoDetect : Screen("autodetect", null)
    object AutoDetectDns : Screen("autodetect_dns", null)
    object SpeedTest : Screen("speedtest", null)
}

@Composable
fun MainScreen(viewModel: AetherViewModel, onboardingViewModel: OnboardingViewModel, platformContext: PlatformContext) {
    val isOnboardingComplete by viewModel.isOnboardingComplete.collectAsStateWithLifecycle()
    val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val crashLog by viewModel.crashLog.collectAsStateWithLifecycle()
    val toastState by viewModel.toastState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkBatteryOptimizationStatus()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = this.maxWidth
        val scaleFactor = (screenWidth.value / 411f).coerceIn(0.7f, 1.1f)

        if (!isOnboardingComplete) {
            OnboardingScreen(
                state = onboardingState,
                onGetStarted = { onboardingViewModel.moveToNextStep() },
                onRetryRegistration = { onboardingViewModel.startProtocolTests() },
                onCancelRegistration = { onboardingViewModel.cancelTests() },
                onUpdateScanMode = { onboardingViewModel.updateScanMode(it) },
                onRequestVpnPermission = {
                    viewModel.prepareVpn { }
                    onboardingViewModel.onPermissionRequested()
                },
                onRequestNotificationPermission = {
                    viewModel.requestNotificationPermission()
                    onboardingViewModel.onPermissionRequested()
                },
                onRequestBatteryOptimization = {
                    viewModel.requestBatteryOptimization()
                    onboardingViewModel.onPermissionRequested()
                },
                onFinish = onboardingViewModel::moveToNextStep,
                appLanguage = config.appLanguage,
                onSelectLanguage = { onboardingViewModel.updateLanguage(it) },
                onConfirmLanguage = { onboardingViewModel.moveToNextStep() }
            )
        } else if (crashLog != null) {
            CrashReportScreen(
                crashLog = crashLog!!,
                appVersion = viewModel.appVersion,
                platformName = if (isDesktop) "Windows Desktop" else "Android",
                deviceModel = try {
                    getDeviceModel()
                } catch (_: Exception) { "Unknown" },
                osVersion = try {
                    getOsVersion()
                } catch (_: Exception) { "Unknown" },
                onRestart = { viewModel.clearCrashLog() },
                onCopy = { viewModel.copyToClipboard(it) },
                onShare = { viewModel.shareLogs() },
                onShowToast = { viewModel.showToast(it) }
            )
        } else if (updateInfo != null) {
            UpdateScreen(
                info = updateInfo!!,
                onDismiss = { viewModel.dismissUpdate() },
                scaleFactor = scaleFactor
            )
        } else {
            DashboardContent(viewModel, scaleFactor, platformContext)
        }

        IosToast(
            message = toastState?.message,
            isError = toastState?.isError ?: false,
            scaleFactor = scaleFactor
        )
    }
}

@Composable
private fun DashboardContent(viewModel: AetherViewModel, scaleFactor: Float, platformContext: PlatformContext) {
    var showTrayAdminDialog by remember { mutableStateOf(false) }
    var zeroTrustOpen by remember { mutableStateOf(false) }
    var isSwipeDragging by remember { mutableStateOf(false) }

    val config by viewModel.config.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val sessionTraffic by viewModel.sessionTraffic.collectAsStateWithLifecycle()
    val ipInfo by viewModel.ipInfo.collectAsStateWithLifecycle()
    val pingState by viewModel.pingState.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val isBatteryOptimized by viewModel.isBatteryOptimized.collectAsStateWithLifecycle()
    val importConflictRules by viewModel.importConflictRules.collectAsStateWithLifecycle()
    val importErrorMessage by viewModel.importErrorMessage.collectAsStateWithLifecycle()
    val isOptimizingMtu by viewModel.isOptimizingMtu.collectAsStateWithLifecycle()
    val isWaitingForLoginCode by viewModel.isWaitingForLoginCode.collectAsStateWithLifecycle()
    val scrollToZeroTrust by viewModel.scrollToZeroTrust.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val topLevelRoutes = listOf(Screen.Dashboard, Screen.Settings, Screen.Logs, Screen.About)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { topLevelRoutes.size })
    val currentEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(initialValue = navController.currentBackStackEntry)
    val currentSubRoute = currentEntry?.destination?.route ?: Screen.None.route
    val showNavBar = currentSubRoute == Screen.None.route
    val selectedTab = pagerState.currentPage

    val trayNavigateKey by TrayState.navigateToSettings.collectAsStateWithLifecycle()
    val trayAdminKey by TrayState.adminDialogRequest.collectAsStateWithLifecycle()
    LaunchedEffect(trayNavigateKey) {
        if (trayNavigateKey != 0L) {
            if (currentSubRoute != Screen.None.route) navController.popBackStack()
            scope.launch { pagerState.animateScrollToPage(Screen.Settings.tabIndex!!) }
        }
    }
    LaunchedEffect(trayAdminKey) {
        if (trayAdminKey != 0L) {
            showTrayAdminDialog = true
        }
    }
    LaunchedEffect(scrollToZeroTrust) {
        if (scrollToZeroTrust) {
            zeroTrustOpen = true
            if (currentSubRoute != Screen.None.route) navController.popBackStack()
            scope.launch { pagerState.animateScrollToPage(Screen.Settings.tabIndex!!) }
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != Screen.Settings.tabIndex) {
            zeroTrustOpen = false
            if (scrollToZeroTrust) viewModel.onZeroTrustScrolled()
        }
    }

    fun selectTab(index: Int) {
        scope.launch { pagerState.animateScrollToPage(index.coerceIn(0, topLevelRoutes.lastIndex)) }
    }

    PlatformBackHandler(enabled = currentSubRoute != Screen.None.route) {
        navController.popBackStack()
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalNavBarHeight = BarContentHeight + navBarPadding

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.None.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.None.route) { }
            composable(Screen.SplitTunneling.route) {
                SplitTunnelingScreen(
                    apps = installedApps,
                    excludedPackages = config.excludedPackages,
                    blockedPackages = config.blockedPackages,
                    tunnelAllApps = config.tunnelAllApps,
                    tunneledPackages = config.tunneledPackages,
                    onUpdateMode = { pkg, mode -> viewModel.updateAppSplitTunnelingMode(pkg, mode) },
                    onBulkUpdateMode = { pkgs, mode -> viewModel.bulkUpdateAppSplitTunnelingMode(pkgs, mode) },
                    onBack = { navController.popBackStack() },
                    scaleFactor = scaleFactor
                )
            }
            composable(Screen.SpeedTest.route) {
                SpeedTestScreen(
                    onBack = { navController.popBackStack() },
                    onCopy = { viewModel.copyToClipboard(it) },
                    bottomContentPadding = 0.dp,
                    connectionStatus = connectionStatus,
                    config = config
                )
            }
            composable(Screen.AutoDetect.route) {
                AutoDetectScreen(
                    onBack = { navController.popBackStack() },
                    onApplyResult = { result ->
                        viewModel.applyAutoDetectResult(result)
                        navController.popBackStack()
                    },
                    platformContext = platformContext,
                    bottomContentPadding = 0.dp,
                    initialSection = 0,
                    tunnelDns = config.dnsList,
                    onApplyDns = { dns -> viewModel.applyDnsList(dns) },
                    onCopy = { viewModel.copyToClipboard(it) }
                )
            }
            composable(Screen.AutoDetectDns.route) {
                AutoDetectScreen(
                    onBack = { navController.popBackStack() },
                    onApplyResult = { result ->
                        viewModel.applyAutoDetectResult(result)
                        navController.popBackStack()
                    },
                    platformContext = platformContext,
                    bottomContentPadding = 0.dp,
                    initialSection = 1,
                    tunnelDns = config.dnsList,
                    onApplyDns = { dns -> viewModel.applyDnsList(dns) },
                    onCopy = { viewModel.copyToClipboard(it) }
                )
            }
            composable(Screen.RoutingRules.route) {
                RoutingRulesScreen(
                    rules = config.routingRules,
                    importConflictRules = importConflictRules,
                    importErrorMessage = importErrorMessage,
                    onAddRule = { pattern, mode -> viewModel.addRoutingRule(pattern, mode) },
                    onRemoveRule = { pattern -> viewModel.removeRoutingRule(pattern) },
                    onUpdateMode = { pattern, mode -> viewModel.updateRoutingRuleMode(pattern, mode) },
                    onClearAllRules = { viewModel.clearAllRoutingRules() },
                    onCleanPattern = { viewModel.cleanRoutingPattern(it) },
                    onValidatePattern = { viewModel.isValidRoutingPattern(it) },
                    onExportRules = { viewModel.exportRoutingRules() },
                    onImportRules = { viewModel.importRoutingRules() },
                    onImportInternalRules = { viewModel.importInternalRoutingRules(it) },
                    onResolveConflict = { rules, replace -> viewModel.resolveConflict(rules, replace) },
                    onCancelImport = { viewModel.cancelImport() },
                    onClearImportError = { viewModel.clearImportError() },
                    onShowToast = { msg: String, err: Boolean -> viewModel.showToast(msg, err) },
                    onBack = { navController.popBackStack() },
                    scaleFactor = scaleFactor
                )
            }
        }
        if (showNavBar) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isSwipeDragging
            ) { page ->
                when (topLevelRoutes[page]) {
                    Screen.Dashboard -> DashboardScreen(
                        config = config,
                        connectionStatus = connectionStatus,
                        elapsedSeconds = elapsedSeconds,
                        sessionTraffic = sessionTraffic,
                        ipInfo = ipInfo,
                        pingState = pingState,
                        onToggleVpn = { viewModel.toggleVpn { } },
                        onForceStop = { viewModel.forceStop() },
                        onUpdateConfig = { viewModel.updateConfig(it) },
                        onUpdateProtocol = { proto ->
                            if (proto == AetherProtocol.ZERO_TRUST) {
                                viewModel.updateConfig(config.copy(protocol = proto, psiphonEnabled = false))
                            } else if (config.psiphonEnabled) {
                                val outer = when (proto) { AetherProtocol.WG -> "wg" ; AetherProtocol.GOOL -> "gool" ; else -> "masque" }
                                viewModel.updateConfig(config.copy(protocol = proto, psiphonChainOuter = outer))
                            } else {
                                viewModel.updateConfig(config.copy(protocol = proto))
                            }
                        },
                        onTogglePsiphon = { enabled -> viewModel.updateConfig(config.copy(psiphonEnabled = enabled)) },
                        onRefreshIpInfo = { viewModel.refreshIpInfo() },
                        onRefreshPing = { viewModel.refreshPing() },
                        onCopy = { viewModel.copyToClipboard(it) },
                        onOpenSettingsToZeroTrust = {
                            zeroTrustOpen = true
                            selectTab(Screen.Settings.tabIndex!!)
                        },
                        appVersion = viewModel.appVersion,
                        bottomContentPadding = totalNavBarHeight,
                        platformContext = platformContext,
                        onSwipeDragging = { isSwipeDragging = it }
                    )
                    Screen.Settings -> SettingsScreen(
                        config = config,
                        isBatteryOptimized = isBatteryOptimized,
                        onUpdateConfig = { viewModel.updateConfig(it) },
                        onUpdateTunnelEngine = { viewModel.updateTunnelEngine(it) },
                        onApplyPreset = { preset -> viewModel.applyPreset(preset) },
                        onOpenSplitTunneling = { navController.navigate(Screen.SplitTunneling.route) },
                        onOpenRoutingRules = { navController.navigate(Screen.RoutingRules.route) },
                        onOpenAutoDetect = { navController.navigate(Screen.AutoDetect.route) },
                        onOpenDnsOptimizer = { navController.navigate(Screen.AutoDetectDns.route) },
                        onOpenSpeedTest = { navController.navigate(Screen.SpeedTest.route) },
                        onResetAll = { viewModel.resetAllSettings() },
                        onExportBackup = { viewModel.exportFullBackup() },
                        onImportBackup = { viewModel.importFullBackup() },
                        onOptimizeMtu = { viewModel.optimizeMtu() },
                        isOptimizingMtu = isOptimizingMtu,
                        onRequestBatteryOptimization = { viewModel.requestBatteryOptimization() },
                        onOpenVpnSettings = { viewModel.openVpnSettings() },
                        onShowToast = { msg: String, err: Boolean -> viewModel.showToast(msg, err) },
                        initialPage = if (zeroTrustOpen) SettingsPage.ZEROTRUST else null,
                        onSubPageClosed = { zeroTrustOpen = false },
                        bottomContentPadding = totalNavBarHeight,
                        loadAutoConnectSettings = {
                            val s = getSettings(platformContext)
                            io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings(
                                autoConnectOnStart = s.getBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_ON_START, false),
                                autoConnectOnBoot = s.getBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_ON_BOOT, false),
                                autoConnectOnNetwork = s.getBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_ON_NETWORK, false),
                                autoRestartOnCrash = s.getBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_RESTART_ON_CRASH, false),
                                autoConnectAfterCrash = s.getBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_AFTER_CRASH, false)
                            )
                        },
                        saveAutoConnectSettings = { acs ->
                            val s = getSettings(platformContext)
                            s.putBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_ON_START, acs.autoConnectOnStart)
                            s.putBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_ON_BOOT, acs.autoConnectOnBoot)
                            s.putBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_ON_NETWORK, acs.autoConnectOnNetwork)
                            s.putBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_RESTART_ON_CRASH, acs.autoRestartOnCrash)
                            s.putBoolean(io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings.PREF_AUTO_CONNECT_AFTER_CRASH, acs.autoConnectAfterCrash)
                        }
                    )
                    Screen.Logs -> LogsScreen(
                        viewModel = viewModel,
                        onShowToast = { msg: String, err: Boolean -> viewModel.showToast(msg, err) },
                        bottomContentPadding = totalNavBarHeight
                    )
                    Screen.About -> AboutUsScreen(
                        appVersion = viewModel.appVersion,
                        bottomContentPadding = totalNavBarHeight
                    )
                    else -> {}
                }
            }
        }
        if (showNavBar) {
            CurvedNavBar(
                selectedTab = selectedTab,
                onTabSelected = { index -> selectTab(index) },
                scaleFactor = scaleFactor,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        if (isWaitingForLoginCode) {
            ZeroTrustLoginDialog(
                onSubmit = { viewModel.submitLoginCode(it) },
                onDismiss = { viewModel.submitLoginCode("") },
                scaleFactor = scaleFactor
            )
        }
        if (showTrayAdminDialog) {
            AdminRequiredDialog(
                onRelaunch = {
                    showTrayAdminDialog = false
                    getSystemUtils(platformContext).relaunchAsAdmin()
                },
                onDismiss = { showTrayAdminDialog = false },
                scaleFactor = scaleFactor
            )
        }
    }
}

@Composable
fun ZeroTrustLoginDialog(
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    scaleFactor: Float
) {
    var code by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width((320 * scaleFactor).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(IosNavBackground)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(IosNavActiveBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = IosNavActiveBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Zero Trust Login",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = (20 * scaleFactor).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A one-time code was sent to your email. Please enter it below to authorize this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IosNavInactiveGrey,
                    textAlign = TextAlign.Center,
                    fontSize = (13 * scaleFactor).sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = IosNavActiveBlue,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (code.length == 6) onSubmit(code)
                    }),
                    cursorBrush = SolidColor(IosNavActiveBlue),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (code.isEmpty()) {
                                Text(
                                    "000000",
                                    color = Color.White.copy(alpha = 0.05f),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 8.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", color = IosNavInactiveGrey, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = { if (code.length == 6) onSubmit(code) },
                        enabled = code.length == 6,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosNavActiveBlue
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Verify", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurvedNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    scaleFactor: Float,
    modifier: Modifier = Modifier
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val isAndroid = try { Class.forName("android.os.Build"); true } catch(_: Throwable) { false }
    val navBarPadding = if (isAndroid) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

    Box(modifier = modifier.fillMaxWidth().background(Color.Transparent)) {
        val scaledBarHeight = (BarContentHeight.value * scaleFactor).dp + navBarPadding
        val scaledButtonSize = (ButtonSize.value * scaleFactor).dp
        val scaledButtonCenterY = (ButtonCenterY.value * scaleFactor).dp
        val scaledCircleGap = (CircleGap.value * scaleFactor).dp
        val scaledBarTopY = (BarTopY.value * scaleFactor).dp
        val scaledItemBottomPadding = (ItemBottomPadding.value * scaleFactor).dp + navBarPadding

        val strings = io.github.immaghzbad.aetherst.shared.i18n.LocalAppStrings.current
        val tabs = listOf(
            strings.NAV_DASHBOARD to Icons.Default.Dashboard,
            strings.NAV_SETTINGS to Icons.Default.Settings,
            strings.NAV_LOGS to Icons.Default.Code,
            strings.NAV_ABOUT to Icons.Default.Info
        )
        val tabCount = tabs.size
        var barWidthPx by remember { mutableIntStateOf(0) }

        val indicatorOffset by animateFloatAsState(
            targetValue = selectedTab.toFloat(),
            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
            label = "indicatorOffset"
        )
        val visualOffset = if (isRtl) (tabCount - 1 - indicatorOffset) else indicatorOffset

        var displayedNavIcon by remember { mutableIntStateOf(selectedTab) }
        SideEffect {
            displayedNavIcon = selectedTab
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(scaledBarHeight)
                .onSizeChanged { barWidthPx = it.width }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = (15 * scaleFactor).dp,
                        spotColor = Color.Black.copy(alpha = 0.5f)
                    )
            ) {
                val tabWidth = size.width / tabCount
                val centerX = (visualOffset * tabWidth) + (tabWidth / 2)
                val barTop = scaledBarTopY.toPx()
                val notchBottom =
                    scaledButtonCenterY.toPx() + (scaledButtonSize.toPx() / 2f) + scaledCircleGap.toPx()
                val shoulderWidth = (45.dp.toPx() * scaleFactor)

                val barShape = Path().apply {
                    moveTo(0f, barTop)
                    lineTo(centerX - shoulderWidth, barTop)

                    cubicTo(
                        centerX - (40.dp.toPx() * scaleFactor),
                        barTop,
                        centerX - (38.dp.toPx() * scaleFactor),
                        barTop + (2.dp.toPx() * scaleFactor),
                        centerX - (35.dp.toPx() * scaleFactor),
                        barTop + (10.dp.toPx() * scaleFactor)
                    )
                    cubicTo(
                        centerX - (28.dp.toPx() * scaleFactor),
                        barTop + (26.dp.toPx() * scaleFactor),
                        centerX - (20.dp.toPx() * scaleFactor),
                        notchBottom,
                        centerX,
                        notchBottom
                    )
                    cubicTo(
                        centerX + (20.dp.toPx() * scaleFactor),
                        notchBottom,
                        centerX + (28.dp.toPx() * scaleFactor),
                        barTop + (26.dp.toPx() * scaleFactor),
                        centerX + (35.dp.toPx() * scaleFactor),
                        barTop + (10.dp.toPx() * scaleFactor)
                    )
                    cubicTo(
                        centerX + (38.dp.toPx() * scaleFactor),
                        barTop + (2.dp.toPx() * scaleFactor),
                        centerX + (40.dp.toPx() * scaleFactor),
                        barTop,
                        centerX + shoulderWidth,
                        barTop
                    )

                    lineTo(size.width, barTop)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = barShape,
                    color = IosNavBackground.copy(alpha = 0.94f),
                    style = Fill
                )
            }

            Box(
                modifier = Modifier
                    .size(scaledButtonSize + (scaledCircleGap * 2))
                    .offset {
                        val tabWidth = barWidthPx.toFloat() / tabCount
                        val outerSize = scaledButtonSize.toPx() + scaledCircleGap.toPx() * 2f
                        val offsetX = visualOffset * tabWidth + (tabWidth / 2) - (outerSize / 2f)
                        IntOffset(
                            if (isRtl) (barWidthPx.toFloat() - offsetX - outerSize).roundToInt()
                            else offsetX.roundToInt(),
                            (scaledButtonCenterY.toPx() - outerSize / 2f).roundToInt()
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val iconScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                    label = "iconScale"
                )

                Box(
                    modifier = Modifier
                        .size(scaledButtonSize)
                        .shadow(
                            elevation = (16 * scaleFactor).dp,
                            shape = CircleShape,
                            spotColor = IosNavActiveBlue.copy(alpha = 0.8f)
                        )
                        .background(IosNavActiveBlue, CircleShape)
                        .border(
                            width = (1.5 * scaleFactor).dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = displayedNavIcon, label = "navIcon") { tab ->
                        Icon(
                            imageVector = tabs[tab].second,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size((28 * scaleFactor).dp)
                                .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaledBarHeight)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.Bottom
            ) {
                tabs.forEachIndexed { index, (label, icon) ->
                    val isSelected = selectedTab == index

                    val contentAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.8f,
                        label = "contentAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(index) }
                            .padding(bottom = scaledItemBottomPadding),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.graphicsLayer(alpha = contentAlpha)
                        ) {
                            if (!isSelected) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = IosNavInactiveGrey.copy(alpha = 0.9f),
                                    modifier = Modifier.size((24 * scaleFactor).dp)
                                )
                                Spacer(modifier = Modifier.height((6 * scaleFactor).dp))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = (10 * scaleFactor).sp,
                                color = if (isSelected) IosNavActiveBlue else IosNavInactiveGrey
                            )
                        }
                    }
                }
            }
        }
    }
}
