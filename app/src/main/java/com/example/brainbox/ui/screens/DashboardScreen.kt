package com.example.brainbox.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.brainbox.R
import com.example.brainbox.navigation.AppRoutes
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.util.Calendar

private const val TAG = "DashboardScreen"

@Composable
fun DashboardScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var underConstructionMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    val auth = remember { Firebase.auth }
    val db = remember { Firebase.firestore }

    LaunchedEffect(auth.currentUser) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            isLoadingProfile = true
            db.collection("profiles").document(currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        isLoadingProfile = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // The updated UserProfile will correctly handle the new isAdmin field
                        userProfile = snapshot.toObject(UserProfile::class.java) ?: UserProfile()
                        Log.d(TAG, "User profile updated from real-time listener. Is Admin: ${userProfile.isAdmin}")
                    } else {
                        Log.d(TAG, "User profile document does not exist.")
                    }
                    isLoadingProfile = false
                }
        } else {
            userProfile = UserProfile()
            isLoadingProfile = false
        }
    }

    LaunchedEffect(underConstructionMessage) {
        if (underConstructionMessage != null) {
            delay(3000L)
            underConstructionMessage = null
        }
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFFBBDEFB)),
                containerColor = Color(0xFFBBDEFB),
                contentColor = Color(0xFF000000)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(AppRoutes.Leaderboard.route) },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFF7289DA),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFF000000)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(AppRoutes.Progress.route) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "My Progress") },
                    label = { Text("Progress") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFF7289DA),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFF000000)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(AppRoutes.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color(0xFF7289DA),
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color(0xFF000000)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF8FAFC), Color(0xFFE3F2FD))
                    )
                )
        ) {
            if (isLoadingProfile) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.Center),
                    strokeWidth = 5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Greeting
                    Text(
                        text = "$greeting, ${userProfile.username}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🌈 Profile card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFE1BEE7), Color(0xFFBBDEFB))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(74.dp)
                                            .clip(CircleShape)
                                            .background(Color.Transparent)
                                            .border(
                                                width = 3.dp,
                                                color = if (userProfile.trophies > 5) Color(0xFFFFD700) else Color(0xFF3F51B5),
                                                shape = CircleShape
                                            )
                                    )

                                    // ✅ Use a safe default avatar if resource is invalid
                                    val avatarId = if (userProfile.avatarResId != 0) {
                                        userProfile.avatarResId
                                    } else {
                                        R.drawable.boy1 // <-- make sure this is a valid PNG/JPG/Vector
                                    }

                                    Image(
                                        painter = painterResource(id = avatarId),
                                        contentDescription = "User Avatar",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                    )
                                }


                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = userProfile.username,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Keep going, champ!\uD83D\uDC4D",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF6F61)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    StatChip(
                                        icon = Icons.Default.Star,
                                        iconTint = Color(0xFFFFC107),
                                        label = if (userProfile.points > 1000) "${userProfile.points / 1000}k" else userProfile.points.toString(),
                                        textColor = Color.Black
                                    )
                                    StatChip(
                                        icon = Icons.Default.EmojiEvents,
                                        iconTint = Color(0xFF4CAF50),
                                        label = userProfile.trophies.toString(),
                                        textColor = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2196F3).copy(alpha = 0.8f)) // Blue500
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFC107).copy(alpha = 0.8f)) // Amber500
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.8f)) // Green500
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dashboard grid fills space and scrolls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardGridCard(
                                modifier = Modifier.weight(1f),
                                title = "Mathematics",
                                icon = Icons.Default.Calculate,
                                gradientColors = listOf(Color(0xFF9FA8DA), Color(0xFF7E57C2))
                            ) {
                                try {
                                    navController.navigate(AppRoutes.MathematicsTopics.route)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Navigation failed: ${e.message}", e)
                                    underConstructionMessage = "Mathematics screen unavailable."
                                }
                            }
                            DashboardGridCard(
                                modifier = Modifier.weight(1f),
                                title = "Physical Sciences",
                                icon = Icons.Default.Science,
                                gradientColors = listOf(Color(0xFF4DB6AC), Color(0xFF2E7D32))
                            ) {
                                try {
                                    navController.navigate(AppRoutes.PhysicalSciencesTopics.route)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Navigation failed: ${e.message}", e)
                                    underConstructionMessage = "Physical Sciences screen unavailable."
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardGridCard(
                                modifier = Modifier.weight(1f),
                                title = "Maths Study Hub",
                                icon = Icons.Default.School,
                                gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF00ACC1))
                            ) {
                                navController.navigate(AppRoutes.MathsStudyHub.route)
                            }
                            DashboardGridCard(
                                modifier = Modifier.weight(1f),
                                title = "Science Study Hub",
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                gradientColors = listOf(Color(0xFFFFCC80), Color(0xFFFFB300))
                            ) {
                                navController.navigate(AppRoutes.ScienceStudyHub.route)
                            }
                        }

                        // ⭐ NEW: Admin Dashboard Card (only visible to admins)
                        if (userProfile.isAdmin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DashboardGridCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Admin Dashboard",
                                    icon = Icons.Default.Security, // Using Security for an admin icon
                                    gradientColors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037)) // Dark, prominent colors
                                ) {
                                    try {
                                        navController.navigate(AppRoutes.AdminDashboard.route)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Navigation failed: ${e.message}", e)
                                        underConstructionMessage = "Admin Dashboard screen unavailable."
                                    }
                                }
                            }
                        }
                        // ⭐ END NEW CARD

                    }
                }
            }

            // Snackbar-style under construction message
            AnimatedVisibility(
                visible = underConstructionMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = underConstructionMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun StatChip(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    textColor: Color = Color.Black
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.6f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun DashboardGridCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}