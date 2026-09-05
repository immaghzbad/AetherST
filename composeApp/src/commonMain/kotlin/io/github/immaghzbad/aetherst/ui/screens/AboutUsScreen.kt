package io.github.immaghzbad.aetherst.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.immaghzbad.aetherst.platform.isDesktop
import io.github.immaghzbad.aetherst.shared.i18n.LocalAppStrings
import io.github.immaghzbad.aetherst.shared.ui.components.IosActionRow
import io.github.immaghzbad.aetherst.shared.ui.components.SectionCard
import io.github.immaghzbad.aetherst.shared.ui.components.AppDivider
import io.github.immaghzbad.aetherst.shared.ui.theme.AppPalette
import io.github.immaghzbad.aetherst.shared.ui.theme.appColors

private val IosActiveBlue = AppPalette.accent
private val IosActiveGreen = AppPalette.statusConnected
private val IosPurple = AppPalette.accentVariant

private const val AetherRepositoryUrl = "https://github.com/CluvexStudio/Aether"
private const val HevRepositoryUrl = "https://github.com/heiher/hev-socks5-tunnel"
private const val PsiphonRepositoryUrl = "https://github.com/Psiphon-Labs/psiphon-tunnel-core"

@Composable
fun AboutUsScreen(
    appVersion: String = "1.0.0",
    bottomContentPadding: Dp = 0.dp
) {
    val strings = LocalAppStrings.current
    val uriHandler = LocalUriHandler.current
    val colors = appColors()
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + if (isDesktop) 16.dp else 12.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = topPadding,
                bottom = bottomContentPadding + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { AboutHero(appVersion = appVersion) }
            item {
                SectionCard {
                    SectionTitle(strings.ABOUT_OVERVIEW)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = strings.ABOUT_OVERVIEW_DESC,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            item {
                SectionCard {
                    SectionTitle(strings.ABOUT_WHY)
                    InfoRow(
                        title = strings.ABOUT_WHY_CENSORSHIP,
                        description = strings.ABOUT_WHY_CENSORSHIP_DESC
                    )
                    AppDivider()
                    InfoRow(
                        title = strings.ABOUT_WHY_HYBRID,
                        description = strings.ABOUT_WHY_HYBRID_DESC
                    )
                    AppDivider()
                    InfoRow(
                        title = strings.ABOUT_WHY_GATEWAY,
                        description = strings.ABOUT_WHY_GATEWAY_DESC
                    )
                    AppDivider()
                    InfoRow(
                        title = strings.ABOUT_WHY_RECOVERY,
                        description = strings.ABOUT_WHY_RECOVERY_DESC
                    )
                }
            }
            item {
                SectionCard {
                    SectionTitle(strings.ABOUT_ARCHITECTURE)
                    InfoRow(
                        title = strings.ABOUT_ARCH_AETHER,
                        description = strings.ABOUT_ARCH_AETHER_DESC
                    )
                    AppDivider()
                    InfoRow(
                        title = strings.ABOUT_ARCH_HEV,
                        description = strings.ABOUT_ARCH_HEV_DESC
                    )
                    AppDivider()
                    InfoRow(
                        title = strings.ABOUT_ARCH_SOCKS,
                        description = strings.ABOUT_ARCH_SOCKS_DESC
                    )
                    AppDivider()
                    InfoRow(
                        title = strings.ABOUT_ARCH_PSIPHON,
                        description = strings.ABOUT_ARCH_PSIPHON_DESC
                    )
                }
            }
            item {
                SectionCard {
                    SectionTitle(strings.ABOUT_LINKS)
                    IosActionRow(
                        iconBg = IosActiveGreen.copy(alpha = 0.16f),
                        title = strings.ABOUT_LINK_AETHER,
                        subtitle = strings.ABOUT_LINK_AETHER_SUB,
                        onClick = { uriHandler.openUri(AetherRepositoryUrl) }
                    )
                    AppDivider()
                    IosActionRow(
                        iconBg = IosPurple.copy(alpha = 0.16f),
                        title = strings.ABOUT_LINK_HEV,
                        subtitle = strings.ABOUT_LINK_HEV_SUB,
                        onClick = { uriHandler.openUri(HevRepositoryUrl) }
                    )
                    AppDivider()
                    IosActionRow(
                        iconBg = IosActiveGreen.copy(alpha = 0.16f),
                        title = strings.ABOUT_LINK_PSIPHON,
                        subtitle = strings.ABOUT_LINK_PSIPHON_SUB,
                        onClick = { uriHandler.openUri(PsiphonRepositoryUrl) }
                    )
                }
            }
            item { AboutFooter() }
        }
    }
}

@Composable
private fun AboutHero(appVersion: String) {
    val strings = LocalAppStrings.current
    val colors = appColors()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.ABOUT_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            fontSize = 30.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = strings.ABOUT_SUBTITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionChip(label = strings.ABOUT_VERSION_APP, value = appVersion)
            VersionChip(label = strings.ABOUT_VERSION_AETHER, value = "1.9.0")
            VersionChip(label = strings.ABOUT_VERSION_HEV, value = "2.17.1")
        }
    }
}

@Composable
private fun VersionChip(label: String, value: String) {
    val colors = appColors()
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceRaised,
        border = BorderStroke(0.5.dp, colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoRow(title: String, description: String) {
    val colors = appColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = appColors()
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = colors.textSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun AboutFooter() {
    val strings = LocalAppStrings.current
    val colors = appColors()
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp))
    }
}
