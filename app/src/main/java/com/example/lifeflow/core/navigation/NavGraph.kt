package com.example.lifeflow.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifeflow.features.analytics.AnalyticsScreen
import com.example.lifeflow.features.auth.ForgotPasswordScreen
import com.example.lifeflow.features.auth.LoginScreen
import com.example.lifeflow.features.auth.RegisterScreen
import com.example.lifeflow.features.calendar.AddEventScreen
import com.example.lifeflow.features.calendar.CalendarScreen
import com.example.lifeflow.features.calendar.EventDetailsScreen
import com.example.lifeflow.features.goals.AddGoalScreen
import com.example.lifeflow.features.goals.GoalDetailsScreen
import com.example.lifeflow.features.goals.GoalsScreen
import com.example.lifeflow.features.home.HomeScreen
import com.example.lifeflow.features.notes.NoteDetailsScreen
import com.example.lifeflow.features.notes.NotesScreen
import com.example.lifeflow.features.notes.QuickNoteScreen
import com.example.lifeflow.features.notifications.NotificationsScreen
import com.example.lifeflow.features.onboarding.OnboardingScreen
import com.example.lifeflow.features.profile.ProfileScreen
import com.example.lifeflow.features.settings.SettingsScreen
import com.example.lifeflow.features.splash.SplashScreen
import com.example.lifeflow.features.tasks.AddTaskScreen
import com.example.lifeflow.features.tasks.EditTaskScreen
import com.example.lifeflow.features.tasks.TaskDetailsScreen
import com.example.lifeflow.features.tasks.TasksScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Tasks.route) {
            TasksScreen(navController = navController)
        }
        composable(Screen.AddTask.route) {
            AddTaskScreen(navController = navController)
        }
        composable(
            route = Screen.TaskDetails.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailsScreen(taskId = taskId, navController = navController)
        }
        composable(
            route = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            EditTaskScreen(taskId = taskId, navController = navController)
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }
        composable(Screen.AddEvent.route) {
            AddEventScreen(navController = navController)
        }
        composable(
            route = Screen.EventDetails.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailsScreen(eventId = eventId, navController = navController)
        }

        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController)
        }
        composable(Screen.AddGoal.route) {
            AddGoalScreen(navController = navController)
        }
        composable(
            route = Screen.GoalDetails.route,
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
            GoalDetailsScreen(goalId = goalId, navController = navController)
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }

        composable(Screen.Notes.route) {
            NotesScreen(navController = navController)
        }
        composable(Screen.QuickNote.route) {
            QuickNoteScreen(navController = navController)
        }
        composable(
            route = Screen.NoteDetails.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteDetailsScreen(noteId = noteId, navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
    }
}
