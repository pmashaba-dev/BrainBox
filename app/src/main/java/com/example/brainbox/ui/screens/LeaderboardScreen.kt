package com.example.brainbox.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

private const val TAG = "LeaderboardScreen"

/**
 * Data class to represent a user on the leaderboard.
 * @property userId The Firebase Authentication UID.
 * @property username The user's display name.
 * @property points The user's total points.
 */
data class User(
    val userId: String, // Added to check for current user
    val username: String,
    val points: Long
)

// --- Color Constants for Medals ---
val Gold = Color(0xFFFFD700)
val Silver = Color(0xFFC0C0C0)
val Bronze = Color(0xFFCD7F32)

/**
 * Composable function for the leaderboard screen.
 * It fetches and displays a list of users ordered by their points.
 * @param onGoBack Callback to handle navigating back from the leaderboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onGoBack: () -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserId = Firebase.auth.currentUser?.uid

    // Use a LaunchedEffect to fetch the leaderboard data when the screen is first composed
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val db = Firebase.firestore
                val profilesCollection = db.collection("profiles")

                // Fetch all profiles, ordered by points in descending order
                val querySnapshot = profilesCollection
                    .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val fetchedUsers = mutableListOf<User>()
                for (document in querySnapshot.documents) {
                    val points = document.getLong("points") ?: 0
                    val username = document.getString("username") ?: "Unknown"
                    // Use document.id as the userId
                    fetchedUsers.add(User(document.id, username, points))
                }

                users = fetchedUsers
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching leaderboard data", e)
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                }
                users.isEmpty() -> {
                    Text(
                        text = "No users found on the leaderboard.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(users) { index, user ->
                            LeaderboardItem(
                                rank = index + 1,
                                user = user,
                                isCurrentUser = user.userId == currentUserId
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Medal Composable ---
@Composable
fun MedalIcon(rank: Int) {
    val color: Color
    val icon: ImageVector?
    val contentDescription: String

    when (rank) {
        1 -> {
            color = Gold
            icon = Icons.Filled.Star
            contentDescription = "Gold Medal"
        }
        2 -> {
            color = Silver
            icon = Icons.Filled.Star
            contentDescription = "Silver Medal"
        }
        3 -> {
            color = Bronze
            icon = Icons.Filled.Star
            contentDescription = "Bronze Medal"
        }
        else -> {
            // For ranks 4+ just display the number
            Box(
                modifier = Modifier
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            return
        }
    }

    // Medal Icon for ranks 1-3
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon!!,
            contentDescription = contentDescription,
            tint = Color.Black.copy(alpha = 0.8f), // Dark tint for contrast
            modifier = Modifier.size(20.dp)
        )
    }
}
// --- Leaderboard Item Composable ---

/**
 * Composable for a single leaderboard item.
 * @param rank The rank of the user.
 * @param user The user data.
 * @param isCurrentUser If the item belongs to the logged-in user.
 */
@Composable
fun LeaderboardItem(rank: Int, user: User, isCurrentUser: Boolean) {
    val cardColor = when {
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) // Highlight current user
        rank <= 3 -> MaterialTheme.colorScheme.surfaceVariant // Highlight top 3
        else -> MaterialTheme.colorScheme.surface
    }

    val elevation = if (isCurrentUser || rank <= 3) CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    else CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        elevation = elevation,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank / Medal Icon
            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.CenterStart) {
                MedalIcon(rank = rank)
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Username
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrentUser) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Points
            Text(
                text = "${user.points} pts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}