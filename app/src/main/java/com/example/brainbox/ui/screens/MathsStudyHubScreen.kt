package com.example.brainbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.brainbox.R
import com.example.brainbox.navigation.AppRoutes
import com.example.brainbox.ui.components.StudyHubCard

/**
 * Composable for the Mathematics Study Hub screen.
 * This screen displays cards for Mathematics Notes and Past Papers.
 *
 * @param navController The NavController for screen navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathsStudyHubScreen(navController: NavController) {
    // Load the Lottie animation from the raw resource directory.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.exams_preparation))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mathematics Study Hub", fontWeight = FontWeight.Bold) },
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

            // Mathematics Notes Card
            StudyHubCard(
                title = "Mathematics Notes",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                backgroundColor = Color(0xFF0CE5C6), // Light blue
                onClick = { navController.navigate(AppRoutes.PDF_LIST.route.replace("{subject}", "Mathematics").replace("{category}", "Notes")) }
            )

            // Mathematics Past Papers Card
            StudyHubCard(
                title = "Past Papers",
                icon = Icons.Default.Description,
                backgroundColor = Color(0xFFEC9705), // Orange
                onClick = { navController.navigate(AppRoutes.PDF_LIST.route.replace("{subject}", "Mathematics").replace("{category}", "Past_Papers")) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MathsStudyHubScreenPreview() {
    MathsStudyHubScreen(navController = rememberNavController())
}