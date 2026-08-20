

package com.example.brainbox.ui.screens

// File: app/src/main/java/com/example/brainbox/ui/screens/AboutScreen.kt



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.R // Assuming R.drawable.brain_icon is your app's logo/icon

/**
 * Composable for the About Brain Box screen.
 * Displays information about the application, version, and developers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Brain Box", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo/Icon
            Image(
                painter = painterResource(id = R.drawable.brainbo), // Placeholder for your icon
                contentDescription = "Brain Box Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "Brain Box",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // App Version (Hardcoded placeholder)
            Text(
                text = "Version 1.0.0 (Build 2025-10-28)",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mission Statement/Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp))
            {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Our Mission",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Brain Box is an educational app designed to make Mathematics and Physical Sciences engaging, interactive, and accessible.\n" +
                                "With quizzes, study notes, and a modern interface, Brain Box empowers learner's to learn effectively and enjoy the process.Our goal is to build your confidence and ensure you are fully prepared for your exams.\n",
                        textAlign = TextAlign.Justify,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Info
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Developed by : BrainBox Team, with a passion for education and technology ❤ .",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreen(navController = rememberNavController())
}