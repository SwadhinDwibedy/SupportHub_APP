package com.example.supporthub.features.agent

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.features.tickets.TicketStatusFilter
import com.example.supporthub.features.tickets.displayTicketId
import com.example.supporthub.features.tickets.matchesStatusFilter
import com.example.supporthub.features.tickets.ticketStatusLabel
import com.example.supporthub.features.tickets.ticketStatusTone
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
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

private val agentQueueZoneId: ZoneId = ZoneId.systemDefault()
private val agentQueueDateFormatter = DateTimeFormatter.ofPattern("EEEE • d MMM", Locale.ENGLISH)
private val agentQueueClockFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
private val agentQueueTimestampFormatter = DateTimeFormatter.ofPattern("d MMM • hh:mm a", Locale.ENGLISH)

private val QueueBlue = Color(0xFF2563EB)
private val QueueBlueSoft = Color(0xFFEAF1FF)
private val QueueOrange = Color(0xFFF59E0B)
private val QueueOrangeSoft = Color(0xFFFFF2DB)
private val QueueGreen = Color(0xFF16A34A)
private val QueueGreenSoft = Color(0xFFE7F8ED)
private val QueueRed = Color(0xFFEF4444)
private val QueueRedSoft = Color(0xFFFFE7E7)
private val QueuePurple = Color(0xFF8B5CF6)
private val QueuePurpleSoft = Color(0xFFF1E8FF)
private val QueueSlate = Color(0xFF0F172A)
private val QueueSlateSoft = Color(0xFF64748B)
private val QueueBorder = Color(0xFFE5EAF2)
private val QueuePageBackground = Color(0xFFFAFAFA)
private val QueueSurface = Color.White
private val QueueCardShape = RoundedCornerShape(22.dp)
private val QueueChipShape = RoundedCornerShape(999.dp)

data class AgentQueueFilterChipState(
    val label: String,
    val selected: Boolean = false,
)

data class AgentQueueSummaryCardState(
    val label: String,
    val valueLabel: String,
    val noteLabel: String,
    val accentColor: Color,
    val tintSoft: Color,
    val icon: ImageVector,
    val routeTag: String,
)

data class AgentQueueTicketCardState(
    val firebaseTicketId: String,
    val displayTicketId: String,
    val title: String,
    val employeeName: String,
    val department: String,
    val updatedLabel: String,
    val assignedLabel: String,
    val priorityLabel: String,
    val priorityColor: Color,
    val prioritySoft: Color,
    val statusLabel: String,
    val statusColor: Color,
    val statusSoft: Color,
    val avatarInitials: String,
)

data class AgentQueueSectionUiState(
    val title: String,
    val ticketCountLabel: String,
    val accentColor: Color,
    val tickets: List<AgentQueueTicketCardState>,
)

data class AgentQueueWorkspaceSnapshot(
    val currentUser: User? = null,
    val tickets: List<Ticket> = emptyList(),
)

data class AgentQueueHeaderState(
    val dateLabel: String,
    val timeLabel: String,
    val workspaceName: String,
    val connectedLabel: String,
    val avatarInitials: String,
    val isOnline: Boolean,
)

