package com.utakatalp.donebot.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeTaskCard(
    task: Task,
    onCheck: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onCheck() })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                task.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = "${task.timeStart.format(TIME_FORMAT)} – ${task.timeEnd.format(TIME_FORMAT)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun HomeTaskCardPreview() {
    DoneBotTheme {
        HomeTaskCard(
            task = Task(
                id = 1L,
                title = "Team standup",
                description = "Discuss sprint progress and blockers",
                date = LocalDate.now(),
                timeStart = LocalTime.of(10, 0),
                timeEnd = LocalTime.of(10, 15),
                isCompleted = false,
            ),
            onCheck = {},
            onClick = {},
            onLongPress = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeTaskCardCompletedPreview() {
    DoneBotTheme {
        HomeTaskCard(
            task = Task(
                id = 2L,
                title = "Morning run",
                description = null,
                date = LocalDate.now(),
                timeStart = LocalTime.of(7, 0),
                timeEnd = LocalTime.of(7, 45),
                isCompleted = true,
            ),
            onCheck = {},
            onClick = {},
            onLongPress = {},
        )
    }
}

// endregion
