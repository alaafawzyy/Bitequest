package com.example.bitequest.ui.presentation.screens.navbar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.bitequest.R
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.theme.backgroundColor
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

// Fetch current location with better error handling and higher accuracy
suspend fun getCurrentLocation(client: FusedLocationProviderClient, context: Context): LatLng? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        println("Location permission not granted")
        return null
    }

    val locationRequest = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setDurationMillis(10000)
        .build()

    return try {
        val location = client.getCurrentLocation(locationRequest, null).await()
        location?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: Exception) {
        println("Error fetching location: ${e.message}")
        null
    }
}

// Alternative: Use getLastLocation
suspend fun getLastKnownLocation(client: FusedLocationProviderClient, context: Context): LatLng? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        println("Location permission not granted")
        return null
    }

    return try {
        val lastLocation = client.lastLocation.await()
        lastLocation?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: Exception) {
        println("Error fetching last known location: ${e.message}")
        null
    }
}

// Check if GPS is enabled
fun isGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

// Calculate distance between two LatLng points in kilometers
fun calculateDistance(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0] / 1000 // Convert meters to kilometers
}

// Get place name from LatLng using Geocoder
fun getPlaceName(context: Context, location: LatLng): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].getAddressLine(0) ?: "Unknown Location"
        } else {
            "Unknown Location"
        }
    } catch (e: Exception) {
        Log.e("Geocoder", "Error fetching place name: ${e.message}")
        "Unknown Location"
    }
}

// Fetch nearby food trucks from Firestore
suspend fun fetchNearbyFoodTrucks(db: FirebaseFirestore, currentLocation: LatLng): List<FoodTruck> {
    return try {
        val query = db.collection("foodTrucks").get().await()
        Log.d("Firestore", "Fetched ${query.documents.size} documents from Firestore")

        query.documents.mapNotNull { doc ->
            try {
                val latitude = doc.getDouble("latitude")
                val longitude = doc.getDouble("longitude")

                if (latitude != null && longitude != null) {
                    val location = LatLng(latitude, longitude)
                    Log.d("Firestore", "Truck: ${doc.getString("name")} at $location")

                    FoodTruck(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        location = location,
                        menu = doc.getString("menu") ?: "N/A",
                        operatingHours = doc.getString("operatingHours") ?: "N/A"
                    )
                } else {
                    Log.e("Firestore", "Skipping truck ${doc.id} due to missing coordinates")
                    null
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error parsing document ${doc.id}: ${e.message}")
                null
            }
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching food trucks: ${e.message}")
        emptyList()
    }
}

// Get custom icon for each food truck based on its ID
@Composable
fun getCustomIcon(truckId: Int): BitmapDescriptor {
    val context = LocalContext.current
    val drawableResId = when (truckId) {
        1 -> R.drawable.baseline_fastfood_24
        2 -> R.drawable.baseline_fastfood_24
        3 -> R.drawable.baseline_fastfood_24
        4 -> R.drawable.baseline_fastfood_24
        5 -> R.drawable.baseline_fastfood_24
        else -> R.drawable.baseline_fastfood_24
    }

    val vectorDrawable = ContextCompat.getDrawable(context, drawableResId)!!
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

// FoodTruck Data Class
data class FoodTruck(
    val id: String,
    val name: String,
    val location: LatLng?,
    val menu: String,
    val operatingHours: String
)

// UI Rendering
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (!fineLocationGranted && !coarseLocationGranted) {
                println("Location permission is required to fetch nearby trucks.")
            }
        }
    )

    // State variables
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var foodTrucks by remember { mutableStateOf(emptyList<FoodTruck>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredTrucks by remember { mutableStateOf(emptyList<FoodTruck>()) }

    // Coroutine scope for launching effects
    val scope = rememberCoroutineScope()

    // Fetch location and food trucks
    LaunchedEffect(Unit) {
        if (isGpsEnabled(context)) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val loc = getCurrentLocation(fusedLocationClient, context)
                    Log.d("Location", "Fetched location: $loc")
                    if (loc != null) {
                        currentLocation = loc
                        foodTrucks = fetchNearbyFoodTrucks(db, loc)
                    } else {
                        errorMessage = "Unable to fetch current location."
                    }
                } catch (e: Exception) {
                    errorMessage = "Error getting location: ${e.message}"
                }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            errorMessage = "Please enable GPS to use this feature."
        }
    }

    // Filter and sort trucks based on search query
    LaunchedEffect(searchQuery, foodTrucks, currentLocation) {
        if (searchQuery.isNotBlank() && currentLocation != null) {
            filteredTrucks = foodTrucks
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .sortedBy { truck ->
                    truck.location?.let { calculateDistance(currentLocation!!, it) } ?: Float.MAX_VALUE
                }
        } else {
            filteredTrucks = emptyList()
        }
    }

    // UI Rendering
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Food Trucks") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )

        // Display search results
        if (searchQuery.isNotBlank()) {
            if (filteredTrucks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)

                ) {
                    Text(
                        text = "Found ${filteredTrucks.size} branch${if (filteredTrucks.size == 1) "" else "es"} for \"$searchQuery\"",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTrucks) { truck ->
                            truck.location?.let { location ->
                                val distance = calculateDistance(currentLocation!!, location)
                                val placeName = getPlaceName(context, location)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(Screen.TruckDetails.createRoute(truck.id))
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1C2526)
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp) ,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row {
                                                Text(
                                                    text = truck.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = truck.operatingHours,
                                                    color = Color(0xFFB0BEC5),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = placeName,
                                                color = Color(0xFFCFD8DC),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "%.2f km away".format(distance),
                                                color = Color(0xFFB0BEC5),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No branches found for \"$searchQuery\"",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (currentLocation != null) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = rememberMarkerState(position = currentLocation!!),
                    title = "Your Current Location",
                    snippet = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                val trucksToDisplay = if (searchQuery.isNotBlank()) filteredTrucks else foodTrucks
                trucksToDisplay.forEach { truck ->
                    truck.location?.let {
                        Marker(
                            state = rememberMarkerState(position = it),
                            title = truck.name,
                            snippet = "${truck.menu} - ${truck.operatingHours}",
                            icon = getCustomIcon(truck.id.toIntOrNull() ?: 0),
                            onClick = {
                                navController.navigate(Screen.TruckDetails.createRoute(truck.id))
                                false
                            }
                        )
                    }
                }
            }
        } else {
            Text(text = "Loading...", color = Color.White)
        }
    }
}