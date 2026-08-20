package com.example.brainbox.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.navigation.AppRoutes
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "AdminDashboardScreen"

// TEMP MODEL (Kept untouched)
data class AdminQuizResult(
    val topic: String = "",
    val subject: String = "",
    val score: Long = 0,
    val totalQuestions: Long = 0,
    val startTime: Long = 0,
    val endTime: Long = 0
)


// ------------------- FIREBASE UTILITY FUNCTIONS -------------------
suspend fun getTotalUserCount(): Long = withContext(Dispatchers.IO) {
    try {
        FirebaseFirestore.getInstance().collection("profiles").get().await().size().toLong()
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching total user count", e)
        -1L
    }
}

suspend fun getTotalQuizzesCount(): Long = withContext(Dispatchers.IO) {
    try {
        FirebaseFirestore.getInstance().collection("quizzes").get().await().size().toLong()
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching total quiz count", e)
        -1L
    }
}

suspend fun getTotalQuizzesCompleted(): Long = withContext(Dispatchers.IO) {
    try {
        FirebaseFirestore.getInstance().collection("quiz_results").get().await().size().toLong()
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching total quizzes completed", e)
        -1L
    }
}

suspend fun getAverageQuizScore(): String = withContext(Dispatchers.IO) {
    try {
        val results = FirebaseFirestore.getInstance()
            .collection("quiz_results")
            .get()
            .await()
            .toObjects(AdminQuizResult::class.java)

        if (results.isEmpty()) return@withContext "N/A"

        val totalScore = results.sumOf { it.score }
        val totalQuestions = results.sumOf { it.totalQuestions }

        if (totalQuestions > 0) {
            val avg = (totalScore.toDouble() / totalQuestions.toDouble()) * 100
            String.format("%.1f%%", avg)
        } else "0.0%"
    } catch (e: Exception) {
        Log.e(TAG, "Error calculating avg score", e)
        "Error"
    }
}

// REMOVED: getNewUsersThisWeek()

suspend fun getTopicPerformanceReport(): Map<String, String> = withContext(Dispatchers.IO) {
    try {
        val results = FirebaseFirestore.getInstance()
            .collection("quiz_results")
            .get()
            .await()
            .toObjects(AdminQuizResult::class.java)

        if (results.isEmpty()) return@withContext emptyMap()

        val topicGroups = results.groupBy { it.topic }
        val performance = topicGroups.mapValues { (_, data) ->
            val totalScore = data.sumOf { it.score }
            val totalQuestions = data.sumOf { it.totalQuestions }
            if (totalQuestions > 0) (totalScore.toDouble() / totalQuestions) * 100 else 0.0
        }

        performance.mapValues { String.format("%.0f%%", it.value) }
    } catch (e: Exception) {
        Log.e(TAG, "Error fetching topic report", e)
        emptyMap()
    }
}


// ------------------- PDF GENERATION LOGIC (UPDATED) -------------------

