package com.example.bitequest.ui.presentation.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Login : Screen("login", "Login", Icons.Outlined.Login)
    object Register : Screen("register", "Register", Icons.Default.PersonAdd)

    // تعريف المسار مع مكان لـ truckId
    object TruckDetails : Screen("truck_details/{truckId}", "Truck Details", Icons.Default.Info) {
        fun createRoute(truckId: String) = "truck_details/$truckId"
    }

    object AddReview : Screen("add_review/{truckId}", "Add Review", Icons.Default.Edit) {
        fun createRoute(truckId: String) = "add_review/$truckId"
    }

    object ManageTruck : Screen("manage_truck", "Manage Truck", Icons.Default.Edit)
}