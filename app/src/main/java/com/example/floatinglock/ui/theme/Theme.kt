package com.example.floatinglock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Graphite = Color(0xFF1A1C20)
val GraphiteRaised = Color(0xFF23262B)
val Steel = Color(0xFFD5D9E0)
val SteelDim = Color(0xFF9399A5)
val LedAmber = Color(0xFFFFB224)
val LedMint = Color(0xFF5BD6A0)

private val DarkColors = darkColorScheme(
    primary = Steel,
    onPrimary = Graphite,
    background = Color(0xFF131417),
    onBackground = Color(0xFFECEEF2),
    surface = GraphiteRaised,
    onSurface = Color(0xFFECEEF2),
    surfaceVariant = Color(0xFF2B2E35),
    onSurfaceVariant = SteelDim,
    outline = Color(0xFF30343B),
)

private val LightColors = lightColorScheme(
    primary = Graphite,
    onPrimary = Color.White,
    background = Color(0xFFEDEEF1),
    onBackground = Color(0xFF15171B),
    surface = Color.White,
    onSurface = Color(0xFF15171B),
    surfaceVariant = Color(0xFFE1E3E8),
    onSurfaceVariant = Color(0xFF5A606D),
    outline = Color(0xFFD3D6DD),
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
