package com.example.bitequest.ui.presentation.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageMenuScreen(navController: NavHostController, truckId: String) {
    var menuItems by remember { mutableStateOf<List<String>>(emptyList()) }
    var newMenuItem by remember { mutableStateOf("") }

    LaunchedEffect(truckId) {
        FirebaseFirestore.getInstance()
            .collection("foodTrucks")
            .document(truckId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    menuItems = (doc.getString("menu") ?: "").split(",").map { it.trim() }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Manage Menu", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // عرض قائمة الطعام
        if (menuItems.isNotEmpty()) {
            menuItems.forEach { item ->
                Text(text = item, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(text = "No items in the menu.", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // إضافة عنصر جديد إلى القائمة
        TextField(
            value = newMenuItem,
            onValueChange = { newMenuItem = it },
            label = { Text("Add new menu item") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (newMenuItem.isNotEmpty()) {
                val updatedMenu = menuItems.toMutableList().apply { add(newMenuItem) }
                FirebaseFirestore.getInstance()
                    .collection("foodTrucks")
                    .document(truckId)
                    .update("menu", updatedMenu.joinToString(", "))
                    .addOnSuccessListener {
                        menuItems = updatedMenu
                        newMenuItem = ""
                    }
            }
        }) {
            Text("Add Item")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}