package com.example.brainbox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.brainbox.navigation.AppRoutes
// Correct Imports for data classes and QuizManager from the correct package
import com.example.quizapp.Quiz
import com.example.quizapp.QuizManager
import com.example.quizapp.Question
import kotlinx.coroutines.launch

// ViewModel to manage the state and data fetching
class QuizManagementViewModel : ViewModel() {
    // The QuizManager must be from the imported package, com.example.quizapp
    private val quizManager = QuizManager()

    // 1. Private MutableState for internal mutations (e.g., in fetchQuizzes)
    private val _quizzes = mutableStateOf(emptyList<Quiz>())
    private val _isLoading = mutableStateOf(true)
    private val _errorMessage = mutableStateOf<String?>(null)

    // 2. Public read-only State properties for the Composable to observe
    val quizzes: State<List<Quiz>> = _quizzes
    val isLoading: State<Boolean> = _isLoading
    val errorMessage: State<String?> = _errorMessage

    init {
        fetchQuizzes()
    }

    // This function now resolves, as fetchAllQuizzes() is implemented in QuizManager.kt
    fun fetchQuizzes() {
        viewModelScope.launch {
            // Modify the private mutable states
            _isLoading.value = true
            _errorMessage.value = null

            // The fetchAllQuizzes() is now resolved
            val result = quizManager.fetchAllQuizzes()

            if (result.isNotEmpty()) {
                _quizzes.value = result
            } else {
                // Only show a message if the list is empty
                _errorMessage.value = "No quizzes found. Start creating one!"
            }
            _isLoading.value = false
        }
    }

    // This function now resolves, as deleteQuiz() is implemented in QuizManager.kt
    suspend fun deleteQuiz(topicTitle: String): Boolean {
        // Assuming deleteQuiz is a suspend function in QuizManager
        return quizManager.deleteQuiz(topicTitle).also { success ->
            if (success) {
                // Refresh the list upon successful deletion
                fetchQuizzes()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizManagementScreen(viewModel: QuizManagementViewModel = remember { QuizManagementViewModel() }, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // LaunchedEffect to refresh data when the screen becomes active (e.g., returning from Create/Edit)
    LaunchedEffect(Unit) {
        viewModel.fetchQuizzes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin: Quiz Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.CreateEditQuiz.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Quiz", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when {
                viewModel.isLoading.value -> {
                    // Show loading indicator
                    CircularProgressIndicator(Modifier.padding(top = 32.dp))
                }
                viewModel.errorMessage.value != null -> {
                    // Show error/empty state message
                    Text(
                        text = viewModel.errorMessage.value!!,
                        modifier = Modifier.padding(top = 32.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    // Display the list of quizzes
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(viewModel.quizzes.value, key = { it.topicTitle }) { quiz ->
                            QuizManagementItem(
                                quiz = quiz,
                                onEdit = { topicTitle ->
                                    // Assuming you have a route like: object CreateEditQuiz : AppRoutes("create_edit_quiz/{topicTitle}")
                                    navController.navigate(AppRoutes.CreateEditQuiz.createRoute(topicTitle))
                                },
                                onDelete = { topicTitle ->
                                    coroutineScope.launch {
                                        if (viewModel.deleteQuiz(topicTitle)) {
                                            Toast.makeText(context, "Quiz '$topicTitle' deleted successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to delete quiz '$topicTitle'.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizManagementItem(quiz: Quiz, onEdit: (String) -> Unit, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(quiz.topicTitle) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.topicTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${quiz.questions.size} Questions",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Edit Button
            IconButton(onClick = { onEdit(quiz.topicTitle) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Quiz", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Button
            IconButton(onClick = { onDelete(quiz.topicTitle) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Quiz", tint = Color.Red)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizManagementScreenPreview() {
    // This preview is simplified and doesn't fully represent the actual behavior
    // but shows the UI structure.
    val navController = rememberNavController()
    // You would typically use a FakeViewModel for a realistic preview
    Column(modifier = Modifier.fillMaxSize()) {
        QuizManagementItem(
            // FIX: Added the 'subject' parameter ("Mathematics") as the first argument.
            quiz = Quiz("Mathematics", "Trigonometry", listOf(Question(), Question())),
            onEdit = {},
            onDelete = {}
        )
        Spacer(modifier = Modifier.height(10.dp))
        QuizManagementItem(
            // FIX: Added the 'subject' parameter ("Physical Sciences") as the first argument.
            quiz = Quiz("Physical Sciences", "Newton's Laws", listOf(Question())),
            onEdit = {},
            onDelete = {}
        )
    }
}