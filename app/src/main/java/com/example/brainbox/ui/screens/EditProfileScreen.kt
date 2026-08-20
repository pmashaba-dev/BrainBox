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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.brainbox.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

private const val TAG = "EditProfileScreen"

// Assuming UserProfile data class exists elsewhere, if not, it's implied:
// data class UserProfile(
//     val username: String = "",
//     val avatarResId: Int = 0,
//     // ... other fields
// )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    val avatars = listOf(
        R.drawable.girl,
        R.drawable.boy1,
        R.drawable.hacker
    )
    var selectedAvatarResId by remember { mutableIntStateOf(R.drawable.brain_icon) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val auth = Firebase.auth
    val db = Firebase.firestore
    val currentUser = auth.currentUser

    // Load current user profile data when the screen is first composed
    LaunchedEffect(key1 = Unit) {
        if (currentUser != null) {
            val docRef = db.collection("profiles").document(currentUser.uid)
            try {
                val document = docRef.get().await()
                if (document.exists()) {
                    // NOTE: UserProfile class must be defined in scope for this to compile
                    // Keeping the original logic structure assuming UserProfile is available.
                    val userProfile = document.toObject(UserProfile::class.java)
                    if (userProfile != null) {
                        username = userProfile.username
                        // Add a fallback for the avatar ID
                        selectedAvatarResId = if (userProfile.avatarResId == 0) R.drawable.brain_icon else userProfile.avatarResId
                        Log.d(TAG, "User profile loaded: $userProfile")
                    }
                } else {
                    Log.d(TAG, "No user profile found for UID: ${currentUser.uid}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error loading user profile", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Edit Your Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Update your username and avatar.",
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
                // Add a check to prevent a crash from bad data
                val avatarId = if (selectedAvatarResId == 0) R.drawable.brain_icon else selectedAvatarResId
                Image(
                    painter = painterResource(id = avatarId),
                    contentDescription = "User Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Choose a new avatar:",
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
                onValueChange = { newValue ->
                    if (newValue.length <= 10) {
                        username = newValue
                    } else {
                        // Warn the user if they exceed the 10-character limit
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
                    coroutineScope.launch {
                        if (currentUser != null && username.isNotEmpty()) {
                            try {
                                val updates = hashMapOf(
                                    "username" to username,
                                    "avatarResId" to selectedAvatarResId
                                )
                                // Use SetOptions.merge() to only update the specified fields
                                db.collection("profiles").document(currentUser.uid)
                                    .set(updates, SetOptions.merge()).await()

                                Log.d(TAG, "User profile updated successfully!")
                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.w(TAG, "Error updating user profile", e)
                                Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a username.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = username.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Update Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen(navController = rememberNavController())
}