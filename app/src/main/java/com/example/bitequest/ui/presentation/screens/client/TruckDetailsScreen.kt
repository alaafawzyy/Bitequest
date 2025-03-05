package com.example.bitequest.ui.presentation.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bitequest.R
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.theme.backgroundColor
import com.example.bitequest.ui.theme.darkPink40
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TruckDetailsScreen(navController: NavHostController, truckId: String) {
    val db = FirebaseFirestore.getInstance()
    var truck by remember { mutableStateOf<FoodTruck?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(truckId) {
        if (truckId.isNotEmpty()) {
            try {
                val doc = db.collection("foodTrucks").document(truckId).get().await()
                if (doc.exists()) {
                    truck = FoodTruck(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        location = LatLng(
                            doc.getDouble("latitude") ?: 0.0,
                            doc.getDouble("longitude") ?: 0.0
                        ),
                        menu = doc.getString("menu") ?: "N/A",
                        operatingHours = doc.getString("operatingHours") ?: "N/A"
                    )

                    val reviewDocs = db.collection("foodTrucks").document(truckId).collection("reviews").get().await()
                    reviews = reviewDocs.mapNotNull { reviewDoc ->
                        try {
                            Review(
                                rating = reviewDoc.getDouble("rating")?.toInt() ?: 0,
                                comment = reviewDoc.getString("comment") ?: "No comment",
                                timestamp = reviewDoc.getDate("timestamp")?.time ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else {
                    errorMessage = "Truck not found."
                }
            } catch (e: Exception) {
                errorMessage = "Error loading truck details: ${e.message}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back to Map")
                }
            }
        } else if (truck != null) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take remaining space
                ) {
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = truck!!.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = backgroundColor,
                                textAlign = TextAlign.Start,
                                fontSize = 24.sp
                            )
                            Image(
                                painter = painterResource(id = R.drawable.food_truck_icon_1),
                                contentDescription = "image",
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(60.dp)
                                    .padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(text = "Menu:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = truck!!.menu,
                            style = MaterialTheme.typography.bodyMedium,
                            color = darkPink40,
                            modifier = Modifier.padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text(text = "Operating Hours:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${truck!!.operatingHours} h",
                            style = MaterialTheme.typography.bodyMedium,
                            color = darkPink40,
                            modifier = Modifier.padding(8.dp)
                        )
                        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    }

                    if (reviews.isNotEmpty()) {
                        item {
                            Text(text = "Reviews:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(reviews) { review ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                tonalElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    RatingStars(rating = review.rating)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = review.comment, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Posted on: ${formatDate(review.timestamp)}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        item {
                            Text(text = "No reviews yet.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.AddReview.createRoute(truckId)) },
                        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Add Review")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Back to Map")
                    }
                }
            }
        }
    }
}
@Composable
fun RatingStars(rating: Int) {
    Row {

        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = if (i <= rating) Color(0xFFFFD700) else Color.LightGray, // لون ذهبي للنجوم المملوءة
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}
        // Helper function to format date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Data class for Review
data class Review(
    val rating: Int,
    val comment: String,
    val timestamp: Long
)

// Data class for FoodTruck
data class FoodTruck(
    val id: String,
    val name: String,
    val location: LatLng,
    val menu: String,
    val operatingHours: String
)