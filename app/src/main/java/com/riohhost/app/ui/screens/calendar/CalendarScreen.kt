package com.riohhost.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import com.riohhost.app.data.models.Reservation
import com.riohhost.app.ui.GlobalFiltersViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    filtersViewModel: GlobalFiltersViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onReservationClick: (String) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(36) }
    var selection by remember { mutableStateOf<LocalDate?>(null) }
    val daysOfWeek = remember { daysOfWeek() }
    
    val uiState by viewModel.uiState.collectAsState()
    val dateRange by filtersViewModel.dateRangeStrings.collectAsState()
    val selectedProperties by filtersViewModel.selectedProperties.collectAsState()
    val selectedPlatform by filtersViewModel.selectedPlatform.collectAsState()
    
    // Reload when filters change
    LaunchedEffect(dateRange, selectedProperties, selectedPlatform) {
        viewModel.loadReservationsFiltered(
            startDate = dateRange.first,
            endDate = dateRange.second,
            propertyIds = filtersViewModel.getPropertyFilter(),
            platform = filtersViewModel.getPlatformFilter()
        )
    }
    
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = remember(state.firstVisibleMonth) { state.firstVisibleMonth.yearMonth }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MonthHeader(
            month = visibleMonth, 
            onPreviousClick = { 
                coroutineScope.launch { state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1)) } 
            },
            onNextClick = { 
                coroutineScope.launch { state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1)) } 
            }
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        if (uiState is CalendarUiState.Loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        val reservations = (uiState as? CalendarUiState.Success)?.reservations ?: emptyList()

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                val dayReservations = remember(day.date, reservations) {
                    viewModel.getReservationsForDate(day.date, reservations)
                }
                
                Day(
                    day = day, 
                    isSelected = selection == day.date,
                    reservations = dayReservations,
                    onClick = { clicked -> selection = clicked }
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selection != null) {
            val selectedReservations = viewModel.getReservationsForDate(selection!!, reservations)
            Text(
                text = "Reservas para ${selection.toString()}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            if (selectedReservations.isEmpty()) {
                Text(text = "Nenhuma reserva para esta data.", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(selectedReservations.size) { index ->
                        val res = selectedReservations[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onReservationClick(res.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = res.guestName ?: "Hóspede", fontWeight = FontWeight.Bold)
                                Text(text = "${res.checkInDate} -> ${res.checkOutDate}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Status: ${res.cleaningStatus ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Day(
    day: CalendarDay, 
    isSelected: Boolean, 
    reservations: List<Reservation>,
    onClick: (LocalDate) -> Unit
) {
    val isCheckIn = reservations.any { it.checkInDate == day.date.toString() }
    val isCheckOut = reservations.any { it.checkOutDate == day.date.toString() }
    val isOccupied = reservations.isNotEmpty() && !isCheckIn && !isCheckOut

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = when {
                    isSelected -> Color(0xFF007AFF)
                    isCheckIn -> Color(0xFF4CAF50)
                    isCheckOut -> Color(0xFFF44336)
                    isOccupied -> Color(0xFFE0E0E0)
                    else -> Color.Transparent
                }
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day.date) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = when {
                isSelected -> Color.White
                isCheckIn || isCheckOut -> Color.White
                day.position == DayPosition.MonthDate -> Color.Black
                else -> Color.LightGray
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (reservations.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun MonthHeader(
    month: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
        }
    }
}
