package com.example.brainbox.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.brainbox.navigation.AppRoutes
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
// Assuming FileItem is defined in models.kt and needs an import
import com.example.brainbox.ui.screens.FileItem
import java.time.Year
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


private const val TAG = "PdfListScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfListScreen(navController: NavController, subject: String, category: String) {
    var files by remember { mutableStateOf<List<FileItem>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // --- State Definitions for the three levels of navigation ---
    val isRootPastPapers = category == "Past_Papers"
    val isPaperTypeSelection = category == "Paper_1" || category == "Paper_2"

    // Level 3: "Notes" or "Paper_1%2F2024" (or similar encoded path)
    val isFileListingMode = !isRootPastPapers && !isPaperTypeSelection


    // Updated to handle categories with slashes for better display
    val displayCategory = category.replace("_", " ").replace("%2F", " - ") // Handles URL-encoded slash for display
    val title = "$subject $displayCategory".replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    // --- Years for Navigation (Last 5 Years) ---
    val currentYear = remember { java.time.Year.now().value }
    // Generates a list of the last 5 years as strings
    val fiveYears = remember { (0..4).map { currentYear - it }.map { it.toString() } }


    // --- File Fetching Logic ---
    // Runs if we are in the final file listing mode
    LaunchedEffect(subject, category, isFileListingMode) {
        if (isFileListingMode) {
            isLoading = true
            error = null
            try {
                // *** FIX 2: Decode the category path for Firebase Storage lookup ***
                // The category might be URL-encoded (e.g., Paper_1%2F2024), so we must decode it.
                val decodedCategory = URLDecoder.decode(category, StandardCharsets.UTF_8.toString())
                val storagePath = "study_materials/$subject/$decodedCategory"
                val storageRef = Firebase.storage.reference.child(storagePath)

                Log.d(TAG, "Fetching files from path: $storagePath (Decoded: $decodedCategory)")

                val listResult = storageRef.listAll().await()
                val fileItems = listResult.items.map { storageItem ->
                    val downloadUrl = storageItem.downloadUrl.await().toString()
                    Log.d(TAG, "File found: ${storageItem.name}, URL: $downloadUrl") // Log URL for debugging
                    FileItem(name = storageItem.name, url = downloadUrl)
                }
                files = fileItems
                Log.d(TAG, "Fetched ${fileItems.size} files from Firebase Storage.")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching files from Firebase Storage", e)
                error = "Failed to load files: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                .background(Color(0xFFF0F2F5))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {

            // --- STATE 1: Paper Type Selection (Level 1) ---
            if (isRootPastPapers) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Paper Type",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Button for Paper 1 - Navigates to Level 2 (Year Selection)
                    PastPaperCategoryCard(
                        title = "Paper 1",
                        description = "Past papers divided by year for Paper 1.",
                        icon = Icons.Default.Description,
                        backgroundColor = Color(0xFF00008B),
                        onClick = {
                            navController.navigate(
                                AppRoutes.PDF_LIST.createRoute(subject = subject, category = "Paper_1")
                            )
                        }
                    )

                    // Button for Paper 2 - Navigates to Level 2 (Year Selection)
                    PastPaperCategoryCard(
                        title = "Paper 2",
                        description = "Past papers divided by year for Paper 2.",
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        backgroundColor = Color(0xFFFFA000),
                        onClick = {
                            navController.navigate(
                                AppRoutes.PDF_LIST.createRoute(subject = subject, category = "Paper_2")
                            )
                        }
                    )
                }

                // --- STATE 2: Year Selection (Level 2) ---
            } else if (isPaperTypeSelection) {
                val paperNumber = category.split("_").last() // Extracts "1" or "2"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Paper $paperNumber: Select Year",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp)
                    ) {
                        items(fiveYears) { year ->
                            PastPaperCategoryCard(
                                title = "$year Papers",
                                description = "All $year Paper $paperNumber examination papers.",
                                icon = Icons.Default.CalendarToday, // Calendar icon for years
                                backgroundColor = Color(0xFF0C2521), // Light Blue for years
                                onClick = {
                                    // *** FIX 1: URL-encode the category path to prevent the crash
                                    val fullCategoryPath = "$category/$year"
                                    val encodedCategory = URLEncoder.encode(fullCategoryPath, StandardCharsets.UTF_8.toString())

                                    navController.navigate(
                                        AppRoutes.PDF_LIST.createRoute(
                                            subject = subject,
                                            // The category argument is now URL-encoded
                                            category = encodedCategory
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                // --- STATE 3: File Listing Mode ("Notes" or a specific year/paper) ---
            } else {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    }
                    error != null -> {
                        Text(
                            text = error ?: "An unknown error occurred.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    files.isNullOrEmpty() -> {
                        // The decoded path is more helpful for the user
                        val decodedCategory = URLDecoder.decode(category, StandardCharsets.UTF_8.toString())
                        Text(
                            text = "No files found for this section. Please check your Firebase Storage path: study_materials/$subject/$decodedCategory",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(files!!) { file ->
                                FileListItem(file = file) {
                                    // Navigate to the PDF Viewer screen
                                    navController.navigate(
                                        AppRoutes.PDF_VIEWER.createRoute(pdfUrl = file.url)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Reusable card component (unchanged)
@Composable
fun PastPaperCategoryCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
fun FileListItem(file: FileItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "PDF File",
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = file.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}