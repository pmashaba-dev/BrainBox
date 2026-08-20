package com.example.brainbox.ui.screens

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.R
import com.example.brainbox.navigation.AppRoutes
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "SignupScreen"

@Composable
fun SignupScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var signupMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = Firebase.auth
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF87CEFA),
                        Color(0xFFE0F2F7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Logo ---
            Image(
                painter = painterResource(id = R.drawable.brainbo),
                contentDescription = "Brain Box Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Brain Box",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(48.dp))

            // --- Card ---
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join Brain Box!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))

                // --- Email Field ---
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = !Patterns.EMAIL_ADDRESS.matcher(it).matches() && it.isNotEmpty()
                    },
                    label = { Text("Email") },
                    isError = emailError,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "User Icon")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (emailError) {
                    Text(
                        text = "Invalid email format.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Password Field ---
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = it.length < 6 && it.isNotEmpty()
                    },
                    label = { Text("Password") },
                    isError = passwordError,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Icon")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (passwordError) {
                    Text(
                        text = "Password must be at least 6 characters.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Sign Up Button ---
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty() && !emailError && !passwordError) {

                            isLoading = true

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        signupMessage = "Sign up successful!"
                                        Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                        navController.navigate(AppRoutes.ProfileCreation.route) {
                                            popUpTo(AppRoutes.Signup.route) { inclusive = true }
                                        }
                                    } else {
                                        signupMessage = task.exception?.message ?: "Sign up failed."
                                        Log.e(TAG, "Signup failed", task.exception)
                                        Toast.makeText(context, signupMessage, Toast.LENGTH_LONG).show()
                                    }
                                }

                        } else {
                            signupMessage = "Please enter valid email and password."
                            Toast.makeText(context, signupMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E8877),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                signupMessage?.let {
                    Text(
                        text = it,
                        color = if (it.contains("successful")) Color(0xFF4CAF50) else Color(0xFFE57373),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Already have an account? ", fontSize = 14.sp)
                    TextButton(onClick = { navController.navigate(AppRoutes.Login.route) }) {
                        Text(
                            text = "Log in",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen(navController = rememberNavController())
}
