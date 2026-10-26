package net.qatar4u.mobile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.qatar4u.mobile.R
import net.qatar4u.mobile.data.Accent

object Q4u {
    val Background = Color(0xFFF5F8FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceHigh = Color(0xFFF0E8EB)
    val Gold = Color(0xFFC6A15B)
    val GoldLight = Color(0xFFFFE4A3)
    val Maroon = Color(0xFF8A1538)
    val Pearl = Color(0xFFF5F8FA)
    val TextPrimary = Color(0xFF461025)
    val TextSecondary = Color(0xFF6E5C64)
    val Danger = Color(0xFFAD203D)
}

fun Accent.color(): Color = when (this) {
    Accent.GOLD -> Q4u.Gold
    Accent.MAROON -> Color(0xFFC23A62)
    Accent.PEARL -> Q4u.Pearl
}

val LocalAccent = staticCompositionLocalOf { Q4u.Gold }

val Cairo = FontFamily(
    Font(R.font.cairo_regular, FontWeight(400)),
    Font(R.font.cairo_semibold, FontWeight(600)),
    Font(R.font.cairo_bold, FontWeight(700)),
    Font(R.font.cairo_extrabold, FontWeight(800)),
)

@Composable
fun Q4uTheme(accent: Color, content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    val t = base.copy(
        displayLarge = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(700), fontSize = 88.sp, lineHeight = 96.sp),
        headlineMedium = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(700), fontSize = 30.sp),
        titleLarge = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(700), fontSize = 24.sp),
        titleMedium = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(600), fontSize = 20.sp),
        bodyLarge = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(400), fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(400), fontSize = 16.sp),
        labelLarge = TextStyle(fontFamily = Cairo, fontWeight = FontWeight(600), fontSize = 14.sp),
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Q4u.Maroon,
            onPrimary = Color.White,
            background = Q4u.Background,
            surface = Q4u.Surface,
            onSurface = Q4u.TextPrimary,
            onBackground = Q4u.TextPrimary,
            secondary = Color(0xFF805D1B),
            onSecondary = Color.White,
            primaryContainer = Q4u.SurfaceHigh,
            onPrimaryContainer = Q4u.TextPrimary,
            secondaryContainer = Color(0xFFF2E5C9),
            onSecondaryContainer = Q4u.TextPrimary,
            surfaceVariant = Color(0xFFF0E8EB),
            onSurfaceVariant = Q4u.TextSecondary,
            outline = Color(0xFF9B793C),
            outlineVariant = Q4u.Gold,
            error = Q4u.Danger,
        ),
        typography = t,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalAccent provides accent, content = content)
    }
}
