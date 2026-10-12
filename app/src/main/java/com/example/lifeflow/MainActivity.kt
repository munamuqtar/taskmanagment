package com.example.lifeflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.lifeflow.core.common.UserPreferences
import com.example.lifeflow.core.navigation.BottomNavBar
import com.example.lifeflow.core.navigation.NavGraph
import com.example.lifeflow.core.navigation.QuickAddBottomSheet
import com.example.lifeflow.core.navigation.Screen
import com.example.lifeflow.core.theme.LifeFlowTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle notification permission result gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        setContent {
            val context = LocalContext.current
            val userPreferences = remember { UserPreferences(context) }
            val themePref by userPreferences.themePreference.collectAsState(initial = "system")

            val darkTheme = when (themePref) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            LifeFlowTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                var showQuickAddSheet by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            navController = navController,
                            onFabClick = { showQuickAddSheet = true }
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        NavGraph(navController = navController)
                    }
                }

                if (showQuickAddSheet) {
                    QuickAddBottomSheet(
                        onDismiss = { showQuickAddSheet = false },
                        onAddTask = { navController.navigate(Screen.AddTask.route) },
                        onAddEvent = { navController.navigate(Screen.AddEvent.route) },
                        onAddGoal = { navController.navigate(Screen.AddGoal.route) },
                        onAddNote = { navController.navigate(Screen.QuickNote.route) }
                    )
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
