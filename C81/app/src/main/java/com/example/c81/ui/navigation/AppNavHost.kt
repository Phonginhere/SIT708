// app/src/main/java/com/example/c81/ui/navigation/AppNavHost.kt
package com.example.c81.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.c81.ChatBotApplication
import com.example.c81.ui.chat.ChatScreen
import com.example.c81.ui.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val CHAT  = "chat/{username}"
    fun chat(username: String) = "chat/$username"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = { username ->
                navController.navigate(Routes.chat(username)) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val context = LocalContext.current
            val repository = (context.applicationContext as ChatBotApplication).chatRepository

            ChatScreen(
                username = username,
                repository = repository,
                onLogout = {
                    // Back to log in, removing chat from the back stack
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.CHAT) { inclusive = true }
                    }
                }
            )
        }
    }
}