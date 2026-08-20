// --- FINAL MERGED + COMPLETED QUIZ GAME SCREEN ---
// Includes explanations, review mode, question navigator,
// optimized layout, and all missing parts filled in.

package com.example.brainbox.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Quiz
import com.example.quizapp.QuizManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val TAG = "QuizGameScreen"

// Stores user's answer + explanation
data class UserAnswer(
    val question: String,
    val userAnswer: String?,
    val correctAnswer: String,
    val explanation: String
)


// ------------------------------------------------------
// QUIZ GAME SCREEN
// ------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGameScreen(topicTitle: String, onGoBack: () -> Unit) {

    var quiz by remember { mutableStateOf<Quiz?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    // holds selection for each question
    val userSelections = remember { mutableStateListOf<String?>() }
    val userAnswers = remember { mutableStateListOf<UserAnswer>() }

    var showReviewScreen by remember { mutableStateOf(false) }

    // UPDATED: Timer Logic state
    var timeTaken by remember { mutableStateOf(0L) }
    var quizStartTime by remember { mutableStateOf(0L) }
    // Removed redundant 'isQuizRunning' state


    // Loading quiz data
    LaunchedEffect(topicTitle) {
        isLoading = true
        withContext(Dispatchers.IO) {
            quiz = QuizManager().fetchQuizByTopic(topicTitle)

            quiz?.let { q ->
                userSelections.clear()
                repeat(q.questions.size) { userSelections.add(null) }
            }
        }
        isLoading = false

        // FIX 1: Set start time immediately after the quiz is loaded and ready
        if (quiz != null) {
            quizStartTime = System.currentTimeMillis()
            Log.d(TAG, "Quiz started at: $quizStartTime")
        }
    }

    // FIX 2: LaunchedEffect to continuously update timeTaken while the quiz is in progress
    LaunchedEffect(isQuizFinished, isLoading) {
        if (!isQuizFinished && !isLoading && quizStartTime > 0) {
            while (!isQuizFinished) {
                // Update timeTaken live every second
                timeTaken = System.currentTimeMillis() - quizStartTime
                delay(1000)
            }
        }
    }


    // SAVE RESULTS - UPDATED
    suspend fun saveQuizResultsToFirestore(
        topicTitle: String, // ADDED: Topic title parameter
        score: Int,
        totalQuestions: Int,
        timeTaken: Long
    ) {

        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        val profileRef = db.collection("profiles").document(user.uid)
        val resultsRef = db.collection("quiz_results") // Reference to the results collection

        val trophies = if (score.toFloat() / totalQuestions > 0.8) 1 else 0

        try {
            // 1. Transaction to update user profile (points, completion count, trophies)
            db.runTransaction { t ->
                val profile = t.get(profileRef)

                val newPoints = (profile.getLong("points") ?: 0) + score
                val newTrophies = (profile.getLong("trophies") ?: 0) + trophies
                val newCompleted = (profile.getLong("quizzesCompleted") ?: 0) + 1
                val username = profile.getString("username") ?: "Player"

                t.set(
                    profileRef,
                    mapOf(
                        "username" to username,
                        "points" to newPoints,
                        "trophies" to newTrophies,
                        "quizzesCompleted" to newCompleted
                    ),
                    SetOptions.merge()
                )
            }.await()

            // 2. ADDED: Add the individual quiz result document to 'quiz_results'
            val quizResultData = mapOf(
                "userId" to user.uid,
                "topic" to topicTitle,
                // Using Long for compatibility with ProgressScreen's model and Firestore type handling
                "score" to score.toLong(),
                "totalQuestions" to totalQuestions.toLong(),
                "timestamp" to System.currentTimeMillis(),
                "duration" to timeTaken,
                // 'subject' is not defined here, assuming topic is sufficient for now
            )

            resultsRef.add(quizResultData).await()
            Log.d(TAG, "Quiz results and profile updated successfully!")

        } catch (e: Exception) {
            Log.e(TAG, "Error saving results", e)
        }
    }


    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color.Black)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(topicTitle, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onGoBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->

            if (isLoading) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    Alignment.Center
                ) {
                    Text("Loading quiz...", color = Color.White)
                }
                return@Scaffold
            }

            val q = quiz ?: return@Scaffold

            // QUIZ COMPLETED
            if (isQuizFinished) {

                // isQuizRunning = false // No longer needed
                // UPDATED: Call saveQuizResultsToFirestore with topicTitle
                LaunchedEffect(Unit) {
                    saveQuizResultsToFirestore(topicTitle, score, q.questions.size, timeTaken)
                }

                if (showReviewScreen) {
                    ReviewAnswersScreen(
                        userAnswers = userAnswers,
                        onGoBack = { showReviewScreen = false }
                    )
                } else {
                    QuizFinishedScreen(
                        score = score,
                        totalQuestions = q.questions.size,
                        timeTaken = timeTaken, // Time taken is passed here
                        onPlayAgain = {
                            currentQuestionIndex = 0
                            userSelections.fill(null)
                            userAnswers.clear()
                            score = 0
                            isQuizFinished = false
                            // Re-initialize start time for new quiz attempt
                            quizStartTime = System.currentTimeMillis()
                        },
                        onReviewAnswers = { showReviewScreen = true },
                        onGoBack = onGoBack
                    )
                }
                return@Scaffold
            }


            val currentQ = q.questions[currentQuestionIndex]
            val total = q.questions.size
            val selected = userSelections[currentQuestionIndex]


            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Progress
                item {
                    Box(Modifier.size(80.dp), Alignment.Center) {
                        CircularProgressIndicator(
                            progress = (currentQuestionIndex + 1f) / total,
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 6.dp,
                            color = Color(0xFF4CAF50)
                        )
                        Text("${currentQuestionIndex + 1}/$total", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Question Navigator
                item {
                    QuizNavigator(
                        totalQuestions = total,
                        currentIndex = currentQuestionIndex,
                        userSelections = userSelections,
                        onQuestionSelect = { currentQuestionIndex = it }
                    )
                }

                // Question Card
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
                    ) {
                        Text(
                            currentQ.questionText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(16.dp),
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Options
                itemsIndexed(currentQ.options) { _, option ->
                    QuizOptionButton(
                        optionText = option,
                        isSelected = option == selected,
                        onClick = { userSelections[currentQuestionIndex] = option }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // Navigation Buttons
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Button(
                            onClick = { currentQuestionIndex-- },
                            enabled = currentQuestionIndex > 0,
                            modifier = Modifier.weight(1f)
                        ) { Text("Back") }

                        Button(
                            onClick = {
                                if (currentQuestionIndex + 1 == total) {
                                    // Finish quiz
                                    var finalScore = 0
                                    userAnswers.clear()

                                    q.questions.forEachIndexed { i, question ->
                                        val answer = userSelections[i]
                                        if (answer == question.correctAnswer) finalScore++
                                        userAnswers.add(
                                            UserAnswer(
                                                question = question.questionText,
                                                userAnswer = answer,
                                                correctAnswer = question.correctAnswer,
                                                explanation = question.explanation
                                            )
                                        )
                                    }

                                    score = finalScore

                                    // FIX 3: Calculate the final time taken EXACTLY at the moment of completion
                                    timeTaken = System.currentTimeMillis() - quizStartTime

                                    isQuizFinished = true

                                } else {
                                    currentQuestionIndex++
                                }
                            },
                            enabled = selected != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (currentQuestionIndex + 1 == total) "Finish" else "Next")
                        }
                    }
                }
            }
        }
    }
}


