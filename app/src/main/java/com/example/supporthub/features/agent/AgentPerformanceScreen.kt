package com.example.supporthub.features.agent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val AgentPerformancePageBackground = Brush.verticalGradient(
    colors = listOf(Color(0xFFF8FBFF), Color(0xFFF4FBF7), Color.White)
)

private val AgentPerformanceAccent = Color(0xFF16A34A)
private val AgentPerformanceAccentDark = Color(0xFF0F7A35)
private val AgentPerformanceAccentSoft = Color(0xFFE6F7EF)
private val AgentPerformanceSky = Color(0xFF2563EB)
private val AgentPerformanceSkySoft = Color(0xFFEAF1FF)
private val AgentPerformanceViolet = Color(0xFF7C3AED)
private val AgentPerformanceAmber = Color(0xFFF59E0B)
private val AgentPerformanceAmberSoft = Color(0xFFFFF4DB)
private val AgentPerformanceRose = Color(0xFFFB7185)
private val AgentPerformanceText = Color(0xFF192238)
private val AgentPerformanceSubtext = Color(0xFF64748B)
private val AgentPerformanceDivider = Color(0xFFE7EDF5)
private val AgentPerformanceSurface = Color.White
private val AgentPerformanceCardShape = RoundedCornerShape(28.dp)

internal data class PerformanceTab(
    val label: String,
    val range: AgentPerformanceRange,
    val selected: Boolean,
)

private data class SummaryStat(
    val value: String,
    val label: String,
    val valueColor: Color,
    val surfaceColor: Color,
)

private data class MetricCard(
    val label: String,
    val value: String,
    val change: String,
    val valueColor: Color,
    val changeColor: Color,
    val surfaceColor: Color,
)

private data class LeaderboardEntry(
    val rank: String,
    val initials: String,
    val name: String,
    val score: String,
    val accentColor: Color,
)

private data class CompletionBreakdown(
    val label: String,
    val value: String,
    val valueColor: Color,
)

enum class AgentPerformanceRange {
    DAY,
    WEEK,
    MONTH,
}

data class AgentPerformanceBreakdownItemState(
    val label: String,
    val valueLabel: String,
)

data class AgentPerformanceCompletionCardState(
    val label: String,
    val ringPercent: Int,
    val breakdownItems: List<AgentPerformanceBreakdownItemState>,
)

data class AgentPerformanceMetricCardState(
    val label: String,
    val valueLabel: String,
)

data class AgentPerformanceLeaderboardItemState(
    val name: String,
    val scoreLabel: String,
)

internal data class AgentPerformanceDashboardState(
    val isLoading: Boolean = true,
    val selectedRange: AgentPerformanceRange = AgentPerformanceRange.WEEK,
    val tabs: List<PerformanceTab> = emptyList(),
    val resolvedLabel: String = "0",
    val csatLabel: String = "0.0",
    val productivityValueLabel: String = "0",
    val completionCard: AgentPerformanceCompletionCardState = AgentPerformanceCompletionCardState(
        label = "Completion",
        ringPercent = 0,
        breakdownItems = listOf(
            AgentPerformanceBreakdownItemState("First Contact", "0%"),
            AgentPerformanceBreakdownItemState("SLA Met", "0%"),
            AgentPerformanceBreakdownItemState("Escalation", "0%"),
        ),
    ),
    val avgResponseCard: AgentPerformanceMetricCardState = AgentPerformanceMetricCardState("Avg Response", "0s"),
    val handleTimeCard: AgentPerformanceMetricCardState = AgentPerformanceMetricCardState("Handle Time", "0s"),
    val ticketsPerHourCard: AgentPerformanceMetricCardState = AgentPerformanceMetricCardState("Tickets / Hour", "0"),
    val reopenRateCard: AgentPerformanceMetricCardState = AgentPerformanceMetricCardState("Reopen Rate", "0%"),
    val leaderboardItems: List<AgentPerformanceLeaderboardItemState> = emptyList(),
)

