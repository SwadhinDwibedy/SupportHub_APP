package com.example.supporthub.features.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketPriorityTone
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.features.tickets.TicketStatusTone
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

private val employeeTicketDetailsOuterShape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
private val employeeTicketCardShape = RoundedCornerShape(30.dp)
private val employeeTicketMetaShape = RoundedCornerShape(22.dp)

@Composable
fun EmployeeTicketDetailsScreen(
    navController: NavController,
    ticketId: String,
    authRepository: AuthRepositoryImpl = AuthRepositoryImpl(),
) {
    val ticketViewModel: TicketViewModel = viewModel(
        factory = TicketViewModelFactory(TicketRepositoryImpl(authRepository = authRepository))
    )
    val uiState by ticketViewModel.uiState.collectAsState()

    LaunchedEffect(ticketId) {
        ticketViewModel.loadTicketDetail(ticketId)
    }

    EmployeeTicketDetailsContent(
        isLoading = uiState.isLoadingTicketDetail,
        ticket = uiState.selectedTicketDetail,
        error = uiState.ticketDetailError,
        onBackClick = { navController.popBackStack() },
    )
}

@Composable
private fun EmployeeTicketDetailsContent(
    isLoading: Boolean,
    ticket: Ticket?,
    error: String?,
    onBackClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SupportHubPremiumBackground(modifier = Modifier.fillMaxSize())

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = Color.Transparent
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF0F8A6B))
                    }
                }

                ticket != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item { TicketDetailsTopBar(onBackClick = onBackClick) }
                        item { ModernTicketHeroCard(ticket = ticket) }
                        item { ModernAssignedAgentCard(ticket = ticket) }
                    }
                }

                else -> {
                    ModernErrorState(
                        message = error ?: "Unable to load ticket details.",
                        onBackClick = onBackClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketDetailsTopBar(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TicketToolbarButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBackClick
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ticket Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "SupportHub Employee Desk",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFD7E2F0)
            )
        }

        TicketToolbarButton(
            icon = Icons.Outlined.MoreHoriz,
            contentDescription = "More",
            onClick = {}
        )
    }
}

