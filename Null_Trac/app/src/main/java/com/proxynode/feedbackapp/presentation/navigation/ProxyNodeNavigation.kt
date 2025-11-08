package com.proxynode.feedbackapp.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.proxynode.feedbackapp.presentation.screen.admin.AdminScreen
import com.proxynode.feedbackapp.presentation.screen.feedback.FeedbackScreen
import com.proxynode.feedbackapp.presentation.screen.history.HistoryScreen
import com.proxynode.feedbackapp.presentation.screen.onboarding.OnboardingScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Welcome", Icons.Default.Feedback)
    object Feedback : Screen("feedback", "Feedback", Icons.Default.Feedback)
    object History : Screen("history", "History", Icons.Default.History)
    object Admin : Screen("admin", "Admin", Icons.Default.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyNodeNavigation(navController: NavHostController) {
    var showOnboarding by remember { mutableStateOf(true) }
    
    if (showOnboarding) {
        OnboardingScreen(
            onComplete = {
                showOnboarding = false
            }
        )
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val items = listOf(Screen.Feedback, Screen.History, Screen.Admin)
                    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Feedback.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Feedback.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Feedback.route) {
                    FeedbackScreen()
                }
                composable(Screen.History.route) {
                    HistoryScreen()
                }
                composable(Screen.Admin.route) {
                    AdminScreen()
                }
            }
        }
    }
}