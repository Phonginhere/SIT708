package com.example.credittask31

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.credittask31.ui.theme.CreditTask31Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            CreditTask31Theme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "welcome") {

                        composable(
                            route = "welcome?name={returnedName}",
                            //from result screen is passed as query parameter, so data from variable userName is passed as query parameter and data from variable returnedName is passed as argument
                            arguments = listOf(navArgument("returnedName") {
                                type = NavType.StringType
                                defaultValue = ""
                            })
                        ) { backStackEntry ->
                            val returnedName = backStackEntry.arguments?.getString("returnedName") ?: ""
                            WelcomeScreen(
                                returnedName = returnedName,
                                isDarkMode = isDarkMode,
                                onThemeToggle = { isDarkMode = it },
                                onStart = { name ->
                                    navController.navigate("quiz/$name")
                                }
                            )
                        }

                        composable(
                            route = "quiz/{userName}",
                            arguments = listOf(navArgument("userName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: ""
                            QuizScreen(
                                userName = userName,
                                isDarkMode = isDarkMode,
                                onThemeToggle = { isDarkMode = it },
                                onFinish = { score, total ->
                                    navController.navigate("result/$userName/$score/$total") {
                                        popUpTo("welcome") { inclusive = false }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "result/{userName}/{score}/{total}",
                            arguments = listOf(
                                navArgument("userName") { type = NavType.StringType },
                                navArgument("score") { type = NavType.IntType },
                                navArgument("total") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val userName = backStackEntry.arguments?.getString("userName") ?: ""
                            val score = backStackEntry.arguments?.getInt("score") ?: 0
                            val total = backStackEntry.arguments?.getInt("total") ?: 0
                            ResultScreen(
                                userName = userName,
                                score = score,
                                total = total,
                                isDarkMode = isDarkMode,
                                onThemeToggle = { isDarkMode = it },
                                onNewQuiz = {
                                    navController.navigate("welcome?name=$userName") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onFinish = {
                                    (navController.context as? ComponentActivity)?.finishAffinity()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}