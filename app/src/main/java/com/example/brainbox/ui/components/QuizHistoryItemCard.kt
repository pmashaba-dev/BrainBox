// File: app/src/main/java/com/example/brainbox/ui/components/QuizHistoryItemCard.kt

package com.example.brainbox.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainbox.ui.screens.QuizResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * A reusable composable for displaying a single quiz history item as a clickable card.
 *
 * @param quizResult The data class containing the quiz's results.
 * @param onClick A lambda function to be called when the card is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryItemCard(quizResult: QuizResult, onClick: () -> Unit) {
    // Determine the performance category based on the score percentage.
    val scorePercentage = quizResult.score.toDouble() / quizResult.totalQuestions.toDouble()
    val (statusText, statusColor) = when {
        scorePercentage >= 0.8 -> "Excellent" to Color(0xFF4CAF50) // Green for excellent
        scorePercentage >= 0.5 -> "Good" to Color(0xFF2196F3) // Blue for good
        else -> "Needs Practice" to Color(0xFFF44336) // Red for needs practice
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Topic Title
            Text(
                text = quizResult.topic,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Performance Status (now on its own line)
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            // Score and Date in a single row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score
                Text(
                    text = "Score: ${quizResult.score} / ${quizResult.totalQuestions}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Date
                val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(quizResult.timestamp))
                Text(
                    text = "Date: $formattedDate",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}