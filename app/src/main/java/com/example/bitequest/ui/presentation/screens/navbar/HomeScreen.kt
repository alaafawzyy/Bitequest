package com.example.bitequest.ui.presentation.screens.navbar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.bitequest.R
import com.example.bitequest.ui.presentation.navigation.Screen
import com.google.android.gms.common.api.ApiException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


// Fetch current location with better error handling and higher accuracy
suspend fun getCurrentLocation(client: FusedLocationProviderClient, context: Context): LatLng? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        println("Location permission not granted")
        return null
    }

    val locationRequest = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY) // Higher accuracy
        .setDurationMillis(10000) // Increased timeout for better accuracy
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

// UI Rendering
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Fused Location Client
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

    // Variables for current location and food trucks
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var foodTrucks by remember { mutableStateOf(emptyList<FoodTruck>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch location and nearby trucks
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

    // UI Rendering
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (currentLocation != null) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
            }

            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
                Marker(
                    state = rememberMarkerState(position = currentLocation!!),
                    title = "Your Current Location",
                    snippet = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                foodTrucks.forEach { truck ->
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
// Fetch nearby food trucks from Firestore with better logging
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












//@Composable
//fun HomeScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    val db = FirebaseFirestore.getInstance()
//
//    // Fused Location Client
//    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
//
//    // Permission Launcher
//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = { granted ->
//            if (!granted) {
//                println("Location permission is required to fetch nearby trucks.")
//            }
//        }
//    )
//
//    // Variables for current location and food trucks
//    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
//    var foodTrucks by remember { mutableStateOf(emptyList<FoodTruck>()) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    // Fetch location and nearby trucks
//    LaunchedEffect(Unit) {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            launch(Dispatchers.IO) {
//                try {
//                    val loc = getCurrentLocation(fusedLocationClient, context)
//                    println("Current location: $loc")
//                    if (loc != null) {
//                        withContext(Dispatchers.Main) {
//                            currentLocation = loc
//                            foodTrucks = fetchNearbyFoodTrucks(db, loc)
//                        }
//                    } else {
//                        withContext(Dispatchers.Main) {
//                            errorMessage = "Unable to fetch current location."
//                        }
//                    }
//                } catch (e: Exception) {
//                    withContext(Dispatchers.Main) {
//                        errorMessage = "Error getting location: ${e.message}"
//                    }
//                }
//            }
//        } else {
//            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//    }
//
//    // UI Rendering
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Display Error Message
//        errorMessage?.let {
//            Text(text = it, color = MaterialTheme.colorScheme.error)
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//
//        // Map View with Nearby Trucks
//        if (currentLocation != null) {
//            val cameraPositionState = rememberCameraPositionState {
//                position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
//            }
//
//            GoogleMap(
//                modifier = Modifier.fillMaxSize(),
//                cameraPositionState = cameraPositionState
//            ) {
//                // Add a marker for the current location
//                Marker(
//                    state = rememberMarkerState(position = currentLocation!!),
//                    title = "Your Current Location",
//                    snippet = "You are here",
//                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
//                )
//
//                // Add markers for nearby food trucks with custom icons
//                foodTrucks.forEach { truck ->
//                    if (truck.location != null) {
//                        Marker(
//                            state = rememberMarkerState(position = truck.location),
//                            title = truck.name,
//                            snippet = "${truck.menu} - ${truck.operatingHours}",
//                            icon = getCustomIcon(truck.id.toIntOrNull() ?: 0),
//                            onClick = {
//                                navController.navigate("${Screen.TruckDetails.route}/${truck.id}")
//                                false
//                            }
//                        )
//                    } else {
//                        println("Skipping truck with invalid location: ${truck.name}")
//                    }
//                }
//            }
//        } else {
//            Text(text = "Loading...", color = Color.White)
//        }
//    }
//}
//
//// Fetch current location with better error handling
//suspend fun getCurrentLocation(client: FusedLocationProviderClient, context: android.content.Context): LatLng? {
//    if (ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED &&
//        ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) != PackageManager.PERMISSION_GRANTED
//    ) {
//        println("Location permission not granted")
//        return null
//    }
//
//    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
//    if (locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == false) {
//        println("Please enable GPS services.")
//        return null
//    }
//
//    return try {
//        val location = client.lastLocation.await()
//        location?.let { LatLng(it.latitude, it.longitude) }
//    } catch (e: Exception) {
//        if (e is ApiException) {
//            println("API exception while fetching location: ${e.message}")
//        } else {
//            println("Error fetching current location: ${e.message}")
//        }
//        null
//    }
//}
//
//// Fetch nearby food trucks from Firestore with better logging
//
//
//suspend fun fetchNearbyFoodTrucks(db: FirebaseFirestore, currentLocation: LatLng): List<FoodTruck> {
//    return try {
//        val query = db.collection("foodTrucks").get().await()
//        Log.d("Firestore", "Fetched ${query.documents.size} documents from Firestore")
//
//        query.documents.mapNotNull { doc ->
//            try {
//                val latitude = doc.getDouble("latitude")
//                val longitude = doc.getDouble("longitude")
//
//                if (latitude != null && longitude != null) {
//                    val location = LatLng(latitude, longitude)
//                    Log.d("Firestore", "Truck: ${doc.getString("name")} at $location")
//
//                    FoodTruck(
//                        id = doc.id,
//                        name = doc.getString("name") ?: "Unknown",
//                        location = location,
//                        menu = doc.getString("menu") ?: "N/A",
//                        operatingHours = doc.getString("operatingHours") ?: "N/A"
//                    )
//                } else {
//                    Log.e("Firestore", "Skipping truck ${doc.id} due to missing coordinates")
//                    null
//                }
//            } catch (e: Exception) {
//                Log.e("Firestore", "Error parsing document ${doc.id}: ${e.message}")
//                null
//            }
//        }
//    } catch (e: Exception) {
//        Log.e("Firestore", "Error fetching food trucks: ${e.message}")
//        emptyList()
//    }
//}
//
//// Calculate latitude bounds
//fun calculateLatitudes(currentLatitude: Double, radiusInKm: Double, earthRadiusInKm: Double): Pair<Double, Double> {
//    val angularDistance = radiusInKm / earthRadiusInKm
//    val minLatitude = currentLatitude - angularDistance * 180 / Math.PI
//    val maxLatitude = currentLatitude + angularDistance * 180 / Math.PI
//    return minLatitude to maxLatitude
//}
//
//// Calculate longitude bounds
//fun calculateLongitudes(currentLongitude: Double, radiusInKm: Double, earthRadiusInKm: Double): Pair<Double, Double> {
//    val angularDistance = radiusInKm / earthRadiusInKm
//    val minLongitude = currentLongitude - angularDistance * 180 / Math.PI
//    val maxLongitude = currentLongitude + angularDistance * 180 / Math.PI
//    return minLongitude to maxLongitude
//}
//
//// FoodTruck Data Class
//data class FoodTruck(
//    val id: String,
//    val name: String,
//    val location: LatLng?,
//    val menu: String,
//    val operatingHours: String
//)
//
//// Get custom icon for each food truck based on its ID
//@Composable
//fun getCustomIcon(truckId: Int): BitmapDescriptor {
//    val context = LocalContext.current
//    val drawableResId = when (truckId) {
//        1 -> R.drawable.baseline_fastfood_24
//        2 -> R.drawable.baseline_fastfood_24
//        3 -> R.drawable.baseline_fastfood_24
//        4 -> R.drawable.baseline_fastfood_24
//        5 -> R.drawable.baseline_fastfood_24
//        else -> R.drawable.baseline_fastfood_24
//    }
//
//    val vectorDrawable = ContextCompat.getDrawable(context, drawableResId)!!
//    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
//
//    val bitmap = Bitmap.createBitmap(
//        vectorDrawable.intrinsicWidth,
//        vectorDrawable.intrinsicHeight,
//        Bitmap.Config.ARGB_8888
//    )
//    val canvas = Canvas(bitmap)
//    vectorDrawable.draw(canvas)
//
//    return BitmapDescriptorFactory.fromBitmap(bitmap)
//}