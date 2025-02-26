package com.example.bitequest.ui.presentation.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bitequest.ui.presentation.screens.navbar.HomeScreen
import com.example.bitequest.ui.presentation.screens.auth.LoginScreen
import com.example.bitequest.ui.presentation.screens.auth.RegisterScreen
import com.example.bitequest.ui.presentation.screens.client.TruckDetailsScreen
import com.example.bitequest.ui.presentation.screens.client.AddReviewScreen
import com.example.bitequest.ui.presentation.screens.navbar.ManageTruckScreen



@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String,
    context: Context,
    modifier: Modifier = Modifier
)  {
    NavHost(navController, startDestination = startDestination) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.TruckDetails.route) { backStackEntry ->
            val truckId = backStackEntry.arguments?.getString("truckId") ?: ""
            TruckDetailsScreen(navController, truckId)
        }
        composable(Screen.AddReview.route) { backStackEntry ->
            val truckId = backStackEntry.arguments?.getString("truckId") ?: ""
            AddReviewScreen(navController, truckId)
        }
        composable(Screen.ManageTruck.route) {
            ManageTruckScreen(navController)
        }
    }
}