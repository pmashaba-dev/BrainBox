package com.example.brainbox.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A reusable composable for displaying a single topic card
 * with glassmorphism style, color accent, and tap animation.
 *
 * @param topicName The name of the topic to display.
 * @param color Accent color for the card (category-based).
 * @param onClick A lambda function to be called when the card is clicked.
 */
@Composable
fun TopicListItem(topicName: String, color: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                color.copy(alpha = 0.15f), // Glass effect
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    pressed = true
                    onClick()
                    pressed = false
                }
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = topicName,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = color.takeOrElse { MaterialTheme.colorScheme.onSurface }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopicListItemPreview() {
    TopicListItem(
        topicName = "Sample Topic",
        color = Color(0xFF4B0082), // Indigo accent
        onClick = {}
    )
}
