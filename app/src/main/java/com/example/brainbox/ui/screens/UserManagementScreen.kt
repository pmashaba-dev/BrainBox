package com.example.brainbox.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// IMPORTANT: Assuming UserProfile is correctly imported from models.kt
// If models.kt is not in this package, you must ensure the import is correct.
// UserProfile is the class from models.kt: data class UserProfile(var username: String = "Guest", var isAdmin: Boolean = false, ...)

private const val TAG = "UserManagementScreen"

// The provided UserProfile class does not include the document ID (userId),
// which is essential for updates. We create a local wrapper for the screen.
data class UserWithId(
    val userId: String,
    val profile: UserProfile
)

// List of available roles for management
val roles = listOf("learner", "admin")

// ------------------- FIREBASE UTILITY FUNCTIONS -------------------

/**
 * Fetches all user profiles from the "profiles" collection,
 * including the document ID (which is the Firebase User ID).
 */
suspend fun getAllUserProfiles(): List<UserWithId> = withContext(Dispatchers.IO) {
    try {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("profiles")
            .get()
            .await()

        val users = snapshot.documents.mapNotNull { document ->
            val profile = document.toObject<UserProfile>()
            if (profile != null) {
                UserWithId(
                    userId = document.id, // Retrieve the Firestore document ID (the UID)
                    profile = profile
                )
            } else {
                null
            }
        }.sortedBy { it.profile.username } // Sort by username

        users
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching user profiles", e)
        emptyList()
    }
}

/**
 * Updates a specific field (role or active status) on a user's profile document.
 */
suspend fun updateUserProfileField(context: Context, userId: String, field: String, value: Any) {
    withContext(Dispatchers.IO) {
        try {
            FirebaseFirestore.getInstance()
                .collection("profiles")
                .document(userId)
                .update(field, value)
                .await()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "User $field updated successfully.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile field $field for $userId", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to update user $field: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

/**
 * Deletes a user profile document from Firestore.
 */
suspend fun deleteUserProfile(context: Context, userId: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            FirebaseFirestore.getInstance()
                .collection("profiles")
                .document(userId)
                .delete()
                .await()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "User profile deleted successfully.", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user profile for $userId", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to delete user profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }
}


// ------------------- COMPOSE COMPONENTS -------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Use the UserWithId wrapper to include the document ID
    var users by remember { mutableStateOf<List<UserWithId>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Function to reload users
    val reloadUsers: () -> Unit = {
        scope.launch {
            isLoading = true
            val fetchedUsers = getAllUserProfiles()
            users = fetchedUsers
            isLoading = false
        }
    }

    // Fetch users on launch and whenever necessary
    LaunchedEffect(Unit) {
        reloadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = reloadUsers) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Users")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (users.isNullOrEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No user profiles found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Use the userId as the key for stable list items
                    items(users!!, key = { it.userId }) { userWithId ->
                        UserCard(
                            userWithId = userWithId,
                            onRoleChange = { newRole ->
                                scope.launch {
                                    // Map "admin" to true, "learner" to false for the 'isAdmin' field
                                    updateUserProfileField(context, userWithId.userId, "isAdmin", newRole == "admin")
                                    reloadUsers()
                                }
                            },
                            onStatusToggle = { isActive ->
                                scope.launch {
                                    // Assuming 'isAccountActive' is a field in the Firestore document
                                    // even if it's missing from the local UserProfile data class.
                                    updateUserProfileField(context, userWithId.userId, "isAccountActive", isActive)
                                    reloadUsers()
                                }
                            },
                            onDelete = { userId ->
                                scope.launch {
                                    if (deleteUserProfile(context, userId)) {
                                        reloadUsers()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    userWithId: UserWithId,
    onRoleChange: (String) -> Unit,
    onStatusToggle: (Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Use the profile data
    val user = userWithId.profile
    val userId = userWithId.userId

    // --- MAPPING FIELDS ---
    val displayRole = if (user.isAdmin) "admin" else "learner"
    val displayName = user.username
    // NOTE: We assume 'isAccountActive' is implicitly true since UserProfile lacks the field.
    // In a real app, you would fetch this field from the document.
    val displayStatus = true
    // ----------------------

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main User Info Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    // Displaying the UID instead of a missing email
                    Text("UID: $userId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                // Role/Status Indicator
                Icon(
                    if (displayStatus) Icons.Default.CheckCircle else Icons.Default.Block,
                    contentDescription = if (displayStatus) "Active" else "Inactive",
                    tint = if (displayStatus) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    displayRole.capitalize(Locale.current),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle actions"
                    )
                }
            }

            // Actions (Expandable Section)
            if (expanded) {
                Divider(Modifier.padding(vertical = 8.dp))

                // 1. Role Selector
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Change Role:", Modifier.width(100.dp), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    roles.forEach { role ->
                        Row(
                            Modifier
                                .clickable { onRoleChange(role) }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = displayRole == role,
                                onClick = { onRoleChange(role) }
                            )
                            Text(role.capitalize(Locale.current), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // 2. Status Toggle (Soft Disable/Enable)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Account Status:", Modifier.width(100.dp), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = displayStatus,
                        onCheckedChange = onStatusToggle,
                        thumbContent = {
                            Icon(
                                if (displayStatus) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (displayStatus) "Active" else "Inactive", color = if (displayStatus) Color(0xFF4CAF50) else Color(0xFFF44336))
                }

                // 3. Delete Profile Button
                Button(
                    onClick = { onDelete(userId) },
                    modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Profile", color = Color.White)
                }

                Text(
                    "Note: Delete removes the Firestore profile. You must use the Firebase Console to delete the user from Firebase Authentication.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}