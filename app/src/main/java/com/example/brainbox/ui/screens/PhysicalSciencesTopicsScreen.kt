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

// Category color mapping
private val topicColors = mapOf(
    "Newton's Laws and Applications" to Color(0xFF4B0082),
    "Momentum and Impulse" to Color(0xFF4B0082),
    "Vertical Projectile Motion" to Color(0xFF4B0082),
    "Work, Energy and Power" to Color(0xFF4B0082),
    "Doppler Effect" to Color(0xFF4B0082),
    "Geometrical Optics" to Color(0xFF4B0082),
    "Electrostatics" to Color(0xFF4B0082),
    "Electric Circuits" to Color(0xFF4B0082),
    "Electrodynamics" to Color(0xFF4B0082),
    "Organic Chemistry" to Color(0xFF4B0082),
    "Rates of Reactions" to Color(0xFF4B0082),
    "Chemical Equilibrium" to Color(0xFF4B0082),
    "Acids and Bases" to Color(0xFF4B0082),
    "Electrochemistry" to Color(0xFF4B0082),
    "Vectors" to Color(0xFF4B0082)
)

/**
 * A composable screen that displays a list of physical sciences topics with enhanced UI/UX.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalSciencesTopicsScreen(navController: NavController) {
    val physicalSciencesTopics = listOf(
        "Newton's Laws and Applications",
        "Momentum and Impulse",
        "Vertical Projectile Motion",
        "Work, Energy and Power",
        "Doppler Effect",
        "Geometrical Optics",
        "Electrostatics",
        "Electric Circuits",
        "Electrodynamics",
        "Organic Chemistry",
        "Rates of Reactions",
        "Chemical Equilibrium",
        "Acids and Bases",
        "Electrochemistry",
        "Vectors"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Physical Sciences Topics", fontWeight = FontWeight.Bold) },
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
                            Color(0xFFE6E6FA) // Light Purple
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(physicalSciencesTopics) { topic ->
                    TopicListItem(
                        topicName = topic,
                        color = topicColors[topic] ?: MaterialTheme.colorScheme.primary,
                        onClick = {
                            navController.navigate(
                                AppRoutes.GameModes.createRoute(
                                    topicName = topic,
                                    parentRoute = AppRoutes.PhysicalSciencesTopics.route
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
fun PhysicalSciencesTopicsScreenPreview() {
    PhysicalSciencesTopicsScreen(navController = rememberNavController())
}
