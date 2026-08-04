package com.example.supporthub.features.agent

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.dashboard.components.SupportHubBottomBar
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.features.tickets.TicketStatusFilter
import com.example.supporthub.features.tickets.displayTicketId
import com.example.supporthub.features.tickets.matchesStatusFilter
import com.example.supporthub.features.tickets.ticketStatusLabel
import com.example.supporthub.features.tickets.ticketStatusTone
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

private val agentHomeDateFormatter = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH).apply {
    timeZone = TimeZone.getDefault()
}

private val agentHomeTimeFormatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
    timeZone = TimeZone.getDefault()
}

private val agentHomeCreatedFormatter = SimpleDateFormat("d MMM • hh:mm a", Locale.ENGLISH).apply {
    timeZone = TimeZone.getDefault()
}

private val AgentHomeSurface = Color.White
private val AgentHomeText = Color(0xFF0F172A)
private val AgentHomeSubtext = Color(0xFF64748B)
private val AgentHomeBorder = Color(0xFFE4EAF3)
private val AgentHomePrimaryBlue = Color(0xFF2563EB)
private val AgentHomeAssigned = Color(0xFF3B82F6)
private val AgentHomeInProgress = Color(0xFFF59E0B)
private val AgentHomeResolved = Color(0xFF16A34A)
private val AgentHomePending = Color(0xFF8B5CF6)
private val AgentHomeCardShape = RoundedCornerShape(24.dp)
private val AgentHomeChipShape = RoundedCornerShape(999.dp)
private val AgentHomeCanvasFont = FontFamily.SansSerif

private data class AgentHomeActionItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val backgroundTint: Color,
)

private data class AgentHomeMetricDisplay(
    val title: String,
    val value: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
    val backgroundTint: Color,
    val statusFilter: TicketStatusFilter,
)

private data class AgentHomeTicketDisplay(
    val ticket: Ticket,
    val priorityLabel: String,
    val priorityColor: Color,
    val priorityBackground: Color,
    val statusLabel: String,
    val statusColor: Color,
    val statusBackground: Color,
    val employeeName: String,
    val createdLabel: String,
)

data class AgentHomeWorkspaceSnapshot(
    val currentUser: User? = null,
    val tickets: List<Ticket> = emptyList(),
)

data class AgentHomeHeaderState(
    val timeLabel: String,
    val dateLabel: String,
    val agentName: String,
    val greeting: String,
    val prompt: String,
    val avatarInitials: String,
    val isOnline: Boolean,
)

data class AgentHomeMetricCardState(
    val label: String,
    val valueLabel: String,
    val icon: ImageVector,
    val accentColor: Color,
    val accentTint: Color,
)

data class AgentHomeTicketCardState(
    val ticketId: String,
    val title: String,
    val employeeName: String,
    val priorityLabel: String,
    val priorityColor: Color,
    val statusLabel: String,
    val createdLabel: String,
    val statusColor: Color,
)

data class AgentHomeUiState(
    val header: AgentHomeHeaderState = AgentHomeHeaderState(
        timeLabel = "",
        dateLabel = "",
        agentName = "Support Agent",
        greeting = "Good Morning",
        prompt = "Ready to help your customers today?",
        avatarInitials = "SA",
        isOnline = true,
    ),
    val assignedTicketsCount: Int = 0,
    val resolvedTodayCount: Int = 0,
    val metrics: List<AgentHomeMetricCardState> = emptyList(),
    val recentTickets: List<AgentHomeTicketCardState> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

interface AgentHomeRepository {
    fun observeWorkspaceSnapshot(): Flow<AgentHomeWorkspaceSnapshot>
}

class FirebaseAgentHomeRepository(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = authRepository),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : AgentHomeRepository {

    override fun observeWorkspaceSnapshot(): Flow<AgentHomeWorkspaceSnapshot> = channelFlow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null || currentUser.uid.isBlank()) {
            send(AgentHomeWorkspaceSnapshot())
            close()
            return@channelFlow
        }

        val workspaceName = currentUser.workspaceName.trim()
        if (workspaceName.isBlank()) {
            send(AgentHomeWorkspaceSnapshot(currentUser = currentUser, tickets = emptyList()))
            close()
            return@channelFlow
        }

        val userFlow = observeUserDocument(currentUser.uid)
        val ticketsFlow = ticketRepository.observeAllTickets()

        launch {
            combine(userFlow, ticketsFlow) { user, tickets ->
                AgentHomeWorkspaceSnapshot(
                    currentUser = user ?: currentUser,
                    tickets = tickets.filter { ticket ->
                        ticket.workspaceName.trim().equals(workspaceName, ignoreCase = true)
                    }
                )
            }.collect { send(it) }
        }
    }

    private fun observeUserDocument(uid: String): Flow<User?> {
        if (uid.isBlank()) return flowOf(null)
        return callbackFlow {
            val registration = firestore.collection(FirebaseCollections.USERS).document(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
            awaitClose { registration.remove() }
        }
    }
}

