package com.example.bitequest.ui.presentation.screens.client

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bitequest.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TruckOrdersScreen(navController: NavHostController, truckId: String?) {

    if (truckId.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Invalid truck ID.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(truckId) {
        try {
            val querySnapshot = db.collection("foodTrucks")
                .document(truckId)
                .collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("FirestoreData", "Orders fetched: ${querySnapshot.documents.size}")

            orders = querySnapshot.documents.mapNotNull { doc ->
                try {
                    val userId = doc.getString("userId") ?: ""
                    val userName = doc.getString("userName") ?: "Anonymous"
                    val orderDetails = doc.getString("orderDetails") ?: ""
                    val timestamp = doc.getDate("timestamp")?.time ?: 0L

                    Log.d("FirestoreData", "Order ID: ${doc.id}, User: $userName, Details: $orderDetails, Timestamp: $timestamp")

                    if (userId.isNotEmpty() && orderDetails.isNotEmpty() && timestamp > 0) {
                        Order(
                            id = doc.id,
                            userId = userId,
                            userName = userName,
                            orderDetails = orderDetails,
                            timestamp = timestamp
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e("FirestoreData", "Error parsing order: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load orders: ${e.message}"
            Log.e("FirestoreData", "Error fetching orders: ${e.message}")
        } finally {
            loading = false
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }

            orders.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "No orders yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_shopping_cart_24), // تأكد من أن الأيقونة موجودة في res/drawable
                        contentDescription = "Empty Orders",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Customer: ${order.userName}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Order: ${order.orderDetails}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Time: ${formatDatee(order.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Function to format timestamps
fun formatDatee(timestamp: Long): String {
    return if (timestamp > 0) {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()) // 🔹 استخدم hh بدل HH و a لـ AM/PM
        sdf.format(Date(timestamp))
    } else {
        "Unknown time"
    }
}

// Data class for Order
data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val orderDetails: String = "",
    val timestamp: Long = 0L
)
