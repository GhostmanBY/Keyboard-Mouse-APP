package com.kbdmouse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF5B6DF5)
private val IndigoDark = Color(0xFFB8BEFC)
private val Slate = Color(0xFF232532)
private val SlateLight = Color(0xFFF2F3F8)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    secondary = IndigoDark,
    surface = SlateLight,
    onSurface = Color(0xFF191A22),
)

private val DarkColors = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color(0xFF101118),
    secondary = Indigo,
    surface = Slate,
    onSurface = Color(0xFFECEDF4),
)

@Composable
fun KbdMouseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}