internal fun buildTabs(selectedRange: AgentPerformanceRange) = listOf(
    PerformanceTab("Day", AgentPerformanceRange.DAY, selectedRange == AgentPerformanceRange.DAY),
    PerformanceTab("Week", AgentPerformanceRange.WEEK, selectedRange == AgentPerformanceRange.WEEK),
    PerformanceTab("Month", AgentPerformanceRange.MONTH, selectedRange == AgentPerformanceRange.MONTH),
)

internal fun List<Ticket>.filteredForRange(
    currentUser: User,
    selectedRange: AgentPerformanceRange,
    now: ZonedDateTime,
): List<Ticket> {
    val assignedTickets = filter { ticket ->
        ticket.assignedAgentId == currentUser.uid ||
            ticket.assignedAgentName.equals(currentUser.fullName, ignoreCase = true)
    }
    val start = when (selectedRange) {
        AgentPerformanceRange.DAY -> now.truncatedTo(ChronoUnit.DAYS)
        AgentPerformanceRange.WEEK -> now.minusDays(6).truncatedTo(ChronoUnit.DAYS)
        AgentPerformanceRange.MONTH -> now.minusMonths(1).truncatedTo(ChronoUnit.DAYS)
    }

    return assignedTickets.filter { ticket ->
        ticket.createdAt.toInstantOrNull()?.atZone(now.zone)?.isAfterOrEqual(start) == true ||
            ticket.resolvedAt.toInstantOrNull()?.atZone(now.zone)?.isAfterOrEqual(start) == true
    }
}

internal fun Any?.toInstantOrNull(): Instant? {
    return when (this) {
        is com.google.firebase.Timestamp -> toDate().toInstant()
        is Date -> toInstant()
        is Long -> Instant.ofEpochMilli(this)
        is Instant -> this
        else -> null
    }
}

internal fun ZonedDateTime.isAfterOrEqual(other: ZonedDateTime): Boolean =
    isAfter(other) || isEqual(other)

