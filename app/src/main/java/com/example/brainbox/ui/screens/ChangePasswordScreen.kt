package com.example.brainbox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var newPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = Firebase.auth

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0F2F5))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Change Your Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter your current and new password below.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Current Password
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    newPasswordError = it.length < 6 && it.isNotEmpty()
                    confirmPasswordError = confirmNewPassword.isNotEmpty() && confirmNewPassword != it
                },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                isError = newPasswordError,
                modifier = Modifier.fillMaxWidth()
            )
            if (newPasswordError) {
                Text(
                    text = "Password must be at least 6 characters.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Confirm New Password
            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = {
                    confirmNewPassword = it
                    confirmPasswordError = it != newPassword && it.isNotEmpty()
                },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )
            if (confirmPasswordError) {
                Text(
                    text = "Passwords do not match.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Change Password Button
            Button(
                onClick = {
                    val user = auth.currentUser
                    when {
                        currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty() -> {
                            Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                        }
                        newPassword.length < 6 -> {
                            Toast.makeText(context, "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                        }
                        newPassword != confirmNewPassword -> {
                            Toast.makeText(context, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                        }
                        user != null -> {
                            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                            coroutineScope.launch {
                                try {
                                    user.reauthenticate(credential).await()
                                    user.updatePassword(newPassword).await()
                                    Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    // Handle incorrect current password more specifically
                                    val toastMessage = when {
                                        // Check for error messages commonly associated with invalid credentials/reauthentication failure
                                        e.message?.contains("INVALID_CREDENTIAL", ignoreCase = true) == true -> "The current password you entered is incorrect."
                                        e.message?.contains("credential", ignoreCase = true) == true -> "The current password you entered is incorrect."
                                        else -> "Failed to change password: ${e.message}"
                                    }
                                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Change Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(navController = rememberNavController())
}