package com.utakatalp.donebot.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.R
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.ui.home.HomeContract.UiAction
import com.utakatalp.donebot.ui.home.HomeContract.UiEffect
import com.utakatalp.donebot.ui.home.HomeContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: UiState,
    uiEffect: Flow<UiEffect>,
    onAction: (UiAction) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is UiEffect.ShowError -> Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (uiState) {
            is UiState.Loading -> HomeLoading()
            is UiState.Error -> HomeError(message = uiState.message, onRetry = { onAction(UiAction.OnRetry) })
            is UiState.Success -> HomeContent(uiState = uiState, onAction = onAction)
        }
    }
}

@Composable
private fun HomeLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.home_retry))
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun HomeScreenSuccessPreview() {
    DoneBotTheme {
        HomeScreen(
            uiState = UiState.Success(
                selectedDate = LocalDate.now(),
                tasks = previewSampleTasks(),
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeScreenEmptyPreview() {
    DoneBotTheme {
        HomeScreen(
            uiState = UiState.Success(
                selectedDate = LocalDate.now(),
                tasks = emptyList(),
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeScreenLoadingPreview() {
    DoneBotTheme {
        HomeScreen(uiState = UiState.Loading, uiEffect = emptyFlow(), onAction = {})
    }
}

@TDPreview
@Composable
private fun HomeScreenErrorPreview() {
    DoneBotTheme {
        HomeScreen(
            uiState = UiState.Error("Something went wrong"),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeScreenDeleteDialogPreview() {
    val sample = previewSampleTasks()
    DoneBotTheme {
        HomeScreen(
            uiState = UiState.Success(
                selectedDate = LocalDate.now(),
                tasks = sample,
                pendingDeleteTask = sample.first(),
                isDeleteDialogOpen = true,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

internal fun previewSampleTasks(): List<Task> = listOf(
    Task(
        id = 1L,
        title = "Morning run",
        description = "5km around the park",
        date = LocalDate.now(),
        timeStart = LocalTime.of(7, 0),
        timeEnd = LocalTime.of(7, 45),
        isCompleted = true,
    ),
    Task(
        id = 2L,
        title = "Team standup",
        description = "Discuss sprint progress",
        date = LocalDate.now(),
        timeStart = LocalTime.of(10, 0),
        timeEnd = LocalTime.of(10, 15),
        isCompleted = false,
    ),
    Task(
        id = 3L,
        title = "Grocery shopping",
        description = null,
        date = LocalDate.now(),
        timeStart = LocalTime.of(18, 0),
        timeEnd = LocalTime.of(19, 0),
        isCompleted = false,
    ),
)

// endregion