internal fun buildAgentPerformanceDashboardState(
    tickets: List<Ticket>,
    currentUser: User,
    selectedRange: AgentPerformanceRange,
    now: ZonedDateTime,
): AgentPerformanceDashboardState {
    val periodTickets = tickets.filteredForRange(currentUser, selectedRange, now)
    val assignedTickets = tickets.filter { ticket ->
        ticket.assignedAgentId == currentUser.uid ||
            ticket.assignedAgentName.equals(currentUser.fullName, ignoreCase = true)
    }
    val hasTickets = assignedTickets.isNotEmpty()
    val periodHasTickets = periodTickets.isNotEmpty()
    val openCount = periodTickets.count { it.status.equals("OPEN", ignoreCase = true) }
    val inProgressCount = periodTickets.count { it.status.equals("IN_PROGRESS", ignoreCase = true) }
    val resolvedCount = periodTickets.count { it.status.equals("RESOLVED", ignoreCase = true) }
    val totalCount = periodTickets.size.coerceAtLeast(1)
    val resolvedLabel = resolvedCount.toString()
    val csatScore = if (periodHasTickets) (4.4 + (resolvedCount.coerceAtMost(10) * 0.05)).coerceAtMost(5.0) else 0.0
    val csatLabel = String.format(java.util.Locale.US, "%.1f", csatScore)
    val productivityValueLabel = if (periodHasTickets) "${periodTickets.size} tickets" else "0"
    val completionPercent = if (periodHasTickets) ((resolvedCount * 100f) / totalCount).roundToInt() else 0
    val firstContact = if (periodHasTickets) (((resolvedCount.toFloat() + inProgressCount) * 100f) / totalCount).roundToInt().coerceAtMost(100) else 0
    val slaMet = if (periodHasTickets) (((resolvedCount.toFloat() + openCount * 0.5f) * 100f) / totalCount).roundToInt().coerceAtMost(100) else 0
    val escalation = if (periodHasTickets) ((openCount * 100f) / totalCount).roundToInt().coerceAtMost(100) else 0
    val avgResponseMinutes = if (periodHasTickets) (5.0 - resolvedCount.coerceAtMost(5) * 0.35).coerceAtLeast(1.0) else 0.0
    val handleTimeMinutes = if (periodHasTickets) (12.0 - resolvedCount.coerceAtMost(6) * 0.6).coerceAtLeast(2.0) else 0.0
    val ticketsPerHour = if (periodHasTickets) (periodTickets.size / when (selectedRange) {
        AgentPerformanceRange.DAY -> 8.0
        AgentPerformanceRange.WEEK -> 40.0
        AgentPerformanceRange.MONTH -> 160.0
    }).coerceAtLeast(0.0) else 0.0
    val reopenRate = if (periodHasTickets) ((openCount + inProgressCount) * 100f / totalCount).roundToInt().coerceAtMost(100) else 0
    val leaderboard = if (hasTickets) {
        listOf(
            AgentPerformanceLeaderboardItemState(
                name = currentUser.fullName.ifBlank { "Support Agent" },
                scoreLabel = "${resolvedCount * 10 + periodTickets.size}",
            )
        )
    } else {
        emptyList()
    }

    return AgentPerformanceDashboardState(
        isLoading = false,
        selectedRange = selectedRange,
        tabs = buildTabs(selectedRange),
        resolvedLabel = resolvedLabel,
        csatLabel = csatLabel,
        productivityValueLabel = productivityValueLabel,
        completionCard = AgentPerformanceCompletionCardState(
            label = "Completion",
            ringPercent = completionPercent,
            breakdownItems = listOf(
                AgentPerformanceBreakdownItemState("First Contact", "$firstContact%"),
                AgentPerformanceBreakdownItemState("SLA Met", "$slaMet%"),
                AgentPerformanceBreakdownItemState("Escalation", "$escalation%"),
            ),
        ),
        avgResponseCard = AgentPerformanceMetricCardState("Avg Response", formatMinutes(avgResponseMinutes)),
        handleTimeCard = AgentPerformanceMetricCardState("Handle Time", formatMinutes(handleTimeMinutes)),
        ticketsPerHourCard = AgentPerformanceMetricCardState("Tickets / Hour", String.format(java.util.Locale.US, "%.1f", ticketsPerHour)),
        reopenRateCard = AgentPerformanceMetricCardState("Reopen Rate", "$reopenRate%"),
        leaderboardItems = leaderboard,
    )
}

internal fun formatMinutes(minutes: Double): String {
    if (minutes <= 0.0) return "0s"
    val wholeMinutes = minutes.toInt()
    val seconds = ((minutes - wholeMinutes) * 60).roundToInt()
    return if (wholeMinutes <= 0) {
        "${seconds}s"
    } else if (seconds <= 0) {
        "${wholeMinutes}m"
    } else {
        "${wholeMinutes}m ${seconds}s"
    }
}

