package com.example.bitequest.ui.presentation.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.presentation.screens.navbar.FoodTruck
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TruckDetailsScreen(navController: NavHostController, truckId: String) {
    var truck by remember { mutableStateOf<FoodTruck?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    LaunchedEffect(truckId) {
        if (truckId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("foodTrucks")
                .document(truckId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        truck = FoodTruck(
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

            FirebaseFirestore.getInstance()
                .collection("foodTrucks")
                .document(truckId)
                .collection("reviews")
                .get()
                .addOnSuccessListener { result ->
                    reviews = result.map { reviewDoc ->
                        Review(
                            rating = reviewDoc.getDouble("rating") ?: 0.0,
                            comment = reviewDoc.getString("comment") ?: "",
                            timestamp = reviewDoc.getLong("timestamp") ?: 0
                        )
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (truck != null) {
            Text(text = truck!!.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = truck!!.menu, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = truck!!.operatingHours, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            if (reviews.isNotEmpty()) {
                Text(text = "Reviews:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                reviews.forEach { review ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Rating: ${review.rating}/5", style = MaterialTheme.typography.bodyMedium)
                            Text(text = review.comment, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(text = "No reviews yet.", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("${Screen.AddReview.route}/${truckId}") }) {
                Text("Leave a Review")
            }
        } else {
            Text("Loading truck details...")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Map")
        }
    }
}

data class Review(
    val rating: Double,
    val comment: String,
    val timestamp: Long
)