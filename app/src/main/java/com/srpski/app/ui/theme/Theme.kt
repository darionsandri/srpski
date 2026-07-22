package com.srpski.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Palette drawn from the Serbian tricolor, softened for daily reading:
// deep plava for primary actions, crvena reserved for wrong answers,
// and a warm off-white ground.
private val Plava = Color(0xFF1D4E89)
private val PlavaLight = Color(0xFFDCE8F7)
private val PlavaDark = Color(0xFF5A83B5)
private val Crvena = Color(0xFFC6363C)
private val CrvenaDark = Color(0xFFE57373)
private val Zelena = Color(0xFF2E7D32)
private val ZelenaDark = Color(0xFF7BC47F)
private val Ground = Color(0xFFFAF8F5)
private val InkDark = Color(0xFF12181F)
private val SurfaceDark = Color(0xFF1B222C)

val CorrectGreen = Zelena
val WrongRed = Crvena

private val LightScheme = lightColorScheme(
    primary = Plava,
    onPrimary = Color.White,
    primaryContainer = PlavaLight,
    onPrimaryContainer = Plava,
    secondary = Zelena,
    error = Crvena,
    background = Ground,
    surface = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = PlavaDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF16324D),
    onPrimaryContainer = PlavaLight,
    secondary = ZelenaDark,
    error = CrvenaDark,
    background = InkDark,
    surface = SurfaceDark
)

@Composable
fun SrpskiTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        useDarkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
