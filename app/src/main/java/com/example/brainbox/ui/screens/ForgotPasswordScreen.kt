package com.example.brainbox.ui.screens

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.R
import com.google.firebase.auth.FirebaseAuthInvalidUserException // Import specific exception
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "ForgotPasswordScreen" // Added TAG for logging

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) } // Track success state for message color
    val auth = Firebase.auth
    val context = LocalContext.current

    // Animation for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo-animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "logo-scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF87CEFA), // Sky Blue
                        Color(0xFFE0F2F7)  // A Soft White
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
            // --- Top Section: Logo and App Name ---
            Image(
                painter = painterResource(id = R.drawable.brainbo),
                contentDescription = "Brain Box Logo",
                modifier = Modifier.size(120.dp).scale(scale)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Brain Box",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(48.dp))

            // --- Password Reset Card Section (Glassmorphic Style) ---
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
                    text = "Forgot Password?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Enter your email address to receive a link to reset your password.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Email Address Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        // Check for error only if the field is not empty
                        emailError = it.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches()
                    },
                    label = { Text("Email") },
                    isError = emailError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    singleLine = true
                )
                if (emailError) {
                    Text(
                        text = "Invalid email format.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Reset Password Button
                Button(
                    onClick = {
                        if (email.isNotEmpty() && !emailError) {
                            isLoading = true
                            resetMessage = null // Clear previous message
                            isSuccess = false

                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        resetMessage = "Password reset email sent to $email. Check your inbox and spam folder."
                                        isSuccess = true
                                        Toast.makeText(context, "Password reset email sent.", Toast.LENGTH_LONG).show()
                                    } else {
                                        // --- FIX & IMPROVEMENT: Log the full exception for debugging ---
                                        Log.e(TAG, "Password reset failed for email: $email", task.exception)

                                        val errorMessage = when (task.exception) {
                                            is FirebaseAuthInvalidUserException -> "No user found with this email. Please check the address."
                                            else -> "Failed to send reset email. Please try again later."
                                        }

                                        resetMessage = errorMessage
                                        isSuccess = false
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            resetMessage = "Please enter a valid email address."
                            isSuccess = false
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4CAF50), Color(0xFF1E8877))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    enabled = !isLoading && email.isNotEmpty() && !emailError // Ensure email is valid and present
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Reset Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Display reset message
                resetMessage?.let {
                    Text(
                        text = it,
                        color = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFE57373),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Go back to Login button
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text(
                        text = "Go back to Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(navController = rememberNavController())
}