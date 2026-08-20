package com.example.quizapp

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Tag for logging
private const val TAG = "QuizManager"

// Data classes
data class Question(
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    // NEW: Field for the detailed explanation of the correct answer
    val explanation: String = ""
)

data class Quiz(
    // ADDED: Field to hold the subject (e.g., "Mathematics", "Physical Sciences")
    val subject: String = "",
    val topicTitle: String = "",
    val questions: List<Question> = emptyList()
)

/**
 * Manages fetching and managing quiz data from a Firestore database.
 */
class QuizManager {

    // Get an instance of the Firestore database
    private val db = FirebaseFirestore.getInstance()

    /**
     * Fetches all quizzes from the Firestore "quizzes" collection.
     * @return A List of Quiz objects. Returns an empty list on failure.
     */
    suspend fun fetchAllQuizzes(): List<Quiz> = withContext(Dispatchers.IO) {
        return@withContext try {
            val querySnapshot = db.collection("quizzes")
                .get()
                .await()

            // Map each document to a Quiz object
            val quizzes = querySnapshot.documents.mapNotNull { document ->
                // toObject will automatically map the new 'subject' field
                document.toObject(Quiz::class.java)
            }
            Log.d(TAG, "Successfully fetched ${quizzes.size} quizzes.")
            quizzes
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all quizzes", e)
            emptyList<Quiz>()
        }
    }

    /**
     * Deletes a specific quiz by its topic title from Firestore.
     * @param topicTitle The title (which is also the document ID) of the quiz to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    suspend fun deleteQuiz(topicTitle: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            db.collection("quizzes").document(topicTitle)
                .delete()
                .await()
            Log.d(TAG, "Successfully deleted quiz: $topicTitle")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting quiz by topic: $topicTitle", e)
            false
        }
    }

    /**
     * Fetches a specific quiz by its topic title from Firestore.
     * This is a suspend function, meaning it should be called from a coroutine.
     * @param topicTitle The title of the quiz to fetch.
     * @return The Quiz object if found, or null if an error occurs or the quiz is not found.
     */
    suspend fun fetchQuizByTopic(topicTitle: String): Quiz? = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get a reference to the specific quiz document using the topicTitle as the document ID
            val docRef = db.collection("quizzes").document(topicTitle)

            // Fetch the document and wait for the result
            val documentSnapshot = docRef.get().await()

            // Check if the document exists
            if (documentSnapshot.exists()) {
                // Convert the document to our Quiz data class
                val quiz = documentSnapshot.toObject(Quiz::class.java)
                Log.d(TAG, "Successfully fetched quiz: ${quiz?.topicTitle}")
                quiz
            } else {
                Log.w(TAG, "No such document found for topic: $topicTitle")
                null
            }
        } catch (e: Exception) {
            // Log any errors that occur during the fetch operation
            Log.e(TAG, "Error fetching quiz by topic: $topicTitle", e)
            null
        }
    }

    /**
     * Saves a Quiz object to Firestore. If a document with the same topicTitle exists,
     * it will be overwritten (updated).
     * @param quiz The Quiz object to save.
     * @return true if the save was successful, false otherwise.
     */
    suspend fun saveQuiz(quiz: Quiz): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Use the topicTitle as the document ID
            db.collection("quizzes").document(quiz.topicTitle)
                .set(quiz) // This performs an upsert (create or update)
                .await()
            Log.d(TAG, "Successfully saved quiz: ${quiz.topicTitle}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving quiz: ${quiz.topicTitle}", e)
            false
        }
    }
}