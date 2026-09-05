package io.github.immaghzbad.aetherst.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import io.github.immaghzbad.aetherst.MainActivity
import io.github.immaghzbad.aetherst.R
import io.github.immaghzbad.aetherst.core.AutoConnectManager
import io.github.immaghzbad.aetherst.core.ConnectionController
import io.github.immaghzbad.aetherst.core.DnsMap
import io.github.immaghzbad.aetherst.core.HevEngineSettings
import io.github.immaghzbad.aetherst.core.HevTun2SocksConfig
import io.github.immaghzbad.aetherst.core.HevTun2SocksEngine
import io.github.immaghzbad.aetherst.core.HevTun2SocksNative
import io.github.immaghzbad.aetherst.core.PsiphonController
import io.github.immaghzbad.aetherst.core.RoutingEngine
import io.github.immaghzbad.aetherst.core.SocksTunBridge
import io.github.immaghzbad.aetherst.platform.PlatformContext
import io.github.immaghzbad.aetherst.platform.getSettings
import io.github.immaghzbad.aetherst.shared.data.ActiveProxyProvider
import io.github.immaghzbad.aetherst.shared.data.AetherConfigRepository
import io.github.immaghzbad.aetherst.shared.data.LogRepository
import io.github.immaghzbad.aetherst.shared.model.ConnectionMode
import io.github.immaghzbad.aetherst.shared.model.ConnectionStatus
import io.github.immaghzbad.aetherst.shared.model.TunnelEngine
import io.github.immaghzbad.aetherst.shared.platform.Bridge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

