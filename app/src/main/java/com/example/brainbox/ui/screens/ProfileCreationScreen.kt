package com.example.brainbox.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.navigation.AppRoutes
import com.example.brainbox.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Import for the .await() function

private const val TAG = "ProfileCreationScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCreationScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    // A list of avatar resource IDs
    val avatars = listOf(
        R.drawable.girl,
        R.drawable.boy1,
        R.drawable.hacker
    )
    // Initialize with the default avatar. A user can now save a profile
    // without explicitly choosing one.
    var selectedAvatarResId by remember { mutableIntStateOf(R.drawable.brain_icon) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Get instances of Firestore and Auth
    val auth = Firebase.auth
    val db = Firebase.firestore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Almost Done! Create Your Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tell us a bit about yourself to personalize your learning experience.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Display the selected avatar.
            Image(
                painter = painterResource(id = selectedAvatarResId),
                contentDescription = "User Avatar",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Choose your avatar:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            avatars.forEach { resId ->
                val isSelected = selectedAvatarResId == resId
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { selectedAvatarResId = resId }
                        .border(
                            2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            CircleShape
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                // Enforce the 10-character limit
                if (it.length <= 10) {
                    username = it
                } else {
                    // Display a warning Toast if the limit is exceeded
                    Toast.makeText(context, "Username cannot exceed 10 characters", Toast.LENGTH_SHORT).show()
                }
            },
            label = { Text("Username (Max 10 Chars)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // Use a coroutine to handle the asynchronous Firestore operation.
                coroutineScope.launch {
                    val currentUser = auth.currentUser
                    // Check if a username is entered. Avatar selection is now optional.
                    if (currentUser != null && username.isNotEmpty()) {
                        try {
                            val userProfile = hashMapOf(
                                "username" to username,
                                "avatarResId" to selectedAvatarResId
                            )
                            db.collection("profiles").document(currentUser.uid)
                                .set(userProfile).await()

                            Log.d(TAG, "User profile saved successfully!")
                            Toast.makeText(context, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate(AppRoutes.Dashboard.route) {
                                popUpTo(AppRoutes.ProfileCreation.route) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error saving user profile", e)
                            Toast.makeText(context, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Please enter a username.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            // The button is now only enabled when there's a username
            enabled = username.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Save Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileCreationScreenPreview() {
    ProfileCreationScreen(navController = rememberNavController())
}