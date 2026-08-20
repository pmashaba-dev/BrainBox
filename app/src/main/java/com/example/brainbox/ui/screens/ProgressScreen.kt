// File: app/src/main/java/com/example/brainbox/ui/screens/ProgressScreen.kt

package com.example.brainbox.ui.screens

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.ui.components.QuizHistoryItemCard
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private const val TAG = "ProgressScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var quizResults by remember { mutableStateOf<List<QuizResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ADDED: Coroutine scope and context for PDF generation
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // ADDED: Derived state to calculate stats once quizResults is loaded
    val overallStats by remember(quizResults) {
        derivedStateOf {
            val totalCorrect = quizResults.sumOf { it.score.toInt() }
            val totalQuestions = quizResults.sumOf { it.totalQuestions.toInt() }
            val overallAverageScore = if (totalQuestions > 0) (totalCorrect.toDouble() / totalQuestions) * 100.0 else 0.0

            val topicAccuracies = quizResults
                .groupBy { it.topic }
                .mapValues { (_, results) ->
                    val topicTotalCorrect = results.sumOf { it.score.toInt() }
                    val topicTotalQuestions = results.sumOf { it.totalQuestions.toInt() }
                    if (topicTotalQuestions > 0) (topicTotalCorrect.toDouble() / topicTotalQuestions) * 100.0 else 0.0
                }
                .toSortedMap(compareBy { it })

            val lowestAccuracyTopic = topicAccuracies
                .filter { it.value < 70.0 } // Weak area defined as < 70%
                .minByOrNull { it.value }?.key

            // Average Score, Topic Mastery Map, Weakest Topic
            Triple(overallAverageScore, topicAccuracies, lowestAccuracyTopic)
        }
    }
    val overallAverageScore = overallStats.first
    val topicAccuracies = overallStats.second
    val weakAreaTopic = overallStats.third

    LaunchedEffect(key1 = Unit) {
        isLoading = true
        val auth = Firebase.auth
        val db = Firebase.firestore
        val currentUser = auth.currentUser

        if (currentUser != null) {
            try {
                val userRef = db.collection("profiles").document(currentUser.uid)
                val userProfileDoc = withContext(Dispatchers.IO) { userRef.get().await() }
                // NOTE: UserProfile needs a 'username' or similar field for the PDF file name
                userProfile = userProfileDoc.toObject(UserProfile::class.java)

                val quizResultsRef = db.collection("quiz_results")
                    .whereEqualTo("userId", currentUser.uid)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                val quizResultsDocs = withContext(Dispatchers.IO) { quizResultsRef.get().await() }
                quizResults = quizResultsDocs.toObjects(QuizResult::class.java)

                Log.d(TAG, "Fetched ${quizResults.size} quiz results.")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user data or quiz results", e)
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Progress", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Stats Section
                    Text(
                        text = "Overall Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProgressStatCard(
                            title = "Points",
                            value = "${userProfile!!.points}",
                            icon = Icons.Default.Star,
                            iconTint = Color(0xFFFFC107)
                        )
                        ProgressStatCard(
                            title = "Trophies",
                            value = "${userProfile!!.trophies}",
                            icon = Icons.Default.EmojiEvents,
                            iconTint = Color(0xFF4CAF50)
                        )
                        ProgressStatCard(
                            title = "Quizzes",
                            value = "${userProfile!!.quizzesCompleted}",
                            icon = Icons.AutoMirrored.Filled.ListAlt,
                            iconTint = Color(0xFF2196F3)
                        )
                    }

                    // Topic Breakdown and Weak Area Identification
                    TopicBreakdownCard(quizResults = quizResults)

                    Spacer(modifier = Modifier.height(16.dp))

                    // ADDED: Download Progress Report Button
                    Button(
                        onClick = {
                            val profile = userProfile
                            val history = quizResults
                            val weakTopic = weakAreaTopic
                            val topics = topicAccuracies.mapValues { (_, value) -> value.toFloat() } // Convert to Float for existing component
                            if (profile != null) {
                                coroutineScope.launch {
                                    generateAndSavePdfReport(
                                        context = context,
                                        profile = profile,
                                        avgScore = overallAverageScore,
                                        weakestTopic = weakTopic,
                                        topicMastery = topicAccuracies, // Pass original Double map for precision in PDF
                                        history = history // Pass full history for PDF table
                                    )
                                }
                            } else {
                                Toast.makeText(context, "User profile not loaded. Cannot generate report.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Download Report")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Progress Report", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detailed Quiz History Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Quiz History",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (quizResults.isEmpty()) {
                                Text(
                                    text = "No quizzes completed yet.",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(216.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 0.dp)
                                ) {
                                    items(quizResults) { result ->
                                        QuizHistoryItemCard(
                                            quizResult = result,
                                            onClick = {
                                                Log.d(TAG, "Quiz history card clicked: ${result.topic}")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Could not load user data.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * A reusable composable for displaying a stat card.
 */
@Composable
fun ProgressStatCard(title: String, value: String, icon: ImageVector, iconTint: Color) {
    Card(
        modifier = Modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

/**
 * Displays a breakdown of quiz results by topic and identifies the weakest one.
 */
@Composable
fun TopicBreakdownCard(quizResults: List<QuizResult>) {
    // Group quiz results by topic and calculate accuracy for each
    val topicAccuracies = quizResults
        .groupBy { it.topic }
        .mapValues { (_, results) ->
            val totalCorrect = results.sumOf { it.score.toInt() }
            val totalQuestions = results.sumOf { it.totalQuestions.toInt() }
            if (totalQuestions > 0) {
                (totalCorrect.toFloat() / totalQuestions) * 100
            } else {
                0f
            }
        }

    // Find the topic with the lowest accuracy
    val lowestAccuracyTopic = topicAccuracies.minByOrNull { it.value }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Topic Mastery Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (topicAccuracies.isEmpty()) {
                Text(
                    text = "No quiz data available for any topics.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    topicAccuracies.forEach { (topic, accuracy) ->
                        Text(
                            text = "$topic: ${"%.1f".format(accuracy)}%",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    // Weak area identification section
    if (lowestAccuracyTopic != null && lowestAccuracyTopic.value < 70f) {
        Spacer(modifier = Modifier.height(16.dp))
        WeakAreaTipCard(lowestAccuracyTopic.key)
    }
}

/**
 * A composable card that displays a tip about the user's weak area.
 */
@Composable
fun WeakAreaTipCard(lowestTopic: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)) // Light orange background
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFE65100), // Dark orange
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Weak Area Identified",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your lowest accuracy is in *$lowestTopic*. Focus on this topic to improve your overall score!",
                    fontSize = 14.sp,
                    color = Color(0xFFE65100).copy(alpha = 0.8f),
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}

// UPDATED: New Suspend Function for PDF Generation with Multi-Page and Table Fixes
suspend fun generateAndSavePdfReport(
    context: Context,
    profile: UserProfile,
    avgScore: Double,
    weakestTopic: String?,
    topicMastery: Map<String, Double>,
    history: List<QuizResult>
) = withContext(Dispatchers.IO) {
    val usernamePart = if (profile.username.isNullOrEmpty()) "User" else profile.username.replace(" ", "_").take(10)
    val fileName = "BrainBox_Progress_Report_$usernamePart.pdf"
    val reportDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    val email = Firebase.auth.currentUser?.email ?: "N/A"

    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // --- PDF Constants ---
        var yPosition = 50f
        val margin = 50f
        val lineHeight = 20f
        val contentEnd = 800f // Effective page height limit before new page
        val maxPageWidth = 595f - 2 * margin

        // --- PDF Drawing Setup ---
        val titlePaint = Paint().apply { textSize = 26f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.DKGRAY }
        val headerPaint = Paint().apply { textSize = 18f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.parseColor("#4B0082") }
        val bodyPaint = Paint().apply { textSize = 14f; color = android.graphics.Color.BLACK }
        val secondaryBodyPaint = Paint().apply { textSize = 14f; color = android.graphics.Color.DKGRAY }

        // Helper function to check space and start a new page
        val checkPageBreak: (Float) -> Unit = { requiredSpace ->
            if (yPosition + requiredSpace > contentEnd) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = margin // Reset yPosition for the new page
                // Redraw a minimal header on the new page
                canvas.drawText("User Progress Report (Cont.)", margin, yPosition, headerPaint)
                yPosition += lineHeight * 2
            }
        }

        // UPDATED Helper function to draw a table with fixed column widths for readability
        val drawTable: (List<String>, List<List<String>>, List<Float>) -> Unit = { columns, rows, colWeights ->
            val numCols = columns.size
            val totalWeight = colWeights.sum()
            val colWidths = colWeights.map { it * maxPageWidth / totalWeight }
            val rowHeight = 25f
            val tableHeaderPaint = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = android.graphics.Color.WHITE }
            val tableBodyPaint = Paint().apply { textSize = 12f; color = android.graphics.Color.BLACK }
            val tableBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#4B0082") }

            // Iterate over rows and handle page breaks
            rows.chunked(100).forEach { chunk -> // Chunking large tables can also help manage memory, though page break handles visuals

                checkPageBreak(rowHeight * (chunk.size + 1) + lineHeight) // Check space for header + all rows

                // Draw Header Row (Only draw if it's the start of the table or a new page)
                var xPos = margin
                canvas.drawRect(margin, yPosition, margin + maxPageWidth, yPosition + rowHeight, tableBgPaint)
                for (i in 0 until numCols) {
                    canvas.drawText(columns[i], xPos + 5, yPosition + rowHeight - 7, tableHeaderPaint)
                    xPos += colWidths[i]
                }
                yPosition += rowHeight

                // Draw Data Rows
                chunk.forEachIndexed { rowIndex, row ->
                    checkPageBreak(rowHeight) // Check space for the next row

                    xPos = margin
                    val rowColor = if (rowIndex % 2 == 0) android.graphics.Color.parseColor("#F0F0F0") else android.graphics.Color.WHITE
                    canvas.drawRect(margin, yPosition, margin + maxPageWidth, yPosition + rowHeight, Paint().apply { color = rowColor })

                    for (i in row.indices) {
                        // Truncate text if it's too long for the column (especially for topic)
                        var cellText = row[i]
                        val maxChars = (colWidths[i] / tableBodyPaint.textSize * 1.5).toInt()
                        if (cellText.length > maxChars) {
                            cellText = cellText.substring(0, maxChars - 3) + "..."
                        }

                        canvas.drawText(cellText, xPos + 5, yPosition + rowHeight - 7, tableBodyPaint)
                        xPos += colWidths[i]
                    }
                    yPosition += rowHeight
                }
                yPosition += 10f // Space after table section
            }
        }

        // --- 0. Report Title ---
        canvas.drawText("User Progress Report – BrainBox", margin, yPosition, titlePaint)
        yPosition += lineHeight
        canvas.drawText("Date Generated: $reportDate", margin, yPosition, secondaryBodyPaint)

        // 1. User Profile Information
        checkPageBreak(lineHeight * 6)
        yPosition += lineHeight * 1.5f
        canvas.drawText("1. User Profile Information", margin, yPosition, headerPaint)
        yPosition += lineHeight
        canvas.drawLine(margin, yPosition, 545f, yPosition, headerPaint)
        yPosition += 10f

        val userProfileLines = listOf(
            "Name: ${profile.username}",
            "Email: $email",
            "Quizzes Completed: ${profile.quizzesCompleted}",
            "Total Points: ${profile.points}",
            "Trophies: ${profile.trophies}"
        )
        userProfileLines.forEach { line ->
            canvas.drawText(line, margin, yPosition, bodyPaint)
            yPosition += lineHeight
        }

        // 2. Overall Statistics
        checkPageBreak(lineHeight * 6)
        yPosition += lineHeight
        canvas.drawText("2. Overall Statistics", margin, yPosition, headerPaint)
        yPosition += lineHeight

        val overallStatsColumns = listOf("Metric", "Value")
        val overallStatsRows = listOf(
            listOf("Total Points", "${profile.points}"),
            listOf("Trophies", "${profile.trophies}"),
            listOf("Quizzes Completed", "${profile.quizzesCompleted}"),
            listOf("Overall Average Score", "${"%.2f".format(avgScore)}%")
        )
        // Use 1:1 weight for stats table
        drawTable(overallStatsColumns, overallStatsRows, listOf(1f, 1f))

        // 3. Topic Mastery Overview
        checkPageBreak(lineHeight * 2 + topicMastery.size * 25f)
        canvas.drawText("3. Topic Mastery Overview", margin, yPosition, headerPaint)
        yPosition += lineHeight

        val masteryColumns = listOf("Topic", "Accuracy (%)")
        // Filter topic mastery for the report (only showing weak areas for the "weak area highlight" section)
        val masteryRows = topicMastery.map { (topic, accuracy) ->
            listOf(topic, "${accuracy.roundToInt()}%")
        }.toList()

        // Use 2:1 weight for Topic Mastery table to accommodate longer topic names
        if (masteryRows.isNotEmpty()) {
            drawTable(masteryColumns, masteryRows, listOf(2f, 1f))
        } else {
            canvas.drawText("No topic data available yet.", margin, yPosition, bodyPaint)
            yPosition += lineHeight * 1.5f
        }


        // 4. Weak Area Highlight
        checkPageBreak(lineHeight * 4)
        canvas.drawText("4. Weak Area Highlight", margin, yPosition, headerPaint)
        yPosition += lineHeight
        if (weakestTopic != null) {
            val weakAreaPaint = Paint().apply { textSize = 14f; color = android.graphics.Color.parseColor("#D32F2F"); typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvas.drawText("⚠️ Weak Area Identified: $weakestTopic", margin, yPosition, weakAreaPaint)
            yPosition += lineHeight
            canvas.drawText("Recommendation: Review notes and practice quizzes on this topic.", margin, yPosition, secondaryBodyPaint)
        } else {
            canvas.drawText("No significant weak areas detected (below 70% accuracy).", margin, yPosition, bodyPaint)
        }
        yPosition += lineHeight * 1.5f


        // 5. Quiz History (Recent Quizzes)
        checkPageBreak(lineHeight * 2 + history.size * 25f) // Check space for section header + all rows
        canvas.drawText("5. Recent Quiz History (Last ${history.size})", margin, yPosition, headerPaint)
        yPosition += lineHeight

        val historyColumns = listOf("Date", "Topic", "Score", "Accuracy (%)")
        val historyRows = history.map { result ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(result.timestamp))
            val accuracy = ((result.score.toDouble() / result.totalQuestions.toDouble()) * 100).roundToInt()
            listOf(date, result.topic, "${result.score}/${result.totalQuestions}", "${accuracy}%")
        }

        // Use custom weights: Date(1), Topic(2), Score(1), Accuracy(1)
        if (historyRows.isNotEmpty()) {
            // This table will now span multiple pages if 'history' is long.
            drawTable(historyColumns, historyRows, listOf(1f, 2f, 1f, 1f))
        } else {
            canvas.drawText("No recent quiz history to display.", margin, yPosition, bodyPaint)
            yPosition += lineHeight * 1.5f
        }


        // 6. Summary Message
        checkPageBreak(lineHeight * 4)
        canvas.drawText("6. Summary Message", margin, yPosition, headerPaint)
        yPosition += lineHeight
        val performanceStatus = when {
            avgScore >= 80 -> "Excellent"
            avgScore >= 65 -> "Good"
            else -> "Developing"
        }

        val summaryMessage = "Current Performance Status: $performanceStatus. Keep practicing! Consistency leads to mastery. You’re making great progress on BrainBox."

        canvas.drawText(summaryMessage, margin, yPosition, bodyPaint)
        yPosition += lineHeight


        pdfDocument.finishPage(page)

        // Save the PDF
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "✅ Report saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        }

    } catch (e: Exception) {
        Log.e(TAG, "PDF Generation failed", e)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "❌ Failed to download report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

// ... (ProgressStatCard, TopicBreakdownCard, WeakAreaTipCard, and Preview remain the same)