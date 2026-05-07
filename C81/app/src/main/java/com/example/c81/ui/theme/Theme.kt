// app/src/main/java/com/example/c81/ui/theme/Theme.kt
package com.example.c81.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = CyanTop,
    secondary = ButtonGreen,
    background = ChatBackground,
    surface = ChatBackground
)

private val DarkColors = darkColorScheme(
    primary = CyanTop,
    secondary = ButtonGreen
)

@Composable
fun C81Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // dynamicColor disabled so our wireframe palette applies consistently
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,   // keep using whatever Typography exists in Type.kt
        content     = content
    )
}