data class AgentQueueUiState(
    val header: AgentQueueHeaderState = AgentQueueHeaderState(
        dateLabel = "",
        timeLabel = "",
        workspaceName = "Workspace",
        connectedLabel = "Live Queue • Connected",
        avatarInitials = "SA",
        isOnline = true,
    ),
    val totalTicketsCount: Int = 0,
    val openCount: Int = 0,
    val inProgressCount: Int = 0,
    val resolvedCount: Int = 0,
    val highPriorityCount: Int = 0,
    val summaryCards: List<AgentQueueSummaryCardState> = emptyList(),
    val filterChips: List<AgentQueueFilterChipState> = emptyList(),
    val sectionCards: List<AgentQueueSectionUiState> = emptyList(),
    val realtimeBanner: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

enum class AgentQueueFilterType(val label: String) {
    ALL("All"),
    ASSIGNED("Assigned"),
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    HIGH_PRIORITY("High Priority"),
    CRITICAL("Critical"),
    PENDING("Pending"),
    RESOLVED("Resolved"),
    TODAY("Today"),
    OVERDUE("Overdue"),
}

interface AgentQueueRepository {
    fun observeWorkspaceSnapshot(): Flow<AgentQueueWorkspaceSnapshot>
}

class FirebaseAgentQueueRepository(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = authRepository),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : AgentQueueRepository {

    override fun observeWorkspaceSnapshot(): Flow<AgentQueueWorkspaceSnapshot> = channelFlow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null || currentUser.uid.isBlank()) {
            send(AgentQueueWorkspaceSnapshot())
            close()
            return@channelFlow
        }

        val workspaceName = currentUser.workspaceName.trim()
        if (workspaceName.isBlank()) {
            send(AgentQueueWorkspaceSnapshot(currentUser = currentUser, tickets = emptyList()))
            close()
            return@channelFlow
        }

        val userFlow = observeUserDocument(currentUser.uid)
        val ticketsFlow = ticketRepository.observeAllTickets()

        launch {
            combine(userFlow, ticketsFlow) { user, tickets ->
                val latestUser = user ?: currentUser
                AgentQueueWorkspaceSnapshot(
                    currentUser = latestUser,
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
            val registration = firestore
                .collection(FirebaseCollections.USERS)
                .document(uid)
                .addSnapshotListener { snapshot, error ->
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

class AgentQueueViewModel(
    private val repository: AgentQueueRepository = FirebaseAgentQueueRepository(),
    private val clock: Clock = Clock.system(agentQueueZoneId),
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(AgentQueueFilterType.ALL)
    val selectedFilter: StateFlow<AgentQueueFilterType> = _selectedFilter.asStateFlow()

    private val _snapshot = MutableStateFlow(AgentQueueWorkspaceSnapshot())
    private val _uiState = MutableStateFlow(AgentQueueUiState())
    val uiState: StateFlow<AgentQueueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWorkspaceSnapshot().collect { snapshot ->
                _snapshot.value = snapshot
                rebuildUiState()
            }
        }

        viewModelScope.launch {
            _selectedFilter.collect {
                rebuildUiState()
            }
        }
    }

    fun setFilter(filter: AgentQueueFilterType) {
        _selectedFilter.value = filter
    }

    private fun rebuildUiState() {
        val snapshot = _snapshot.value
        val now = ZonedDateTime.now(clock).toInstant()

        _uiState.value = buildAgentQueueUiState(
            currentAgentName = resolveAgentDisplayName(snapshot.currentUser),
            currentUserId = snapshot.currentUser?.uid.orEmpty(),
            workspaceName = snapshot.currentUser?.workspaceName.orEmpty(),
            tickets = snapshot.tickets,
            now = now,
            selectedFilter = _selectedFilter.value,
        )
    }
}

class AgentQueueViewModelFactory(
    private val clock: Clock = Clock.system(agentQueueZoneId),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentQueueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentQueueViewModel(clock = clock) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

fun buildAgentQueueUiState(
    currentAgentName: String,
    currentUserId: String,
    workspaceName: String,
    tickets: List<Ticket>,
    now: Instant,
    selectedFilter: AgentQueueFilterType,
): AgentQueueUiState {
    val zonedNow = now.atZone(agentQueueZoneId)

    val workspaceTickets = tickets
        .filter { ticket ->
            workspaceName.isBlank() ||
                    ticket.workspaceName.trim().equals(workspaceName.trim(), ignoreCase = true)
        }
        .sortedByDescending { ticket ->
            ticket.updatedAt.toEpochMillis()
        }

    val filteredTickets = workspaceTickets.filter { ticket ->
        ticket.matchesAgentQueueFilter(
            filter = selectedFilter,
            currentUserId = currentUserId,
            now = now,
        )
    }

    val openTickets = filteredTickets.count { it.isOpenTicket() }
    val inProgressTickets = filteredTickets.count { it.isInProgressTicket() }
    val resolvedTickets = filteredTickets.count { it.isResolvedTicket() }
    val highPriorityTickets = filteredTickets.filter { it.isHighPriorityTicket() }

    val summaryCards = listOf(
        AgentQueueSummaryCardState(
            label = "Open Tickets",
            valueLabel = openTickets.toString(),
            noteLabel = if (openTickets == 1) "1 ticket awaiting action" else "$openTickets tickets awaiting action",
            accentColor = QueueBlue,
            tintSoft = QueueBlueSoft,
            icon = Icons.Outlined.Inbox,
            routeTag = TicketStatusFilter.OPEN.name,
        ),
        AgentQueueSummaryCardState(
            label = "In Progress",
            valueLabel = inProgressTickets.toString(),
            noteLabel = "Active assignments in motion",
            accentColor = QueueOrange,
            tintSoft = QueueOrangeSoft,
            icon = Icons.Outlined.Assignment,
            routeTag = TicketStatusFilter.IN_PROGRESS.name,
        ),
        AgentQueueSummaryCardState(
            label = "Resolved Today",
            valueLabel = resolvedTickets.toString(),
            noteLabel = if (resolvedTickets == 1) "1 ticket closed today" else "$resolvedTickets tickets closed today",
            accentColor = QueueGreen,
            tintSoft = QueueGreenSoft,
            icon = Icons.Outlined.CloudDone,
            routeTag = TicketStatusFilter.RESOLVED.name,
        ),
        AgentQueueSummaryCardState(
            label = "High Priority",
            valueLabel = highPriorityTickets.size.toString(),
            noteLabel = "Needs escalation attention",
            accentColor = QueueRed,
            tintSoft = QueueRedSoft,
            icon = Icons.Outlined.PriorityHigh,
            routeTag = "HIGH_PRIORITY",
        ),
    )

    val filterChips = AgentQueueFilterType.entries.map { filter ->
        AgentQueueFilterChipState(
            label = filter.label,
            selected = filter == selectedFilter,
        )
    }

    val highPrioritySectionTickets = filteredTickets.filter { it.isHighPriorityTicket() }
    val workingNowTickets = filteredTickets.filter { !it.isResolvedTicket() && !it.isHighPriorityTicket() }
    val resolvedTodayTickets = filteredTickets.filter { it.isResolvedTicket() }

    val highPrioritySection = AgentQueueSectionUiState(
        title = "High Priority",
        ticketCountLabel = if (highPrioritySectionTickets.size == 1) "1 Ticket" else "${highPrioritySectionTickets.size} Tickets",
        accentColor = QueueRed,
        tickets = highPrioritySectionTickets.map { it.toQueueTicketCardState() },
    )

    val workingNowSection = AgentQueueSectionUiState(
        title = "Working Now",
        ticketCountLabel = if (workingNowTickets.size == 1) "1 Ticket" else "${workingNowTickets.size} Tickets",
        accentColor = QueueOrange,
        tickets = workingNowTickets.map { it.toQueueTicketCardState() },
    )

    val resolvedTodaySection = AgentQueueSectionUiState(
        title = "Resolved Today",
        ticketCountLabel = if (resolvedTodayTickets.size == 1) "1 Ticket" else "${resolvedTodayTickets.size} Tickets",
        accentColor = QueueGreen,
        tickets = resolvedTodayTickets.map { it.toQueueTicketCardState() },
    )

    val realtimeLabel = "Live Queue • Connected"

    return AgentQueueUiState(
        header = AgentQueueHeaderState(
            dateLabel = zonedNow.format(agentQueueDateFormatter),
            timeLabel = zonedNow.format(agentQueueClockFormatter),
            workspaceName = workspaceName.ifBlank { "Workspace" },
            connectedLabel = realtimeLabel,
            avatarInitials = initialsForName(currentAgentName.ifBlank { "Support Agent" }),
            isOnline = true,
        ),
        totalTicketsCount = filteredTickets.size,
        openCount = openTickets,
        inProgressCount = inProgressTickets,
        resolvedCount = resolvedTickets,
        highPriorityCount = highPriorityTickets.size,
        summaryCards = summaryCards,
        filterChips = filterChips,
        sectionCards = listOf(highPrioritySection, workingNowSection, resolvedTodaySection),
        realtimeBanner = realtimeLabel,
        isLoading = false,
        errorMessage = null,
    )
}

private fun Ticket.matchesAgentQueueFilter(
    filter: AgentQueueFilterType,
    currentUserId: String,
    now: Instant,
): Boolean {
    return when (filter) {
        AgentQueueFilterType.ALL -> true
        AgentQueueFilterType.ASSIGNED ->
            assignedAgentId?.equals(currentUserId, ignoreCase = true) == true
        AgentQueueFilterType.OPEN -> isOpenTicket()
        AgentQueueFilterType.IN_PROGRESS -> isInProgressTicket()
        AgentQueueFilterType.RESOLVED -> isResolvedTicket()
        AgentQueueFilterType.HIGH_PRIORITY -> isHighPriorityTicket()
        AgentQueueFilterType.CRITICAL -> priority.equals("critical", ignoreCase = true)
        AgentQueueFilterType.PENDING -> priority.equals("pending", ignoreCase = true)
        AgentQueueFilterType.TODAY -> isCreatedOrUpdatedToday(now)
        AgentQueueFilterType.OVERDUE -> isOverdueTicket(now)
    }
}

private fun Ticket.isOpenTicket(): Boolean = matchesStatusFilter(TicketStatusFilter.OPEN)
private fun Ticket.isInProgressTicket(): Boolean = matchesStatusFilter(TicketStatusFilter.IN_PROGRESS)
private fun Ticket.isResolvedTicket(): Boolean = matchesStatusFilter(TicketStatusFilter.RESOLVED)

private fun Ticket.isHighPriorityTicket(): Boolean {
    return priority.equals("critical", ignoreCase = true) ||
            priority.equals("high", ignoreCase = true)
}

private fun Ticket.isCreatedOrUpdatedToday(now: Instant): Boolean {
    val today = now.atZone(agentQueueZoneId).toLocalDate()

    val updatedInstant = when (val value = updatedAt) {
        is Timestamp -> value.toDate().toInstant()
        is Date -> value.toInstant()
        is Long -> Instant.ofEpochMilli(value)
        is String -> runCatching { Instant.parse(value) }.getOrNull()
        else -> null
    } ?: return false

    return updatedInstant.atZone(agentQueueZoneId).toLocalDate() == today
}

private fun Ticket.isOverdueTicket(now: Instant): Boolean = false

@Composable
fun AgentQueueScreen(
    clock: Clock = Clock.system(agentQueueZoneId),
    viewModel: AgentQueueViewModel = viewModel(factory = AgentQueueViewModelFactory(clock)),
    onNavigateQueue: (TicketStatusFilter?) -> Unit = {},
    onNavigateTicket: (Ticket) -> Unit = {},
    onNavigateCompleteQueue: () -> Unit = {},
    onRefreshQueue: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTicketForDialog by remember { mutableStateOf<AgentQueueTicketCardState?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(QueuePageBackground)
            .statusBarsPadding(),
        color = QueuePageBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            AgentQueueHeaderSection(state = uiState.header, onRefresh = onRefreshQueue)
            AgentQueueMetricsSection(
                cards = uiState.summaryCards,
                onCardClick = { card ->
                    when (card.routeTag) {
                        TicketStatusFilter.OPEN.name -> onNavigateQueue(TicketStatusFilter.OPEN)
                        TicketStatusFilter.IN_PROGRESS.name -> onNavigateQueue(TicketStatusFilter.IN_PROGRESS)
                        TicketStatusFilter.RESOLVED.name -> onNavigateQueue(TicketStatusFilter.RESOLVED)
                        else -> onNavigateQueue(null)
                    }
                },
            )
            AgentQueueFilterRow(
                chips = uiState.filterChips,
                selectedLabel = selectedFilter.label,
                onSelected = { label ->
                    AgentQueueFilterType.entries.firstOrNull { it.label == label }?.let {
                        viewModel.setFilter(it)
                    }
                },
            )
            AgentQueueSectionCard(
                section = uiState.sectionCards.getOrNull(0),
                onViewAll = onNavigateCompleteQueue,
                onTicketClick = { ticket ->
                    if (selectedFilter == AgentQueueFilterType.ASSIGNED) {
                        selectedTicketForDialog = ticket
                    } else {
                        onNavigateTicket(ticket.toTicket())
                    }
                },
                isLoading = uiState.isLoading,
            )
            AgentQueueSectionCard(
                section = uiState.sectionCards.getOrNull(1),
                onViewAll = onNavigateCompleteQueue,
                onTicketClick = { ticket ->
                    if (selectedFilter == AgentQueueFilterType.ASSIGNED) {
                        selectedTicketForDialog = ticket
                    } else {
                        onNavigateTicket(ticket.toTicket())
                    }
                },
                isLoading = uiState.isLoading,
            )
            AgentQueueSectionCard(
                section = uiState.sectionCards.getOrNull(2),
                onViewAll = onNavigateCompleteQueue,
                onTicketClick = { ticket ->
                    if (selectedFilter == AgentQueueFilterType.ASSIGNED) {
                        selectedTicketForDialog = ticket
                    } else {
                        onNavigateTicket(ticket.toTicket())
                    }
                },
                isLoading = uiState.isLoading,
            )
            AgentQueueFooterActions(onViewAll = onNavigateCompleteQueue, onRefresh = onRefreshQueue)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    selectedTicketForDialog?.let { ticket ->
        AgentQueueTicketDetailsDialog(
            ticket = ticket,
            onDismiss = { selectedTicketForDialog = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentQueueTicketDetailsDialog(
    ticket: AgentQueueTicketCardState,
    onDismiss: () -> Unit,
) {
    val ticketRepository = remember { TicketRepositoryImpl() }
    val scope = rememberCoroutineScope()
    var selectedStatus by rememberSaveable(ticket.displayTicketId) {
        mutableStateOf(normalizeTicketStatus(ticket.statusLabel))
    }
    val statusOptions = remember { listOf("Open", "In Progress", "Resolved") }
    val isResolvedSelected = selectedStatus.equals("Resolved", ignoreCase = true)
    var isSaving by rememberSaveable(ticket.displayTicketId) {
        mutableStateOf(false)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        sheetMaxWidth = androidx.compose.ui.unit.Dp.Unspecified,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 0.dp)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 5.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
            )
            Text(
                text = ticket.displayTicketId,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = QueueSlate,
            )
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = QueueSlate,
            )
            TicketDetailRow("Employee", ticket.employeeName)
            TicketDetailRow("Department", ticket.department)
            TicketDetailRow("Assigned To", ticket.assignedLabel)
            TicketDetailRow("Priority", ticket.priorityLabel)
            StatusDropdownField(
                value = selectedStatus,
                options = statusOptions,
                onSelected = { selectedStatus = it },
            )
            TicketDetailRow("Updated", ticket.updatedLabel)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isSaving) return@Button
                    scope.launch {
                        isSaving = true
                        val normalizedStatus = normalizeTicketStatus(selectedStatus)
                        val updateResult = ticketRepository.updateTicketDetails(
                            ticketId = ticket.firebaseTicketId,
                            updates = mapOf(
                                "status" to normalizedStatus.replace(' ', '_').uppercase(Locale.ENGLISH),
                                "updatedAt" to Timestamp.now(),
                            ),
                        )
                        isSaving = false
                        if (updateResult.isSuccess) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isResolvedSelected) QueueGreen else QueueBlue,
                ),
                shape = RoundedCornerShape(18.dp),
                enabled = !isSaving,
            ) {
                Text(if (isSaving) "Saving..." else if (isResolvedSelected) "Mark as Resolved" else "Save Changes")
            }
        }
    }
}

@Composable
private fun TicketDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = QueueSlateSoft)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = QueueSlate)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdownField(
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),

            label = {
                Text(
                    text = "Status",
                    color = QueueSlateSoft
                )
            },

            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = QueueSlate
            ),

            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },

            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = QueueSlate,
                unfocusedTextColor = QueueSlate,
                disabledTextColor = QueueSlate,

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,

                focusedBorderColor = QueueBlue,
                unfocusedBorderColor = QueueBorder,

                focusedLabelColor = QueueBlue,
                unfocusedLabelColor = QueueSlateSoft,

                cursorColor = QueueBlue
            )
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = Color.White
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = QueueSlate
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun normalizeTicketStatus(status: String): String {
    val normalized = status.trim().lowercase(Locale.ENGLISH)
    return when {
        normalized.contains("resolved") -> "Resolved"
        normalized.contains("progress") -> "In Progress"
        else -> "Open"
    }
}

