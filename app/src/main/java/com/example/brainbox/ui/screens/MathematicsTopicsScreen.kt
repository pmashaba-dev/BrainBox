package com.example.brainbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.navigation.AppRoutes
import com.example.brainbox.ui.components.TopicListItem

// Topic → Color Mapping
private val topicColors = mapOf(
    "Algebra" to Color(0xFF4B0082),              // Indigo
    "Number Patterns" to Color(0xFF4B0082),      // Indigo
    "Functions and Graphs" to Color(0xFF4B0082), // Indigo
    "Financial Mathematics" to Color(0xFF4B0082),// Indigo

    "Euclidean Geometry" to Color(0xFF4B0082),   // Emerald
    "Analytical Geometry" to Color(0xFF4B0082),  // Emerald

    "Trigonometry" to Color(0xFF4B0082),         // Coral
    "Differential Calculus" to Color(0xFF4B0082),// Coral

    "Probability" to Color(0xFF4B0082),          // Teal
    "Statistics and Data Handling" to Color(0xFF4B0082)
)

/**
 * A composable screen that displays a list of mathematics topics with enhanced UI/UX.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathematicsTopicsScreen(navController: NavController) {
    val mathematicsTopics = listOf(
        "Algebra",
        "Number Patterns",
        "Functions and Graphs",
        "Financial Mathematics",
        "Trigonometry",
        "Euclidean Geometry",
        "Analytical Geometry",
        "Probability",
        "Differential Calculus",
        "Statistics and Data Handling"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Mathematics Topics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF87CEFA), // Sky Blue
                            Color(0xFFE6E6FA)  // Lavender
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mathematicsTopics) { topic ->
                    TopicListItem(
                        topicName = topic,
                        color = topicColors[topic] ?: MaterialTheme.colorScheme.primary,
                        onClick = {
                            navController.navigate(
                                AppRoutes.GameModes.createRoute(
                                    topicName = topic,
                                    parentRoute = AppRoutes.MathematicsTopics.route
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MathematicsTopicsScreenPreview() {
    MathematicsTopicsScreen(navController = rememberNavController())
}
