// File: app/src/main/java/com/example/brainbox/ui/screens/models.kt

package com.example.brainbox.ui.screens

import com.example.brainbox.R
import com.google.firebase.firestore.PropertyName

/**
 * Data class to hold user profile information retrieved from Firestore.
 * This is a unified data class that will be used by all screens that need user data.
 */
data class UserProfile(
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "Guest",
    @get:PropertyName("avatarResId") @set:PropertyName("avatarResId") var avatarResId: Int = R.drawable.brain_icon,
    @get:PropertyName("points") @set:PropertyName("points") var points: Long = 0,
    @get:PropertyName("trophies") @set:PropertyName("trophies") var trophies: Long = 0,
    @get:PropertyName("quizzesCompleted") @set:PropertyName("quizzesCompleted") var quizzesCompleted: Long = 0,
    // NEW: Field to determine if the user has administrative privileges.
    @get:PropertyName("isAdmin") @set:PropertyName("isAdmin") var isAdmin: Boolean = false
)

/**
 * Data class to hold the results of a quiz.
 */
data class QuizResult(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("topic") @set:PropertyName("topic") var topic: String = "",
    @get:PropertyName("score") @set:PropertyName("score") var score: Long = 0,
    @get:PropertyName("totalQuestions") @set:PropertyName("totalQuestions") var totalQuestions: Long = 0,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class to represent an item in the PDF list (for Study Hub).
 */
data class FileItem(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("url") @set:PropertyName("url") var url: String = "",
    @get:PropertyName("subject") @set:PropertyName("subject") var subject: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = ""
)