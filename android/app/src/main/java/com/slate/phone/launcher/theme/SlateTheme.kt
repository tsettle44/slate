package com.slate.phone.launcher.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object SlateColors {
    val Background = Color(0xFF0F0F0F)
    val TextPrimary = Color(0xFFE8E4DF)
    val TextSecondary = Color(0xFF6B6560)
    val Accent = Color(0xFFC4A882)
    val Divider = Color(0xFF2A2A2A)

    val DaylightBackground = Color(0xFFF5F2ED)
    val DaylightTextPrimary = Color(0xFF2C2825)
    val DaylightTextSecondary = Color(0xFF8A847C)
}

object SlateTypography {
    val Clock = TextStyle(
        fontSize = 56.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-1).sp,
    )
    val Date = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
    )
    val AppName = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
    )
    val SectionLabel = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
    )
    val Countdown = TextStyle(
        fontSize = 72.sp,
        fontWeight = FontWeight.Light,
    )
}

enum class SlateThemeMode {
    PAPER_NIGHT,
    DAYLIGHT,
}

private val LocalSlateThemeMode = staticCompositionLocalOf { SlateThemeMode.PAPER_NIGHT }

private val PaperNightScheme = darkColorScheme(
    background = SlateColors.Background,
    onBackground = SlateColors.TextPrimary,
    primary = SlateColors.Accent,
    onPrimary = SlateColors.Background,
    surface = SlateColors.Background,
    onSurface = SlateColors.TextPrimary,
    outline = SlateColors.Divider,
)

private val DaylightScheme = lightColorScheme(
    background = SlateColors.DaylightBackground,
    onBackground = SlateColors.DaylightTextPrimary,
    primary = SlateColors.Accent,
    onPrimary = SlateColors.DaylightBackground,
    surface = SlateColors.DaylightBackground,
    onSurface = SlateColors.DaylightTextPrimary,
    outline = Color(0xFFD8D2CA),
)

@Composable
fun SlateTheme(
    themeMode: SlateThemeMode = SlateThemeMode.PAPER_NIGHT,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        SlateThemeMode.PAPER_NIGHT -> PaperNightScheme
        SlateThemeMode.DAYLIGHT -> DaylightScheme
    }

    CompositionLocalProvider(LocalSlateThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

@Composable
fun slateSecondaryTextColor(): Color {
    return when (LocalSlateThemeMode.current) {
        SlateThemeMode.PAPER_NIGHT -> SlateColors.TextSecondary
        SlateThemeMode.DAYLIGHT -> SlateColors.DaylightTextSecondary
    }
}

@Composable
fun slatePrimaryFont(): FontFamily = FontFamily.SansSerif
