package com.example.supporthub.features.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.TICKET_STATUS_OPEN
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketPriorityTone
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.features.tickets.TicketStatusFilter
import com.example.supporthub.features.tickets.TicketStatusTone
import com.example.supporthub.features.tickets.TicketUiState
import com.example.supporthub.features.tickets.TicketViewModel
import com.example.supporthub.features.tickets.TicketViewModelFactory
import com.example.supporthub.features.tickets.displayTicketId
import com.example.supporthub.features.tickets.ticketPriorityTone
import com.example.supporthub.features.tickets.ticketStatusLabel
import com.example.supporthub.features.tickets.ticketStatusTone
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeTicketsScreen(
    navController: NavController,
    authRepository: AuthRepositoryImpl = AuthRepositoryImpl(),
) {
    val ticketViewModel: TicketViewModel = viewModel(
        factory = TicketViewModelFactory(TicketRepositoryImpl(authRepository = authRepository))
    )
    val uiState by ticketViewModel.uiState.collectAsState()
    val currentUser by produceState<User?>(initialValue = null) {
        value = authRepository.getCurrentUser()
    }

    LaunchedEffect(currentUser?.uid) {
        ticketViewModel.start(currentUser?.uid.orEmpty())
    }

    EmployeeTicketsContent(
        uiState = uiState,
        onBackClick = {
            navController.navigate(EmployeeNavRoutes.Home) {
                launchSingleTop = true
                restoreState = true
                popUpTo(EmployeeNavRoutes.Home) {
                    saveState = true
                }
            }
        },
        onSearchChanged = ticketViewModel::onTicketSearchChanged,
        onStatusFilterSelected = ticketViewModel::onTicketStatusFilterSelected,
        onTicketClick = { ticket ->
            navController.navigate(EmployeeNavRoutes.ticketDetails(ticket.ticketId)) {
                launchSingleTop = true
            }
        }
    )
}

@Composable
private fun EmployeeTicketsContent(
    uiState: TicketUiState,
    onBackClick: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onStatusFilterSelected: (TicketStatusFilter) -> Unit,
    onTicketClick: (Ticket) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MyTicketsTopBar(onBackClick = onBackClick)
            }

            item {
                TicketSearchBar(
                    value = uiState.ticketSearchQuery,
                    onValueChange = onSearchChanged,
                )
            }

            item {
                TicketFilterTabs(
                    selected = uiState.selectedTicketStatusFilter,
                    onSelected = onStatusFilterSelected,
                )
            }

            when {
                uiState.isLoadingTickets -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0F8A6B))
                        }
                    }
                }

                uiState.visibleTickets.isEmpty() -> {
                    item {
                        EmptyTicketsState(hasSearch = uiState.ticketSearchQuery.isNotBlank())
                    }
                }

                else -> {
                    items(
                        items = uiState.visibleTickets,
                        key = { ticket -> ticket.ticketId.ifBlank { ticket.subject } }
                    ) { ticket ->
                        TicketListCard(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyTicketsTopBar(
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(42.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            shape = CircleShape,
            color = Color(0xFFEFF3F7),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to home",
                    tint = Color(0xFF111827)
                )
            }
        }

        Text(
            text = "My Tickets",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun TicketSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search tickets...",
                color = Color(0xFF94A3B8)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        },
        trailingIcon = {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = Color(0xFFE7ECF2)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF1F5F9),
            unfocusedContainerColor = Color(0xFFF1F5F9),

            // Add these
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            disabledTextColor = Color(0xFF111827),

            focusedPlaceholderColor = Color(0xFF94A3B8),
            unfocusedPlaceholderColor = Color(0xFF94A3B8),

            focusedLeadingIconColor = Color(0xFF64748B),
            unfocusedLeadingIconColor = Color(0xFF64748B),

            focusedTrailingIconColor = Color(0xFF64748B),
            unfocusedTrailingIconColor = Color(0xFF64748B),

            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color(0xFF0F8A6B)
        )
    )
}

@Composable
private fun TicketFilterTabs(
    selected: TicketStatusFilter,
    onSelected: (TicketStatusFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TicketStatusFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelected(filter) }
                    ),
                shape = RoundedCornerShape(50),
                color = if (isSelected) Color(0xFF0F8A6B) else Color(0xFFF8FAFC),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFFD6DEE8)
                )
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else Color(0xFF64748B),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyTicketsState(
    hasSearch: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (hasSearch) "No matching tickets" else "No tickets yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = if (hasSearch) {
                    "Try a different search or switch filters to see more results."
                } else {
                    "Your support requests will be organized here as you create them."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun TicketListCard(
    ticket: Ticket,
    onClick: () -> Unit,
) {
    val priorityTone = ticketPriorityTone(ticket.priority)
    val priorityAccentColor = when (priorityTone) {
        TicketPriorityTone.LOW -> Color(0xFF16A34A)
        TicketPriorityTone.MEDIUM -> Color(0xFFEAB308)
        TicketPriorityTone.HIGH -> Color(0xFFF97316)
        TicketPriorityTone.CRITICAL -> Color(0xFFDC2626)
    }
    val statusTone = ticketStatusTone(ticket.status)
    val statusAccentColor = when (statusTone) {
        TicketStatusTone.OPEN -> Color(0xFFF59E0B)
        TicketStatusTone.IN_PROGRESS -> Color(0xFF3B82F6)
        TicketStatusTone.RESOLVED -> Color(0xFF0F8A6B)
    }
    val statusLabel = ticketStatusLabel(ticket.status)
    val categoryIcon = if (ticket.category.trim().equals("HR", ignoreCase = true)) {
        Icons.Outlined.Workspaces
    } else {
        Icons.Outlined.Computer
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 3.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildTicketNumber(ticket.displayTicketId),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    StatusPill(
                        label = statusLabel,
                        accentColor = statusAccentColor,
                    )
                }

                Text(
                    text = ticket.subject.ifBlank { "Untitled ticket" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = ticket.category.ifBlank { "IT" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Text(
                        text = formatTicketDate(ticket),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                .background(priorityAccentColor)
                .width(3.dp)
                .size(height = 72.dp, width = 3.dp)
        )
    }
}

@Composable
private fun StatusPill(
    label: String,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accentColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = label.uppercase(Locale.ENGLISH),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

private fun buildTicketNumber(ticketId: String): String {
    val trimmed = ticketId.trim()
    if (trimmed.isBlank()) {
        return "#----"
    }
    return if (trimmed.startsWith("#")) trimmed else "#$trimmed"
}

private fun formatTicketDate(ticket: Ticket): String {
    val date = when (val value = ticket.createdAt) {
        is Timestamp -> value.toDate()
        is Date -> value
        is Long -> Date(value)
        else -> null
    } ?: return "Today"

    val formatter = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    return formatter.format(date)
}