class AgentHomeViewModel(
    private val repository: AgentHomeRepository = FirebaseAgentHomeRepository(),
    private val clock: () -> Date = { Date() },
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentHomeUiState())
    val uiState: StateFlow<AgentHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWorkspaceSnapshot().collect { snapshot ->
                val now = clock()
                _uiState.update { buildAgentHomeUiStateInternal(snapshot = snapshot, now = now) }
            }
        }
    }
}

class AgentHomeViewModelFactory(
    private val clock: () -> Date = { Date() },
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentHomeViewModel(clock = clock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

fun buildAgentHomeUiState(
    snapshot: AgentHomeWorkspaceSnapshot,
    now: Date,
): AgentHomeUiState = buildAgentHomeUiStateInternal(snapshot = snapshot, now = now)

@Composable
fun AgentHomeScreen(
    viewModel: AgentHomeViewModel = viewModel(factory = AgentHomeViewModelFactory()),
) {
    val state by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AgentNavRoutes.Home

    Scaffold(
        containerColor = Color.White,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AgentNavRoutes.Home,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AgentNavRoutes.Home) {
                    AgentHomeDashboard(
                        state = state,
                        onNavigateQueue = { navController.navigate(AgentNavRoutes.Queue) },
                        onNavigateQuickAction = { route -> navController.navigate(route) }
                    )
                }
                composable(AgentNavRoutes.Queue) { AgentQueueScreen() }
                composable(AgentNavRoutes.Chat) { AgentChatScreen() }
                composable(AgentNavRoutes.Performance) { AgentPerformanceScreen() }
                composable(AgentNavRoutes.Profile) { AgentProfileScreen(navController = navController) }
            }
        }
    }
}

