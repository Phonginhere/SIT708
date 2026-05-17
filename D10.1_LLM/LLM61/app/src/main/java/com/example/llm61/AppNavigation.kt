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
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val UPGRADE = "upgrade"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
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
                }
            )
        }

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
                    navController.navigate("${Routes.INTERESTS}?fromHome=true")
                },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onLogoutClick = { activity ->
                    userViewModel.logout(activity) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
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

        composable(Routes.HISTORY) {
            HistoryScreen(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onUpgradeClick = { navController.navigate(Routes.UPGRADE) },
                onShareClick = { /* TODO: wired in Phase 7 */ }
            )
        }

        composable(Routes.UPGRADE) {
            UpgradeScreen(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}