@Composable
private fun TicketToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ModernTicketHeroCard(ticket: Ticket) {
    val statusColor = ticketStatusAccent(ticket.status)
    val priorityColor = ticketPriorityAccent(ticket.priority)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = employeeTicketCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFDFEFF), Color(0xFFF2F6FB))
                    ),
                    shape = employeeTicketCardShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.75f),
                    shape = employeeTicketCardShape
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusColor.copy(alpha = 0.14f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Text(
                                    text = ticketStatusLabel(ticket.status),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor
                                )
                            }
                        }

                        Text(
                            text = buildTicketNumber(ticket.displayTicketId),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9AA9BC)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFEEF3F8)
                    ) {
                        Text(
                            text = ticket.priority.ifBlank { "High" },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = priorityColor
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = ticket.subject.ifBlank { "Untitled ticket" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF162033),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = ticket.description.ifBlank { "No description provided." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF617287),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }

                HorizontalDivider(color = Color(0xFFE3EAF2), thickness = 1.dp)

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val stacked = maxWidth < 360.dp
                    if (stacked) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Created",
                                    value = detailDate(ticket.createdAt)
                                )
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Priority",
                                    value = ticket.priority.ifBlank { "High" },
                                    valueColor = priorityColor
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Category",
                                    value = ticket.category.ifBlank { "IT Support" }
                                )
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Due",
                                    value = detailDueDate(ticket)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Created",
                                    value = detailDate(ticket.createdAt)
                                )
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Priority",
                                    value = ticket.priority.ifBlank { "High" },
                                    valueColor = priorityColor
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Category",
                                    value = ticket.category.ifBlank { "IT Support" }
                                )
                                ModernMetaCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Due",
                                    value = detailDueDate(ticket)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ModernMetaCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF152033),
) {
    Card(
        modifier = modifier,
        shape = employeeTicketMetaShape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE7EEF6))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label.uppercase(Locale.ENGLISH),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF97A6B7)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernAssignedAgentCard(ticket: Ticket) {
    val hasAgent = !ticket.assignedAgentName.isNullOrBlank()
    val initials = ticket.assignedAgentName
        ?.trim()
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString(separator = "") { it.take(1).uppercase(Locale.ENGLISH) }
        .orEmpty()
        .ifBlank { "NA" }
    val accentColor = if (hasAgent) Color(0xFF0F8A6B) else Color(0xFF64748B)
    val supportingText = if (hasAgent) {
        ticket.assignedAgentRole?.takeIf { it.isNotBlank() }
            ?: "Assigned support specialist"
    } else {
        "Till now no agent is given this responsibility"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = employeeTicketCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (hasAgent) {
                            listOf(Color(0xFFFAFFFD), Color(0xFFF1FBF7), Color(0xFFE8F6F1))
                        } else {
                            listOf(Color(0xFFFBFCFE), Color(0xFFF4F7FB), Color(0xFFEEF3F8))
                        }
                    ),
                    shape = employeeTicketCardShape
                )
                .border(
                    width = 1.dp,
                    color = if (hasAgent) Color(0xFFD3EEE5) else Color(0xFFE1E8F0),
                    shape = employeeTicketCardShape
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Responsibility",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                        Text(
                            text = if (hasAgent) "Assigned Agent" else "Pending Assignment",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF162033)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = accentColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = accentColor.copy(alpha = 0.14f)
                        )
                    ) {
                        Text(
                            text = if (hasAgent) "LIVE" else "OPEN",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (hasAgent) {
                                        listOf(Color(0xFF0F8A6B), Color(0xFF2563EB))
                                    } else {
                                        listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1))
                                    }
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = ticket.assignedAgentName ?: "No agent assigned yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF162033)
                        )
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5D6F84)
                        )
                        ticket.assignedAgentEmail
                            ?.takeIf { it.isNotBlank() && hasAgent }
                            ?.let { email ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.74f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = 0.72f)
                                    )
                                ) {
                                    Text(
                                        text = email,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4B5E73)
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernErrorState(
    message: String,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please try again later or return to your ticket list.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD6E3F1)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Text(
                text = "Go Back",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102033)
            )
        }
    }
}

private fun ticketStatusAccent(status: String): Color {
    return when (ticketStatusTone(status)) {
        TicketStatusTone.OPEN -> Color(0xFFF59E0B)
        TicketStatusTone.IN_PROGRESS -> Color(0xFF0F8A6B)
        TicketStatusTone.RESOLVED -> Color(0xFF3B82F6)
    }
}

private fun ticketPriorityAccent(priority: String): Color {
    return when (ticketPriorityTone(priority)) {
        TicketPriorityTone.LOW -> Color(0xFF16A34A)
        TicketPriorityTone.MEDIUM -> Color(0xFFEAB308)
        TicketPriorityTone.HIGH -> Color(0xFFFF4D6D)
        TicketPriorityTone.CRITICAL -> Color(0xFFDC2626)
    }
}

private fun buildTicketNumber(ticketId: String): String {
    val trimmed = ticketId.trim()
    if (trimmed.isBlank()) {
        return "#----"
    }
    return if (trimmed.startsWith("#")) trimmed else "#$trimmed"
}

private fun detailDate(value: Any?): String {
    val date = when (value) {
        is Timestamp -> value.toDate()
        is Date -> value
        is Long -> Date(value)
        else -> null
    } ?: return "Today"

    return SimpleDateFormat("MMM d", Locale.ENGLISH).format(date)
}

private fun detailDueDate(ticket: Ticket): String {
    val baseDate = when (val value = ticket.updatedAt ?: ticket.createdAt) {
        is Timestamp -> value.toDate()
        is Date -> value
        is Long -> Date(value)
        else -> null
    } ?: return "Not set"

    val dueDate = Date(baseDate.time + 2L * 24L * 60L * 60L * 1000L)
    return SimpleDateFormat("MMM d", Locale.ENGLISH).format(dueDate)
}
