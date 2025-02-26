package com.example.bitequest

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.presentation.navigation.SetupNavGraph
import com.example.bitequest.ui.theme.BiteQuestTheme
import com.example.bitequest.ui.theme.backgroundColor
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContent {
            BiteQuestTheme {
                val navController = rememberNavController()
                val startDestination = if (auth.currentUser != null) Screen.Home.route else Screen.Login.route
                AppContent(navController, this, startDestination)
            }
        }
    }
}

@Composable
fun AppContent(navController: NavHostController, context: Context, startDestination: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            if (currentRoute !in listOf(Screen.Login.route, Screen.Register.route)) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        SetupNavGraph(navController, startDestination, context, Modifier.padding(innerPadding))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.ManageTruck)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = backgroundColor) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = null,
                        tint = if (currentRoute == screen.route) Color(0xFFFFD700) else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        color = if (currentRoute == screen.route) Color(0xFFFFD700) else Color.Gray
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD700),
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color(0xFFFFD700),
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Black
                )
            )
        }
    }
}
