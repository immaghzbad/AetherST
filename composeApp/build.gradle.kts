import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

val isAndroidDisabled = providers.gradleProperty("skipAndroid").getOrElse("false") == "true"

if (!isAndroidDisabled) {
    apply(plugin = "com.android.library")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    if (!isAndroidDisabled) {
        androidTarget {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            }
        }
    }

    jvm("desktop")

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalMultiplatform")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.okhttp)
            implementation(libs.okio)
            implementation(libs.moshi.kotlin)
            implementation(libs.kotlinx.serialization.json)
        }
        if (!isAndroidDisabled) {
            androidMain.dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidsvg)
            }
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

if (!isAndroidDisabled) {
    extensions.configure<LibraryExtension>("android") {
        namespace = "io.github.immaghzbad.aetherst.shared"
        compileSdk = 36
        defaultConfig {
            minSdk = 26
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}

val buildCloakWindows by tasks.registering(Exec::class) {
    group = "cloak"
    description = "Compile cloak_windows.c to cloak.exe for Windows package"
    val src = project.file("src/desktopMain/native/cloak_windows.c")
    val outDir = project.file("src/desktopMain/resources/bin")
    val outFile = File(outDir, "cloak.exe")
    val buildDir = project.file("build/cloak")
    outputs.file(outFile)
    inputs.file(src)
    isIgnoreExitValue = true
    notCompatibleWithConfigurationCache("Uses Exec with file copy at execution")
    doFirst {
        outDir.mkdirs()
        buildDir.mkdirs()
        if (!src.exists()) throw GradleException("cloak source missing: $src")
    }
    commandLine("cmd", "/c", "where gcc >nul 2>&1 && gcc -O2 -o \"${outFile.absolutePath}\" \"${src.absolutePath}\" -lws2_32 || where clang >nul 2>&1 && clang -O2 -o \"${outFile.absolutePath}\" \"${src.absolutePath}\" -lws2_32 || echo cloak compiler not found, using embedded Kotlin fallback")
    doLast {
        if (outFile.exists() && outFile.length() > 0) {
            outFile.copyTo(File(buildDir, outFile.name), overwrite = true)
            println("cloak.exe built: ${outFile.length()} bytes")
        } else {
            println("cloak.exe not built, embedded Kotlin relay will be used")
        }
    }
}
tasks.named("desktopProcessResources") { dependsOn(buildCloakWindows) }

compose.desktop {
    application {
        mainClass = "io.github.immaghzbad.aetherst.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "AetherY"
            packageVersion = "1.1.1"
            vendor = "AetherY"
            description = "AetherY High-Performance Proxy Tunnel"

            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/desktopMain/resources"))

            windows {
                dirChooser = true
                menu = true
                shortcut = true
                upgradeUuid = "d7d4c82e-6f8b-4a5f-8c31-97a2e3f6d4d1"
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }

            buildTypes.release.proguard {
                isEnabled.set(true)
                optimize.set(false)
                obfuscate.set(true)
                configurationFiles.from(project.file("proguard-rules.pro"))
            }
        }
    }
}


