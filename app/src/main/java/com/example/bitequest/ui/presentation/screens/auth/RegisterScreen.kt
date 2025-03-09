package com.example.bitequest.ui.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bitequest.ui.presentation.navigation.Screen
import com.example.bitequest.ui.theme.backgroundColor
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var secondName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRole by rememberSaveable { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    var adminCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .verticalScroll(rememberScrollState())

        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Username Field
        OutlinedTextField(
            value = user,
            onValueChange = {
                if (it.isNotEmpty() && Character.isDigit(it.first()).not()) {
                    user = it
                } else if (it.isEmpty()) {
                    user = it
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            label = { Text("User Name") },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // First Name Field
        OutlinedTextField(
            value = firstName,
            onValueChange = {
                if (it.isNotEmpty() && Character.isDigit(it.first()).not()) {
                    firstName = it
                } else if (it.isEmpty()) {
                    firstName = it
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            label = { Text("First Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Last Name Field
        OutlinedTextField(
            value = secondName,
            onValueChange = {
                if (it.isNotEmpty() && Character.isDigit(it.first()).not()) {
                    secondName = it
                } else if (it.isEmpty()) {
                    secondName = it
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            label = { Text("Last Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phone Number Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.isEmpty() || it == "0" || it == "05" || it.matches(Regex("^05\\d{0,8}$"))) {
                    phoneNumber = it
                }



    },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            label = { Text("Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFFFFD700),
                unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                focusedTextColor = Color(0xFFFFD700),
                unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
            ),
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Register as",
            color = Color.Gray,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Admin Button
            OutlinedButton(
                onClick = {
                    selectedRole = "Admin"
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedRole == "Admin") Color(0xFFFFD700) else Color.LightGray,
                    contentColor = if (selectedRole == "Admin") Color.Black else Color.Gray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text(text = "Admin")
            }
            if (selectedRole == "Admin") {
                OutlinedTextField(
                    value = adminCode,
                    onValueChange = { adminCode = it },
                    label = { Text("Admin Code") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color(0xFFFFD700).copy(alpha = 0.5f),
                        focusedTextColor = Color(0xFFFFD700),
                        unfocusedTextColor = Color(0xFFFFD700).copy(alpha = 0.8f)
                    ),                    modifier = Modifier.fillMaxWidth()
                )
            }


            // User Button
            OutlinedButton(
                onClick = {
                    selectedRole = "User"
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedRole == "User") Color(0xFFFFD700) else Color.LightGray,
                    contentColor = if (selectedRole == "User") Color.Black else Color.Gray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text(text = "User")
            }
        }

        roleError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.White
            ),
            onClick = {
                errorMessage = null
                roleError = null


                if (selectedRole == null) {
                    roleError = "Please select a role (Admin/User)"
                    return@Button
                }


                if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || user.isBlank() || firstName.isBlank() || secondName.isBlank() || phoneNumber.isBlank()) {
                    errorMessage = "All fields are required"
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Invalid email format"
                } else if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                } else if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else if (phoneNumber.length != 10 || !phoneNumber.startsWith("05")) {
                    errorMessage = "Phone number must be 10 digits and start with 05"
                } else if (selectedRole == "Admin" && adminCode != "0000") {
                    errorMessage = "Invalid admin code"
                } else {
                    val isAdmin = selectedRole == "Admin"
                    val isUser = selectedRole == "User"
                    val firestore = FirebaseFirestore.getInstance()


                    firestore.collection("usernames").document(user).get()
                        .addOnCompleteListener { usernameTask ->
                            if (usernameTask.isSuccessful) {
                                val usernameDocument = usernameTask.result
                                if (usernameDocument != null && usernameDocument.exists()) {
                                    errorMessage = "Username already exists"
                                } else {

                                    firestore.collection("usernames").whereEqualTo("phoneNumber", phoneNumber).get()
                                        .addOnCompleteListener { phoneTask ->
                                            if (phoneTask.isSuccessful) {
                                                val phoneDocuments = phoneTask.result
                                                if (!phoneDocuments.isEmpty) {
                                                    errorMessage = "Phone number already exists"
                                                } else {
                                                    // إنشاء المستخدم في Firebase Authentication
                                                    val auth = FirebaseAuth.getInstance()
                                                    auth.createUserWithEmailAndPassword(email, password)
                                                        .addOnCompleteListener { createUserTask ->
                                                            if (createUserTask.isSuccessful) {
                                                                val userId = auth.currentUser?.uid
                                                                userId?.let {
                                                                    // تحديث جدول usernames
                                                                    firestore.collection("usernames").document(user).set(
                                                                        mapOf(
                                                                            "username" to user,
                                                                            "taken" to true,
                                                                            "phoneNumber" to phoneNumber,
                                                                            "role" to selectedRole,
                                                                            "uid" to userId,

                                                                            )
                                                                    )


                                                                    firestore.collection("users").document(it).set(
                                                                        mapOf(
                                                                            "username" to user,
                                                                            "email" to email,
                                                                            "firstName" to firstName,
                                                                            "lastName" to secondName,
                                                                            "phoneNumber" to phoneNumber,
                                                                            "isAdmin" to isAdmin,
                                                                            "isUser" to isUser
                                                                        )
                                                                    )


                                                                    navController.navigate(Screen.Home.route) {
                                                                        popUpTo(Screen.Register.route) { inclusive = true }
                                                                    }
                                                                }
                                                            } else {
                                                                errorMessage = "Registration failed: ${createUserTask.exception?.message}"
                                                            }
                                                        }
                                                }
                                            } else {
                                                errorMessage = "Error checking phone number: ${phoneTask.exception?.message}"
                                            }
                                        }
                                }
                            } else {
                                errorMessage = "Error checking username: ${usernameTask.exception?.message}"
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }




        Spacer(modifier = Modifier.height(8.dp))


        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
            Text("Back to Login", color = Color.White)
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}