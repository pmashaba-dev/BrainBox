package com.example.brainbox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.quizapp.Quiz
import com.example.quizapp.QuizManager
import com.example.quizapp.Question
import kotlinx.coroutines.launch
import java.util.UUID

// Internal state model for questions during editing
data class EditableQuestion(
    val id: String = UUID.randomUUID().toString(),
    var questionText: String = "",
    var options: MutableList<String> = MutableList(4) { "" },
    var correctAnswerIndex: Int = 0,
    // NEW FIELD: Holds the detailed explanation for the correct answer
    var explanation: String = ""
)

// ViewModel for Create/Edit operations
class CreateEditQuizViewModel(private val initialTopicTitle: String?) : ViewModel() {
    private val quizManager = QuizManager()

    // --- Define Max Lengths ---
    private val MAX_TITLE_LENGTH = 100
    private val MAX_QUESTION_LENGTH = 500
    private val MAX_OPTION_LENGTH = 100
    // NEW: Max length for the explanation text
    private val MAX_EXPLANATION_LENGTH = 1000
    // --------------------------

    // State for the main quiz details
    var topicTitle by mutableStateOf(initialTopicTitle ?: "")
        private set
    var isEditing by mutableStateOf(initialTopicTitle != null)
    var isTitleLocked by mutableStateOf(initialTopicTitle != null) // Cannot change title of existing quiz

    // State for the list of questions
    var questions by mutableStateOf(emptyList<EditableQuestion>())
        private set

    // State for loading and error handling
    var isLoading by mutableStateOf(initialTopicTitle != null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        if (isEditing) {
            loadQuizForEditing(initialTopicTitle!!)
        } else {
            isLoading = false
        }
    }

    // Custom setter for topicTitle to enforce max length
    fun updateTopicTitle(text: String) {
        if (!isTitleLocked && text.length <= MAX_TITLE_LENGTH) {
            topicTitle = text
        }
    }

    private fun loadQuizForEditing(title: String) {
        viewModelScope.launch {
            isLoading = true
            val quiz = quizManager.fetchQuizByTopic(title)
            if (quiz != null) {
                topicTitle = quiz.topicTitle
                questions = quiz.questions.map { q ->
                    // Preserve or create a new stable ID upon loading
                    EditableQuestion(
                        questionText = q.questionText,
                        options = q.options.toMutableList(),
                        correctAnswerIndex = q.options.indexOf(q.correctAnswer).coerceAtLeast(0),
                        // NEW: Load the explanation text
                        explanation = q.explanation
                    )
                }
            } else {
                errorMessage = "Failed to load quiz: $title"
            }
            isLoading = false
        }
    }

    fun addQuestion() {
        questions = questions + EditableQuestion()
    }

    fun removeQuestion(index: Int) {
        questions = questions.toMutableList().apply { removeAt(index) }
    }

    // Enforce max length
    fun updateQuestionText(index: Int, text: String) {
        if (text.length <= MAX_QUESTION_LENGTH) {
            questions = questions.toMutableList().apply {
                this[index] = this[index].copy(questionText = text)
            }
        }
    }

    // NEW: Update the explanation text
    fun updateExplanationText(index: Int, text: String) {
        if (text.length <= MAX_EXPLANATION_LENGTH) {
            questions = questions.toMutableList().apply {
                this[index] = this[index].copy(explanation = text)
            }
        }
    }

    // Enforce max length
    fun updateOptionText(questionIndex: Int, optionIndex: Int, text: String) {
        if (text.length <= MAX_OPTION_LENGTH) {
            questions = questions.toMutableList().apply {
                val updatedOptions = this[questionIndex].options.toMutableList().apply {
                    this[optionIndex] = text
                }
                this[questionIndex] = this[questionIndex].copy(options = updatedOptions)
            }
        }
    }

    fun updateCorrectAnswer(questionIndex: Int, correctIndex: Int) {
        questions = questions.toMutableList().apply {
            this[questionIndex] = this[questionIndex].copy(correctAnswerIndex = correctIndex)
        }
    }

