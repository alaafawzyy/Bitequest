package com.example.bitequest.ui.presentation.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddReviewScreen(navController: NavHostController, truckId: String) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Slider(
            value = rating,
            onValueChange = { rating = it },
            valueRange = 1f..5f,
            steps = 3
        )
        TextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val review = mapOf(
                "rating" to rating,
                "comment" to comment,
                "timestamp" to System.currentTimeMillis()
            )
            FirebaseFirestore.getInstance()
                .collection("foodTrucks")
                .document(truckId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener {
                    navController.popBackStack()
                }
        }) {
            Text("Submit Review")
        }
    }
}