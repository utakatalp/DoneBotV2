package com.utakatalp.donebot.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.utakatalp.donebot.R
import com.utakatalp.donebot.domain.model.Task

@Composable
internal fun HomeTaskList(
    tasks: List<Task>,
    onTaskCheck: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskLongPress: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) {
        HomeEmptyState(modifier = modifier)
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tasks, key = { it.id }) { task ->
            HomeSwipeToDeleteRow(
                task = task,
                onSwipeDelete = onTaskLongPress,
            ) {
                HomeTaskCard(
                    task = task,
                    onCheck = { onTaskCheck(task) },
                    onClick = { onTaskClick(task) },
                    onLongPress = { onTaskLongPress(task) },
                )
            }
        }
    }
}

@Composable
private fun HomeEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_no_tasks_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.home_no_tasks_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSwipeToDeleteRow(
    task: Task,
    onSwipeDelete: (Task) -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSwipeDelete(task)
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipe_bg_color",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        },
    ) {
        content()
    }
}
