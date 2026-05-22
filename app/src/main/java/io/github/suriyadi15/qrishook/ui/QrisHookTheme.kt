package io.github.suriyadi15.qrishook.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    secondary = Color(0xFF334155),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE5E7EB),
)

@Composable
fun QrisHookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content,
    )
}