@Composable
private fun AgentHomeDashboard(
    state: AgentHomeUiState,
    onNavigateQueue: (TicketStatusFilter?) -> Unit,
    onNavigateQuickAction: (String) -> Unit,
) {
    val tickets = remember(state.recentTickets) {
        state.recentTickets.map { ticket ->
            AgentHomeTicketDisplay(
                ticket = Ticket(
                    ticketId = ticket.ticketId,
                    employeeUid = "",
                    employeeName = ticket.employeeName,
                    employeeEmail = "",
                    workspaceName = "",
                    subject = ticket.title,
                    description = "",
                    category = "",
                    priority = ticket.priorityLabel,
                    status = ticket.statusLabel.replace(" ", "_"),
                    createdAt = Date(),
                    updatedAt = Date(),
                ),
                priorityLabel = ticket.priorityLabel,
                priorityColor = ticket.priorityColor,
                priorityBackground = ticket.priorityColor.copy(alpha = 0.12f),
                statusLabel = ticket.statusLabel,
                statusColor = ticket.statusColor,
                statusBackground = ticket.statusColor.copy(alpha = 0.14f),
                employeeName = ticket.employeeName,
                createdLabel = ticket.createdLabel,
            )
        }
    }

    val metrics = remember(state) {
        listOf(
            AgentHomeMetricDisplay(
                title = "Assigned Tickets",
                value = state.assignedTicketsCount.toString(),
                description = "+3 today",
                icon = Icons.Outlined.Dashboard,
                tint = AgentHomeAssigned,
                backgroundTint = AgentHomeAssigned.copy(alpha = 0.10f),
                statusFilter = TicketStatusFilter.OPEN,
            ),
            AgentHomeMetricDisplay(
                title = "In Progress",
                value = state.metrics.getOrNull(1)?.valueLabel ?: "0",
                description = "Updated just now",
                icon = Icons.Outlined.Schedule,
                tint = AgentHomeInProgress,
                backgroundTint = AgentHomeInProgress.copy(alpha = 0.12f),
                statusFilter = TicketStatusFilter.IN_PROGRESS,
            ),
            AgentHomeMetricDisplay(
                title = "Resolved Today",
                value = state.resolvedTodayCount.toString(),
                description = "Last refreshed 2 min ago",
                icon = Icons.Outlined.CheckCircle,
                tint = AgentHomeResolved,
                backgroundTint = AgentHomeResolved.copy(alpha = 0.10f),
                statusFilter = TicketStatusFilter.RESOLVED,
            ),
            AgentHomeMetricDisplay(
                title = "Pending",
                value = state.metrics.getOrNull(3)?.valueLabel ?: "0",
                description = "4 waiting",
                icon = Icons.Outlined.PendingActions,
                tint = AgentHomePending,
                backgroundTint = AgentHomePending.copy(alpha = 0.10f),
                statusFilter = TicketStatusFilter.ALL,
            ),
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("agent_home_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { AgentHomeHeader(state = state.header) }
        item {
            AgentHomeMetricsSection(
                metrics = metrics,
                onMetricClick = { metric -> onNavigateQueue(metric.statusFilter) }
            )
        }
        item {
            AgentHomeQuickActionsSection(
                onViewQueue = { onNavigateQuickAction(AgentNavRoutes.Queue) },
                onMyTickets = { onNavigateQuickAction(AgentNavRoutes.Performance) },
                onResolved = { onNavigateQueue(TicketStatusFilter.RESOLVED) },
                onRefresh = { onNavigateQuickAction(AgentNavRoutes.Home) },
            )
        }
        item {
            AgentHomeRecentTicketsSection(
                tickets = tickets,
                isLoading = state.isLoading,
                onRefresh = { onNavigateQuickAction(AgentNavRoutes.Home) },
                onViewAll = { onNavigateQueue(null) },
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun AgentHomeHeader(
    state: AgentHomeHeaderState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Surface(shape = AgentHomeChipShape, color = AgentHomePrimaryBlue.copy(alpha = 0.08f)) {
            Text(
                text = state.greeting.uppercase(Locale.ENGLISH),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                color = AgentHomePrimaryBlue,
                fontWeight = FontWeight.Bold,
                fontFamily = AgentHomeCanvasFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = state.agentName,
            style = MaterialTheme.typography.headlineLarge,
            color = AgentHomeText,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = AgentHomeCanvasFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = state.prompt,
            style = MaterialTheme.typography.bodyLarge,
            color = AgentHomeSubtext,
            fontFamily = AgentHomeCanvasFont,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AgentHomeStatusChip(label = state.timeLabel.ifBlank { state.dateLabel }, color = AgentHomePrimaryBlue)
            AgentHomeStatusChip(label = if (state.isOnline) "Online" else "Offline", color = if (state.isOnline) AgentHomeResolved else Color(0xFFFB7185))
        }
    }
}

@Composable
private fun AgentHomeMetricsSection(
    metrics: List<AgentHomeMetricDisplay>,
    onMetricClick: (AgentHomeMetricDisplay) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            metrics.take(2).forEach { metric ->
                AgentHomePremiumMetricCard(metric = metric, modifier = Modifier.weight(1f), onClick = { onMetricClick(metric) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            metrics.drop(2).take(2).forEach { metric ->
                AgentHomePremiumMetricCard(metric = metric, modifier = Modifier.weight(1f), onClick = { onMetricClick(metric) })
            }
        }
    }
}

@Composable
private fun AgentHomePremiumMetricCard(
    metric: AgentHomeMetricDisplay,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "metricScale"
    )
    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = AgentHomeCardShape,
        color = Color.White,
        border = BorderStroke(1.dp, metric.tint.copy(alpha = 0.14f)),
        shadowElevation = 12.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(metric.tint.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(54.dp)
                    .height(54.dp)
                    .background(metric.tint.copy(alpha = 0.08f), CircleShape)
            )
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.96f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(metric.backgroundTint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = metric.icon, contentDescription = null, tint = metric.tint)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = metric.value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = AgentHomeText,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = AgentHomeCanvasFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metric.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = AgentHomeText,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = AgentHomeCanvasFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metric.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = AgentHomeSubtext,
                        fontFamily = AgentHomeCanvasFont,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentHomeQuickActionsSection(
    onViewQueue: () -> Unit,
    onMyTickets: () -> Unit,
    onResolved: () -> Unit,
    onRefresh: () -> Unit,
) {
    val actions = listOf(
        AgentHomeActionItem("Queue", Icons.Outlined.Inbox, AgentHomeAssigned, AgentHomeAssigned.copy(alpha = 0.10f)),
        AgentHomeActionItem("My Tickets", Icons.Outlined.SupportAgent, AgentHomeInProgress, AgentHomeInProgress.copy(alpha = 0.10f)),
        AgentHomeActionItem("Resolved", Icons.Outlined.CheckCircle, AgentHomeResolved, AgentHomeResolved.copy(alpha = 0.10f)),
        AgentHomeActionItem("Refresh", Icons.Outlined.Refresh, AgentHomePrimaryBlue, AgentHomePrimaryBlue.copy(alpha = 0.10f)),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.headlineSmall,
            color = AgentHomeText,
            fontWeight = FontWeight.Bold,
            fontFamily = AgentHomeCanvasFont,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            AgentHomeActionCard(action = actions[0], modifier = Modifier.weight(1f), onClick = onViewQueue)
            AgentHomeActionCard(action = actions[1], modifier = Modifier.weight(1f), onClick = onMyTickets)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            AgentHomeActionCard(action = actions[2], modifier = Modifier.weight(1f), onClick = onResolved)
            AgentHomeActionCard(action = actions[3], modifier = Modifier.weight(1f), onClick = onRefresh)
        }
    }
}

@Composable
private fun AgentHomeActionCard(
    action: AgentHomeActionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "actionScale"
    )
    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = AgentHomeCardShape,
        color = AgentHomeSurface,
        border = BorderStroke(1.dp, action.tint.copy(alpha = 0.14f)),
        shadowElevation = 9.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Surface(shape = CircleShape, color = action.backgroundTint) {
                Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    Icon(imageVector = action.icon, contentDescription = null, tint = action.tint)
                }
            }
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleSmall,
                color = AgentHomeText,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AgentHomeCanvasFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AgentHomeRecentTicketsSection(
    tickets: List<AgentHomeTicketDisplay>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onViewAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Recent Tickets",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AgentHomeText,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AgentHomeCanvasFont,
                )
                Text(
                    text = "Latest assigned tickets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgentHomeSubtext,
                    fontFamily = AgentHomeCanvasFont,
                    maxLines = 1,
                )
            }
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = AgentHomePrimaryBlue,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AgentHomeCanvasFont,
                modifier = Modifier.clip(AgentHomeChipShape).clickable(onClick = onViewAll),
                maxLines = 1,
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AgentHomePrimaryBlue)
                }
            }
            tickets.isEmpty() -> {
                AgentHomeEmptyState(onRefresh = onRefresh)
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    tickets.take(5).forEach { ticket ->
                        AgentHomeTicketCard(ticket = ticket)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentHomeTicketCard(ticket: AgentHomeTicketDisplay) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ticketScale"
    )
    Surface(
        shape = AgentHomeCardShape,
        color = AgentHomeSurface,
        border = BorderStroke(1.dp, AgentHomeBorder),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, role = Role.Button) { }
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 18.dp)
                        .background(ticket.priorityColor, RoundedCornerShape(999.dp))
                )
                Text(
                    text = ticket.ticket.displayTicketId.ifBlank { ticket.ticket.ticketId },
                    style = MaterialTheme.typography.labelLarge,
                    color = AgentHomeText,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = AgentHomeCanvasFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.weight(1f))
                AgentHomeFilledChip(label = ticket.statusLabel, background = ticket.statusBackground, contentColor = ticket.statusColor)
            }
            Text(
                text = ticket.ticket.subject,
                style = MaterialTheme.typography.titleMedium,
                color = AgentHomeText,
                fontWeight = FontWeight.Bold,
                fontFamily = AgentHomeCanvasFont,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = ticket.employeeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgentHomeSubtext,
                    fontFamily = AgentHomeCanvasFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = ticket.createdLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgentHomeSubtext,
                    fontFamily = AgentHomeCanvasFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = AgentHomeSubtext)
            }
        }
    }
}