@Suppress("VpnServicePolicy")
class AetherVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var hevEngine: HevTun2SocksEngine? = null
    private var socksBridge: SocksTunBridge? = null
    private var routingEngine: RoutingEngine? = null
    private var activeTunnelEngine: TunnelEngine? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stateMutex = Mutex()
    private val activeAttemptId = AtomicLong(0)
    private val commandCounter = AtomicLong(0)
    private var startupJob: Job? = null
    private var statsJob: Job? = null
    private var lastHevUpstream: String? = null
    private var lastBridgeUpstream: String? = null

    private var isUserInitiatedStop = false
    private var wasEverRunning = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isNetworkCallbackRegistered = false
    private var autoReconnectJob: Job? = null
    private var lastNetworkLostTime: Long = 0L

    companion object {
        const val ACTION_START = "io.github.immaghzbad.aetherst.ACTION_START"
        const val ACTION_STOP = "io.github.immaghzbad.aetherst.ACTION_STOP"
        const val ACTION_RESTART = "io.github.immaghzbad.aetherst.ACTION_RESTART"
        const val ACTION_SWITCH_HEV = "io.github.immaghzbad.aetherst.SWITCH_HEV"
        const val CHANNEL_ID = "aether_vpn_status_v2"
        const val ALERT_CHANNEL_ID = "aether_vpn_alerts"
        const val NOTIFICATION_ID = 1001
        const val ALERT_NOTIFICATION_ID = 1003

        fun startVpn(context: Context): Boolean = runCatching {
            val intent = Intent(context, AetherVpnService::class.java).apply { action = ACTION_START }
            context.startForegroundService(intent)
            true
        }.getOrElse {
            LogRepository.e("[VpnService] Start failed: ${it.localizedMessage}")
            false
        }

        fun stopVpn(context: Context): Boolean = runCatching {
            val intent = Intent(context, AetherVpnService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
            true
        }.getOrElse {
            LogRepository.e("[VpnService] Stop failed: ${it.localizedMessage}")
            false
        }

        fun restartVpn(context: Context): Boolean = runCatching {
            val intent = Intent(context, AetherVpnService::class.java).apply { action = ACTION_RESTART }
            context.startForegroundService(intent)
            true
        }.getOrElse {
            LogRepository.e("[VpnService] Restart failed: ${it.localizedMessage}")
            false
        }
    }

    private fun getController() = ConnectionController.getInstance(this)

    override fun onCreate() {
        super.onCreate()
        LogRepository.initialize(getSettings(PlatformContext(this)))
        PsiphonController.setVpnService(this)
        createNotificationChannel()
        
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AetherST:VpnWakeLock")

        scope.launch {
            ConnectionController.status.collect { status ->
                updateNotification()
                handleTunLifecycle(status)
                handleAutoReconnectOnStatus(status)
                runCatching { AetherWidgetProvider.updateAllWidgets(this@AetherVpnService) }
            }
        }

        scope.launch {
            AetherConfigRepository.getInstance(getSettings(PlatformContext(this@AetherVpnService))).config.collect {
                routingEngine?.clearCache()
            }
        }
        registerNetworkMonitor()
    }

    private fun registerNetworkMonitor() {
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    scope.launch { handleNetworkLost() }
                }
                override fun onAvailable(network: Network) {
                    scope.launch {
                        handleNetworkAvailable()
                        AutoConnectManager.handleNetworkChange(this@AetherVpnService)
                    }
                }
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        scope.launch {
                            handleNetworkAvailable()
                            AutoConnectManager.handleNetworkChange(this@AetherVpnService)
                        }
                    }
                }
            }
            networkCallback = callback
            cm.registerNetworkCallback(request, callback)
            isNetworkCallbackRegistered = true
        } catch (e: Exception) {
            LogRepository.w("[VpnService] Network monitor registration failed: ${e.message}")
        }
    }

    private fun unregisterNetworkMonitor() {
        if (!isNetworkCallbackRegistered) {
            networkCallback = null
            return
        }
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            LogRepository.w("[VpnService] unregisterNetworkCallback failed: ${e.message}")
        }
        networkCallback = null
        isNetworkCallbackRegistered = false
    }

    private fun handleNetworkLost() {
        val status = ConnectionController.status.value
        val config = AetherConfigRepository.getInstance(getSettings(PlatformContext(this))).config.value
        if (!config.smartReconnect) return
        if (isUserInitiatedStop) return
        if (status == ConnectionStatus.RUNNING || status == ConnectionStatus.TUN_ACTIVE) {
            lastNetworkLostTime = System.currentTimeMillis()
            LogRepository.w("[VpnService] Network lost while $status -> RECONNECTING")
            try {
                getController().markReconnecting()
            } catch (e: Exception) {
                LogRepository.w("[VpnService] markReconnecting failed: ${e.message}")
                Bridge.statusOverride.value = ConnectionStatus.RECONNECTING
            }
        }
    }

    private fun handleNetworkAvailable() {
        val status = ConnectionController.status.value
        val config = AetherConfigRepository.getInstance(getSettings(PlatformContext(this))).config.value
        if (!config.smartReconnect) return
        if (isUserInitiatedStop) return
        if (status == ConnectionStatus.RECONNECTING || status == ConnectionStatus.ERROR || status == ConnectionStatus.FAILED) {
            autoReconnectJob?.cancel()
            autoReconnectJob = scope.launch {
                delay(1500.milliseconds)
                if (!isNetworkValidated()) {
                    delay(2000.milliseconds)
                }
                if (!isNetworkValidated()) return@launch
                val cur = ConnectionController.status.value
                if (cur == ConnectionStatus.RECONNECTING || cur == ConnectionStatus.ERROR || cur == ConnectionStatus.FAILED) {
                    LogRepository.i("[VpnService] Network restored -> auto-reconnect from $cur")
                    withContext(Dispatchers.Main) {
                        try {
                            if (vpnInterface == null) {
                                startAttempt(commandCounter.incrementAndGet())
                            } else {
                                hevEngine?.resume()
                            }
                        } catch (_: Exception) {
                            startAttempt(commandCounter.incrementAndGet())
                        }
                    }
                    delay(3000.milliseconds)
                    if (ConnectionController.status.value == ConnectionStatus.RECONNECTING) {
                        getController().start()
                    }
                }
            }
        }
    }

    private fun isNetworkValidated(): Boolean {
        return try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: Exception) { false }
    }

    private fun handleAutoReconnectOnStatus(status: ConnectionStatus) {
        if (isUserInitiatedStop) return
        val cfg = AetherConfigRepository.getInstance(getSettings(PlatformContext(this))).config.value
        if (!cfg.smartReconnect) return
        if (status == ConnectionStatus.ERROR || status == ConnectionStatus.FAILED) {
            autoReconnectJob?.cancel()
            autoReconnectJob = scope.launch {
                var attempts = 0
                while (attempts < cfg.reconnectRetryLimit && isActive) {
                    if (!isNetworkValidated()) {
                        LogRepository.w("[VpnService] Auto-reconnect waiting for network...")
                        delay(3000.milliseconds)
                        continue
                    }
                    delay((cfg.reconnectSecs * 1000L).coerceAtLeast(2000L).milliseconds)
                    val cur = ConnectionController.status.value
                    if (cur != ConnectionStatus.ERROR && cur != ConnectionStatus.FAILED) break
                    if (isUserInitiatedStop) break
                    attempts++
                    LogRepository.i("[VpnService] Auto-reconnect attempt $attempts/${cfg.reconnectRetryLimit} from $cur")
                    try {
                        getController().start()
                    } catch (e: Exception) {
                        LogRepository.e("[VpnService] Auto-reconnect start failed: ${e.message}")
                    }
                    delay(5000.milliseconds)
                }
                if (attempts >= cfg.reconnectRetryLimit) {
                    LogRepository.e("[VpnService] Auto-reconnect limit reached")
                }
            }
        } else if (status == ConnectionStatus.RUNNING || status == ConnectionStatus.STOPPED) {
            autoReconnectJob?.cancel()
            autoReconnectJob = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                isUserInitiatedStop = false
                AutoConnectManager.clearManualDisconnect(this)
                AutoConnectManager.clearCrashCount(this)
                autoReconnectJob?.cancel()
                autoReconnectJob = null
                showInitialNotification()
                startAttempt(commandCounter.incrementAndGet())
            }
            ACTION_RESTART -> {
                isUserInitiatedStop = false
                showInitialNotification()
                restartTunnel(commandCounter.incrementAndGet())
            }
            ACTION_STOP -> {
                isUserInitiatedStop = true
                AutoConnectManager.setManualDisconnect(this, true)
                showInitialNotification()
                stopVpnService(commandCounter.incrementAndGet())
            }
            ACTION_SWITCH_HEV -> {
                showInitialNotification()
                val host = intent.getStringExtra("host") ?: "127.0.0.1"
                val port = intent.getIntExtra("port", 3080)
                scope.launch { switchHevInternal(host, port) }
            }
            else -> {
                if (isUserInitiatedStop) {
                    stopSelf()
                } else {
                    LogRepository.i("[VpnService] System-initiated start with null intent ignored (START_NOT_STICKY)")
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onRevoke() {
        super.onRevoke()
        isUserInitiatedStop = false
        LogRepository.w("[VpnService] VPN revoked by system or other app")
        val config = AetherConfigRepository.getInstance(getSettings(PlatformContext(this))).config.value
        val attemptId = activeAttemptId.getAndSet(0)
        runCatching { startupJob?.cancel() }
        stopStatsJob()
        runCatching {
            kotlinx.coroutines.runBlocking {
                stateMutex.withLock {
                    hevEngine?.requestStop()
                    hevEngine = null
                    socksBridge?.stop()
                    socksBridge = null
                    closeVpnInterface(attemptId)
                    activeTunnelEngine = null
                }
            }
        }
        scope.launch {
            if (config.connectionMode != ConnectionMode.PROXY_ONLY) {
                getController().stop()
            } else {
                LogRepository.i("[VpnService] Revoked but keeping core alive for Proxy Mode")
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startAttempt(commandId: Long) {
        startupJob = scope.launch {
            if (commandCounter.get() != commandId) return@launch

            val attemptId = stateMutex.withLock {
                if (commandCounter.get() != commandId) return@launch
                val current = ConnectionController.status.value
                if (current == ConnectionStatus.RUNNING || current == ConnectionStatus.VALIDATING) return@launch
                
                val id = System.currentTimeMillis()
                activeAttemptId.set(id)
                id
            }

            runCatching { wakeLock?.acquire(4 * 60 * 60 * 1000L) }.onFailure { LogRepository.w("[VpnService] wakeLock acquire failed: ${it.message}") }

            try {
                val config = AetherConfigRepository.getInstance(getSettings(PlatformContext(this@AetherVpnService))).config.value

                routingEngine = RoutingEngine(config.routingRules)

                val effectiveEngine = if (
                    config.tunnelEngine == TunnelEngine.HEV_TUN2SOCKS &&
                    (config.routingRules.isNotEmpty() || config.blockedPackages.isNotEmpty()) &&
                    !config.tunnelAllApps
                ) {
                    TunnelEngine.SOCKS_TUN_BRIDGE
                } else {
                    config.tunnelEngine
                }
                activeTunnelEngine = effectiveEngine

                if (!establishVpnTun(attemptId, effectiveEngine)) throw IllegalStateException("TUN establishment failed")
                ensureCurrentAttempt(attemptId)
                val descriptor = vpnInterface ?: throw IllegalStateException("TUN descriptor unavailable")

                if (effectiveEngine == TunnelEngine.HEV_TUN2SOCKS) {
                    if (!HevTun2SocksNative.isAvailable) throw IllegalStateException("HEV Native library not available")

                    hevEngine = HevTun2SocksEngine()

                    val hevSettings = HevEngineSettings(
                        logLevel = config.hevLogLevel,
                        connectTimeoutMs = config.hevConnectTimeoutMs,
                        readWriteTimeoutMs = config.hevReadWriteTimeoutMs,
                        maxSessionCount = config.hevMaxSessionCount,
                        mapdnsCacheSize = config.hevMapdnsCacheSize
                    )
                    LogRepository.i(
                        "[VpnService] HEV settings: log=${hevSettings.logLevel} connectTimeout=${hevSettings.connectTimeoutMs}ms " +
                                "rwTimeout=${hevSettings.readWriteTimeoutMs}ms maxSessions=${if (hevSettings.maxSessionCount == 0) "unlimited" else hevSettings.maxSessionCount.toString()} mapdnsCache=${hevSettings.mapdnsCacheSize} udp=${config.hevUdpMode}"
                    )

                    val (hevHost, hevPort) = resolveEffectiveSocks(config, ActiveProxyProvider.psiphonProxyUrl)
                    val ok = hevEngine?.start(
                        tunPfd = descriptor,
                        socksAddress = hevHost,
                        socksPort = hevPort,
                        mtu = config.mtu.coerceIn(576, 9000),
                        attemptId = attemptId,
                        settings = hevSettings,
                        udpMode = config.hevUdpMode
                    ) == true
                    if (!ok) throw IllegalStateException("HEV engine failed to start mtu=${config.mtu}")
                    lastHevUpstream = "$hevHost:$hevPort"
                } else {
                    val psiphonUrl = ActiveProxyProvider.psiphonProxyUrl
                    val (bridgeHost, bridgePort) = resolveEffectiveSocks(config, psiphonUrl)
                    socksBridge = SocksTunBridge(
                        vpnService = this@AetherVpnService,
                        tunDescriptor = descriptor,
                        socksHost = bridgeHost,
                        socksPort = bridgePort,
                        mtu = config.mtu.coerceIn(576, 9000),
                        blockedPackagesProvider = { if (config.tunnelAllApps) emptySet() else config.blockedPackages },
                        routingEngine = routingEngine!!
                    ).apply { start() }
                    lastBridgeUpstream = "$bridgeHost:$bridgePort"
                }

                getController().start()
                if (ConnectionController.status.value != ConnectionStatus.RUNNING) {
                    throw IllegalStateException("Core failed to start")
                }
                ensureCurrentAttempt(attemptId)

                val socksPort = config.socksPort.toIntOrNull() ?: 1819
                runCatching {
                    val domainCode = probeCoreSocks5(config.socksHost, socksPort, domainTarget = "www.cloudflare.com", ipLiteralTarget = null)
                    val ipCode = probeCoreSocks5(config.socksHost, socksPort, domainTarget = null, ipLiteralTarget = "1.1.1.1")
                    LogRepository.i("[VpnService] Core proxy probe: domain-reply=$domainCode ip-literal-reply=$ipCode (0x00=granted)")
                }.onFailure {
                    LogRepository.e("[VpnService] Core proxy probe failed: ${it.localizedMessage}")
                }

                LogRepository.i("[VpnService] VPN tunnel active")
                wasEverRunning = true
                startStatsJob()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                if (activeAttemptId.get() == attemptId && commandCounter.get() == commandId) {
                    rollback(attemptId, throwable.localizedMessage ?: "Startup failed")
                }
            }
        }
    }

    private fun ensureCurrentAttempt(attemptId: Long) {
        if (activeAttemptId.get() != attemptId) throw IllegalStateException("Connection attempt invalidated")
    }

    private fun establishVpnTun(attemptId: Long, engine: TunnelEngine): Boolean = runCatching {
        val cfgForMtu = AetherConfigRepository.getInstance(getSettings(PlatformContext(this))).config.value
        val effectiveMtu = cfgForMtu.mtu.coerceIn(576, 9000)
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val builder = Builder()
            .addAddress("198.18.0.1", 24)
            .addAddress("fd00::1", 120)
            .addRoute("0.0.0.0", 0)
            .setMtu(effectiveMtu)
            .setSession("AetherST Tunnel")
            .setConfigureIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), pendingFlags))

        if (engine == TunnelEngine.HEV_TUN2SOCKS) {
            builder.addDnsServer(HevTun2SocksConfig.MAP_DNS_ADDRESS)
        } else {
            builder.addDnsServer("1.1.1.1")
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("2606:4700:4700::1111")
            builder.addDnsServer("2001:4860:4860::8888")
        }

        val config = AetherConfigRepository.getInstance(getSettings(PlatformContext(this))).config.value
        if (config.ipv6Leak && Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            runCatching { builder.addRoute("::", 0) }
        }

        if (config.tunnelAllApps) {
            builder.addDisallowedApplication(packageName)
        } else {
            if (config.tunneledPackages.isNotEmpty()) {
                var added = 0
                config.tunneledPackages
                    .asSequence()
                    .filterNot { it == packageName }
                    .forEach { pkg ->
                        try {
                            builder.addAllowedApplication(pkg)
                            added++
                        } catch (_: PackageManager.NameNotFoundException) {
                            LogRepository.w("[Tun] Ignoring uninstalled package: $pkg")
                        } catch (e: Exception) {
                            LogRepository.w("[Tun] Skipping package $pkg: ${e.message}")
                        }
                    }
                LogRepository.i("[Tun] Bypass-default: $added apps tunneled, rest bypass (allowed mode)")
            } else {
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (e: Exception) {
                    LogRepository.w("[Tun] Failed to disallow self: ${e.message}")
                }
                try {
                    val pm = packageManager
                    val allPkgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
                    } else {
                        @Suppress("DEPRECATION") pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    }
                    var disallowed = 0
                    for (app in allPkgs) {
                        val pkg = app.packageName
                        if (pkg == packageName) continue
                        try {
                            builder.addDisallowedApplication(pkg)
                            disallowed++
                        } catch (_: PackageManager.NameNotFoundException) {
                        } catch (e: Exception) {
                            LogRepository.w("[Tun] Skipping disallow $pkg: ${e.message}")
                        }
                    }
                    LogRepository.i("[Tun] Bypass-default: 0 tunneled, $disallowed apps bypassed (all bypass)")
                } catch (e: Exception) {
                    LogRepository.w("[Tun] Failed to enumerate apps for all-bypass: ${e.message}")
                }
            }
        }

        vpnInterface = builder.establish()
        if (vpnInterface == null) {
            val prep = VpnService.prepare(this@AetherVpnService)
            LogRepository.e("[Tun] [attempt=$attemptId] Failed: builder.establish() returned null mtu=$effectiveMtu engine=$engine prepare=${prep != null}")
            return@runCatching false
        }
        LogRepository.i("[Tun] [attempt=$attemptId] Established mtu=$effectiveMtu engine=$engine")
        scope.launch(Dispatchers.IO) {
            withTimeoutOrNull(2000) {
                runCatching {
                    val bypassIps = mutableSetOf<String>()
                    val cfg = AetherConfigRepository.getInstance(getSettings(PlatformContext(this@AetherVpnService))).config.value
                    val ipv4Regex = Regex("""\d+\.\d+\.\d+\.\d+""")
                    val ipv6Regex = Regex("""(?:[0-9a-fA-F]{1,4}:){2,}[0-9a-fA-F:]*""")
                    fun extractIps(text: String, out: MutableSet<String>) {
                        ipv4Regex.findAll(text).forEach { out.add(it.value) }
                        ipv6Regex.findAll(text).forEach {
                            val ip = it.value.trim().trim('[', ']')
                            if (ip.contains(":") && ip.length >= 3) {
                                try { InetAddress.getByName(ip); out.add(ip) } catch (_: Exception) {}
                            }
                        }
                    }
                    if (cfg.peer.isNotEmpty()) {
                        extractIps(cfg.peer, bypassIps)
                    }
                    filesDir.listFiles()?.filter { it.name.contains("lastconn") }?.take(8)?.forEach { f ->
                        try {
                            extractIps(f.readText().take(8192), bypassIps)
                        } catch (e: Exception) {
                            LogRepository.w("[VpnService] read lastconn failed ${f.name}: ${e.message}")
                        }
                    }
                    bypassIps.add("162.159.198.39")
                    bypassIps.add("162.159.198.2")
                    bypassIps.add("162.159.192.1")
                    bypassIps.add("188.114.96.1")
                    for (ip in bypassIps) {
                        try {
                            val s = Socket()
                            if (!protect(s)) {
                                LogRepository.d("[VpnService] protect failed for bypass $ip")
                            }
                            val port = if (ip.contains(":")) 443 else 443
                            s.connect(InetSocketAddress(ip, port), 200)
                            s.close()
                        } catch (e: Exception) {
                            LogRepository.d("[VpnService] bypass probe failed $ip: ${e.message}")
                        }
                    }
                }
            }
        }
        true
    }.getOrElse {
        LogRepository.e("[Tun] [attempt=$attemptId] Failed: ${it.localizedMessage}")
        false
    }

    private suspend fun rollback(attemptId: Long, reason: String) {
        LogRepository.e("[VpnService] Rollback: $reason")
        stopStatsJob()
        val status = ConnectionController.status.value
        val pauseOnly = !isUserInitiatedStop &&
                (status == ConnectionStatus.RECONNECTING || status == ConnectionStatus.DATAPLANE_VALIDATED || status == ConnectionStatus.SOCKS_READY)
        if (wasEverRunning && !isUserInitiatedStop) {
            showDisconnectionAlert(reason)
        }
        cleanupResources(attemptId)
        if (!pauseOnly) {
            getController().stop()
        }
    }

    private fun stopVpnService(commandId: Long) {
        autoReconnectJob?.cancel()
        autoReconnectJob = null
        scope.launch {
            val attemptId = activeAttemptId.getAndSet(0)
            startupJob?.cancelAndJoin()
            stopStatsJob()

            cleanupResources(attemptId, forceTeardown = true)
            if (commandCounter.get() != commandId) return@launch

            runCatching { getController().stop() }.onFailure {
                LogRepository.e("[VpnService] Controller stop failed: ${it.localizedMessage}")
            }

            if (commandCounter.get() != commandId) return@launch

            scope.launch(Dispatchers.Main) {
                if (isActive && commandCounter.get() == commandId) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun restartTunnel(commandId: Long) {
        autoReconnectJob?.cancel()
        autoReconnectJob = null
        scope.launch {
            val attemptId = activeAttemptId.getAndSet(0)
            startupJob?.cancelAndJoin()
            stopStatsJob()

            cleanupResources(attemptId, forceTeardown = true)
            runCatching { getController().stop() }.onFailure {
                LogRepository.e("[VpnService] Controller stop failed during restart: ${it.localizedMessage}")
            }

            if (commandCounter.get() != commandId || isUserInitiatedStop) return@launch
            startAttempt(commandCounter.incrementAndGet())
        }
    }

    private suspend fun cleanupResources(attemptId: Long, forceTeardown: Boolean = false) {
        val status = ConnectionController.status.value
        val pauseOnly = !forceTeardown && !isUserInitiatedStop &&
                (status == ConnectionStatus.RECONNECTING || status == ConnectionStatus.DATAPLANE_VALIDATED || status == ConnectionStatus.SOCKS_READY)
        stateMutex.withLock {
            if (pauseOnly) {
                LogRepository.i("[VpnService] Reconnect in progress; pausing TUN instead of tearing down")
                stopStatsJob()
                hevEngine?.pause()
            } else {
                hevEngine?.requestStop()
                hevEngine = null
                socksBridge?.stop()
                socksBridge = null
                closeVpnInterface(attemptId)
                activeTunnelEngine = null
                lastHevUpstream = null
                lastBridgeUpstream = null
                DnsMap.clear()
            }
            runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        }
    }

    private suspend fun handleTunLifecycle(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.RECONNECTING -> {
                if (!wasEverRunning) return
                stopStatsJob()
                hevEngine?.pause()
                LogRepository.i("[VpnService] RECONNECTING: TUN paused, waiting for core recovery")
            }
            ConnectionStatus.DATAPLANE_VALIDATED, ConnectionStatus.SOCKS_READY -> {
                if (!wasEverRunning) return
                stopStatsJob()
                hevEngine?.pause()
            }
            ConnectionStatus.RUNNING, ConnectionStatus.TUN_ACTIVE -> {
                if (!wasEverRunning) return
                val psiphonUrl = ActiveProxyProvider.psiphonProxyUrl
                val cfg = AetherConfigRepository.getInstance(getSettings(PlatformContext(this@AetherVpnService))).config.value
                val (targetHost, targetPort) = resolveEffectiveSocks(cfg, psiphonUrl)
                val target = "$targetHost:$targetPort"
                when (activeTunnelEngine) {
                    TunnelEngine.HEV_TUN2SOCKS -> {
                        if (lastHevUpstream != target && hevEngine != null && vpnInterface != null) {
                            LogRepository.i("[VpnService] HEV restart to $target for psiphon chain (was $lastHevUpstream)")
                            stopStatsJob()
                            val restartDescriptor: android.os.ParcelFileDescriptor?
                            stateMutex.withLock {
                                hevEngine?.requestStop()
                                restartDescriptor = vpnInterface
                            }
                            delay(400.milliseconds)
                            if (restartDescriptor != null) {
                                val newEngine = HevTun2SocksEngine()
                                val hevSettings = HevEngineSettings(
                                    logLevel = cfg.hevLogLevel,
                                    connectTimeoutMs = cfg.hevConnectTimeoutMs,
                                    readWriteTimeoutMs = cfg.hevReadWriteTimeoutMs,
                                    maxSessionCount = cfg.hevMaxSessionCount,
                                    mapdnsCacheSize = cfg.hevMapdnsCacheSize
                                )
                                val ok = newEngine.start(
                                    tunPfd = restartDescriptor,
                                    socksAddress = targetHost,
                                    socksPort = targetPort,
                                    mtu = cfg.mtu.coerceIn(576, 9000),
                                    attemptId = activeAttemptId.get(),
                                    settings = hevSettings,
                                    udpMode = cfg.hevUdpMode
                                )
                                stateMutex.withLock {
                                    if (ok) {
                                        hevEngine = newEngine
                                        lastHevUpstream = target
                                        LogRepository.i("[VpnService] HEV restarted to $target mtu=${cfg.mtu}")
                                    } else {
                                        LogRepository.e("[VpnService] HEV restart to $target failed")
                                        hevEngine?.resume()
                                    }
                                }
                            }
                            if (statsJob == null) startStatsJob()
                        } else {
                            hevEngine?.resume()
                            if (statsJob == null) startStatsJob()
                            if (lastHevUpstream == null) lastHevUpstream = target
                        }
                    }
                    TunnelEngine.SOCKS_TUN_BRIDGE -> {
                        if (lastBridgeUpstream != target && socksBridge != null) {
                            LogRepository.i("[VpnService] SocksTunBridge switch to $target mtu=${cfg.mtu} for psiphon chain (was $lastBridgeUpstream)")
                            socksBridge?.updateUpstream(targetHost, targetPort)
                            lastBridgeUpstream = target
                            if (statsJob == null) startStatsJob()
                        } else {
                            if (lastBridgeUpstream == null) lastBridgeUpstream = target
                            if (statsJob == null) startStatsJob()
                        }
                    }
                    else -> {
                        if (statsJob == null) startStatsJob()
                    }
                }
                DnsMap.clear()
                routingEngine?.clearCache()
            }
            ConnectionStatus.ERROR, ConnectionStatus.FAILED -> {
                if (!wasEverRunning) return
                stopStatsJob()
                hevEngine?.pause()
            }
            else -> {}
        }
    }

    private suspend fun switchHevInternal(host: String, port: Int) {
        val target = "$host:$port"
        if (activeTunnelEngine == TunnelEngine.SOCKS_TUN_BRIDGE) {
            if (lastBridgeUpstream == target) return
            if (socksBridge == null || vpnInterface == null) return
            LogRepository.i("[VpnService] Switching SocksTunBridge to $target")
            socksBridge?.updateUpstream(host, port)
            lastBridgeUpstream = target
            DnsMap.clear()
            routingEngine?.clearCache()
            return
        }
        if (lastHevUpstream == target) return
        if (hevEngine == null || vpnInterface == null) return
        LogRepository.i("[VpnService] Switching HEV to $target")
        stopStatsJob()
        val switchDescriptor: android.os.ParcelFileDescriptor?
        var switchCfg: io.github.immaghzbad.aetherst.shared.model.AetherConfig? = null
        var switchHevSettings: HevEngineSettings? = null
        stateMutex.withLock {
            hevEngine?.requestStop()
            switchDescriptor = vpnInterface
            if (switchDescriptor != null) {
                val cfg = AetherConfigRepository.getInstance(getSettings(PlatformContext(this@AetherVpnService))).config.value
                switchCfg = cfg
                switchHevSettings = HevEngineSettings(
                    logLevel = cfg.hevLogLevel,
                    connectTimeoutMs = cfg.hevConnectTimeoutMs,
                    readWriteTimeoutMs = cfg.hevReadWriteTimeoutMs,
                    maxSessionCount = cfg.hevMaxSessionCount,
                    mapdnsCacheSize = cfg.hevMapdnsCacheSize
                )
            }
        }
        val descriptor = switchDescriptor ?: return
        val cfg = switchCfg ?: return
        val hevSettings = switchHevSettings ?: return
        delay(400.milliseconds)
        val newEngine = HevTun2SocksEngine()
        val ok = newEngine.start(descriptor, host, port, cfg.mtu.coerceIn(576, 9000), activeAttemptId.get(), hevSettings, cfg.hevUdpMode)
        stateMutex.withLock {
            if (ok) {
                hevEngine = newEngine
                lastHevUpstream = target
                LogRepository.i("[VpnService] HEV switched to $target mtu=${cfg.mtu}")
                DnsMap.clear()
                routingEngine?.clearCache()
                if (statsJob == null) startStatsJob()
            } else {
                LogRepository.e("[VpnService] HEV switch to $target failed")
            }
        }
    }

    private fun resolveEffectiveSocks(config: io.github.immaghzbad.aetherst.shared.model.AetherConfig, psiphonUrl: String?): Pair<String, Int> {
        val isPsiphon = psiphonUrl?.contains("3080") == true || config.upstreamProxy.contains("3080")
        val host = if (isPsiphon) "127.0.0.1" else config.socksHost
        val port = if (isPsiphon) 3080 else config.socksPort.toIntOrNull() ?: 1819
        return host to port
    }

    private fun closeVpnInterface(attemptId: Long) {
        vpnInterface?.let {
            runCatching { it.close() }
            vpnInterface = null
            LogRepository.i("[Tun] [attempt=$attemptId] Closed")
        }
    }

    private fun startStatsJob() {
        statsJob?.cancel()
        statsJob = scope.launch {
            while (isActive) {
                delay(1000.milliseconds)
                updateTraffic()
            }
        }
    }

    private fun stopStatsJob() {
        statsJob?.cancel()
        statsJob = null
    }

    private fun updateTraffic() {
        if (activeTunnelEngine == TunnelEngine.HEV_TUN2SOCKS) {
            hevEngine?.stats?.value?.let {
                getController().setTraffic(it.txBytes, it.rxBytes)
            }
        } else {
            socksBridge?.getStats()?.let {
                getController().setTraffic(it.txBytes, it.rxBytes)
                logPeriodicTraffic("[VpnService] TUN stats (Bridge): txBytes=${it.txBytes} rxBytes=${it.rxBytes}")
            }
        }
    }

    private var trafficLogTick = 0L

    private fun logPeriodicTraffic(message: String) {
        trafficLogTick++
        if (trafficLogTick % 5 == 0L) LogRepository.i(message)
    }

    private fun probeCoreSocks5(socksHost: String, socksPort: Int, domainTarget: String?, ipLiteralTarget: String?): Int {
        val socket = Socket()
        val isLoopback = socksHost == "127.0.0.1" || socksHost == "::1" || socksHost == "localhost"
        if (!isLoopback) {
            if (!protect(socket)) {
                LogRepository.w("[VpnService] probe protect failed for $socksHost:$socksPort")
            }
        }
        try {
            socket.tcpNoDelay = true
            socket.connect(InetSocketAddress(socksHost, socksPort), 3000)
            socket.soTimeout = 6000
            val ins = socket.getInputStream()
            val out = socket.getOutputStream()

            out.write(byteArrayOf(5, 1, 0))
            out.flush()
            val method = ByteArray(2)
            if (!fillStream(ins, method)) return -255
            if (method[0] != 5.toByte() || method[1] != 0.toByte()) return -254

            val addrPart: ByteArray = if (domainTarget != null) {
                val d = domainTarget.toByteArray()
                val buf = ByteArray(1 + d.size)
                buf[0] = d.size.toByte()
                System.arraycopy(d, 0, buf, 1, d.size)
                buf
            } else {
                InetAddress.getByName(requireNotNull(ipLiteralTarget)).address
            }
            val atyp: Byte = if (domainTarget != null) 3 else 1

            val req = ByteArray(5 + addrPart.size + 2)
            req[0] = 5
            req[1] = 1
            req[2] = 0
            req[3] = atyp
            System.arraycopy(addrPart, 0, req, 4, addrPart.size)
            req[4 + addrPart.size] = (80 shr 8).toByte()
            req[5 + addrPart.size] = 80.toByte()
            out.write(req)
            out.flush()

            val hdr = ByteArray(4)
            if (!fillStream(ins, hdr)) return -253
            when (hdr[3].toInt() and 0xFF) {
                1 -> if (!fillStream(ins, ByteArray(6))) return -252
                4 -> if (!fillStream(ins, ByteArray(18))) return -252
                3 -> {
                    val len = ins.read()
                    if (len <= 0 || !fillStream(ins, ByteArray(len + 2))) return -252
                }
            }
            return hdr[1].toInt() and 0xFF
        } finally {
            runCatching { socket.close() }
        }
    }

    private fun fillStream(ins: InputStream, buffer: ByteArray): Boolean {
        var offset = 0
        while (offset < buffer.size) {
            val n = ins.read(buffer, offset, buffer.size - offset)
            if (n <= 0) return false
            offset += n
        }
        return true
    }

    private fun showInitialNotification() {
        val text = when (ConnectionController.status.value) {
            ConnectionStatus.RUNNING, ConnectionStatus.TUN_ACTIVE -> "VPN connected"
            ConnectionStatus.RECONNECTING -> "Reconnecting..."
            ConnectionStatus.STOPPING -> "Disconnecting..."
            ConnectionStatus.ERROR, ConnectionStatus.FAILED -> "Connection error"
            else -> "Connecting..."
        }
        try {
            val notification = buildNotification(text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            LogRepository.w("[VpnService] startForeground failed: ${e.message}")
            try { stopSelf() } catch (_: Exception) {}
        }
    }

    private fun updateNotification() {
        val status = ConnectionController.status.value
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (status == ConnectionStatus.STOPPED) {
            manager.cancel(NOTIFICATION_ID)
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }
        val text = when (status) {
            ConnectionStatus.RUNNING, ConnectionStatus.TUN_ACTIVE -> "VPN connected"
            ConnectionStatus.STARTING, ConnectionStatus.VALIDATING, ConnectionStatus.DATAPLANE_VALIDATED, ConnectionStatus.SOCKS_READY -> "Connecting..."
            ConnectionStatus.RECONNECTING -> "Reconnecting..."
            ConnectionStatus.STOPPING -> "Disconnecting..."
            ConnectionStatus.ERROR, ConnectionStatus.FAILED -> "Connection error"
        }
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(statusText: String): Notification {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flags)
        val stopIntent = PendingIntent.getService(this, 1, Intent(this, AetherVpnService::class.java).apply { action = ACTION_STOP }, flags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AetherST Tunnel")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_stat_aether)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", stopIntent)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun showDisconnectionAlert(reason: String) {
        try {
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flags)
            val reconnectIntent = PendingIntent.getService(
                this, 2,
                Intent(this, AetherVpnService::class.java).apply { action = ACTION_START },
                flags
            )

            val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setContentTitle("⚠️ VPN Disconnected")
                .setContentText("Connection lost unexpectedly. Tap to reconnect.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("AetherST Tunnel was disconnected unexpectedly.\nReason: $reason\n\nTap 'Reconnect' to restore your secure connection.")
                )
                .setSmallIcon(R.drawable.ic_stat_aether)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.ic_popup_sync, "Reconnect", reconnectIntent)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFFFF3B30.toInt())
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(ALERT_NOTIFICATION_ID, notification)
            LogRepository.i("[VpnService] Disconnection alert notification sent")
        } catch (e: Exception) {
            LogRepository.e("[VpnService] Failed to send disconnection alert: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        val statusChannel = NotificationChannel(CHANNEL_ID, "AetherST Tunnel", NotificationManager.IMPORTANCE_DEFAULT).apply {
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            setShowBadge(false)
        }
        val alertChannel = NotificationChannel(ALERT_CHANNEL_ID, "AetherST Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notifications for unexpected disconnections"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(statusChannel)
        manager.createNotificationChannel(alertChannel)
    }

    override fun onDestroy() {
        autoReconnectJob?.cancel()
        scope.cancel()
        unregisterNetworkMonitor()
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
