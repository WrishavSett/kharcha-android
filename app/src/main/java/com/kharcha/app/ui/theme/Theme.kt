package com.kharcha.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kharcha.app.R
import com.kharcha.app.model.Category

/** The web version's design tokens, one-for-one. */
@Immutable
data class KharchaColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val muted: Color,
    val faint: Color,
    val line: Color,
    val accent: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val danger: Color,
    val warn: Color,
    val heroBg: Color,
    val heroInk: Color,
    val heroMuted: Color,
    val heroLine: Color,
    val heroField: Color,
    val heroFill: Color,
    val heroWarn: Color,
    val heroBad: Color,
    val food: Color,
    val groceries: Color,
    val transport: Color,
    val bills: Color,
    val shopping: Color,
    val health: Color,
    val entertainment: Color,
    val other: Color,
    val isDark: Boolean,
) {
    fun category(c: Category): Color = when (c) {
        Category.Food -> food
        Category.Groceries -> groceries
        Category.Transport -> transport
        Category.Bills -> bills
        Category.Shopping -> shopping
        Category.Health -> health
        Category.Fun -> entertainment
        Category.Other -> other
    }
}

val LightKharcha = KharchaColors(
    bg = Color(0xFFF1F4F3),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF6F8F7),
    ink = Color(0xFF14201C),
    muted = Color(0xFF5D6C67),
    faint = Color(0xFF8B9893),
    line = Color(0xFFE0E6E3),
    accent = Color(0xFF0B6B52),
    accentInk = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFE1F0EA),
    danger = Color(0xFFB3261E),
    warn = Color(0xFFA35F00),
    heroBg = Color(0xFF0F3B30),
    heroInk = Color(0xFFEEF7F2),
    heroMuted = Color(0xFFA6C4B8),
    heroLine = Color(0x24EEF7F2),
    heroField = Color(0x14EEF7F2),
    heroFill = Color(0xFF7FDCB5),
    heroWarn = Color(0xFFFFC56E),
    heroBad = Color(0xFFFF9D90),
    food = Color(0xFFD27A22),
    groceries = Color(0xFF3B8D4C),
    transport = Color(0xFF2C6DB0),
    bills = Color(0xFF7F55BD),
    shopping = Color(0xFFC03F78),
    health = Color(0xFF1F9696),
    entertainment = Color(0xFFA8901C),
    other = Color(0xFF6F7D78),
    isDark = false,
)

val DarkKharcha = LightKharcha.copy(
    bg = Color(0xFF0D1211),
    surface = Color(0xFF151C1A),
    surface2 = Color(0xFF1A2220),
    ink = Color(0xFFE4EBE8),
    muted = Color(0xFF98A6A1),
    faint = Color(0xFF6B7A75),
    line = Color(0xFF25302D),
    accent = Color(0xFF56C9A1),
    accentInk = Color(0xFF06231A),
    accentSoft = Color(0xFF17312A),
    danger = Color(0xFFF2877D),
    warn = Color(0xFFE6A446),
    heroBg = Color(0xFF133A30),
    heroLine = Color(0x1FEEF7F2),
    heroField = Color(0x12EEF7F2),
    food = Color(0xFFEC9A4B),
    groceries = Color(0xFF6BBF7A),
    transport = Color(0xFF6AA6E6),
    bills = Color(0xFFAE8EE6),
    shopping = Color(0xFFE57AA8),
    health = Color(0xFF55C6C6),
    entertainment = Color(0xFFD8C253),
    other = Color(0xFF9AA8A3),
    isDark = true,
)

val Bricolage = FontFamily(
    Font(R.font.bricolage_regular, FontWeight.Normal),
    Font(R.font.bricolage_bold, FontWeight.Bold),
)

val PlexMono = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_bold, FontWeight.Bold),
)

/** Display type is Bricolage Grotesque, figures are IBM Plex Mono, body text is the system font. */
object KType {
    val brand = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = (-0.03).em)
    val h2 = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = (-0.01).em)
    val eyebrow = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.09.em)
    val body = TextStyle(fontSize = 15.sp, lineHeight = 22.sp)
    val small = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
    val mono = TextStyle(fontFamily = PlexMono, fontSize = 15.sp, fontFeatureSettings = "tnum")
}

private val LocalKharchaColors = staticCompositionLocalOf { LightKharcha }

object Kharcha {
    val colors: KharchaColors
        @Composable @ReadOnlyComposable get() = LocalKharchaColors.current
}

private fun schemeFor(c: KharchaColors): ColorScheme = if (c.isDark) darkScheme(c) else lightScheme(c)

private fun lightScheme(c: KharchaColors) = lightColorScheme(
    primary = c.accent, onPrimary = c.accentInk,
    primaryContainer = c.accentSoft, onPrimaryContainer = c.accent,
    secondary = c.accent, onSecondary = c.accentInk,
    secondaryContainer = c.accentSoft, onSecondaryContainer = c.accent,
    background = c.bg, onBackground = c.ink,
    surface = c.surface, onSurface = c.ink,
    surfaceVariant = c.surface2, onSurfaceVariant = c.muted,
    surfaceTint = c.surface,
    surfaceContainerLowest = c.surface, surfaceContainerLow = c.surface,
    surfaceContainer = c.surface, surfaceContainerHigh = c.surface,
    surfaceContainerHighest = c.surface2,
    inverseSurface = c.ink, inverseOnSurface = c.bg, inversePrimary = c.heroFill,
    outline = c.line, outlineVariant = c.line,
    error = c.danger, onError = c.surface,
)

private fun darkScheme(c: KharchaColors) = darkColorScheme(
    primary = c.accent, onPrimary = c.accentInk,
    primaryContainer = c.accentSoft, onPrimaryContainer = c.accent,
    secondary = c.accent, onSecondary = c.accentInk,
    secondaryContainer = c.accentSoft, onSecondaryContainer = c.accent,
    background = c.bg, onBackground = c.ink,
    surface = c.surface, onSurface = c.ink,
    surfaceVariant = c.surface2, onSurfaceVariant = c.muted,
    surfaceTint = c.surface,
    surfaceContainerLowest = c.surface, surfaceContainerLow = c.surface,
    surfaceContainer = c.surface, surfaceContainerHigh = c.surface,
    surfaceContainerHighest = c.surface2,
    inverseSurface = c.ink, inverseOnSurface = c.bg, inversePrimary = LightKharcha.accent,
    outline = c.line, outlineVariant = c.line,
    error = c.danger, onError = c.surface,
)

@Composable
fun KharchaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkKharcha else LightKharcha
    CompositionLocalProvider(LocalKharchaColors provides colors) {
        MaterialTheme(colorScheme = schemeFor(colors), content = content)
    }
}
