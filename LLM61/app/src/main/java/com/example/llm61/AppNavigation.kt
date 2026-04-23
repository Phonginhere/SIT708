package com.example.llm61

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.llm61.screens.*
import com.example.llm61.viewmodel.UserViewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val INTERESTS = "interests"
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val RESULTS = "results"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        // Forward navigation: slide in from the right + fade in
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        // Old screen exits: slide out to the left + fade out
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        // Back navigation: slide in from the left + fade in
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        // Back navigation: current screen exits to the right + fade out
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                userViewModel = userViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignupClick = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                userViewModel = userViewModel,
                onCreateAccountClick = {
                    // Go to Interests with fromHome=false (first-time onboarding path)
                    navController.navigate("${Routes.INTERESTS}?fromHome=false")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Interests route now takes a "fromHome" argument
        composable(
            route = "${Routes.INTERESTS}?fromHome={fromHome}",
            arguments = listOf(navArgument("fromHome") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val fromHome = backStackEntry.arguments?.getBoolean("fromHome") ?: false
            InterestsScreen(
                userViewModel = userViewModel,
                fromHome = fromHome,
                onNextClick = {
                    userViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = {
                    // From Home → just pop back. From Signup → skip to Home.
                    if (fromHome) {
                        navController.popBackStack()
                    } else {
                        userViewModel.completeOnboarding()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                userViewModel = userViewModel,
                onTaskClick = { taskId ->
                    navController.navigate("${Routes.QUIZ}?taskId=$taskId")
                },
                onEditInterestsClick = {
                    // From Home → go to Interests with fromHome=true
                    navController.navigate("${Routes.INTERESTS}?fromHome=true")
                },
                onLogoutClick = {
                    userViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Routes.QUIZ}?taskId={taskId}",
            arguments = listOf(navArgument("taskId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            QuizScreen(
                taskId = taskId,
                userViewModel = userViewModel,
                onSubmitClick = { navController.navigate(Routes.RESULTS) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.RESULTS) {
            ResultsScreen(
                userViewModel = userViewModel,
                onContinueClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}