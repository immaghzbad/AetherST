package io.github.immaghzbad.aetherst

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.github.immaghzbad.aetherst.platform.PlatformContext
import io.github.immaghzbad.aetherst.platform.getSettings
import io.github.immaghzbad.aetherst.platform.getSystemUtils
import io.github.immaghzbad.aetherst.shared.App
import io.github.immaghzbad.aetherst.shared.core.ConnectionController
import io.github.immaghzbad.aetherst.shared.core.NetworkHealer
import io.github.immaghzbad.aetherst.shared.data.AetherConfigRepository
import io.github.immaghzbad.aetherst.desktop.RenderCompat
import io.github.immaghzbad.aetherst.shared.desktop.AetherTray
import io.github.immaghzbad.aetherst.shared.desktop.TrayActions
import io.github.immaghzbad.aetherst.shared.desktop.TrayState
import io.github.immaghzbad.aetherst.shared.model.ConnectionMode
import io.github.immaghzbad.aetherst.shared.model.AutoConnectSettings
import io.github.immaghzbad.aetherst.shared.core.SingleInstanceLock
import io.github.immaghzbad.aetherst.shared.model.ConnectionStatus
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.io.File
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

private var lockSocket: ServerSocket? = null
private var requestShowWindow: (() -> Unit)? = null

private fun sweepChildProcesses() {
    runCatching {
        ProcessBuilder("taskkill", "/F", "/T", "/IM", "aether.exe")
            .redirectErrorStream(true).start().waitFor(5, TimeUnit.SECONDS)
    }
    runCatching {
        ProcessBuilder("taskkill", "/F", "/T", "/IM", "hev-socks5-tunnel.exe")
            .redirectErrorStream(true).start().waitFor(5, TimeUnit.SECONDS)
    }
}

private fun cleanTempFiles() {
    try {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "AetherST")
        if (tempDir.exists()) tempDir.walkBottomUp().forEach { it.delete() }
    } catch (_: Exception) {}
}

