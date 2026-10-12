package com.example.lifeflow.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    object Home : Screen("home")
    object Tasks : Screen("tasks")
    object TaskDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
    object AddTask : Screen("add_task")
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }

    object Calendar : Screen("calendar")
    object AddEvent : Screen("add_event")
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String) = "event_details/$eventId"
    }

    object Goals : Screen("goals")
    object AddGoal : Screen("add_goal")
    object GoalDetails : Screen("goal_details/{goalId}") {
        fun createRoute(goalId: String) = "goal_details/$goalId"
    }

    object Analytics : Screen("analytics")

    object Notes : Screen("notes")
    object QuickNote : Screen("quick_note")
    object NoteDetails : Screen("note_details/{noteId}") {
        fun createRoute(noteId: String) = "note_details/$noteId"
    }

    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
}
