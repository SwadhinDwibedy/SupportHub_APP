package com.example.supporthub.features.admin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.TICKET_STATUS_OPEN
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.ui.theme.SupportHubFontFamilies
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
private val adminHomeZoneId: ZoneId = ZoneId.systemDefault()

@RequiresApi(Build.VERSION_CODES.O)
private val adminHomeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "EEEE, d MMMM",
    Locale.ENGLISH
)

@RequiresApi(Build.VERSION_CODES.O)
private val adminHomeTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "hh:mm a",
    Locale.ENGLISH
)

data class AdminCommandCenterHeaderState(
    val dateLabel: String,
    val timeLabel: String,
    val title: String,
)

data class AdminDashboardMetricsState(
    val label: String = "TOTAL TICKETS THIS MONTH",
    val totalTicketsLabel: String = "0",
    val deltaLabel: String = "0% vs last month",
    val openTicketsLabel: String = "0 open",
)

data class AdminDashboardCardSpec(
    val cornerRadius: Dp = 28.dp,
    val containerColor: Color = Color(0xFF0F766E),
    val horizontalPadding: Dp = 22.dp,
    val verticalPadding: Dp = 24.dp,
    val heroCircleSize: Dp = 120.dp,
    val secondaryCircleSize: Dp = 52.dp,
)

data class OrganizationHealthState(
    val scoreLabel: String = "0",
    val summaryLabel: String = "Needs attention • Uptime 0.00%",
    val totalUsersLabel: String = "0",
    val departmentsLabel: String = "0",
)

data class TodayTicketsState(
    val createdLabel: String = "0",
    val resolvedLabel: String = "0",
    val pendingLabel: String = "0",
)

data class RecentUserItem(
    val profileName: String,
    val dateLabel: String,
)

data class RecentUsersState(
    val items: List<RecentUserItem> = emptyList(),
)

data class RecentTicketItem(
    val name: String,
    val documentId: String,
    val createdDateLabel: String,
    val statusLabel: String,
)

data class RecentTicketsState(
    val items: List<RecentTicketItem> = emptyList(),
)

data class AdminWorkspaceSummary(
    val users: List<User> = emptyList(),
    val tickets: List<Ticket> = emptyList(),
)

interface AdminSummaryRepository {
    fun observeWorkspaceSummary(): Flow<AdminWorkspaceSummary>
}

class FirebaseAdminSummaryRepository(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = authRepository),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : AdminSummaryRepository {

