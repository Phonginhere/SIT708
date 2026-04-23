package com.example.llm61.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = OnSurfaceLight,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = OnBrand,
    secondary = BrandGreenLight,
    onSecondary = OnSurfaceLight,
    tertiary = BrandAccent,
    background = SurfaceDark,
    onBackground = OnSurfaceDarkMode,
    surface = SurfaceDark,
    onSurface = OnSurfaceDarkMode,
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = OnBrand,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = OnSurfaceLight,
    secondary = BrandGreen,
    onSecondary = OnBrand,
    secondaryContainer = BrandGreenLight,
    tertiary = BrandAccent,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun LLM61Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false to always use our brand colors, regardless of Android 12+ dynamic theming
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}