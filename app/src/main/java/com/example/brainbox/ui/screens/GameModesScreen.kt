package com.example.brainbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.navigation.AppRoutes
import com.example.brainbox.R
import com.airbnb.lottie.compose.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Brush

/**
 * A composable screen that displays a list of available game modes for a selected topic.
 *
 * @param navController The NavController for screen navigation.
 * @param topicName The name of the topic selected by the user.
 * @param parentRoute The route of the parent screen to return to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModesScreen(navController: NavController, topicName: String, parentRoute: String) {
    // List of game modes. Currently, only Quiz Game is available.
    val gameModes = listOf("Start Quiz \uD83D\uDD25")

    // Load the Lottie animation from the raw resource directory.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.play_quiz))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$topicName Modes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA), // Light Cyan
                            Color(0xFFF3E5F5)  // Light Purple
                        )
                    )
                )
                .padding(paddingValues),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally // Align items horizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Displays the game mode animation.
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(gameModes) { mode ->
                    GameModeListItem(
                        gameModeName = mode,
                        onClick = {
                            navController.navigate(
                                AppRoutes.QuizGame.createRoute(
                                    topicName = topicName,
                                    parentRoute = parentRoute
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * A reusable composable for displaying a single game mode item.
 *
 * This composable is defined here, so no import is needed.
 *
 * @param gameModeName The name of the game mode to display.
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
fun GameModeListItem(gameModeName: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f)
    val cardColor = Color(0xFFE70D0D) // A vibrant coral color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                cardColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    pressed = true
                    onClick()
                    pressed = false
                }
            )
            .border(
                width = 1.dp,
                color = cardColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = gameModeName,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = cardColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameModesScreenPreview() {
    GameModesScreen(navController = rememberNavController(), topicName = "Newton's Laws and Applications", parentRoute = AppRoutes.PhysicalSciencesTopics.route)
}