    override fun observeWorkspaceSummary(): Flow<AdminWorkspaceSummary> = channelFlow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            send(AdminWorkspaceSummary())
            close()
            return@channelFlow
        }

        val workspaceName = currentUser.workspaceName.trim()
        if (workspaceName.isBlank()) {
            send(AdminWorkspaceSummary())
            close()
            return@channelFlow
        }

        val usersFlow = observeWorkspaceUsers(workspaceName)
        val ticketsFlow = ticketRepository.observeAllTickets()

        launch {
            combine(usersFlow, ticketsFlow) { users, tickets ->
                AdminWorkspaceSummary(
                    users = users,
                    tickets = tickets.filter { ticket ->
                        ticket.workspaceName.trim().equals(workspaceName, ignoreCase = true)
                    }
                )
            }.collect { send(it) }
        }
    }

    private fun observeWorkspaceUsers(workspaceName: String): Flow<List<User>> {
        if (workspaceName.isBlank()) {
            return flowOf(emptyList())
        }
        return callbackFlow {
            val registration = firestore
                .collection(FirebaseCollections.USERS)
                .whereEqualTo("workspaceName", workspaceName)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val users = snapshot?.documents
                        .orEmpty()
                        .mapNotNull { document -> document.toObject(User::class.java) }
                        .sortedBy { user -> user.fullName.lowercase(Locale.ENGLISH) }

                    trySend(users)
                }

            awaitClose { registration.remove() }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildAdminCommandCenterHeaderState(
    dateTime: ZonedDateTime,
): AdminCommandCenterHeaderState = AdminCommandCenterHeaderState(
    dateLabel = dateTime.format(adminHomeDateFormatter).uppercase(Locale.ENGLISH),
    timeLabel = dateTime.format(adminHomeTimeFormatter).uppercase(Locale.ENGLISH),
    title = "Command Center"
)

@RequiresApi(Build.VERSION_CODES.O)
fun buildAdminDashboardMetricsState(
    tickets: List<Ticket>,
    now: ZonedDateTime,
): AdminDashboardMetricsState {
    val currentMonth = now.monthValue
    val currentYear = now.year
    val previousMonthDate = now.minusMonths(1)
    val previousMonth = previousMonthDate.monthValue
    val previousYear = previousMonthDate.year

    val currentMonthTickets = tickets.filter { ticket ->
        val createdAt = ticket.createdAt.toAdminDashboardDateTime(now.zone) ?: return@filter false
        createdAt.year == currentYear && createdAt.monthValue == currentMonth
    }
    val previousMonthTickets = tickets.filter { ticket ->
        val createdAt = ticket.createdAt.toAdminDashboardDateTime(now.zone) ?: return@filter false
        createdAt.year == previousYear && createdAt.monthValue == previousMonth
    }

    val currentTotal = currentMonthTickets.size
    val previousTotal = previousMonthTickets.size
    val openTickets = currentMonthTickets.count { ticket ->
        !ticket.status.trim().equals("RESOLVED", ignoreCase = true)
    }

    val deltaPercent = if (previousTotal == 0) {
        0
    } else {
        (((currentTotal - previousTotal).toDouble() / previousTotal.toDouble()) * 100).toInt()
    }
    val deltaPrefix = if (deltaPercent > 0) "+" else ""

    return AdminDashboardMetricsState(
        totalTicketsLabel = currentTotal.toString(),
        deltaLabel = "$deltaPrefix$deltaPercent% vs last month",
        openTicketsLabel = "$openTickets open",
    )
}

fun buildOrganizationHealthState(
    users: List<User>,
    tickets: List<Ticket>,
): OrganizationHealthState {
    val activeUsers = users.filter { user ->
        user.status.trim().equals("active", ignoreCase = true)
    }
    val departments = activeUsers.map { user -> user.department.trim() }
        .filter { it.isNotBlank() }
        .distinct()
    val resolvedTickets = tickets.count { ticket ->
        ticket.status.trim().equals("RESOLVED", ignoreCase = true)
    }
    val uptimePercent = if (tickets.isEmpty()) {
        100.0
    } else {
        (resolvedTickets.toDouble() / tickets.size.toDouble()) * 100.0
    }
    val healthScore = (activeUsers.size * 25).coerceAtMost(100)

    return OrganizationHealthState(
        scoreLabel = healthScore.toString(),
        summaryLabel = "${organizationHealthQualifier(healthScore)} • Uptime ${"%.2f".format(Locale.ENGLISH, uptimePercent)}%",
        totalUsersLabel = activeUsers.size.toString(),
        departmentsLabel = departments.size.toString(),
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildTodayTicketsState(
    tickets: List<Ticket>,
    now: ZonedDateTime,
): TodayTicketsState {
    val startOfDay = now.toLocalDate().atStartOfDay(now.zone)
    val endOfDay = startOfDay.plusDays(1)

    val createdToday = tickets.filter { ticket ->
        val createdAt = ticket.createdAt.toAdminDashboardDateTime(now.zone) ?: return@filter false
        !createdAt.isBefore(startOfDay) && createdAt.isBefore(endOfDay)
    }
    val resolvedToday = tickets.count { ticket ->
        val resolvedAt = ticket.resolvedAt.toAdminDashboardDateTime(now.zone) ?: return@count false
        !resolvedAt.isBefore(startOfDay) && resolvedAt.isBefore(endOfDay)
    }
    val pendingToday = createdToday.count { ticket ->
        !ticket.status.trim().equals("RESOLVED", ignoreCase = true)
    }

    return TodayTicketsState(
        createdLabel = createdToday.size.toString(),
        resolvedLabel = resolvedToday.toString(),
        pendingLabel = pendingToday.toString(),
    )
}

private fun organizationHealthQualifier(score: Int): String {
    return when {
        score >= 75 -> "Excellent"
        score >= 50 -> "Stable"
        score >= 25 -> "Growing"
        else -> "Needs attention"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildRecentUsersState(
    users: List<User>,
    now: ZonedDateTime,
): RecentUsersState {
    val recentUsers = users
        .sortedByDescending { user -> user.createdAt }
        .take(5)
        .map { user ->
            val createdAt = user.createdAt
                .toAdminDashboardDateTime(now.zone)
                ?: now
            RecentUserItem(
                profileName = user.fullName.trim().ifBlank { user.email.trim() },
                dateLabel = createdAt.format(adminHomeDateFormatter).uppercase(Locale.ENGLISH),
            )
        }
    return RecentUsersState(items = recentUsers)
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildRecentTicketsState(
    tickets: List<Ticket>,
    now: ZonedDateTime,
): RecentTicketsState {
    val recentTickets = tickets
        .sortedByDescending { ticket ->
            ticket.createdAt.toAdminDashboardDateTime(now.zone)?.toInstant()?.toEpochMilli() ?: Long.MIN_VALUE
        }
        .take(5)
        .map { ticket ->
            val createdAt = ticket.createdAt.toAdminDashboardDateTime(now.zone) ?: now
            val documentId = ticket.ticketId.trim().take(4).ifBlank { "--" }
            RecentTicketItem(
                name = ticket.subject.trim().ifBlank { "Untitled ticket" },
                documentId = documentId,
                createdDateLabel = createdAt.format(adminHomeDateFormatter).uppercase(Locale.ENGLISH),
                statusLabel = ticket.status.trim().uppercase(Locale.ENGLISH).ifBlank { TICKET_STATUS_OPEN },
            )
        }
    return RecentTicketsState(items = recentTickets)
}

fun adminCommandCenterFontFamily(): FontFamily = SupportHubFontFamilies.poppins

fun adminDashboardCardSpec(): AdminDashboardCardSpec = AdminDashboardCardSpec()

@RequiresApi(Build.VERSION_CODES.O)
private fun Any?.toAdminDashboardDateTime(zoneId: ZoneId): ZonedDateTime? {
    return when (this) {
        is Timestamp -> ZonedDateTime.ofInstant(toInstant(), zoneId)
        is Instant -> ZonedDateTime.ofInstant(this, zoneId)
        is Long -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
        is java.util.Date -> ZonedDateTime.ofInstant(toInstant(), zoneId)
        is LocalDateTime -> atZone(zoneId)
        is ZonedDateTime -> this.withZoneSameInstant(zoneId)
        else -> null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class AdminHomeViewModel(
    private val summaryRepository: AdminSummaryRepository = FirebaseAdminSummaryRepository(),
    private val clock: Clock = Clock.system(adminHomeZoneId),
) : ViewModel() {

    private val _dashboardMetricsState = MutableStateFlow(
        buildAdminDashboardMetricsState(emptyList(), ZonedDateTime.now(clock))
    )
    val dashboardMetricsState: StateFlow<AdminDashboardMetricsState> = _dashboardMetricsState.asStateFlow()

    private val _organizationHealthState = MutableStateFlow(OrganizationHealthState())
    val organizationHealthState: StateFlow<OrganizationHealthState> = _organizationHealthState.asStateFlow()

    private val _todayTicketsState = MutableStateFlow(TodayTicketsState())
    val todayTicketsState: StateFlow<TodayTicketsState> = _todayTicketsState.asStateFlow()

    private val _recentUsersState = MutableStateFlow(RecentUsersState())
    val recentUsersState: StateFlow<RecentUsersState> = _recentUsersState.asStateFlow()

    private val _recentTicketsState = MutableStateFlow(RecentTicketsState())
    val recentTicketsState: StateFlow<RecentTicketsState> = _recentTicketsState.asStateFlow()

    init {
        launchAdminObserver()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun launchAdminObserver() {
        viewModelScope.launch {
            summaryRepository.observeWorkspaceSummary().collect { summary ->
                val now = ZonedDateTime.now(clock)
                _dashboardMetricsState.update {
                    buildAdminDashboardMetricsState(
                        tickets = summary.tickets,
                        now = now
                    )
                }
                _organizationHealthState.update {
                    buildOrganizationHealthState(users = summary.users, tickets = summary.tickets)
                }
                _todayTicketsState.update {
                    buildTodayTicketsState(tickets = summary.tickets, now = now)
                }
                _recentUsersState.update {
                    buildRecentUsersState(users = summary.users, now = now)
                }
                _recentTicketsState.update {
                    buildRecentTicketsState(tickets = summary.tickets, now = now)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class AdminHomeViewModelFactory(
    private val clock: Clock = Clock.system(adminHomeZoneId),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminHomeViewModel(clock = clock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminHomeScreen(
    clock: Clock = Clock.system(adminHomeZoneId),
    viewModel: AdminHomeViewModel = viewModel(factory = AdminHomeViewModelFactory(clock)),
) {
    val headerState = remember(clock) {
        buildAdminCommandCenterHeaderState(
            ZonedDateTime.now(clock)
        )
    }
    val metricsState by viewModel.dashboardMetricsState.collectAsState()
    val organizationHealthState by viewModel.organizationHealthState.collectAsState()
    val todayTicketsState by viewModel.todayTicketsState.collectAsState()
    val recentUsersState by viewModel.recentUsersState.collectAsState()
    val recentTicketsState by viewModel.recentTicketsState.collectAsState()

    AdminHomeContent(
        headerState = headerState,
        metricsState = metricsState,
        organizationHealthState = organizationHealthState,
        todayTicketsState = todayTicketsState,
        recentUsersState = recentUsersState,
        recentTicketsState = recentTicketsState,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun AdminHomeContent(
    headerState: AdminCommandCenterHeaderState,
    metricsState: AdminDashboardMetricsState,
    organizationHealthState: OrganizationHealthState,
    todayTicketsState: TodayTicketsState,
    recentUsersState: RecentUsersState,
    recentTicketsState: RecentTicketsState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .testTag("admin_home_scroll_container")
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = headerState.dateLabel,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            ),
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = headerState.timeLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.2.sp
            ),
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = headerState.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                lineHeight = 42.sp,
                letterSpacing = (-0.5).sp
            ),
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(top = 6.dp)
        )
        AdminDashboardMetricsCard(
            state = metricsState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )
        OrganizationHealthCard(
            state = organizationHealthState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )
        TodayTicketsCard(
            state = todayTicketsState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )
        RecentUsersCard(
            state = recentUsersState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )
        RecentTicketsCard(
            state = recentTicketsState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )
    }
}

@Composable
private fun AdminDashboardMetricsCard(
    state: AdminDashboardMetricsState,
    modifier: Modifier = Modifier,
) {
    val spec = adminDashboardCardSpec()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spec.cornerRadius))
            .background(spec.containerColor)
            .padding(horizontal = spec.horizontalPadding, vertical = spec.verticalPadding)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 30.dp, end = 34.dp)
                .size(spec.secondaryCircleSize)
                .clip(CircleShape)
                .background(Color(0x12FFFFFF))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = state.label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.1.sp
                ),
                color = Color(0xB3FFFFFF)
            )
            Text(
                text = state.totalTicketsLabel,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 42.sp,
                    letterSpacing = (-1).sp
                ),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = state.deltaLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = adminCommandCenterFontFamily(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color(0xCCFFFFFF)
                )
                Text(
                    text = state.openTicketsLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = adminCommandCenterFontFamily(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun OrganizationHealthCard(
    state: OrganizationHealthState,
    modifier: Modifier = Modifier,
) {
    val progress = (state.scoreLabel.toFloatOrNull() ?: 0f).coerceIn(0f, 100f) / 100f

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularScoreIndicator(
                scoreLabel = state.scoreLabel,
                progress = progress,
                modifier = Modifier.size(58.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Organization Health",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = adminCommandCenterFontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    text = state.summaryLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = adminCommandCenterFontFamily(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrganizationInfoMiniCard(
                value = state.totalUsersLabel,
                label = "Total Users",
                modifier = Modifier.weight(1f)
            )
            OrganizationInfoMiniCard(
                value = state.departmentsLabel,
                label = "Departments",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TodayTicketsCard(
    state: TodayTicketsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Today's Tickets",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color(0xFF111827)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TodayTicketMetricCard(
                value = state.createdLabel,
                label = "Created",
                modifier = Modifier.weight(1f)
            )
            TodayTicketMetricCard(
                value = state.resolvedLabel,
                label = "Resolved",
                modifier = Modifier.weight(1f)
            )
            TodayTicketMetricCard(
                value = state.pendingLabel,
                label = "Pending",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CircularScoreIndicator(
    scoreLabel: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            drawArc(
                color = Color(0xFFE5E7EB),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height),
                topLeft = Offset.Zero
            )
            drawArc(
                color = Color(0xFF7C3AED),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height),
                topLeft = Offset.Zero
            )
        }
        Text(
            text = scoreLabel,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
            color = Color(0xFF7C3AED)
        )
    }
}

@Composable
private fun OrganizationInfoMiniCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            ),
            color = Color(0xFF111827)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun TodayTicketMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            ),
            color = Color(0xFF111827)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun RecentUsersCard(
    state: RecentUsersState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Users",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color(0xFF111827)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.items.forEach { item ->
                RecentUserRow(item = item)
            }
        }
    }
}

@Composable
private fun RecentUserRow(
    item: RecentUserItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDE9FE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.profileName.take(1).uppercase(Locale.ENGLISH),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = Color(0xFF7C3AED)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.profileName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = Color(0xFF111827)
            )
            Text(
                text = item.dateLabel,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun RecentTicketsCard(
    state: RecentTicketsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Tickets",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color(0xFF111827)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.items.forEach { item ->
                RecentTicketRow(item = item)
            }
        }
    }
}

@Composable
private fun RecentTicketRow(
    item: RecentTicketItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.documentId.takeIf { it.isNotBlank() } ?: "--",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color(0xFF4F46E5)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = Color(0xFF111827)
            )
            Text(
                text = "Doc ID ${item.documentId}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )
            Text(
                text = item.createdDateLabel,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = adminCommandCenterFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )
        }
        Text(
            text = item.statusLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = adminCommandCenterFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = Color(0xFF4F46E5)
        )
    }
}

@Composable
internal fun AdminPageTemplate(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
