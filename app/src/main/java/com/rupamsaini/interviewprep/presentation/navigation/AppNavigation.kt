package com.rupamsaini.interviewprep.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rupamsaini.interviewprep.presentation.home.HomeScreen
import com.rupamsaini.interviewprep.presentation.question.QuestionScreen

import com.rupamsaini.interviewprep.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Question : Screen("question/{questionId}") {
        fun createRoute(questionId: Long) = "question/$questionId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onQuestionClick = { id ->
                    navController.navigate(Screen.Question.createRoute(id))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Question.route,
            arguments = listOf(navArgument("questionId") { type = NavType.LongType })
        ) {
            QuestionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