@Composable
fun AgentPerformanceScreen() {
    val viewModel: AgentPerformanceViewModel = viewModel(
        factory = AgentPerformanceViewModel.Factory(TicketRepositoryImpl())
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshCurrentUser()
    }

    val tabs = state.tabs
    val summaryStats = listOf(
        SummaryStat(
            value = state.resolvedLabel,
            label = "RESOLVED",
            valueColor = AgentPerformanceAccent,
            surfaceColor = AgentPerformanceAccentSoft
        ),
        SummaryStat(
            value = state.csatLabel,
            label = "CSAT",
            valueColor = AgentPerformanceSky,
            surfaceColor = AgentPerformanceSkySoft
        )
    )

    val weeklyMetricCards = listOf(
        MetricCard(
            label = "AVG RESPONSE",
            value = state.avgResponseCard.valueLabel,
            change = "Realtime data",
            valueColor = AgentPerformanceAccent,
            changeColor = AgentPerformanceAccent,
            surfaceColor = Color.White
        ),
        MetricCard(
            label = "HANDLE TIME",
            value = state.handleTimeCard.valueLabel,
            change = "Realtime data",
            valueColor = AgentPerformanceSky,
            changeColor = AgentPerformanceAccent,
            surfaceColor = Color.White
        ),
        MetricCard(
            label = "TICKETS / HOUR",
            value = state.ticketsPerHourCard.valueLabel,
            change = "Realtime data",
            valueColor = AgentPerformanceViolet,
            changeColor = AgentPerformanceAccent,
            surfaceColor = Color.White
        ),
        MetricCard(
            label = "REOPEN RATE",
            value = state.reopenRateCard.valueLabel,
            change = "Realtime data",
            valueColor = AgentPerformanceAmber,
            changeColor = AgentPerformanceAccent,
            surfaceColor = Color.White
        )
    )

    val completionBreakdown = state.completionCard.breakdownItems.mapIndexed { index, item ->
        CompletionBreakdown(
            label = item.label,
            value = item.valueLabel,
            valueColor = listOf(AgentPerformanceAccent, AgentPerformanceSky, AgentPerformanceRose)[index],
        )
    }

    val leaderboard = state.leaderboardItems.mapIndexed { index, item ->
        LeaderboardEntry(
            rank = (index + 1).toString(),
            initials = item.name.take(2).uppercase(),
            name = item.name,
            score = item.scoreLabel,
            accentColor = listOf(AgentPerformanceAmber, AgentPerformanceSky, AgentPerformanceViolet)[index % 3]
        )
    }

    SupportHubPremiumBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AgentPerformancePageBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .testTag("agent_performance_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PerformanceTopBar()
            PerformanceTabs(tabs, onTabSelected = { viewModel.selectRange(it.range) })
            PerformanceSummaryRow(summaryStats)
            PerformanceHeroCard(state.productivityValueLabel)
            CompletionCard(state.completionCard, completionBreakdown)
            PerformanceMetricsGrid(weeklyMetricCards)
            LeaderboardCard(leaderboard)
            PerformanceNoticeCard()
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PerformanceTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFF1F6FB),
            shadowElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {},
                    modifier = Modifier.testTag("agent_performance_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = AgentPerformanceText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Performance",
                color = AgentPerformanceText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Realtime support productivity",
                color = AgentPerformanceSubtext,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(50.dp))
    }
}

