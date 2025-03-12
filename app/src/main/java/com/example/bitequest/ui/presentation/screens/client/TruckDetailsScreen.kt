package com.example.bitequest.ui.presentation.screens.client

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bitequest.R
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.theme.backgroundColor
import com.example.bitequest.ui.theme.darkPink40
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TruckDetailsScreen(navController: NavHostController, truckId: String) {
    val db = FirebaseFirestore.getInstance()
    var truck by remember { mutableStateOf<FoodTruck?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var customerName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    var hasReviewed by remember { mutableStateOf(false) }
    var showOrderInput by remember { mutableStateOf(false) }
    var orderText by remember { mutableStateOf("") }
    var orderSuccess by remember { mutableStateOf<String?>(null) }
    var orderError by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val userDoc = db.collection("usernames")
                .whereEqualTo("uid", currentUser.uid)
                .get()
                .await()
                .documents
                .firstOrNull()
            Log.e("TruckDetailsScreen", "User document: $userDoc")
            if (userDoc != null && userDoc.exists()) {
                isAdmin = userDoc.getString("role") == "Admin"
                println("User role: ${userDoc.getString("role")}")
            } else {
                isAdmin = false
                println("User document not found")
            }
        }
    }

    LaunchedEffect(truckId, refreshTrigger) {
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
                        operatingHours = doc.getString("operatingHours") ?: "N/A",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )

                    val reviewDocs = db.collection("foodTrucks").document(truckId).collection("reviews").get().await()
                    reviews = reviewDocs.mapNotNull { reviewDoc ->
                        try {
                            Review(
                                reviewId = reviewDoc.id,
                                rating = reviewDoc.getDouble("rating")?.toInt() ?: 0,
                                comment = reviewDoc.getString("comment") ?: "No comment",
                                timestamp = reviewDoc.getDate("timestamp")?.time ?: System.currentTimeMillis(),
                                userId = reviewDoc.getString("userId") ?: "",
                                userName = reviewDoc.getString("userName") ?: "Anonymous"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else {
                    errorMessage = "Truck not found."
                }
                if (currentUser != null) {
                    hasReviewed = reviews.any { it.userId == currentUser.uid }
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
                        .weight(1f)
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

                        if (truck!!.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = truck!!.imageUrl,
                                contentDescription = "Menu Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp)
                                    .clickable { showFullImage = true },
                                placeholder = painterResource(R.drawable.loading),
                                error = painterResource(R.drawable.error)
                            )
                        } else {
                            Text(
                                text = "No image available",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        if (showFullImage && truck!!.imageUrl.isNotEmpty()) {
                            Dialog(onDismissRequest = { showFullImage = false }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .clickable { showFullImage = false }
                                ) {
                                    AsyncImage(
                                        model = truck!!.imageUrl,
                                        contentDescription = "Full Menu Image",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.9f),
                                        placeholder = painterResource(R.drawable.loading),
                                        error = painterResource(R.drawable.error)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text(text = "Operating Hours:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${truck!!.operatingHours}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = darkPink40,
                            modifier = Modifier.padding(8.dp)
                        )
                        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.White
                            ),
                            onClick = { showOrderInput = true },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            Text("Make an Order")
                        }

                        // الـ Order Input يظهر هنا تحت "Make an Order" مباشرة
                        if (showOrderInput) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Your Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = backgroundColor,
                                        unfocusedBorderColor = backgroundColor.copy(alpha = 0.5f),
                                        focusedLabelColor = backgroundColor,
                                        unfocusedLabelColor = backgroundColor.copy(alpha = 0.5f),
                                        focusedTextColor = backgroundColor,
                                        unfocusedTextColor = backgroundColor.copy(alpha = 0.8f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = orderText,
                                    onValueChange = { orderText = it },
                                    label = { Text("Order Details") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = backgroundColor,
                                        unfocusedBorderColor = backgroundColor.copy(alpha = 0.5f),
                                        focusedLabelColor = backgroundColor,
                                        unfocusedLabelColor = backgroundColor.copy(alpha = 0.5f),
                                        focusedTextColor = backgroundColor,
                                        unfocusedTextColor = backgroundColor.copy(alpha = 0.8f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Green,
                                            contentColor = Color.White
                                        ),
                                        onClick = {
                                            if (orderText.isNotBlank() && currentUser != null && customerName.isNotBlank()) {
                                                val order = hashMapOf(
                                                    "userId" to currentUser.uid,
                                                    "userName" to customerName,
                                                    "orderDetails" to orderText,
                                                    "timestamp" to Timestamp.now()
                                                )

                                                db.collection("foodTrucks")
                                                    .document(truckId)
                                                    .collection("orders")
                                                    .add(order)
                                                    .addOnSuccessListener {
                                                        orderSuccess = "Order sent successfully!"
                                                        orderText = ""
                                                        customerName = ""
                                                        showOrderInput = false
                                                        Toast.makeText(context, "Order sent successfully!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener {
                                                        orderError = "Error sending order: ${it.message}"
                                                    }
                                            }
                                        },
                                        enabled = orderText.isNotBlank() && customerName.isNotBlank()
                                    ) {
                                        Text("Send Order")
                                    }

                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ),
                                        onClick = {
                                            showOrderInput = false
                                            orderText = ""
                                            customerName = ""
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                }

                                if (orderSuccess != null) {
                                    Text(
                                        text = orderSuccess!!,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                if (orderError != null) {
                                    Text(
                                        text = orderError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // الأزرار الثلاثة تظهر هنا بعد الـ Order Input
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    if (!hasReviewed) {
                                        navController.navigate(Screen.AddReview.createRoute(truckId))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasReviewed) Color.Gray else backgroundColor,
                                    contentColor = Color.White
                                ),
                                enabled = !hasReviewed,
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                Text("Add Review")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isAdmin) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ),
                                    onClick = { deleteFoodTruck(truckId, context, navController) },
                                    modifier = Modifier.fillMaxWidth(0.7f)
                                ) {
                                    Text("Delete Food Truck")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = Color.White),

                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Back to Map")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
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
                                    if (isAdmin) {
                                        Button(
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Red,
                                                contentColor = Color.White
                                            ),
                                            onClick = {
                                                deleteReview(
                                                    review.reviewId,
                                                    truckId,
                                                    context,
                                                    reviews,
                                                    { newReviews -> reviews = newReviews },
                                                    { refreshTrigger = !refreshTrigger }
                                                )
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    } else {
                        item {
                            Text(text = "No reviews yet.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))

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
                tint = if (i <= rating) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun deleteReview(
    reviewId: String,
    truckId: String,
    context: Context,
    currentReviews: List<Review>,
    onUpdateReviews: (List<Review>) -> Unit,
    onRefresh: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("foodTrucks")
        .document(truckId)
        .collection("reviews")
        .document(reviewId)
        .delete()
        .addOnSuccessListener {
            onUpdateReviews(currentReviews.filter { it.reviewId != reviewId })
            Toast.makeText(context, "Review deleted successfully", Toast.LENGTH_SHORT).show()
            onRefresh()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to delete review: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun deleteFoodTruck(
    truckId: String,
    context: Context,
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("foodTrucks")
        .document(truckId)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Food truck deleted successfully", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to delete: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

data class Review(
    val reviewId: String = "",
    val rating: Int,
    val comment: String,
    val timestamp: Long,
    val userId: String,
    val userName: String
)

data class FoodTruck(
    val id: String,
    val name: String,
    val location: LatLng,
    val menu: String,
    val operatingHours: String,
    val imageUrl: String = ""
)