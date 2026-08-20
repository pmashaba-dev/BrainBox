// File: app/src/main/java/com/example/brainbox/ui/screens/SplashScreen.kt

package com.example.brainbox.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.example.brainbox.R
import com.example.brainbox.navigation.AppRoutes
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// Tag for logging purposes
private const val TAG = "SplashScreen"
// Firestore collection path for user profiles
private const val USERS_COLLECTION = "user_profiles"

@Composable
fun SplashScreen(navController: NavController) {
    // Load the Lottie animation from the raw resource directory.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_animation))

    // This LaunchedEffect block runs when the composable first enters the composition.
    LaunchedEffect(key1 = true) {
        // Delay for the splash screen duration.
        delay(3000L)

        val currentUser = Firebase.auth.currentUser
        Log.d(TAG, "Current user: ${currentUser?.email}")

        if (currentUser != null) {
            val userId = currentUser.uid
            var isUserAdmin = false

            try {
                // 1. Fetch the UserProfile document from Firestore
                val userDoc = Firebase.firestore.collection(USERS_COLLECTION).document(userId).get().await()

                if (userDoc.exists()) {
                    // 2. Convert the document to the UserProfile data class
                    val userProfile = userDoc.toObject(UserProfile::class.java)

                    // 3. Check the isAdmin flag
                    isUserAdmin = userProfile?.isAdmin ?: false
                    Log.d(TAG, "User $userId is admin: $isUserAdmin")
                } else {
                    Log.w(TAG, "User profile document not found for UID: $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile from Firestore: $e")
                // Fallback: Assume not an admin if there's an error
            }

            // 4. Navigate based on the isAdmin flag
            val destinationRoute = if (isUserAdmin) {
                AppRoutes.AdminDashboard.route
            } else {
                AppRoutes.Dashboard.route
            }

            navController.navigate(destinationRoute) {
                // Remove the splash screen from the back stack to prevent the user from navigating back to it.
                popUpTo(AppRoutes.Splash.route) { inclusive = true }
            }
        } else {
            // No user is signed in, navigate to the Welcome screen.
            navController.navigate(AppRoutes.Welcome.route) {
                // Remove the splash screen from the back stack.
                popUpTo(AppRoutes.Splash.route) { inclusive = true }
            }
        }
    }

    // The UI layout for the splash screen.
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Displays the app icon/logo using the Lottie animation.
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever, // Loop the animation continuously.
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Displays the app name.
        Text(
            text = "Brain Box",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Preview function for Android Studio to render the splash screen.
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
}