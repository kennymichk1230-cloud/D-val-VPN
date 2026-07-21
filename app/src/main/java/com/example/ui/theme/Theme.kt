package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF000000),
    secondary = TechBlue,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = CrypticPurple,
    background = SpaceBackground,
    onBackground = Color(0xFFFFFFFF),
    surface = SlateSurface,
    onSurface = SoftGrayText,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = SoftGrayText
  )

private val LightColorScheme =
  darkColorScheme( // We prefer our dark slate look even in default/light setups for consistent branding
    primary = CyanPrimary,
    onPrimary = Color(0xFF000000),
    secondary = TechBlue,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = CrypticPurple,
    background = SpaceBackground,
    onBackground = Color(0xFFFFFFFF),
    surface = SlateSurface,
    onSurface = SoftGrayText,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = SoftGrayText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to preserve the premium custom Frosted Glass theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      else -> DarkColorScheme // Force our DarkColorScheme for consistent Frosted Glass branding
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
