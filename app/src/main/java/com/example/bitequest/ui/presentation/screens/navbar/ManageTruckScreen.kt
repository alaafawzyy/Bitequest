package com.example.bitequest.ui.presentation.screens.navbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.theme.backgroundColor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageTruckScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Load data from SharedPreferences
    val sharedPreferences = context.getSharedPreferences("TruckPrefs", Context.MODE_PRIVATE)
    var truckId by remember { mutableStateOf(sharedPreferences.getString("truckId", "") ?: "") }
    var name by remember { mutableStateOf(sharedPreferences.getString("name", "") ?: "") }
    var latitude by remember { mutableStateOf((sharedPreferences.getFloat("latitude", 0f).toDouble())) }
    var longitude by remember { mutableStateOf((sharedPreferences.getFloat("longitude", 0f).toDouble())) }
    var menu by remember { mutableStateOf(sharedPreferences.getString("menu", "") ?: "") }
    var operatingHours by remember { mutableStateOf(sharedPreferences.getString("operatingHours", "") ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    // Fused Location Client
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                errorMessage = "Location permission is required to select a location."
            }
        }
    )

    // Fetch current location when the screen is loaded
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(fusedLocationClient) { loc ->
                if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                } else {
                    errorMessage = "Unable to fetch current location."
                }
            }
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Fetch truck data from Firestore
        fetchTruckData(db, currentUserId) { fetchedTruck ->
            if (fetchedTruck != null) {
                truckId = fetchedTruck.id
                name = fetchedTruck.name
                latitude = fetchedTruck.latitude
                longitude = fetchedTruck.longitude
                menu = fetchedTruck.menu
                operatingHours = fetchedTruck.operatingHours

                saveToSharedPreferences(context, fetchedTruck)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // التمرير هنا
            .background(backgroundColor)
            .padding(top = 16.dp, bottom = 50.dp, start = 16.dp, end = 16.dp), // إضافة padding لتجنب قطع المحتوى
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // تغيير الترتيب إلى الأعلى
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Truck Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Truck Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        // Menu
        OutlinedTextField(
            value = menu,
            onValueChange = { menu = it },
            label = { Text("Menu") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        // Operating Hours
        OutlinedTextField(
            value = operatingHours,
            onValueChange = { operatingHours = it },
            label = { Text("Operating Hours") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Location TextField
        OutlinedTextField(
            value = "${if (latitude != 0.0 && longitude != 0.0) "$latitude, $longitude" else ""}",
            onValueChange = {}, // Disabled input
            label = { Text("Current Location") },
            readOnly = true, // Use readOnly instead of enabled = false
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Get Current Location Button
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.White
            ),
            onClick = {
                if (ActivityCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    getCurrentLocation(fusedLocationClient) { loc ->
                        if (loc != null) {
                            latitude = loc.latitude
                            longitude = loc.longitude
                        } else {
                            errorMessage = "Unable to fetch current location."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text("Get Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save/Update Button
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.White
            ),
            onClick = {
                if (name.isEmpty() || menu.isEmpty() || operatingHours.isEmpty() || latitude == 0.0 || longitude == 0.0) {
                    errorMessage = "Please fill in all fields and select a location."
                    return@Button
                }

                val truckData = mapOf(
                    "name" to name,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "menu" to menu,
                    "operatingHours" to operatingHours,
                    "ownerId" to currentUserId
                )

                if (truckId.isEmpty()) {
                    // Add new truck to Firestore
                    db.collection("foodTrucks").add(truckData)
                        .addOnSuccessListener { docRef ->
                            truckId = docRef.id
                            saveToSharedPreferences(context, Truck(truckId!!, name, latitude, longitude, menu, operatingHours))
                            Toast.makeText(context,"Truck added successfully",Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Error adding truck: ${e.message}"
                        }
                } else {
                    // Update existing truck in Firestore
                    db.collection("foodTrucks").document(truckId).update(truckData as Map<String, Any>)
                        .addOnSuccessListener {
                            saveToSharedPreferences(context, Truck(truckId, name, latitude, longitude, menu, operatingHours))
                            Toast.makeText(context,"Truck updated successfully",Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Error updating truck: ${e.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(if (truckId.isEmpty()) "Save Truck" else "Update Truck")
        }


        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
                auth.signOut() // Sign out the user from Firebase
                navController.popBackStack() // Navigate back to login screen or home screen
                navController.navigate(Screen.Login.route) // Navigate to Login Screen
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text("Logout")
        }
        Spacer(modifier = Modifier.height(50.dp))

    }
}
// Fetch current location
@SuppressLint("MissingPermission")
fun getCurrentLocation(client: FusedLocationProviderClient, onLocationReceived: (Location?) -> Unit) {
    client.lastLocation.addOnSuccessListener { location: Location? ->
        onLocationReceived(location)
    }.addOnFailureListener {
        onLocationReceived(null) // Return null if failed
    }
}

// Save data to SharedPreferences
fun saveToSharedPreferences(context: Context, truck: Truck) {
    val sharedPreferences = context.getSharedPreferences("TruckPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("truckId", truck.id)
        putString("name", truck.name)
        putFloat("latitude", truck.latitude.toFloat())
        putFloat("longitude", truck.longitude.toFloat())
        putString("menu", truck.menu)
        putString("operatingHours", truck.operatingHours)
        apply()
    }
}

// Fetch truck data from Firestore
fun fetchTruckData(db: FirebaseFirestore, userId: String, onComplete: (Truck?) -> Unit) {
    db.collection("foodTrucks").whereEqualTo("ownerId", userId).limit(1).get()
        .addOnSuccessListener { documents ->
            val truck = documents.documents.firstOrNull()?.let {
                Truck(
                    id = it.id,
                    name = it.getString("name") ?: "",
                    latitude = it.getDouble("latitude") ?: 0.0,
                    longitude = it.getDouble("longitude") ?: 0.0,
                    menu = it.getString("menu") ?: "",
                    operatingHours = it.getString("operatingHours") ?: ""
                )
            }
            onComplete(truck)
        }
}

// Truck Data Class
data class Truck(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val menu: String,
    val operatingHours: String
)