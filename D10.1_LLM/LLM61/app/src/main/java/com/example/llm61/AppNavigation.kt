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
import androidx.navigation.navDeepLink
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

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
    const val SHARED_PROFILE = "shared_profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    val context = LocalContext.current
    DisposableEffect(navController) {
        val activity = context as ComponentActivity
        val listener = androidx.core.util.Consumer<Intent> { intent ->
            navController.handleDeepLink(intent)
        }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

    if (!userViewModel.isSessionChecked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (userViewModel.isLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
            )
        }

        composable(Routes.UPGRADE) {
            UpgradeScreen(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.SHARED_PROFILE}?data={data}",
            arguments = listOf(navArgument("data") {
                type = NavType.StringType
                defaultValue = ""
            }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "llm61://profile?data={data}" }
            )
        ) { backStackEntry ->
            val data = backStackEntry.arguments?.getString("data") ?: ""
            SharedProfileScreen(
                sharedData = data,
                onBackClick = {
                    val previous = navController.previousBackStackEntry
                    val previousRoute = previous?.destination?.route
                    // If we got here from a real screen (warm), just pop back to it.
                    // Otherwise (cold deep-link), go to HOME if authed, LOGIN otherwise.
                    if (previousRoute != null && previousRoute != Routes.LOGIN) {
                        navController.popBackStack()
                    } else {
                        val destination = if (userViewModel.isLoggedIn) Routes.HOME else Routes.LOGIN
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}