@Composable
private fun AgentQueueHeaderSection(
    state: AgentQueueHeaderState,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.headlineLarge,
                    color = QueueSlate,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.workspaceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = QueueSlateSoft,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.timeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = QueueSlateSoft,
                    fontWeight = FontWeight.Medium,
                )
                QueueStatusDot(isOnline = state.isOnline)
                QueueAvatar(initials = state.avatarInitials)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = state.dateLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = QueueSlate,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = state.connectedLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = QueueGreen,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            IconButtonPill(
                icon = Icons.Outlined.Refresh,
                label = "Refresh",
                color = QueueBlue,
                onClick = onRefresh,
            )
        }
    }
}

@Composable
private fun QueueStatusDot(isOnline: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (isOnline) QueueGreen else QueueRed, CircleShape)
        )
        Text(
            text = if (isOnline) "Online" else "Offline",
            style = MaterialTheme.typography.labelMedium,
            color = QueueSlateSoft,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun QueueAvatar(initials: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(QueueSlate, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.take(2),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IconButtonPill(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background by animateColorAsState(
        targetValue = if (pressed) color.copy(alpha = 0.14f) else color.copy(alpha = 0.07f),
        label = "refreshButtonBackground"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "refreshButtonScale"
    )

    Surface(
        shape = QueueChipShape,
        color = background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, color.copy(alpha = if (pressed) 0.26f else 0.14f)),
        modifier = Modifier
            .scale(contentScale)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AgentQueueMetricsSection(
    cards: List<AgentQueueSummaryCardState>,
    onCardClick: (AgentQueueSummaryCardState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.take(2).forEach { card ->
                AgentQueueMetricCard(card = card, modifier = Modifier.weight(1f), onClick = { onCardClick(card) })
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.drop(2).take(2).forEach { card ->
                AgentQueueMetricCard(card = card, modifier = Modifier.weight(1f), onClick = { onCardClick(card) })
            }
        }
    }
}

@Composable
private fun AgentQueueMetricCard(
    card: AgentQueueSummaryCardState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "metricScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (pressed) 4.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "metricElevation"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 132.dp)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = QueueSurface,
        shadowElevation = elevation,
        border = BorderStroke(1.dp, QueueBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(card.tintSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(card.accentColor.copy(alpha = 0.10f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = null,
                            tint = card.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .background(card.accentColor, CircleShape)
                )
            }
            Text(
                text = card.label,
                style = MaterialTheme.typography.labelMedium,
                color = QueueSlateSoft,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = card.valueLabel,
                style = MaterialTheme.typography.headlineLarge,
                color = QueueSlate,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = card.noteLabel,
                style = MaterialTheme.typography.labelSmall,
                color = QueueSlateSoft,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AgentQueueFilterRow(
    chips: List<AgentQueueFilterChipState>,
    selectedLabel: String,
    onSelected: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(chips) { chip ->
            val isSelected = chip.label == selectedLabel
            AgentQueueFilterChip(
                label = chip.label,
                selected = isSelected,
                onClick = { onSelected(chip.label) }
            )
        }
    }
}

@Composable
private fun AgentQueueFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(targetValue = if (selected) QueueSlate else QueueSurface, label = "chipBg")
    val contentColor by animateColorAsState(targetValue = if (selected) Color.White else QueueSlateSoft, label = "chipContent")

    Surface(
        shape = QueueChipShape,
        color = background,
        shadowElevation = if (selected) 7.dp else 3.dp,
        border = BorderStroke(1.dp, if (selected) QueueSlate else QueueBorder),
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AgentQueueSectionCard(
    section: AgentQueueSectionUiState?,
    onViewAll: () -> Unit,
    onTicketClick: (AgentQueueTicketCardState) -> Unit,
    isLoading: Boolean,
) {
    if (section == null) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .background(section.accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = section.title, style = MaterialTheme.typography.titleLarge, color = QueueSlate, fontWeight = FontWeight.Bold)
                Text(text = section.ticketCountLabel, style = MaterialTheme.typography.labelLarge, color = QueueSlateSoft)
            }
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = section.accentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(role = Role.Button, onClick = onViewAll),
            )
        }
        when {
            isLoading -> AgentQueueLoadingList()
            section.tickets.isEmpty() -> AgentQueueEmptySection(section)
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                section.tickets.forEach { ticket ->
                    AgentQueueTicketCard(ticket = ticket, onClick = { onTicketClick(ticket) })
                }
            }
        }
    }
}

@Composable
private fun AgentQueueTicketCard(
    ticket: AgentQueueTicketCardState,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ticketScale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick),
        shape = QueueCardShape,
        color = QueueSurface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, QueueBorder),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(106.dp)
                    .background(ticket.priorityColor, CircleShape)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        Text(text = ticket.displayTicketId, style = MaterialTheme.typography.labelLarge, color = QueueSlateSoft, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            QueueBadge(text = ticket.priorityLabel, tint = ticket.priorityColor, tintSoft = ticket.prioritySoft)
                            QueueBadge(text = ticket.statusLabel, tint = ticket.statusColor, tintSoft = ticket.statusSoft)
                        }
                    }
                    Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = QueueSlateSoft)
                }
                Text(text = ticket.title, style = MaterialTheme.typography.titleMedium, color = QueueSlate, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    QueueAvatar(initials = ticket.avatarInitials)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text(text = ticket.employeeName, style = MaterialTheme.typography.bodyLarge, color = QueueSlate, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = ticket.department, style = MaterialTheme.typography.labelLarge, color = QueueSlateSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(text = ticket.updatedLabel, style = MaterialTheme.typography.labelMedium, color = QueueSlateSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text(text = "Assigned to", style = MaterialTheme.typography.labelMedium, color = QueueSlateSoft)
                        Text(text = ticket.assignedLabel, style = MaterialTheme.typography.bodyMedium, color = QueueSlate, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(text = ticket.displayTicketId, style = MaterialTheme.typography.labelMedium, color = QueueSlateSoft)
                }
            }
        }
    }
}

