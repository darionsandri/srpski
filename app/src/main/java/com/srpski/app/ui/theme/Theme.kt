package com.srpski.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette drawn from the Serbian tricolor, softened for daily reading:
// deep plava for primary actions, crvena reserved for wrong answers,
// and a warm off-white ground.
private val Plava = Color(0xFF1D4E89)
private val PlavaLight = Color(0xFFDCE8F7)
private val Crvena = Color(0xFFC6363C)
private val Zelena = Color(0xFF2E7D32)
private val Ground = Color(0xFFFAF8F5)

val CorrectGreen = Zelena
val WrongRed = Crvena

private val ColorScheme = lightColorScheme(
    primary = Plava,
    onPrimary = Color.White,
    primaryContainer = PlavaLight,
    onPrimaryContainer = Plava,
    secondary = Zelena,
    error = Crvena,
    background = Ground,
    surface = Color.White
)

@Composable
fun SrpskiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, content = content)
}