@Composable
private fun AgentHomeFilledChip(
    label: String,
    background: Color,
    contentColor: Color,
) {
    Surface(shape = AgentHomeChipShape, color = background) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = AgentHomeCanvasFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AgentHomeStatusChip(label: String, color: Color) {
    Surface(shape = AgentHomeChipShape, color = color.copy(alpha = 0.10f)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = AgentHomeCanvasFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AgentHomePriorityDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun AgentHomeEmptyState(onRefresh: () -> Unit) {
    Surface(
        shape = AgentHomeCardShape,
        color = AgentHomeSurface,
        border = BorderStroke(1.dp, AgentHomeBorder),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No recent tickets",
                style = MaterialTheme.typography.titleMedium,
                color = AgentHomeText,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AgentHomeCanvasFont,
            )
            Text(
                text = "Refresh to sync the latest workspace activity.",
                style = MaterialTheme.typography.bodyMedium,
                color = AgentHomeSubtext,
                fontFamily = AgentHomeCanvasFont,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = AgentHomePrimaryBlue),
                shape = AgentHomeChipShape,
            ) {
                Text(text = "Refresh", color = Color.White)
            }
        }
    }
}

private fun buildAgentHomeUiStateInternal(
    snapshot: AgentHomeWorkspaceSnapshot,
    now: Date,
): AgentHomeUiState {
    val currentUser = snapshot.currentUser
    val tickets = snapshot.tickets
    val assignedTickets = tickets.count { it.matchesStatusFilter(TicketStatusFilter.OPEN) }
    val inProgressTickets = tickets.count { it.matchesStatusFilter(TicketStatusFilter.IN_PROGRESS) }
    val resolvedTickets = tickets.count { it.matchesStatusFilter(TicketStatusFilter.RESOLVED) }
    val pendingTickets = tickets.count { it.status.trim().isBlank() || it.status.trim().uppercase(Locale.ENGLISH) == "PENDING" }
    val recentTickets = tickets
        .sortedByDescending { ticket -> ticket.updatedAt.timestampValue() ?: ticket.createdAt.timestampValue() ?: 0L }
        .take(5)
        .map { ticket ->
            val statusTone = ticketStatusTone(ticket.status)
            val statusColor = when (statusTone) {
                com.example.supporthub.features.tickets.TicketStatusTone.OPEN -> AgentHomeAssigned
                com.example.supporthub.features.tickets.TicketStatusTone.IN_PROGRESS -> AgentHomeInProgress
                com.example.supporthub.features.tickets.TicketStatusTone.RESOLVED -> AgentHomeResolved
            }
            val priorityColor = when (ticket.priority.trim().uppercase(Locale.ENGLISH)) {
                "LOW" -> AgentHomeResolved
                "MEDIUM" -> AgentHomeInProgress
                "CRITICAL" -> Color(0xFFEF4444)
                else -> AgentHomeAssigned
            }
            AgentHomeTicketCardState(
                ticketId = ticket.ticketId,
                title = ticket.subject,
                employeeName = ticket.employeeName.ifBlank { "Employee" },
                priorityLabel = ticket.priority,
                priorityColor = priorityColor,
                statusLabel = ticketStatusLabel(ticket.status),
                createdLabel = agentHomeCreatedFormatter.format(Date(ticket.createdAt.timestampValue() ?: now.time)),
                statusColor = statusColor,
            )
        }

    return AgentHomeUiState(
        header = AgentHomeHeaderState(
            timeLabel = agentHomeTimeFormatter.format(now),
            dateLabel = agentHomeDateFormatter.format(now),
            agentName = currentUser?.fullName?.trim().takeIf { !it.isNullOrBlank() } ?: "Support Agent",
            greeting = when ((now.time / 3_600_000L % 24).toInt()) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            },
            prompt = "You have $assignedTickets active tickets waiting.",
            avatarInitials = buildAvatarInitials(currentUser?.fullName ?: "Support Agent"),
            isOnline = true,
        ),
        assignedTicketsCount = assignedTickets,
        resolvedTodayCount = resolvedTickets,
        metrics = listOf(
            AgentHomeMetricCardState("Assigned Tickets", assignedTickets.toString(), Icons.Outlined.Dashboard, AgentHomeAssigned, AgentHomeAssigned.copy(alpha = 0.10f)),
            AgentHomeMetricCardState("In Progress", inProgressTickets.toString(), Icons.Outlined.Schedule, AgentHomeInProgress, AgentHomeInProgress.copy(alpha = 0.12f)),
            AgentHomeMetricCardState("Resolved Today", resolvedTickets.toString(), Icons.Outlined.CheckCircle, AgentHomeResolved, AgentHomeResolved.copy(alpha = 0.10f)),
            AgentHomeMetricCardState("Pending", pendingTickets.toString(), Icons.Outlined.PendingActions, AgentHomePending, AgentHomePending.copy(alpha = 0.10f)),
        ),
        recentTickets = recentTickets,
        isLoading = false,
        errorMessage = null,
    )
}

private fun buildAvatarInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+"))
    return parts.take(2).joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString().orEmpty() }.ifBlank { "SA" }
}

private fun Any?.timestampValue(): Long? = when (this) {
    is com.google.firebase.Timestamp -> this.toDate().time
    is Date -> time
    is Long -> this
    is Number -> this.toLong()
    else -> null
}