suspend fun generateAndSaveSystemReportPdf(
    context: Context,
    totalUsers: Long?,
    totalQuizzes: Long?,
    quizzesCompleted: Long?,
    avgScore: String,
    topicPerformance: Map<String, String> // 'newUsersWeekly' parameter removed
) = withContext(Dispatchers.IO) {

    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

    var page = document.startPage(pageInfo)
    var canvas = page.canvas
    val paint = Paint()
    var y = 60f

    // --- REPORT HEADER ---
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Admin System Report", 40f, y, paint)
    y += 30f

    paint.isFakeBoldText = false
    paint.textSize = 12f
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText("Generated on: $date", 40f, y, paint)
    y += 50f

    // --- SYSTEM METRICS OVERVIEW ---
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("System Metrics Overview", 40f, y, paint)
    y += 30f

    paint.textSize = 14f
    canvas.drawText("Metric", 40f, y, paint)
    canvas.drawText("Value", 300f, y, paint)
    y += 20f

    paint.textSize = 12f

    val stats = listOf(
        "Total Users" to (totalUsers?.toString() ?: "Loading..."),
        "Total Quizzes" to (totalQuizzes?.toString() ?: "Loading..."),
        "Quizzes Completed" to (quizzesCompleted?.toString() ?: "Loading..."),
        "Avg Score" to avgScore,
    )

    stats.forEach { (name, value) ->
        canvas.drawText(name, 40f, y, paint)
        canvas.drawText(value, 300f, y, paint)
        y += 20f
    }

    // --- TOPIC PERFORMANCE REPORT SECTION ---
    y += 40f // Extra space before the new section

    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("Topic Performance Report", 40f, y, paint)
    y += 30f

    paint.isFakeBoldText = false
    paint.textSize = 14f
    canvas.drawText("Topic", 40f, y, paint)
    canvas.drawText("Avg Score", 300f, y, paint)
    y += 20f

    paint.textSize = 12f

    // Sort the topics by score for a cleaner report
    val sortedTopicPerformance = topicPerformance.toList()
        .sortedByDescending { it.second.filter { c -> c.isDigit() }.toDoubleOrNull() ?: 0.0 }

    if (sortedTopicPerformance.isEmpty()) {
        canvas.drawText("No topic performance data available.", 40f, y, paint)
        y += 20f
    } else {
        sortedTopicPerformance.forEach { (topic, score) ->
            // Check for page overflow (780f is a safe margin from the bottom of 842)
            if (y > 780f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 60f // Reset y for new page header

                // Redraw section header on new page for context
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("Topic Performance Report (Cont.)", 40f, y, paint)
                y += 30f

                paint.isFakeBoldText = false
                paint.textSize = 14f
                canvas.drawText("Topic", 40f, y, paint)
                canvas.drawText("Avg Score", 300f, y, paint)
                y += 20f
                paint.textSize = 12f
            }

            canvas.drawText(topic, 40f, y, paint)
            canvas.drawText(score, 300f, y, paint)
            y += 20f
        }
    }


    document.finishPage(page)

    val fileName = "SystemReport_${System.currentTimeMillis()}.pdf"
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, fileName)

    try {
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Log.e(TAG, "PDF error", e)
    }
}


// ------------------- DOWNLOAD BUTTON -------------------

@Composable
fun DownloadReportButton(
    totalUsers: Long?,
    totalQuizzes: Long?,
    quizzesCompleted: Long?,
    avgScore: String,
    topicPerformance: Map<String, String>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scope.launch {
                generateAndSaveSystemReportPdf(
                    context, totalUsers, totalQuizzes, quizzesCompleted,
                    avgScore, topicPerformance
                )
            }
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    Button(
        onClick = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                scope.launch {
                    generateAndSaveSystemReportPdf(
                        context, totalUsers, totalQuizzes, quizzesCompleted,
                        avgScore, topicPerformance
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFF00C853))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Download PDF Report")
        }
    }
}


