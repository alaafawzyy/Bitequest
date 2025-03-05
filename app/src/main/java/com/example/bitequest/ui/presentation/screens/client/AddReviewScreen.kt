package com.example.bitequest.ui.presentation.screens.client

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bitequest.ui.theme.backgroundColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

@Composable
fun AddReviewScreen(navController: NavHostController, truckId: String) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add Review for Truck ", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))


        RatingBar(rating = rating, onRatingChanged = { newRating ->
            rating = newRating // تحديث قيمة التقييم
        })

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("enter your review") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF857213),
                unfocusedBorderColor = Color(0xFF857213).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFF857213),
                unfocusedLabelColor = Color(0xFF857213).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFF857213),
                unfocusedTextColor = Color(0xFF857213).copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),onClick = {
            if (rating > 0 && comment.isNotEmpty()) {
                saveReviewToFirestore(truckId, rating, comment, context) {
                    navController.popBackStack()
                }
            } else {
                Toast.makeText(context,"please enter a rating and a review",Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Submit Review")
        }
    }
}

@Composable
fun RatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            IconButton(onClick = {
                // تحديث التقييم إلى رقم النجمة المحددة
                onRatingChanged(index + 1)
            }) {
                Icon(
                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star ${index + 1}",
                    tint = if (index < rating) Color(0xFFFFD700) else Color.Gray // لون النجوم المضيئة
                )
            }
        }
    }
}

fun saveReviewToFirestore(
    truckId: String,
    rating: Int,
    comment: String,
    context: Context,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    if (user == null) {
        Toast.makeText(context, "يجب تسجيل الدخول لإضافة تقييم!", Toast.LENGTH_SHORT).show()
        return
    }

    val userId = user.uid
    val userName = user.displayName ?: "Unknown User"

    val review = mapOf(
        "userId" to userId,
        "userName" to userName,
        "rating" to rating,
        "comment" to comment,
        "timestamp" to Timestamp.now()
    )

    db.collection("foodTrucks").document(truckId).collection("reviews").add(review)
        .addOnSuccessListener {
            Toast.makeText(context, "تم إضافة التقييم بنجاح!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
