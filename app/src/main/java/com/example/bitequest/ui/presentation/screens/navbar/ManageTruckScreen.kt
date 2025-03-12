package com.example.bitequest.ui.presentation.screens.navbar

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.Calendar

@Composable
fun ManageTruckScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // State variables for truck data
    var truckId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var menu by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) } // متغير لتتبع حالة الرفع
    val operatingHours = "from $startTime to $endTime"
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true // بدء الـ Progress Bar
            uploadImageToImgur(context, it) { url ->
                isUploading = false // إيقاف الـ Progress Bar
                if (url != null) {
                    imageUrl = url
                    Toast.makeText(context, "Image uploaded successfully: $url", Toast.LENGTH_SHORT).show()
                } else {
                    errorMessage = "Failed to upload image"
                    Toast.makeText(context, "Failed to upload image. Check Logcat for details.", Toast.LENGTH_LONG).show()
                }
            }
        } ?: Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        fetchTruckData(db, currentUserId) { fetchedTruck ->
            if (fetchedTruck != null) {
                truckId = fetchedTruck.id
                name = fetchedTruck.name
                latitude = fetchedTruck.latitude
                longitude = fetchedTruck.longitude
                menu = fetchedTruck.menu
                imageUrl = fetchedTruck.imageUrl
                val hours = fetchedTruck.operatingHours.split("from ", " to ")
                if (hours.size == 3) {
                    startTime = hours[1]
                    endTime = hours[2]
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(backgroundColor)
            .padding(top = 16.dp, bottom = 50.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (truckId.isNotEmpty()) {
                    navController.navigate(Screen.TruckOrders.createRoute(truckId))
                } else {
                    Toast.makeText(context, "No truck found", Toast.LENGTH_LONG).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Orders",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Orders", color = Color.White)
            }
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

        // Select Image Button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC7BF93)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            enabled = !isUploading // تعطيل الزر أثناء الرفع
        ) {
            Text("Select Menu Image")
        }

        // Progress Bar أثناء الرفع
        if (isUploading) {
            CircularProgressIndicator(
                color = Color(0xFFFFD700),
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        }

        // Start Time Picker
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        val amPm = if (selectedHour < 12) "AM" else "PM"
                        val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                        startTime = String.format("%02d:%02d %s", formattedHour, selectedMinute, amPm)
                    },
                    hour,
                    minute,
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Start Time",
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (startTime.isEmpty()) "Select Start Time" else "Start Time: $startTime",
                    color = Color(0xFFFFD700)
                )
            }
        }

        // End Time Picker
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        val amPm = if (selectedHour < 12) "AM" else "PM"
                        val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                        endTime = String.format("%02d:%02d %s", formattedHour, selectedMinute, amPm)
                    },
                    hour,
                    minute,
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "End Time",
                    tint = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (endTime.isEmpty()) "Select End Time" else "End Time: $endTime",
                    color = Color(0xFFFFD700)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Location TextField
        OutlinedTextField(
            value = "${if (latitude != 0.0 && longitude != 0.0) "$latitude, $longitude" else ""}",
            onValueChange = {},
            label = { Text("Current Location") },
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Get Current Location Button
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC7BF93)),
            onClick = {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
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

        Spacer(modifier = Modifier.height(30.dp))

        // Save/Update Button
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            onClick = {
                if (name.isEmpty() || menu.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || latitude == 0.0 || longitude == 0.0) {
                    errorMessage = "Please fill in all fields and select a location."
                    return@Button
                }
                val truckData = mapOf(
                    "name" to name,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "menu" to menu,
                    "imageUrl" to imageUrl,
                    "operatingHours" to operatingHours,
                    "ownerId" to currentUserId
                )
                if (truckId.isEmpty()) {
                    db.collection("foodTrucks").add(truckData)
                        .addOnSuccessListener { docRef ->
                            truckId = docRef.id
                            Toast.makeText(context, "Truck added successfully", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            errorMessage = "Error adding truck: ${e.message}"
                        }
                } else {
                    db.collection("foodTrucks").document(truckId).update(truckData as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Truck updated successfully", Toast.LENGTH_LONG).show()
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

        // Logout Button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.popBackStack()
                navController.navigate(Screen.Login.route)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE1624)),
            modifier = Modifier.padding(16.dp).align(Alignment.Start)
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}

fun uploadImageToImgur(context: android.content.Context, uri: Uri, onComplete: (String?) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.imgur.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val imgurApi = retrofit.create(ImgurApi::class.java)

    try {
        val file = File(context.cacheDir, "temp_image.jpg").apply {
            context.contentResolver.openInputStream(uri)?.use { input ->
                outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                Log.e("ImgurUpload", "Failed to open input stream for URI: $uri")
                onComplete(null)
                return
            }
        }
        Log.d("ImgurUpload", "File created: ${file.absolutePath}, size: ${file.length()} bytes")

        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)

        val call = imgurApi.uploadImage("Client-ID 8263a745e844f59", body)
        Log.d("ImgurUpload", "Starting upload for file: ${file.name}")

        call.enqueue(object : Callback<ImgurResponse> {
            override fun onResponse(call: Call<ImgurResponse>, response: Response<ImgurResponse>) {
                if (response.isSuccessful) {
                    val url = response.body()?.data?.link
                    Log.d("ImgurUpload", "Upload successful, URL: $url")
                    onComplete(url)
                } else {
                    Log.e("ImgurUpload", "Upload failed, code: ${response.code()}, message: ${response.message()}, body: ${response.errorBody()?.string()}")
                    onComplete(null)
                }
            }

            override fun onFailure(call: Call<ImgurResponse>, t: Throwable) {
                Log.e("ImgurUpload", "Upload error: ${t.message}")
                onComplete(null)
            }
        })
    } catch (e: Exception) {
        Log.e("ImgurUpload", "Error preparing file: ${e.message}")
        onComplete(null)
    }
}
// Fetch current location
@SuppressLint("MissingPermission")
fun getCurrentLocation(client: FusedLocationProviderClient, onLocationReceived: (Location?) -> Unit) {
    client.lastLocation.addOnSuccessListener { location: Location? ->
        onLocationReceived(location)
    }.addOnFailureListener {
        onLocationReceived(null)
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
                    imageUrl = it.getString("imageUrl") ?: "",
                    operatingHours = it.getString("operatingHours") ?: ""
                )
            }
            onComplete(truck)
        }
}

// Truck Data Class مع حقل الصورة
data class Truck(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val menu: String,
    val imageUrl: String = "",
    val operatingHours: String
)

// واجهة Imgur API
interface ImgurApi {
    @Multipart
    @POST("3/image")
    fun uploadImage(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part
    ): Call<ImgurResponse>
}

data class ImgurResponse(
    val data: ImgurData,
    val success: Boolean,
    val status: Int
)

data class ImgurData(
    val link: String
)