// ------------------- DASHBOARD SCREEN (FLOWROW IMPLEMENTED) -------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Added ExperimentalLayoutApi for FlowRow
@Composable
fun AdminDashboardScreen(navController: NavController) {

    var totalUsers by remember { mutableStateOf<Long?>(null) }
    var totalQuizzes by remember { mutableStateOf<Long?>(null) }
    var quizzesCompleted by remember { mutableStateOf<Long?>(null) }
    var avgScore by remember { mutableStateOf("Loading...") }
    // Removed: var newUsersWeekly state variable
    var topicPerformance by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            launch { totalUsers = getTotalUserCount() }
            launch { totalQuizzes = getTotalQuizzesCount() }
            launch { quizzesCompleted = getTotalQuizzesCompleted() }
            launch { avgScore = getAverageQuizScore() }
            // Removed: launch { newUsersWeekly = getNewUsersThisWeek() } coroutine
            launch { topicPerformance = getTopicPerformanceReport() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ----------------- MANAGEMENT TOOLS FIRST -----------------
            item {
                Text("Management Tools", style = MaterialTheme.typography.headlineSmall)
            }

            item {
                ManagementToolCard(
                    title = "User Management",
                    description = "View and delete users.",
                    icon = Icons.Default.ManageAccounts,
                    backgroundColor = Color(0xFFFBC02D),
                    onClick = { navController.navigate(AppRoutes.UserManagement.route) }
                )
            }

            item {
                ManagementToolCard(
                    title = "Quiz Management",
                    description = "Create, edit, or delete quizzes.",
                    icon = Icons.Default.EditNote,
                    backgroundColor = Color(0xFF673AB7),
                    onClick = { navController.navigate(AppRoutes.QuizManagement.route) }
                )
            }

            // ----------------- SYSTEM REPORTS (NOW USES FLOWROW) -----------------
            item {
                Text("System Reports", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                // FLOWROW IMPLEMENTATION
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    val halfWidth = Modifier.fillMaxWidth().weight(1f)

                    // SystemReportCard calls
                    SystemReportCard("Total Users", "${totalUsers ?: "..."}", "Registered accounts.", Icons.Filled.People, Color(0xFF3F51B5), halfWidth)
                    SystemReportCard("Total Quizzes", "${totalQuizzes ?: "..."}", "Published quiz topics.", Icons.AutoMirrored.Filled.ListAlt, Color(0xFFE91E63), halfWidth)
                    SystemReportCard("Quizzes Completed", "${quizzesCompleted ?: "..."}", "Total attempts.", Icons.Filled.CheckCircle, Color(0xFFFF9800), halfWidth)
                    SystemReportCard("Avg Score", avgScore, "Overall performance.", Icons.Filled.TrendingUp, Color(0xFF4CAF50), halfWidth)
                    // Removed: SystemReportCard for New Users (7 Days)
                }
            }

            // TOPIC PERFORMANCE REPORT
            item {
                TopicPerformanceReport(topicPerformance)
            }

            // ----------------- DOWNLOAD BUTTON AT BOTTOM -----------------
            item {
                Spacer(Modifier.height(12.dp))
                DownloadReportButton(
                    totalUsers, totalQuizzes, quizzesCompleted,
                    avgScore, topicPerformance
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}


// ------------------- REUSABLE COMPONENTS (ADJUSTED FOR FLOWROW) -------------------

@Composable
fun SystemReportCard(title: String, value: String, description: String, icon: ImageVector, backgroundColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ManagementToolCard (Kept untouched)
@Composable
fun ManagementToolCard(title: String, description: String, icon: ImageVector, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(description, color = Color.White.copy(alpha = 0.8f))
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = Color.White)
        }
    }
}

// TopicPerformanceReport (Kept untouched)
@Composable
fun TopicPerformanceReport(topicPerformance: Map<String, String>) {
    val sorted = topicPerformance.toList()
        .sortedByDescending { it.second.filter { c -> c.isDigit() }.toDoubleOrNull() ?: 0.0 }

    val top = sorted.take(3)
    val low = sorted.filter { it.second.filter { c -> c.isDigit() }.toDoubleOrNull() ?: 0.0 < 50.0 }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Topic Performance Analysis", style = MaterialTheme.typography.titleLarge)

        TopicReportList("Top Performing Topics", top, Icons.Default.MilitaryTech, Color(0xFF00BCD4))
        TopicReportList("Low Performing Topics", low, Icons.Default.Warning, Color(0xFFFF5722))
    }
}

// TopicReportList (Kept untouched)
@Composable
fun TopicReportList(title: String, items: List<Pair<String, String>>, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text("No data available.", color = Color.White)
            } else {
                items.forEachIndexed { idx, (topic, score) ->
                    Text("${idx + 1}. $topic - $score", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    val navController = rememberNavController()
    AdminDashboardScreen(navController)
}