fun main(args: Array<String> = emptyArray()) {
    val forceNoTransparent = args.contains("--no-transparent")
    val isWin = System.getProperty("os.name")?.lowercase()?.contains("win") == true
    if (isWin) {
        try { System.setProperty("sun.java2d.opengl", "false") } catch (_: Throwable) {}
        try { System.setProperty("sun.java2d.d3d", "true") } catch (_: Throwable) {}
    }
    try { RenderCompat.apply(args) } catch (_: Throwable) {}
    if (forceNoTransparent) {
        try { System.setProperty("awt.useSystemAAFontSettings", "on") } catch (_: Throwable) {}
    }
    if (args.contains("--clean-appdata") || args.contains("--uninstall-cleanup")) {
        try {
            io.github.immaghzbad.aetherst.platform.UninstallCleanup.cleanAllData()
            val tmp = File(System.getProperty("java.io.tmpdir"), "AetherST")
            if (tmp.exists()) tmp.walkBottomUp().forEach { it.delete() }
        } catch (_: Throwable) {}
        exitProcess(0)
    }
    fun emergencyLog(msg: String) {
        val line = "[${java.util.Date()}] [BOOT] $msg | java=${System.getProperty("java.version")} os=${System.getProperty("os.name")}\n"
        try { System.err.println(line) } catch (_: Throwable) {}
        try { System.out.println(line) } catch (_: Throwable) {}
        for (path in listOf(
            File(System.getProperty("java.io.tmpdir"), "AetherST/aetherst-boot.log"),
            File(System.getProperty("user.home"), "AetherST-boot.log"),
            File("C:/Temp/AetherST-boot.log")
        )) {
            try { path.parentFile?.mkdirs(); path.appendText(line) } catch (_: Throwable) {}
        }
    }
    emergencyLog("main() entered")
    try {
        File(System.getProperty("java.io.tmpdir"), "AetherST").mkdirs()
    } catch (_: Throwable) {}
    try {
        io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.init()
    } catch (e: Throwable) {
        try {
            System.err.println("DesktopLogger init failed: ${e.message}")
            e.printStackTrace()
            File(System.getProperty("java.io.tmpdir"), "AetherST/aetherst-boot.log")
                .appendText("[BOOT] DesktopLogger init failed: ${e.stackTraceToString().take(2000)}\n")
        } catch (_: Throwable) {}
    }
    try {
        io.github.immaghzbad.aetherst.shared.data.LogRepository.fileLogWriter = { level, tag, msg ->
            io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.log(level.name, tag, msg)
        }
    } catch (_: Throwable) {}
    try {
        io.github.immaghzbad.aetherst.platform.UninstallCleanup.handleStartupCleanup()
    } catch (_: Throwable) {}
    Thread.setDefaultUncaughtExceptionHandler { thread, e ->
        try {
            val isVerifyError = e is VerifyError || e.cause is VerifyError
            val concise = if (isVerifyError) {
                "VerifyError in ${thread.name}: ${e.message?.lineSequence()?.firstOrNull()?.take(200) ?: e::class.simpleName} | cause=${e.cause?.message?.take(150)} | hint=ProGuard/OkHttp mismatch on Java ${System.getProperty("java.version")}"
            } else {
                val firstLine = e.stackTraceToString().lineSequence().take(12).joinToString(" | ").take(1200)
                "${e::class.simpleName}: ${e.message?.take(300)} | $firstLine"
            }
            if (isVerifyError) {
                io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Uncaught", concise)
            } else {
                io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Uncaught", concise)
            }
            val crashFile = File(System.getProperty("java.io.tmpdir"), "last_crash.log")
            crashFile.writeText(e.stackTraceToString().take(8000))
            try {
                File(System.getProperty("java.io.tmpdir"), "AetherST/aetherst-boot.log")
                    .appendText("[UNCAUGHT] $concise\n")
            } catch (_: Throwable) {}
        } catch (_: Throwable) {
            try { e.printStackTrace() } catch (_: Throwable) {}
        }
    }
    try {
        io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Application starting | heal network")
    } catch (_: Throwable) {}
    try { System.err.println("[BOOT] AetherST starting heal network") } catch (_: Throwable) {}
    try { NetworkHealer.heal() } catch (e: Throwable) {
        try {
            io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "NetworkHealer failed: ${e.message}")
            File(System.getProperty("java.io.tmpdir"), "AetherST/aetherst-boot.log").appendText("[BOOT] NetworkHealer failed: ${e.stackTraceToString().take(2000)}\n")
        } catch (_: Throwable) {}
    }

    var bound = false
    for (attempt in 0..7) {
        try {
            val s = ServerSocket(18195)
            lockSocket = s
            SingleInstanceLock.socket = s
            bound = true
            break
        } catch (_: Exception) {
            if (attempt >= 7) break
            try { Thread.sleep(350) } catch (_: Exception) {}
        }
    }
    if (!bound) {
        try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Another instance is running - signaling to show window and exiting") } catch (_: Throwable) {}
        runCatching {
            java.net.Socket("127.0.0.1", 18195).use { socket ->
                socket.getOutputStream().write(1)
                socket.getOutputStream().flush()
            }
        }
        exitProcess(0)
    }
    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Single instance lock acquired on port 18195") } catch (_: Throwable) {}

    Thread({
        while (true) {
            val client = try { lockSocket?.accept() ?: break } catch (_: Exception) { break }
            runCatching { client.close() }
            try {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Received show-window signal from second instance") } catch (_: Throwable) {}
                SwingUtilities.invokeLater { requestShowWindow?.invoke() }
            } catch (e: Exception) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Failed to invoke show window: ${e.message}") } catch (_: Throwable) {}
            }
        }
    }, "Aether-SingleInstance").apply { isDaemon = true }.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            SingleInstanceLock.release()
            runCatching { lockSocket?.close() }
            AetherTray.uninstall()
            sweepChildProcesses()
            cleanTempFiles()
        }
    )

    try { System.err.println("[BOOT] Entering Compose application block") } catch (_: Throwable) {}
    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Entering Compose application block") } catch (_: Throwable) {}
    try {
    application {
        val viewModelStoreOwner = remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }

        var isVisible by remember { mutableStateOf(true) }
        var showCloseDialog by remember { mutableStateOf(false) }
        var bringToFrontTrigger by remember { mutableStateOf(0) }

        val isWindows = remember { System.getProperty("os.name")?.lowercase()?.contains("win") == true }

        val useTransparentWindow = remember {
            try {
                if (forceNoTransparent) {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Transparency disabled via --no-transparent flag") } catch (_: Throwable) {}
                    false
                } else if (GraphicsEnvironment.isHeadless()) {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Headless environment - disabling transparent window") } catch (_: Throwable) {}
                    false
                } else {
                    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    val gd = ge.defaultScreenDevice
                    val gc = gd.defaultConfiguration
                    val capable = gc.isTranslucencyCapable
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Translucency capable=$capable isWindows=$isWindows") } catch (_: Throwable) {}
                    if (!capable) {
                        try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Translucency not capable - using decorated window fallback") } catch (_: Throwable) {}
                    }
                    capable
                }
            } catch (e: Throwable) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Failed to check translucency: ${e.message}") } catch (_: Throwable) {}
                false
            }
        }

        fun computeCenterPosition(): WindowPosition {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val maxBounds: Rectangle = ge.maximumWindowBounds
            val w = minOf(432, maxBounds.width)
            val h = minOf(784, maxBounds.height)
            val cx = maxBounds.x + (maxBounds.width - w) / 2
            val cy = maxBounds.y + (maxBounds.height - h) / 2
            return WindowPosition.Absolute(cx.dp, cy.dp)
        }

        val windowState = rememberWindowState(
            width = minOf(432, GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds.width).dp,
            height = minOf(784, GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds.height).dp,
            position = computeCenterPosition()
        )

        fun bringWindowToFront(composeWindow: ComposeWindow?) {
            try {
                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                val maxBounds: Rectangle = ge.maximumWindowBounds
                composeWindow?.let { w ->
                    val loc = w.location
                    val winW = w.width
                    val winH = w.height
                    val outOfBounds = loc.x + winW < maxBounds.x + 20 || loc.x > maxBounds.x + maxBounds.width - 20 || loc.y < maxBounds.y || loc.y + winH < maxBounds.y + 20 || loc.y > maxBounds.y + maxBounds.height - 20
                    if (outOfBounds) {
                        val clampedW = minOf(winW, maxBounds.width)
                        val clampedH = minOf(winH, maxBounds.height)
                        val cx = maxBounds.x + (maxBounds.width - clampedW) / 2
                        val cy = maxBounds.y + (maxBounds.height - clampedH) / 2
                        try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Window out of bounds location=$loc size=${w.size} maxBounds=$maxBounds - fixing to $cx,$cy") } catch (_: Throwable) {}
                        SwingUtilities.invokeLater {
                            try {
                                w.setLocation(cx, cy)
                                if (w.width != clampedW || w.height != clampedH) w.setSize(clampedW, clampedH)
                                windowState.position = WindowPosition.Absolute(cx.dp, cy.dp)
                                windowState.size = androidx.compose.ui.unit.DpSize(clampedW.dp, clampedH.dp)
                            } catch (_: Throwable) {}
                        }
                    }
                }
            } catch (e: Exception) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Bounds check failed: ${e.message}") } catch (_: Throwable) {}
            }
            try { windowState.isMinimized = false } catch (e: Exception) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Failed to unminimize windowState: ${e.message}") } catch (_: Throwable) {}
            }
            try {
                composeWindow?.let { w ->
                    SwingUtilities.invokeLater {
                        try {
                            if (!w.isVisible) {
                                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Window was not visible - forcing visible") } catch (_: Throwable) {}
                                w.isVisible = true
                            }
                            w.isMinimized = false
                            w.extendedState = java.awt.Frame.NORMAL
                            w.toFront()
                            w.requestFocus()
                            w.requestFocusInWindow()
                            w.isAlwaysOnTop = true
                            w.isAlwaysOnTop = false
                            w.toFront()
                            try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "bringWindowToFront executed visible=${w.isVisible} minimized=${w.isMinimized} showing=${w.isShowing} location=${w.location} size=${w.size}") } catch (_: Throwable) {}
                        } catch (e: Exception) {
                            try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "bringWindowToFront inner failed: ${e.message}") } catch (_: Throwable) {}
                            try { w.isVisible = true; w.isMinimized = false; w.toFront() } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "bringWindowToFront failed: ${e.message}") } catch (_: Throwable) {}
            }
        }

        remember {
            requestShowWindow = {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "requestShowWindow invoked isWindows=$isWindows") } catch (_: Throwable) {}
                SwingUtilities.invokeLater {
                    isVisible = true
                    bringToFrontTrigger++
                    try { windowState.isMinimized = false } catch (_: Exception) {}
                }
            }
            true
        }

        val trayActions = remember {
            TrayActions(
                onShowWindow = {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Tray onShowWindow clicked") } catch (_: Throwable) {}
                    SwingUtilities.invokeLater {
                        isVisible = true
                        bringToFrontTrigger++
                        try { windowState.isMinimized = false } catch (_: Exception) {}
                    }
                },
                onToggleConnection = {
                    val context = PlatformContext()
                    val currentStatus = ConnectionController.status.value
                    if (currentStatus == ConnectionStatus.RUNNING || currentStatus == ConnectionStatus.RECONNECTING) {
                        ConnectionController.getImpl(context).stop()
                    } else {
                        val config = AetherConfigRepository.getInstance(getSettings(context)).config.value
                        val isAdmin = getSystemUtils(context).isAdministrator()
                        if (config.connectionMode == ConnectionMode.TUNNEL && !isAdmin) {
                            SwingUtilities.invokeLater {
                                isVisible = true
                                windowState.isMinimized = false
                            }
                            TrayState.requestAdminDialog()
                        } else {
                            ConnectionController.getImpl(context).start()
                        }
                    }
                },
                onOpenSettings = {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Tray onOpenSettings clicked") } catch (_: Throwable) {}
                    SwingUtilities.invokeLater {
                        isVisible = true
                        try { windowState.isMinimized = false } catch (_: Exception) {}
                    }
                    TrayState.requestSettings()
                },
                onOpenRouting = {
                    SwingUtilities.invokeLater {
                        isVisible = true
                        try { windowState.isMinimized = false } catch (_: Exception) {}
                    }
                },
                onOpenLogs = {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Tray onOpenLogs clicked") } catch (_: Throwable) {}
                    val opened = io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.openInEditor()
                    if (!opened) {
                        try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Failed to open log file in editor") } catch (_: Throwable) {}
                    }
                },
                onExit = {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Tray onExit clicked - shutting down") } catch (_: Throwable) {}
                    val context = PlatformContext()
                    ConnectionController.getImpl(context).stop()
                    AetherTray.uninstall()
                    cleanTempFiles()
                    sweepChildProcesses()
                    SingleInstanceLock.release()
                    runCatching { lockSocket?.close() }
                    lockSocket = null
                    exitProcess(0)
                }
            )
        }

        LaunchedEffect(trayActions) {
            val installed = AetherTray.install(trayActions)
            try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Tray install result=$installed") } catch (_: Throwable) {}
        }

        LaunchedEffect(Unit) {
            ConnectionController.status.collect { status ->
                AetherTray.setConnectionState(
                    status == ConnectionStatus.RUNNING || status == ConnectionStatus.RECONNECTING
                )
            }
        }

        LaunchedEffect(Unit) {
            runCatching {
                kotlinx.coroutines.delay(2000)
                val context = PlatformContext()
                val s = getSettings(context)
                val onStart = s.getBoolean(AutoConnectSettings.PREF_AUTO_CONNECT_ON_START, false)
                val manual = s.getBoolean(AutoConnectSettings.PREF_MANUAL_DISCONNECT, false)
                if (!onStart || manual) return@runCatching
                val st = ConnectionController.status.value
                if (st == ConnectionStatus.RUNNING || st == ConnectionStatus.TUN_ACTIVE ||
                    st == ConnectionStatus.STARTING || st == ConnectionStatus.VALIDATING ||
                    st == ConnectionStatus.RECONNECTING) return@runCatching
                val config = AetherConfigRepository.getInstance(s).config.value
                if (config.connectionMode == ConnectionMode.TUNNEL &&
                    !getSystemUtils(context).isAdministrator()) {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Auto-connect on app start skipped: TUNNEL mode needs admin") } catch (_: Throwable) {}
                    return@runCatching
                }
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Auto-connecting on app start") } catch (_: Throwable) {}
                ConnectionController.getImpl(context).start()
            }
        }

        LaunchedEffect(isVisible) {
            try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "isVisible changed to $isVisible") } catch (_: Throwable) {}
            if (isVisible) {
                try { windowState.isMinimized = false } catch (e: Exception) {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Failed to unminimize on isVisible change: ${e.message}") } catch (_: Throwable) {}
                }
            }
        }

        LaunchedEffect(Unit) {
            if (isWindows) {
                kotlinx.coroutines.delay(400)
                if (!isVisible) {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Windows startup visibility check - window was invisible after 400ms, forcing visible") } catch (_: Throwable) {}
                    isVisible = true
                    try { windowState.isMinimized = false } catch (_: Exception) {}
                }
                kotlinx.coroutines.delay(1200)
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Windows startup check after 1.6s isVisible=$isVisible minimized=${windowState.isMinimized}") } catch (_: Throwable) {}
            }
        }

        Window(
            onCloseRequest = {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Window close requested - showing dialog isVisible=$isVisible") } catch (_: Throwable) {}
                showCloseDialog = true
            },
            title = "AetherST Tunnel",
            state = windowState,
            resizable = false,
            undecorated = useTransparentWindow,
            transparent = useTransparentWindow,
            visible = isVisible
        ) {
            LaunchedEffect(isVisible) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Window LaunchedEffect isVisible=$isVisible windowVisible=${window.isVisible} showing=${window.isShowing} minimized=${window.isMinimized}") } catch (_: Throwable) {}
                if (isVisible) bringWindowToFront(window)
            }
            LaunchedEffect(bringToFrontTrigger) {
                if (bringToFrontTrigger > 0) bringWindowToFront(window)
            }
            LaunchedEffect(Unit) {
                try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.i("Main", "Window composed undecorated=$useTransparentWindow transparent=$useTransparentWindow isWindows=$isWindows isVisible=$isVisible") } catch (_: Throwable) {}
                kotlinx.coroutines.delay(100)
                bringWindowToFront(window)
                kotlinx.coroutines.delay(250)
                if (isWindows && !window.isShowing) {
                    try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.w("Main", "Window not showing after 350ms - forcing show isVisible=$isVisible isShowing=${window.isShowing}") } catch (_: Throwable) {}
                    SwingUtilities.invokeLater {
                        try {
                            window.isVisible = true
                            window.isMinimized = false
                            window.extendedState = java.awt.Frame.NORMAL
                            window.toFront()
                            window.requestFocus()
                        } catch (e: Exception) {
                            try { io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Force show failed: ${e.message}") } catch (_: Throwable) {}
                        }
                    }
                }
                kotlinx.coroutines.delay(1500)
                bringWindowToFront(window)
            }
                CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (useTransparentWindow) Modifier.padding(8.dp) else Modifier)
                            .then(if (useTransparentWindow) Modifier.shadow(8.dp, RoundedCornerShape(16.dp)) else Modifier)
                            .then(if (useTransparentWindow) Modifier.clip(RoundedCornerShape(16.dp)) else Modifier)
                            .background(Color(0xFF1C1C1E))
                            .then(if (useTransparentWindow) Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)) else Modifier)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (useTransparentWindow) {
                            WindowDraggableArea {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(Color(0xFF1C1C1E))
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color(0xFF007AFF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "A",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "AetherST Tunnel",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .clickable { windowState.isMinimized = true }
                                                .background(Color.White.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Minimize",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .clickable { showCloseDialog = true }
                                                .background(Color(0xFFFF3B30).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = Color(0xFFFF3B30),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                App(PlatformContext())
                            }
                        }
                        if (showCloseDialog) {
                            val traySupported = remember(showCloseDialog) { AetherTray.isSupported() && AetherTray.isInstalled() }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .clickable(enabled = false) {},
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth().padding(16.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Close AetherST?",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (traySupported) "Hide to system tray or exit completely?" else "Do you want to exit AetherST Tunnel?",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (traySupported) {
                                                Button(
                                                    onClick = {
                                                        showCloseDialog = false
                                                        isVisible = false
                                                    },
                                                    modifier = Modifier.weight(1f).height(46.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF), contentColor = Color.White)
                                                ) {
                                                    Text("Hide to Tray", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    showCloseDialog = false
                                                    val context = PlatformContext()
                                                    ConnectionController.getImpl(context).stop()
                                                    AetherTray.uninstall()
                                                    cleanTempFiles()
                                                    sweepChildProcesses()
                                                    SingleInstanceLock.release()
                                                    runCatching { lockSocket?.close() }
                                                    lockSocket = null
                                                    exitProcess(0)
                                                },
                                                modifier = Modifier.weight(if (traySupported) 1f else 1f).height(46.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30), contentColor = Color.White)
                                            ) {
                                                Text("Exit", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        TextButton(
                                            onClick = { showCloseDialog = false },
                                            modifier = Modifier.fillMaxWidth().height(44.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Cancel", color = Color.White.copy(alpha = 0.6f), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Throwable) {
        try {
            System.err.println("[BOOT] Compose application crashed: ${e.message}")
            e.printStackTrace()
            io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.e("Main", "Compose application crashed: ${e.stackTraceToString().take(4000)}")
            File(System.getProperty("java.io.tmpdir"), "AetherST/aetherst-boot.log").appendText("[BOOT] Compose crash: ${e.stackTraceToString().take(4000)}\n")
            File(System.getProperty("java.io.tmpdir"), "last_crash.log").writeText(e.stackTraceToString().take(8000))
            javax.swing.JOptionPane.showMessageDialog(null, "AetherST failed to start:\n${e.message}\n\nLog: ${io.github.immaghzbad.aetherst.shared.desktop.DesktopLogger.getLogFilePath()}\nBoot log: ${System.getProperty("java.io.tmpdir")}/AetherST/aetherst-boot.log", "AetherST Error", javax.swing.JOptionPane.ERROR_MESSAGE)
        } catch (_: Throwable) {}
        throw e
    }
}