    suspend fun saveQuiz(): Boolean {
        if (topicTitle.isBlank()) {
            errorMessage = "Quiz Title cannot be empty."
            return false
        }
        if (questions.isEmpty()) {
            errorMessage = "A quiz must have at least one question."
            return false
        }

        // Convert EditableQuestion list to Quiz format
        val quizToSave = Quiz(
            topicTitle = topicTitle.trim(),
            questions = questions.mapIndexedNotNull { qIndex, eq ->
                // Basic validation for question and options
                if (eq.questionText.isBlank() || eq.options.any { it.isBlank() }) {
                    errorMessage = "Question ${qIndex + 1} has missing text or options."
                    return false // Exit the whole function
                }

                // Ensure the correct answer index is valid
                val correctAnswer = eq.options.getOrNull(eq.correctAnswerIndex)
                if (correctAnswer.isNullOrBlank()) {
                    errorMessage = "Question ${qIndex + 1} has an invalid correct answer selected."
                    return false
                }

                Question(
                    questionText = eq.questionText.trim(),
                    options = eq.options.map { it.trim() },
                    correctAnswer = correctAnswer.trim(),
                    // NEW: Include the explanation text in the saved Question object
                    explanation = eq.explanation.trim()
                )
            }
        )

        // Clear error message before save attempt
        errorMessage = null

        val success = quizManager.saveQuiz(quizToSave)
        if (!success) {
            errorMessage = "Failed to save quiz. Please try again."
        }
        return success
    }
}

// Factory to create the ViewModel with the argument
@Composable
private fun rememberCreateEditQuizViewModel(initialTopicTitle: String?): CreateEditQuizViewModel {
    return remember { CreateEditQuizViewModel(initialTopicTitle) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditQuizScreen(
    navController: NavController,
    initialTopicTitle: String?
) {
    val viewModel = rememberCreateEditQuizViewModel(initialTopicTitle)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Quiz: ${viewModel.topicTitle}" else "Create New Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (viewModel.saveQuiz()) {
                            val action = if (viewModel.isEditing) "updated" else "created"
                            Toast.makeText(context, "Quiz '${viewModel.topicTitle}' $action successfully!", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        } else if (viewModel.errorMessage != null) {
                            Toast.makeText(context, viewModel.errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (viewModel.isEditing) "Save Changes" else "Save Quiz", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (viewModel.errorMessage != null && viewModel.questions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Quiz Title Field
                OutlinedTextField(
                    value = viewModel.topicTitle,
                    onValueChange = { viewModel.updateTopicTitle(it) },
                    label = { Text("Quiz Topic Title") },
                    singleLine = true,
                    enabled = !viewModel.isTitleLocked,
                    readOnly = viewModel.isTitleLocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Divider(Modifier.padding(vertical = 4.dp))

                // Question List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = viewModel.questions,
                        key = { _, question -> question.id } // <-- Using stable ID here
                    ) { index, question ->
                        QuestionEditorCard(
                            question = question,
                            questionNumber = index + 1,
                            onUpdateText = { text -> viewModel.updateQuestionText(index, text) },
                            // NEW: Pass the explanation update function
                            onUpdateExplanation = { text -> viewModel.updateExplanationText(index, text) },
                            onUpdateOption = { optionIndex, text -> viewModel.updateOptionText(index, optionIndex, text) },
                            onUpdateCorrectAnswer = { correctIndex -> viewModel.updateCorrectAnswer(index, correctIndex) },
                            onDelete = { viewModel.removeQuestion(index) }
                        )
                    }
                }

                // Add Question Button
                OutlinedButton(
                    onClick = { viewModel.addQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Question")
                    Spacer(Modifier.width(8.dp))
                    Text("New Question")
                }
            }
        }
    }
}

@Composable
fun QuestionEditorCard(
    question: EditableQuestion,
    questionNumber: Int,
    onUpdateText: (String) -> Unit,
    // NEW: Function to handle explanation text changes
    onUpdateExplanation: (String) -> Unit,
    onUpdateOption: (Int, String) -> Unit,
    onUpdateCorrectAnswer: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $questionNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Text Field
            OutlinedTextField(
                value = question.questionText,
                onValueChange = onUpdateText,
                label = { Text("Question Text") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Options (Select Correct Answer):", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // Options and Radio Buttons
            question.options.forEachIndexed { optionIndex, optionText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = question.correctAnswerIndex == optionIndex,
                        onClick = { onUpdateCorrectAnswer(optionIndex) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    OutlinedTextField(
                        value = optionText,
                        onValueChange = { onUpdateOption(optionIndex, it) },
                        label = { Text("Option ${optionIndex + 1}") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }

            // NEW: Explanation Text Field
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = question.explanation,
                onValueChange = onUpdateExplanation,
                label = { Text("Detailed Explanation (for Review Screen)") },
                placeholder = { Text("Explain why the correct answer is right.") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEditQuizScreenPreview() {
    CreateEditQuizScreen(navController = rememberNavController(), initialTopicTitle = "Preview Quiz")
}