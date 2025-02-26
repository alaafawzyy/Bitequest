package com.example.bitequest.ui.presentation.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Login : Screen("login", "Login", Icons.Outlined.Login) // استخدمت Outlined.Login كبديل
    object Register : Screen("register", "Register", Icons.Default.PersonAdd)
    object TruckDetails : Screen("truck_details/{truckId}", "Truck Details", Icons.Default.Info)
    object AddReview : Screen("add_review/{truckId}", "Add Review", Icons.Default.Star)
    object ManageTruck : Screen("manage_truck", "Manage Truck", Icons.Default.Edit)
}