@Composable
private fun QueueBadge(
    text: String,
    tint: Color,
    tintSoft: Color,
) {
    Surface(shape = QueueChipShape, color = tintSoft) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun AgentQueueEmptySection(section: AgentQueueSectionUiState) {
    Surface(
        shape = QueueCardShape,
        color = QueueSurface,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, QueueBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(section.accentColor.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Outlined.SearchOff, contentDescription = null, tint = section.accentColor)
            }
            Text(text = "Queue is clear", style = MaterialTheme.typography.titleLarge, color = QueueSlate, fontWeight = FontWeight.Bold)
            Text(text = "No active tickets at the moment.", style = MaterialTheme.typography.bodyMedium, color = QueueSlateSoft)
            Button(
                onClick = {},
                shape = QueueChipShape,
                colors = ButtonDefaults.buttonColors(containerColor = section.accentColor, contentColor = Color.White),
            ) {
                Text(text = "Refresh Queue", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AgentQueueLoadingList() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Surface(
                shape = QueueCardShape,
                color = QueueSurface,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, QueueBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.width(120.dp).height(18.dp).background(QueueBorder, QueueChipShape))
                        Box(modifier = Modifier.fillMaxWidth().height(18.dp).background(QueueBorder, QueueChipShape))
                        Box(modifier = Modifier.fillMaxWidth().height(72.dp).background(QueueBorder.copy(alpha = 0.8f), QueueCardShape))
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentQueueFooterActions(
    onViewAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onViewAll,
            modifier = Modifier.weight(1f),
            shape = QueueChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = QueueSlate, contentColor = Color.White),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(text = "Complete Queue", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onRefresh,
            modifier = Modifier.weight(1f),
            shape = QueueChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = QueueBlue, contentColor = Color.White),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Refresh", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun Ticket.toQueueTicketCardState(): AgentQueueTicketCardState {
    val status = status.lowercase(Locale.ENGLISH)
    val priority = priority.lowercase(Locale.ENGLISH)

    val priorityColor = when {
        priority.contains("critical") -> QueueRed
        priority.contains("high") -> QueueOrange
        priority.contains("medium") -> QueueBlue
        else -> QueueGreen
    }

    val statusColor = when {
        status.contains("open") -> QueueBlue
        status.contains("progress") -> QueueOrange
        status.contains("pending") -> QueuePurple
        else -> QueueGreen
    }

    val statusSoft = statusColor.copy(alpha = 0.12f)

    val updatedLabel = when (val value = updatedAt) {
        is Timestamp -> "Updated ${value.toDate().toInstant().atZone(agentQueueZoneId).format(agentQueueTimestampFormatter)}"
        is Date -> "Updated ${value.toInstant().atZone(agentQueueZoneId).format(agentQueueTimestampFormatter)}"
        is Long -> "Updated ${Instant.ofEpochMilli(value).atZone(agentQueueZoneId).format(agentQueueTimestampFormatter)}"
        is String -> "Updated $value"
        else -> "Updated recently"
    }

    return AgentQueueTicketCardState(
        firebaseTicketId = ticketId,
        displayTicketId = displayTicketId,
        title = subject,
        employeeName = employeeName.ifBlank { "Unknown requester" },
        department = category.ifBlank { "General" },
        updatedLabel = updatedLabel,
        assignedLabel = assignedAgentName?.takeIf { it.isNotBlank() } ?: "Unassigned",
        priorityLabel = priority.uppercase(Locale.ENGLISH),
        priorityColor = priorityColor,
        prioritySoft = priorityColor.copy(alpha = 0.10f),
        statusLabel = ticketStatusLabel(status).uppercase(Locale.ENGLISH),
        statusColor = statusColor,
        statusSoft = statusSoft,
        avatarInitials = initialsForName(employeeName),
    )
}

private fun AgentQueueTicketCardState.toTicket(): Ticket = Ticket(
    ticketId = firebaseTicketId,
    subject = title,
    employeeName = employeeName,
    category = department,
    status = statusLabel,
    priority = priorityLabel,
    assignedAgentName = assignedLabel.takeIf { it != "Unassigned" },
    updatedAt = updatedLabel,
)

private fun Any?.toEpochMillis(): Long = when (this) {
    is Timestamp -> toDate().toInstant().toEpochMilli()
    is Date -> toInstant().toEpochMilli()
    is Long -> this
    is String -> runCatching { Instant.parse(this).toEpochMilli() }.getOrDefault(0L)
    else -> 0L
}

private fun initialsForName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.ENGLISH)
        parts.size == 1 -> parts[0].take(2).uppercase(Locale.ENGLISH)
        else -> "SA"
    }
}

private fun resolveAgentDisplayName(user: User?): String {
    val candidate = user?.fullName?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore('@')
        ?: "Support Agent"
    return candidate.trim().ifBlank { "Support Agent" }
}

