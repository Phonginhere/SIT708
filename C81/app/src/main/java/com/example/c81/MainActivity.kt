// app/src/main/java/com/example/c81/MainActivity.kt
package com.example.c81

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.c81.ui.navigation.AppNavHost
import com.example.c81.ui.theme.C81Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            C81Theme {
                AppNavHost()
            }
        }
    }
}