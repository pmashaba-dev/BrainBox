package com.example.brainbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.navigation.AppRoutes
import com.example.brainbox.ui.components.StudyHubCard
import com.airbnb.lottie.compose.*
import com.example.brainbox.R

/**
 * Composable for the Science Study Hub screen.
 * This screen displays cards for Physical Sciences Notes and Past Papers.
 *
 * @param navController The NavController for screen navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScienceStudyHubScreen(navController: NavController) {
    // Load the Lottie animation from the raw resource directory.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.exams_preparation))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Physical Sciences Study Hub", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Displays the study animation.
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )

            // Physical Sciences Notes Card
            StudyHubCard(
                title = "Physical Sciences Notes",
                icon = Icons.Default.Science,
                backgroundColor = Color(0xFF0D8CEC), // Light green
                onClick = { navController.navigate(AppRoutes.PDF_LIST.route.replace("{subject}", "Physical_Sciences").replace("{category}", "Notes")) }
            )

            // Physical Sciences Past Papers Card
            StudyHubCard(
                title = "Past Papers",
                icon = Icons.Default.Description,
                backgroundColor = Color(0xFFDEB50A), // Yellow
                onClick = { navController.navigate(AppRoutes.PDF_LIST.route.replace("{subject}", "Physical_Sciences").replace("{category}", "Past_Papers")) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScienceStudyHubScreenPreview() {
    ScienceStudyHubScreen(navController = rememberNavController())
}