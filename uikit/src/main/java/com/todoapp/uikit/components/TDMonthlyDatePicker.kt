package com.todoapp.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.previews.TDPreview
import com.todoapp.uikit.previews.TDPreviewWide
import com.todoapp.uikit.theme.TDTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

@Composable
fun TDMonthlyDatePicker(
    modifier: Modifier,
    displayedMonth: YearMonth,
    selectedDate: LocalDate? = LocalDate.now(),
    today: LocalDate,
    taskDates: Set<LocalDate> = emptySet(),
    overdueDates: Set<LocalDate> = emptySet(),
    hasOverdueBeforeDisplayedMonth: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    onDateSelect: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val daysInMonth = displayedMonth.lengthOfMonth()
    val days = remember(displayedMonth) {
        (1..daysInMonth).map { displayedMonth.atDay(it) }
    }

    LaunchedEffect(displayedMonth, selectedDate) {
        val scrollIndex = max(
            0,
            selectedDate
                ?.takeIf { YearMonth.from(it) == displayedMonth }
                ?.dayOfMonth
                ?.minus(4)
                ?: 0,
        )
        listState.animateScrollToItem(scrollIndex)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        MonthNavigationHeader(
            displayedMonth = displayedMonth,
            hasOverdueBeforeDisplayedMonth = hasOverdueBeforeDisplayedMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            state = listState,
        ) {
            items(items = days, key = { it.toEpochDay() }) { date ->
                DatePickerCard(
                    modifier = Modifier,
                    currentDate = date,
                    isSelected = selectedDate == date,
                    isToday = date == today,
                    hasTask = date in taskDates,
                    hasOverdue = date in overdueDates,
                    onDateSelect = onDateSelect,
                )
            }
        }
    }
}

@Composable
private fun MonthNavigationHeader(
    displayedMonth: YearMonth,
    hasOverdueBeforeDisplayedMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val locale =
        if (!configuration.locales.isEmpty) configuration.locales[0] else Locale.getDefault()
    val monthLabel = displayedMonth.month.getDisplayName(TextStyle.FULL, locale)
    val yearLabel = displayedMonth.year.toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = if (hasOverdueBeforeDisplayedMonth) "Overdue tasks in previous months" else "Previous month",
                    tint = TDTheme.colors.onBackground,
                )
            }
            if (hasOverdueBeforeDisplayedMonth) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                        .size(8.dp)
                        .background(TDTheme.colors.crossRed, CircleShape),
                )
            }
        }
        TDText(
            text = "$monthLabel $yearLabel",
            style = TDTheme.typography.heading4,
            color = TDTheme.colors.onBackground,
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = TDTheme.colors.onBackground,
            )
        }
    }
}

@Composable
private fun DatePickerCard(
    modifier: Modifier,
    currentDate: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasTask: Boolean = false,
    hasOverdue: Boolean = false,
    onDateSelect: (LocalDate) -> Unit = {},
) {
    val textColor = if (isSelected) TDTheme.colors.white else TDTheme.colors.lightGray
    val barColor: Color? = when {
        hasOverdue -> TDTheme.colors.crossRed
        hasTask && isSelected -> TDTheme.colors.white
        hasTask -> TDTheme.colors.pendingGray
        else -> null
    }
    Column(
        modifier = modifier
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = if (isToday) TDTheme.colors.pendingGray.copy(alpha = 0.8f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .background(
                color = if (isSelected) TDTheme.colors.pendingGray.copy(alpha = 0.8f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                )
            .size(width = 48.dp, height = 80.dp)
            .clickable { onDateSelect(currentDate) }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier.weight(0.8f))
        TDText(
            text = shortDayOfWeekLabel(currentDate.dayOfWeek),
            style = TDTheme.typography.regularTextStyle,
            color = textColor,
        )
        Spacer(modifier.weight(0.2f))
        TDText(
            text = currentDate.dayOfMonth.toString(),
            style = TDTheme.typography.heading4,
            color = textColor,
        )
        Spacer(modifier.weight(1f))
        if (barColor != null) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .background(barColor, RoundedCornerShape(2.dp)),
            )
        }
        Spacer(modifier.weight(0.3f))
    }
}

@Composable
private fun shortDayOfWeekLabel(day: DayOfWeek): String {
    val configuration = LocalConfiguration.current
    val locale =
        if (!configuration.locales.isEmpty) configuration.locales[0] else Locale.getDefault()
    return day.getDisplayName(TextStyle.SHORT, locale)
}

@TDPreviewWide
@Composable
private fun MonthlyDatePickerPreview() {
    TDTheme {
        var selected by remember { mutableStateOf(LocalDate.of(2025, 12, 3)) }
        TDMonthlyDatePicker(
            modifier = Modifier,
            displayedMonth = YearMonth.of(2025, 12),
            selectedDate = selected,
            onDateSelect = { selected = it },
            onPreviousMonth = {},
            onNextMonth = {},
            today = LocalDate.now()
        )
    }
}

@TDPreviewWide
@Composable
private fun MonthlyDatePickerWithOverduePreview() {
    TDTheme {
        val month = YearMonth.of(2025, 12)
        var selected by remember { mutableStateOf(LocalDate.of(2025, 12, 17)) }
        TDMonthlyDatePicker(
            modifier = Modifier,
            displayedMonth = month,
            selectedDate = selected,
            taskDates = setOf(month.atDay(10), month.atDay(20)),
            overdueDates = setOf(month.atDay(3), month.atDay(9), month.atDay(15)),
            hasOverdueBeforeDisplayedMonth = true,
            onDateSelect = { selected = it },
            onPreviousMonth = {},
            onNextMonth = {},
            today = LocalDate.now()
        )
    }
}

@TDPreview
@Composable
private fun DatePickerCardSelectedPreview() {
    TDTheme {
        DatePickerCard(
            modifier = Modifier,
            currentDate = LocalDate.of(2025, 12, 17),
            isSelected = true,
            isToday = false,
        )
    }
}

@TDPreview
@Composable
private fun DatePickerCardUnselectedPreview() {
    TDTheme {
        DatePickerCard(
            modifier = Modifier,
            currentDate = LocalDate.of(2025, 12, 18),
            isSelected = false,
            isToday = false,
        )
    }
}

@TDPreview
@Composable
private fun DatePickerCardHasTaskPreview() {
    TDTheme {
        DatePickerCard(
            modifier = Modifier,
            currentDate = LocalDate.of(2025, 12, 10),
            isSelected = false,
            isToday = false,
            hasTask = true,
        )
    }
}

@TDPreview
@Composable
private fun DatePickerCardOverduePreview() {
    TDTheme {
        DatePickerCard(
            modifier = Modifier,
            currentDate = LocalDate.of(2025, 12, 9),
            isSelected = false,
            isToday = false,
            hasOverdue = true,
        )
    }
}