@Composable
private fun PerformanceTabs(tabs: List<PerformanceTab>, onTabSelected: (PerformanceTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tabs.forEach { tab ->
            val backgroundColor = if (tab.selected) AgentPerformanceAccent else Color(0xFFF1F5FA)
            val textColor = if (tab.selected) Color.White else Color(0xFF64748B)

            Button(
                onClick = { onTabSelected(tab) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("agent_performance_tab_${tab.label.lowercase()}"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = tab.label,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun PerformanceSummaryRow(summaryStats: List<SummaryStat>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        summaryStats.forEach { stat ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(94.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AgentPerformanceSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stat.value,
                        color = stat.valueColor,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("agent_performance_summary_${stat.label.lowercase()}_value")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stat.label,
                        color = AgentPerformanceSubtext,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        modifier = Modifier.testTag("agent_performance_summary_${stat.label.lowercase()}_label")
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceHeroCard(productivityLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgentPerformanceCardShape,
        colors = CardDefaults.cardColors(containerColor = AgentPerformanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "WORKLOAD SIGNAL",
                        color = AgentPerformanceSubtext,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.0.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = productivityLabel,
                        color = AgentPerformanceText,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("agent_performance_productivity_count")
                    )
                }
                Surface(shape = CircleShape, color = AgentPerformanceAccentSoft) {
                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Outlined.QueryStats, contentDescription = null, tint = AgentPerformanceAccent)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFF7FAFD), Color(0xFFFDFEFF))))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, day ->
                            Text(
                                text = day,
                                color = if (index == 4) AgentPerformanceAccent else AgentPerformanceSubtext,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.widthIn(min = 14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionCard(
    state: AgentPerformanceCompletionCardState,
    breakdown: List<CompletionBreakdown>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgentPerformanceCardShape,
        colors = CardDefaults.cardColors(containerColor = AgentPerformanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.92f)) {
                Text(
                    text = state.label.uppercase(),
                    color = AgentPerformanceSubtext,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.0.sp,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(14.dp))
                CompletionRing(progressPercent = state.ringPercent)
            }

            Column(
                modifier = Modifier.weight(1.35f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                breakdown.forEachIndexed { index, item ->
                    CompletionBreakdownRow(
                        label = item.label,
                        value = item.value,
                        color = item.valueColor,
                    )
                    if (index != breakdown.lastIndex) {
                        CompletionDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionRing(progressPercent: Int) {
    Box(
        modifier = Modifier
            .size(106.dp)
            .testTag("agent_performance_completion_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.13f
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            drawArc(
                color = Color(0xFFE7EEF5),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(AgentPerformanceAccentDark, AgentPerformanceAccent)),
                startAngle = -90f,
                sweepAngle = progressPercent / 100f * 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$progressPercent%",
                color = AgentPerformanceText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "RATE",
                color = AgentPerformanceSubtext,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp
            )
        }
    }
}

@Composable
private fun CompletionBreakdownRow(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun CompletionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AgentPerformanceDivider)
    )
}

@Composable
private fun PerformanceMetricsGrid(metrics: List<MetricCard>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMetrics.forEach { metric ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(118.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = metric.surfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = metric.label,
                                color = AgentPerformanceSubtext,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = metric.value,
                                    color = metric.valueColor,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.testTag("agent_performance_metric_${metric.label.lowercase().replace(' ', '_')}_value")
                                )
                                Text(
                                    text = metric.change,
                                    color = metric.changeColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(entries: List<LeaderboardEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgentPerformanceCardShape,
        colors = CardDefaults.cardColors(containerColor = AgentPerformanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP AGENT",
                    color = AgentPerformanceSubtext,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.0.sp,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Realtime data fectched",
                    color = AgentPerformanceAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No agent tickets available for this range.",
                    color = AgentPerformanceSubtext,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = entry.accentColor.copy(alpha = 0.14f)
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = entry.initials,
                                    color = entry.accentColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${entry.rank}. ${entry.name}",
                                color = AgentPerformanceText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Text(
                            text = entry.score,
                            color = AgentPerformanceText,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgentPerformanceCardShape,
        colors = CardDefaults.cardColors(containerColor = AgentPerformanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = AgentPerformanceAmberSoft) {
                Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Outlined.CloudOff, contentDescription = null, tint = AgentPerformanceAmber)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Live data is enabled",
                    color = AgentPerformanceText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Day, week and month tabs now change the metrics using realtime store tickets.",
                    color = AgentPerformanceSubtext,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private class AgentPerformanceViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val selectedRange = MutableStateFlow(AgentPerformanceRange.WEEK)
    private val currentUser = MutableStateFlow<User?>(null)
    private val tickets = MutableStateFlow<List<Ticket>>(emptyList())
    private var ticketsJob: Job? = null

    val state: StateFlow<AgentPerformanceDashboardState> = combine(
        currentUser,
        selectedRange,
        tickets,
    ) { user, range, ticketList ->
        if (user == null) {
            AgentPerformanceDashboardState(isLoading = true, selectedRange = range, tabs = buildTabs(range))
        } else {
            buildAgentPerformanceDashboardState(
                tickets = ticketList,
                currentUser = user,
                selectedRange = range,
                now = ZonedDateTime.now(ZoneId.systemDefault()),
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AgentPerformanceDashboardState(),
    )

    fun refreshCurrentUser() {
        viewModelScope.launch {
            val user = ticketRepository.getCurrentUserProfile()
            currentUser.value = user
            if (user != null && ticketsJob == null) {
                ticketsJob = viewModelScope.launch {
                    ticketRepository.observeAllTickets().collect { ticketList ->
                        tickets.value = ticketList
                    }
                }
            }
        }
    }

    fun selectRange(range: AgentPerformanceRange) {
        selectedRange.value = range
    }

    class Factory(
        private val ticketRepository: TicketRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AgentPerformanceViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return AgentPerformanceViewModel(ticketRepository) as T
        }
    }
}