// ------------------------------------------------------
// NAVIGATOR
// ------------------------------------------------------
@Composable
fun QuizNavigator(
    totalQuestions: Int,
    currentIndex: Int,
    userSelections: List<String?>,
    onQuestionSelect: (Int) -> Unit
) {
    LazyRow(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(List(totalQuestions) { it }) { index, _ ->

            val bg = when {
                index == currentIndex -> MaterialTheme.colorScheme.primary
                userSelections[index] != null -> Color(0xFF4CAF50)
                else -> Color(0x55FFFFFF)
            }

            Button(
                onClick = { onQuestionSelect(index) },
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = bg)
            ) {
                Text("${index + 1}", fontSize = 12.sp)
            }
        }
    }
}


// ------------------------------------------------------
// OPTION BUTTON
// ------------------------------------------------------
@Composable
fun QuizOptionButton(optionText: String, isSelected: Boolean, onClick: () -> Unit) {

    val pressedScale by animateFloatAsState(
        if (isSelected) 0.97f else 1f, label = ""
    )

    val highlight = Color(0xFFFF6F61)

    Box(
        Modifier
            .fillMaxWidth()
            .scale(pressedScale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) highlight.copy(alpha = 0.35f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) highlight else Color(0x33FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(optionText, color = Color.White, fontSize = 16.sp)

            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "selected", tint = Color.White)
            }
        }
    }
}


// ------------------------------------------------------
// FINISHED SCREEN
// ------------------------------------------------------
@Composable
fun QuizFinishedScreen(
    score: Int,
    totalQuestions: Int,
    timeTaken: Long, // Time taken is received here
    onPlayAgain: () -> Unit,
    onReviewAnswers: () -> Unit,
    onGoBack: () -> Unit
) {

    val message = when {
        score.toFloat() / totalQuestions > 0.8 -> " Outstanding! You’ve mastered this topic.\uD83C\uDF1F"
        score.toFloat() / totalQuestions > 0.5 -> "Good Effort!"
        else -> "Don’t worry\uD83D\uDE14, mistakes help you learn.try again!\uD83E\uDDE0"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color.Black)))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(message, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Text(
            "Score: $score / $totalQuestions",
            fontSize = 30.sp,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Time is correctly converted and displayed
        Text(
            "Time: ${TimeUnit.MILLISECONDS.toSeconds(timeTaken)}s",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        Button(
            onClick = onReviewAnswers,
            modifier = Modifier.fillMaxWidth(0.8f).height(55.dp)
        ) { Text("Review Answers") }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(0.8f).height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61))
        ) { Text("Play Again") }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onGoBack,
            modifier = Modifier.fillMaxWidth(0.8f).height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
        ) { Text("Try Another Topic", color = Color.White) }
    }
}


// ------------------------------------------------------
// REVIEW SCREEN (Layout Fixed)
// ------------------------------------------------------
@Composable
fun ReviewAnswersScreen(userAnswers: List<UserAnswer>, onGoBack: () -> Unit) {

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color.Black)))
            .padding(horizontal = 16.dp)
            .statusBarsPadding(), // Ensures content is below the status bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Increased top spacer to 48.dp to push the title down
        Spacer(Modifier.height(48.dp))

        Text(
            "Review Answers",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(userAnswers) { index, ans ->

                val correct = ans.userAnswer == ans.correctAnswer
                val indicator = if (correct) Color(0xFF4CAF50) else Color(0xFFE57373)

                Card(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "Question ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            color = indicator
                        )

                        Text(
                            ans.question,
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Text("Your Answer: ${ans.userAnswer ?: "Skipped"}", color = Color.White)
                        Text("Correct Answer: ${ans.correctAnswer}", color = Color(0xFF4CAF50))

                        Spacer(Modifier.height(6.dp))

                        Text("Explanation:", color = MaterialTheme.colorScheme.primary)
                        Text(
                            ans.explanation,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onGoBack,
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Text("Back to Score")
        }

        Spacer(Modifier.height(16.dp))
    }
}