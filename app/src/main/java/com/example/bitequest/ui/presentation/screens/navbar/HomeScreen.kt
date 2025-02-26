package com.example.bitequest.ui.presentation.screens.navbar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bitequest.ui.presentation.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun HomeScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var foodTrucks by remember { mutableStateOf<List<FoodTruck>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("foodTrucks").get().addOnSuccessListener { result ->
            foodTrucks = result.map { doc ->
                FoodTruck(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    location = LatLng(
                        doc.getDouble("latitude") ?: 0.0,
                        doc.getDouble("longitude") ?: 0.0
                    ),
                    menu = doc.getString("menu") ?: "",
                    operatingHours = doc.getString("operatingHours") ?: ""
                )
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(37.7749, -122.4194), 12f
        )
    }

    Box(modifier = Modifier.height(300.dp)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            foodTrucks.forEach { truck ->
                Marker(
                    state = rememberMarkerState(position = truck.location),
                    title = truck.name,
                    snippet = truck.menu,
                    onClick = {
                        navController.navigate("${Screen.TruckDetails.route}/${truck.id}")
                        false
                    }
                )
            }
        }
    }
}

data class FoodTruck(
    val id: String,
    val name: String,
    val location: LatLng,
    val menu: String,
    val operatingHours: String
)