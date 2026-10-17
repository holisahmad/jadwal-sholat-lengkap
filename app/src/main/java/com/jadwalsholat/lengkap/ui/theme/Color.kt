package com.jadwalsholat.lengkap.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NurColors(
    val bg: Color,
    val surface: Color,
    val raised: Color,
    val border: Color,
    val fg: Color,
    val muted: Color,
    val faint: Color,
    val primary: Color,
    val primaryFg: Color,
    val danger: Color,
    val gold: Color,
    val isDark: Boolean
)

// Palette Mode Gelap (Nocturnal Pine)
val DarkNurColors = NurColors(
    bg = Color(0xFF0C1210),
    surface = Color(0xFF151C19),
    raised = Color(0xFF1C2622),
    border = Color(0xFF2A3530),
    fg = Color(0xFFE8ECE8),
    muted = Color(0xFF8A968C),
    faint = Color(0xFF5D6B62),
    primary = Color(0xFF5D9B7E),
    primaryFg = Color(0xFF06110C),
    danger = Color(0xFFC47A72),
    gold = Color(0xFFD4AF37),
    isDark = true
)

// Palette Mode Terang (Warm Mushaf / Daylight)
val LightNurColors = NurColors(
    bg = Color(0xFFF7FAF8),
    surface = Color(0xFFFFFFFF),
    raised = Color(0xFFEEF3EF),
    border = Color(0xFFD6E0D9),
    fg = Color(0xFF16211C),
    muted = Color(0xFF5C6E64),
    faint = Color(0xFF899B90),
    primary = Color(0xFF267352),
    primaryFg = Color(0xFFFFFFFF),
    danger = Color(0xFFB8453D),
    gold = Color(0xFFB8860B),
    isDark = false
)

val LocalNurColors = staticCompositionLocalOf { DarkNurColors }

// Dynamic properties that react automatically to theme changes everywhere in Compose!
val NurBg: Color @Composable get() = LocalNurColors.current.bg
val NurSurface: Color @Composable get() = LocalNurColors.current.surface
val NurRaised: Color @Composable get() = LocalNurColors.current.raised
val NurBorder: Color @Composable get() = LocalNurColors.current.border
val NurFg: Color @Composable get() = LocalNurColors.current.fg
val NurMuted: Color @Composable get() = LocalNurColors.current.muted
val NurFaint: Color @Composable get() = LocalNurColors.current.faint
val NurPrimary: Color @Composable get() = LocalNurColors.current.primary
val NurPrimaryFg: Color @Composable get() = LocalNurColors.current.primaryFg
val NurDanger: Color @Composable get() = LocalNurColors.current.danger
val NurGold: Color @Composable get() = LocalNurColors.current.gold

// Backward compatibility aliases
val EmeraldPrimary: Color @Composable get() = NurPrimary
val EmeraldDark: Color @Composable get() = NurRaised
val EmeraldLight: Color @Composable get() = NurPrimary.copy(alpha = 0.8f)
val EmeraldContainer: Color @Composable get() = NurRaised
val GoldAccent: Color @Composable get() = NurGold
val BackgroundDark: Color @Composable get() = NurBg
val SurfaceDark: Color @Composable get() = NurSurface
val SurfaceVariantDark: Color @Composable get() = NurRaised
val TextPrimaryDark: Color @Composable get() = NurFg
val TextSecondaryDark: Color @Composable get() = NurMuted
val TextMutedDark: Color @Composable get() = NurFaint
val GreenActive: Color @Composable get() = NurPrimary
val BorderColorDark: Color @Composable get() = NurBorder
