// File: app/src/main/java/com/example/brainbox/ui/screens/SettingsScreen.kt

package com.example.brainbox.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.R
import com.example.brainbox.SoundManager
import com.example.brainbox.navigation.AppRoutes
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "SettingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var enableSoundEffects by remember { mutableStateOf(true) }
    // New state for showing the logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val soundId = remember { soundManager.loadSound(R.raw.button_click) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... (Settings categories and cards remain the same)

            // Personal Information section
            SettingsSectionTitle(title = "Personal Information")
            SettingsCard(
                icon = Icons.Default.Person,
                text = "Edit Profile",
                onClick = {
                    if (enableSoundEffects) soundManager.playSound(R.raw.button_click)
                    Log.d(TAG, "Navigating to Edit Profile Screen.")
                    navController.navigate(AppRoutes.EditProfile.route)
                }
            )

            // Account Management section
            SettingsSectionTitle(title = "Account Management")
            SettingsCard(
                icon = Icons.Default.Lock,
                text = "Change Password",
                onClick = {
                    if (enableSoundEffects) soundManager.playSound(R.raw.button_click)
                    Log.d(TAG, "Navigating to Change Password Screen.")
                    navController.navigate(AppRoutes.ChangePassword.route)
                }
            )

            // Sound & Music section
            SettingsSectionTitle(title = "Sound & Music")
            SettingsToggleCard(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                text = "Enable Sound Effects",
                checked = enableSoundEffects,
                onCheckedChange = {
                    enableSoundEffects = it
                    if (it) {
                        soundManager.playSound(R.raw.button_click)
                    }
                }
            )

            // Support section
            SettingsSectionTitle(title = "Support")
            SettingsCard(
                icon = Icons.Default.Support,
                text = "Contact Support",
                onClick = {
                    if (enableSoundEffects) soundManager.playSound(R.raw.button_click)

                    val emailAddress = "blvckblvck25@gmail.com"
                    val subject = "BrainBox App Support Request"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${emailAddress}?subject=${Uri.encode(subject)}")
                    }

                    try {
                        context.startActivity(intent)
                        Log.d(TAG, "Attempted to launch email client.")
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "No email app found. Please email us manually at $emailAddress",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(TAG, "Failed to launch email client: ${e.message}")
                    }
                }
            )
            SettingsCard(
                icon = Icons.Default.Info,
                text = "About BrainBox",
                onClick = {
                    navController.navigate(AppRoutes.About.route)
                    Log.d(TAG, "Navigating to About BrainBox Screen.")
                }
            )

            Spacer(modifier = Modifier.weight(1f)) // Spacer to push the button to the bottom

            // Log Out button (Now sets the showLogoutDialog state to true)
            Button(
                onClick = {
                    if (enableSoundEffects) soundManager.playSound(R.raw.button_click)
                    showLogoutDialog = true // <--- Trigger the dialog
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Log Out"
                    )
                    Text(
                        text = "Log Out",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // --- LOGOUT CONFIRMATION DIALOG ---
    // ----------------------------------------------------
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog if the user taps outside or presses back
                showLogoutDialog = false
            },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout Icon") },
            title = {
                Text(text = "Confirm Logout")
            },
            text = {
                Text(text = "Are you sure you want to log out of your BrainBox account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Execute logout sequence
                        showLogoutDialog = false // Dismiss dialog first
                        if (enableSoundEffects) soundManager.playSound(R.raw.button_click)

                        Firebase.auth.signOut()
                        Log.d(TAG, "User confirmed and signed out. Navigating to Welcome screen.")

                        // Navigate to the welcome screen and clear the back stack
                        navController.navigate(AppRoutes.Welcome.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Yes, Log Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false // Dismiss the dialog without logging out
                    }
                ) {
                    Text("No, Cancel")
                }
            }
        )
    }
}

// ----------------------------------------------------
// --- REUSABLE COMPONENTS (Kept untouched) ---
// ----------------------------------------------------

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Navigate")
        }
    }
}

@Composable
fun SettingsToggleCard(
    icon: ImageVector,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController())
}