package com.example.supporthub.features.admin

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketAssignmentUpdate
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.features.tickets.displayTicketId
import com.example.supporthub.features.tickets.ticketPriorityTone
import com.example.supporthub.features.tickets.ticketStatusLabel
import com.example.supporthub.features.tickets.ticketStatusTone
import com.example.supporthub.ui.theme.SupportHubFontFamilies
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val adminTicketsBackground = Color(0xFFF5F7FB)
private val adminTicketsCard = Color.White
private val adminTicketsAccent = Color(0xFF0F766E)
private val adminTicketsAccentSoft = Color(0xFFDBF5F2)
private val adminTicketsBlue = Color(0xFF2563EB)
private val adminTicketsPending = Color(0xFFF59E0B)
private val adminTicketsResolved = Color(0xFF0F8A6B)
private val adminTicketsMuted = Color(0xFF64748B)
private val adminTicketsHeading = Color(0xFF0F172A)
private val adminTicketsBorder = Color(0xFFD9E2EC)
private val adminTicketsOnline = Color(0xFF059669)
private val adminTicketsOffline = Color(0xFF94A3B8)
private val adminTicketsWarning = Color(0xFFF59E0B)

private enum class AdminTicketStatusTone {
    OPEN,
    PENDING,
    RESOLVED,
}

enum class AdminTicketStatusFilter(val label: String) {
    ALL("All"),
    OPEN("Open"),
    PENDING("Pending"),
    RESOLVED("Resolved"),
}

enum class AgentQuickFilter(val label: String) {
    ALL("All"),
    ONLINE("Online"),
    OFFLINE("Offline"),
    LEAST_BUSY("Least Busy"),
}

data class AdminTicketSummaryCard(
    val label: String,
    val value: String,
    val accentColor: Color,
)

data class AdminTicketListItem(
    val ticketId: String,          // REAL Firestore ID
    val displayTicketId: String,   // Short ID shown in UI
    val subject: String,
    val requesterName: String,
    val requesterInitials: String,
    val createdAtLabel: String,
    val categoryLabel: String,
    val priorityLabel: String,
    val priorityColor: Color,
    val statusLabel: String,
    val statusColor: Color,
    val assignmentStatusLabel: String,
    val assignmentAgentName: String,
    val assignmentAgentDepartment: String,
    val assignedAgentId: String? = null,
    val requesterEmail: String = "",
    val requesterDepartment: String = "",
)

data class SupportAgentOption(
    val uid: String,
    val fullName: String,
    val department: String,
    val activeTicketCount: Int,
    val isOnline: Boolean,
    val initials: String,
    val email: String,
)

data class AdminTicketsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: AdminTicketStatusFilter = AdminTicketStatusFilter.ALL,
    val totalTicketsCount: Int = 0,
    val openTicketsCount: Int = 0,
    val pendingTicketsCount: Int = 0,
    val resolvedTicketsCount: Int = 0,
    val summaryCards: List<AdminTicketSummaryCard> = emptyList(),
    val visibleTickets: List<AdminTicketListItem> = emptyList(),
) {
    val filterChips: List<AdminTicketStatusFilter> = AdminTicketStatusFilter.entries
}

data class AdminTicketAssignmentUiState(
    val isSheetVisible: Boolean = false,
    val isLoadingAgents: Boolean = false,
    val isAssigning: Boolean = false,
    val ticketId: String? = null,
    val ticketSubject: String = "",
    val ticketDepartment: String = "",
    val searchQuery: String = "",
    val quickFilter: AgentQuickFilter = AgentQuickFilter.ALL,
    val availableAgents: List<SupportAgentOption> = emptyList(),
    val assignmentMessage: String? = null,
    val errorMessage: String? = null,
)

interface AdminTicketsRepository {
    suspend fun getCurrentUser(): User?
    fun observeAllTickets(): Flow<List<Ticket>>
    suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>>
    suspend fun assignTicket(ticketId: String, assignment: TicketAssignmentUpdate): Result<Unit>
}

class FirebaseAdminTicketsRepository(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = authRepository),
) : AdminTicketsRepository {
    override suspend fun getCurrentUser(): User? = authRepository.getCurrentUser()
    override fun observeAllTickets(): Flow<List<Ticket>> = ticketRepository.observeAllTickets()
    override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> =
        ticketRepository.getWorkspaceUsers(workspaceName)

    override suspend fun assignTicket(ticketId: String, assignment: TicketAssignmentUpdate): Result<Unit> =
        ticketRepository.assignTicket(ticketId, assignment)
}

class AdminTicketsViewModel(
    private val repository: AdminTicketsRepository = FirebaseAdminTicketsRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTicketsUiState())
    val uiState: StateFlow<AdminTicketsUiState> = _uiState.asStateFlow()

    private val _assignmentUiState = MutableStateFlow(AdminTicketAssignmentUiState())
    val assignmentUiState: StateFlow<AdminTicketAssignmentUiState> = _assignmentUiState.asStateFlow()

    private var workspaceName: String = ""
    private var latestTickets: List<Ticket> = emptyList()
    private var observeTicketsJob: Job? = null

    init {
        loadTickets()
    }

    fun loadTickets() {
        observeTicketsJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentUser = repository.getCurrentUser()
            val currentWorkspace = currentUser?.workspaceName.orEmpty().trim()

            if (currentWorkspace.isBlank()) {
                _uiState.value = AdminTicketsUiState(
                    isLoading = false,
                    errorMessage = "Workspace tickets could not be loaded."
                )
                return@launch
            }

            workspaceName = currentWorkspace
            observeTicketsJob = launch {
                repository.observeAllTickets().collectLatest { tickets ->
                    latestTickets = tickets
                    _uiState.value = buildAdminTicketsUiState(
                        tickets = tickets,
                        selectedFilter = _uiState.value.selectedFilter,
                        searchQuery = _uiState.value.searchQuery,
                        workspaceName = workspaceName,
                    ).copy(isLoading = false)
                }
            }
        }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
        rebuildVisibleState()
    }

    fun onFilterSelected(filter: AdminTicketStatusFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        rebuildVisibleState()
    }

    fun onAssignAgentRequested(ticket: AdminTicketListItem) {
        viewModelScope.launch {
            _assignmentUiState.value = _assignmentUiState.value.copy(
                isSheetVisible = true,
                isLoadingAgents = true,
                isAssigning = false,
                ticketId = ticket.ticketId,
                ticketSubject = ticket.subject,
                ticketDepartment = ticket.categoryLabel,
                searchQuery = "",
                quickFilter = AgentQuickFilter.ALL,
                availableAgents = emptyList(),
                assignmentMessage = null,
                errorMessage = null,
            )
            loadAgentsForTicket(ticket)
        }
    }

    fun onReassignAgentRequested(ticket: AdminTicketListItem) = onAssignAgentRequested(ticket)

    fun dismissAssignmentSheet() {
        _assignmentUiState.value = AdminTicketAssignmentUiState()
    }

    fun onAgentSearchChanged(value: String) {
        _assignmentUiState.value = _assignmentUiState.value.copy(searchQuery = value)
    }

    fun onAssignAgentConfirmed(agent: SupportAgentOption) {
        val sheetState = _assignmentUiState.value
        val ticketId = sheetState.ticketId ?: return

        viewModelScope.launch {
            _assignmentUiState.value = sheetState.copy(
                isAssigning = true,
                errorMessage = null,
                assignmentMessage = null
            )
            Log.d("ASSIGN", "====================")
            Log.d("ASSIGN", "Ticket ID = $ticketId")
            Log.d("ASSIGN", "Agent UID = ${agent.uid}")
            Log.d("ASSIGN", "Agent Name = ${agent.fullName}")
            Log.d("ASSIGN", "Agent Email = ${agent.email}")
            Log.d("ASSIGN", "====================")

            val result = repository.assignTicket(
                ticketId = ticketId,
                assignment = TicketAssignmentUpdate(
                    assignedAgentId = agent.uid,
                    assignedAgentName = agent.fullName,
                    assignedAgentEmail = agent.email,
                    assignedAgentRole = null,
                    assignedAgentDepartment = agent.department,
                    assignedTimestamp = System.currentTimeMillis(),
                    assignedByAdmin = repository.getCurrentUser()?.fullName ?: repository.getCurrentUser()?.email,
                    assignmentStatus = "Assigned",
                    ticketStatus = ticketStatusForAssignedTicket(ticketId),
                    updatedAt = System.currentTimeMillis(),
                )
            )

            if (result.isSuccess) {

                Log.d("ASSIGN", "Assignment SUCCESS")

                _assignmentUiState.value = sheetState.copy(
                    isAssigning = false,
                    isSheetVisible = false,
                    assignmentMessage = "Ticket assigned to ${agent.fullName}",
                )

            } else {

                Log.e(
                    "ASSIGN",
                    "Assignment FAILED",
                    result.exceptionOrNull()
                )

                _assignmentUiState.value = sheetState.copy(
                    isAssigning = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Unable to assign ticket."
                )
            }
        }
    }

    private suspend fun loadAgentsForTicket(ticket: AdminTicketListItem) {
        val workspaceUsersResult = repository.getWorkspaceUsers(workspaceName)
        val workspaceUsers = workspaceUsersResult.getOrDefault(emptyList())

        val usersInWorkspace = workspaceUsers.filter { user ->
            user.workspaceName.trim().equals(workspaceName.trim(), ignoreCase = true)
        }

        val eligibleSupportAgents = usersInWorkspace.filter { user ->
            user.uid.isNotBlank() && user.isApprovedSupportAgent()
        }

        val approvedAgents = eligibleSupportAgents
            .map { user ->
                SupportAgentOption(
                    uid = user.uid,
                    fullName = user.fullName.ifBlank { user.email },
                    department = user.department.ifBlank { ticket.categoryLabel },
                    activeTicketCount = latestTickets.count { it.assignedAgentId == user.uid && it.status != "RESOLVED" },
                    isOnline = user.status.equals("online", ignoreCase = true),
                    initials = user.fullName.split(" ").filter { it.isNotBlank() }.take(2)
                        .joinToString("") { it.first().uppercaseChar().toString() }.ifBlank {
                            user.email.take(2).uppercase(Locale.ENGLISH)
                        },
                    email = user.email,
                )
            }
            .sortedWith(
                compareBy<SupportAgentOption> { it.activeTicketCount }
                    .thenBy { it.fullName.lowercase(Locale.ENGLISH) }
            )

        _assignmentUiState.value = _assignmentUiState.value.copy(
            isLoadingAgents = false,
            availableAgents = approvedAgents,
            errorMessage = if (approvedAgents.isEmpty()) "No support agents available for this workspace." else null,
        )
    }

    private fun ticketStatusForAssignedTicket(ticketId: String): String {
        val ticket = latestTickets.firstOrNull { it.ticketId == ticketId }
        return when (ticketStatusTone(ticket?.status.orEmpty())) {
            com.example.supporthub.features.tickets.TicketStatusTone.RESOLVED -> "RESOLVED"
            com.example.supporthub.features.tickets.TicketStatusTone.IN_PROGRESS -> "IN_PROGRESS"
            else -> "IN_PROGRESS"
        }
    }

    private fun rebuildVisibleState() {
        if (workspaceName.isBlank()) return
        _uiState.value = buildAdminTicketsUiState(
            tickets = latestTickets,
            selectedFilter = _uiState.value.selectedFilter,
            searchQuery = _uiState.value.searchQuery,
            workspaceName = workspaceName,
        ).copy(
            isLoading = false,
            errorMessage = null,
        )
    }
}

class AdminTicketsViewModelFactory(
    private val repository: AdminTicketsRepository = FirebaseAdminTicketsRepository(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminTicketsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminTicketsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun AdminTicketsScreen(
    viewModel: AdminTicketsViewModel = viewModel(factory = AdminTicketsViewModelFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val assignmentUiState by viewModel.assignmentUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(assignmentUiState.assignmentMessage) {
        assignmentUiState.assignmentMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissAssignmentSheet()
        }
    }

    AdminTicketsContent(
        uiState = uiState,
        assignmentUiState = assignmentUiState,
        snackbarHostState = snackbarHostState,
        onBackClick = { },
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onAssignAgentRequested = viewModel::onAssignAgentRequested,
        onReassignAgentRequested = viewModel::onReassignAgentRequested,
        onDismissAssignmentSheet = viewModel::dismissAssignmentSheet,
        onAgentSearchChanged = viewModel::onAgentSearchChanged,
        onAssignAgentConfirmed = viewModel::onAssignAgentConfirmed,
    )
}

@Composable
internal fun AdminTicketsContent(
    uiState: AdminTicketsUiState,
    assignmentUiState: AdminTicketAssignmentUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (AdminTicketStatusFilter) -> Unit,
    onAssignAgentRequested: (AdminTicketListItem) -> Unit,
    onReassignAgentRequested: (AdminTicketListItem) -> Unit,
    onDismissAssignmentSheet: () -> Unit,
    onAgentSearchChanged: (String) -> Unit,
    onAssignAgentConfirmed: (SupportAgentOption) -> Unit,
) {
    var searchQuery by remember(uiState.searchQuery) { mutableStateOf(uiState.searchQuery) }

    LaunchedEffect(uiState.searchQuery) {
        if (searchQuery != uiState.searchQuery) searchQuery = uiState.searchQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(adminTicketsBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("admin_tickets_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { AdminTicketsHeader(onBackClick = onBackClick) }
            item {
                AdminTicketsFilterChips(
                    selected = uiState.selectedFilter,
                    onSelected = onFilterSelected,
                )
            }
            item { AdminTicketsSummaryRow(uiState = uiState) }
            item {
                AdminTicketsSearchBar(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    }
                )
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = adminTicketsAccent)
                        }
                    }
                }
                uiState.errorMessage != null -> {
                    item {
                        AdminTicketsEmptyState(
                            title = "Unable to load tickets",
                            message = uiState.errorMessage,
                        )
                    }
                }
                uiState.visibleTickets.isEmpty() -> {
                    item {
                        AdminTicketsEmptyState(
                            title = "No matching tickets",
                            message = if (uiState.searchQuery.isBlank()) {
                                "Tickets from the selected workspace will appear here in real time."
                            } else {
                                "Try a different search or switch filters to see more results."
                            },
                        )
                    }
                }
                else -> {
                    item {
                        Text(
                            text = "Today — ${currentDateLabel()}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = SupportHubFontFamilies.poppins,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = adminTicketsMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    items(
                        items = uiState.visibleTickets,
                        key = { item -> item.ticketId }
                    ) { ticket ->
                        AdminTicketCard(
                            item = ticket,
                            onAssignAgent = { onAssignAgentRequested(ticket) },
                            onReassignAgent = { onReassignAgentRequested(ticket) },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (assignmentUiState.isSheetVisible) {
        AssignAgentBottomSheet(
            state = assignmentUiState,
            onDismiss = onDismissAssignmentSheet,
            onSearchChanged = onAgentSearchChanged,
            onAgentAssign = onAssignAgentConfirmed,
        )
    }
}

@Composable
private fun AdminTicketsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .clickable(onClick = onBackClick),
            shape = CircleShape,
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, adminTicketsBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = adminTicketsHeading
                )
            }
        }

        Text(
            text = "All Tickets",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = SupportHubFontFamilies.poppins,
                fontWeight = FontWeight.ExtraBold,
            ),
            color = adminTicketsHeading
        )

        Surface(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, adminTicketsBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = null,
                    tint = adminTicketsHeading
                )
            }
        }
    }
}

@Composable
private fun AdminTicketsFilterChips(
    selected: AdminTicketStatusFilter,
    onSelected: (AdminTicketStatusFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AdminTicketStatusFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)),
                shape = RoundedCornerShape(999.dp),
                color = if (isSelected) adminTicketsAccent else Color.White,
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else adminTicketsBorder),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier
                        .clickable { onSelected(filter) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else adminTicketsMuted,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = SupportHubFontFamilies.poppins,
                        fontWeight = FontWeight.SemiBold,
                    )
                )
            }
        }
    }
}

@Composable
private fun AdminTicketsSummaryRow(uiState: AdminTicketsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdminSummaryStatCard(
            modifier = Modifier.weight(1f),
            value = uiState.openTicketsCount.toString(),
            label = "Open",
            tint = adminTicketsBlue,
        )
        AdminSummaryStatCard(
            modifier = Modifier.weight(1f),
            value = uiState.pendingTicketsCount.toString(),
            label = "Pending",
            tint = adminTicketsPending,
        )
        AdminSummaryStatCard(
            modifier = Modifier.weight(1f),
            value = uiState.resolvedTicketsCount.toString(),
            label = "Resolved",
            tint = adminTicketsResolved,
        )
    }
}

@Composable
private fun AdminSummaryStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    tint: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = adminTicketsCard),
        border = BorderStroke(1.dp, adminTicketsBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = tint
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.Medium,
                ),
                color = adminTicketsMuted
            )
        }
    }
}

@Composable
private fun AdminTicketsSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        placeholder = {
            Text(
                text = "Search by ticket, requester, or status",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SupportHubFontFamilies.poppins)
            )
        },
        singleLine = true,
    )
}

@Composable
private fun AdminTicketCard(
    item: AdminTicketListItem,
    onAssignAgent: () -> Unit,
    onReassignAgent: () -> Unit,
) {
    val shadowElevation by animateDpAsState(targetValue = 2.dp, label = "ticket-card-shadow")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, adminTicketsBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(
                            text = item.ticketId,
                            background = Color(0xFFEFF4FF),
                            content = Color(0xFF7C8AA5)
                        )

                        StatusPill(
                            text = item.priorityLabel,
                            background = item.priorityColor.copy(alpha = 0.12f),
                            content = item.priorityColor
                        )
                    }

                    Text(
                        text = item.subject,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SupportHubFontFamilies.poppins,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = adminTicketsHeading,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Ticket actions")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TicketInfoRow(label = "Category", value = item.categoryLabel)
                TicketInfoRow(label = "Created", value = item.createdAtLabel)
                TicketInfoRow(label = "Requester", value = item.requesterName)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    text = item.displayTicketId,
                    background = item.statusColor.copy(alpha = 0.12f),
                    content = item.statusColor,
                )
                Text(
                    text = item.requesterEmail.ifBlank { item.assignmentStatusLabel },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SupportHubFontFamilies.poppins,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = adminTicketsMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val isAssigned = !item.assignedAgentId.isNullOrBlank()
            AssignedAgentSection(
                agentName = item.assignmentAgentName,
                department = item.assignmentAgentDepartment,
                isAssigned = isAssigned,
                onAssign = onAssignAgent,
                onReassign = onReassignAgent,
            )
        }
    }
}

@Composable
private fun TicketInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = SupportHubFontFamilies.poppins,
                fontWeight = FontWeight.SemiBold,
            ),
            color = adminTicketsMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = SupportHubFontFamilies.poppins,
                fontWeight = FontWeight.Medium,
            ),
            color = adminTicketsHeading,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AssignedAgentSection(
    agentName: String,
    department: String,
    isAssigned: Boolean,
    onAssign: () -> Unit,
    onReassign: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = adminTicketsBackground),
        border = BorderStroke(1.dp, adminTicketsBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Assigned Agent",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = adminTicketsHeading
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(adminTicketsAccent, adminTicketsBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (agentName.isBlank()) "NA" else agentName.take(2).uppercase(Locale.ENGLISH),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Assigned To", style = MaterialTheme.typography.labelMedium, color = adminTicketsMuted)
                    Text(
                        text = agentName.ifBlank { if (isAssigned) "Assigned Agent" else "No Agent Assigned" },
                        style = MaterialTheme.typography.titleSmall,
                        color = adminTicketsHeading
                    )
                    Text(
                        text = if (isAssigned) department.ifBlank { "Support Team" } else "Select an agent to enable assignment",
                        style = MaterialTheme.typography.bodySmall,
                        color = adminTicketsMuted
                    )
                }
            }
            Button(
                onClick = if (isAssigned) onReassign else onAssign,
                colors = ButtonDefaults.buttonColors(containerColor = adminTicketsAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isAssigned) "Reassign Agent" else "Assign Agent")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignAgentBottomSheet(
    state: AdminTicketAssignmentUiState,
    onDismiss: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onAgentAssign: (SupportAgentOption) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Assign Support Agent",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = adminTicketsHeading
            )
            Text(
                text = "Select the best agent for this ticket",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.Medium
                ),
                color = adminTicketsMuted
            )
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search agents by name, department, or email") },
                singleLine = true,
            )
            when {
                state.isLoadingAgents -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = adminTicketsAccent)
                    }
                }
                else -> {
                    val agents = filteredAgents(state)
                    if (agents.isEmpty()) {
                        AdminTicketsEmptyState(
                            title = "No agents found",
                            message = "Try another search or filter."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(agents, key = { it.uid }) { agent ->
                                RecommendedAgentCard(
                                    agent = agent,
                                    isRecommended = agent == agents.first(),
                                    onAssign = { onAgentAssign(agent) },
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
private fun RecommendedAgentCard(
    agent: SupportAgentOption,
    isRecommended: Boolean,
    onAssign: () -> Unit,
) {
    AgentCard(
        agent = agent,
        isRecommended = isRecommended,
        onAssign = onAssign,
    )
}

@Composable
private fun AgentCard(
    agent: SupportAgentOption,
    isRecommended: Boolean,
    onAssign: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = adminTicketsCard),
        border = BorderStroke(1.dp, adminTicketsBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(adminTicketsAccent, adminTicketsBlue))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        agent.initials,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            agent.fullName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = adminTicketsHeading
                        )
                        if (isRecommended) {
                            StatusPill(
                                text = "⭐ Recommended",
                                background = Color(0xFFFFF7E6),
                                content = adminTicketsWarning
                            )
                        }
                    }
                    Text(agent.department, style = MaterialTheme.typography.bodyMedium, color = adminTicketsMuted)
                }
            }

            Text(
                if (agent.isOnline) "Online" else "Offline",
                color = if (agent.isOnline) adminTicketsOnline else adminTicketsOffline
            )
            Text("${agent.activeTicketCount} Active Tickets", color = adminTicketsHeading)
            Text(
                agent.email,
                color = adminTicketsMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Button(
                onClick = onAssign,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = adminTicketsAccent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Assign")
            }
        }
    }
}

private fun filteredAgents(state: AdminTicketAssignmentUiState): List<SupportAgentOption> {
    val query = state.searchQuery.trim()
    return state.availableAgents.filter { agent ->
        query.isBlank() || listOf(agent.fullName, agent.department, agent.email).any {
            it.contains(query, ignoreCase = true)
        }
    }.sortedWith(
        compareByDescending<SupportAgentOption> { it.department.equals(state.ticketDepartment, ignoreCase = true) }
            .thenBy { it.activeTicketCount }
            .thenBy { it.fullName.lowercase(Locale.ENGLISH) }
    )
}

private fun User.isApprovedSupportAgent(): Boolean {
    val approved = approvedRole?.trim().orEmpty().lowercase(Locale.ENGLISH)
    val roleValue = role?.trim().orEmpty().lowercase(Locale.ENGLISH)
    return approved == "agent" || roleValue == "agent"
}

@Composable
private fun StatusPill(
    text: String,
    background: Color,
    content: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = content,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = SupportHubFontFamilies.poppins,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
}

@Composable
private fun AdminTicketsEmptyState(
    title: String,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, adminTicketsBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.Bold,
                ),
                color = adminTicketsHeading
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = SupportHubFontFamilies.poppins,
                    fontWeight = FontWeight.Medium,
                ),
                color = adminTicketsMuted
            )
        }
    }
}

fun buildAdminTicketsUiState(
    tickets: List<Ticket>,
    selectedFilter: AdminTicketStatusFilter,
    searchQuery: String,
    workspaceName: String,
): AdminTicketsUiState {
    val workspaceTickets = tickets.filter { ticket ->
        ticket.workspaceName.trim().equals(workspaceName.trim(), ignoreCase = true)
    }.sortedByDescending { ticket -> ticket.createdAt.toAdminTicketEpochMillis() }

    val openTickets = workspaceTickets.count { ticket -> ticket.isOpenTicket() }
    val pendingTickets = workspaceTickets.count { ticket -> ticket.isPendingTicket() }
    val resolvedTickets = workspaceTickets.count { ticket -> ticket.isResolvedTicket() }

    val visibleTickets = workspaceTickets
        .filter { ticket -> ticket.matchesAdminTicketFilter(selectedFilter) }
        .filter { ticket -> ticket.matchesAdminTicketSearch(searchQuery) }
        .map { ticket -> ticket.toAdminTicketListItem() }

    return AdminTicketsUiState(
        isLoading = false,
        searchQuery = searchQuery,
        selectedFilter = selectedFilter,
        totalTicketsCount = workspaceTickets.size,
        openTicketsCount = openTickets,
        pendingTicketsCount = pendingTickets,
        resolvedTicketsCount = resolvedTickets,
        summaryCards = listOf(
            AdminTicketSummaryCard("Open", openTickets.toString(), adminTicketsBlue),
            AdminTicketSummaryCard("Pending", pendingTickets.toString(), adminTicketsPending),
            AdminTicketSummaryCard("Resolved", resolvedTickets.toString(), adminTicketsResolved),
        ),
        visibleTickets = visibleTickets,
    )
}

private fun Ticket.matchesAdminTicketFilter(filter: AdminTicketStatusFilter): Boolean {
    return when (filter) {
        AdminTicketStatusFilter.ALL -> true
        AdminTicketStatusFilter.OPEN -> isOpenTicket()
        AdminTicketStatusFilter.PENDING -> isPendingTicket()
        AdminTicketStatusFilter.RESOLVED -> isResolvedTicket()
    }
}

private fun Ticket.matchesAdminTicketSearch(query: String): Boolean {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return true
    return listOf(ticketId, subject, category, status, employeeName, employeeEmail, assignedAgentName ?: "", assignedAgentDepartment ?: "")
        .any { it.contains(normalizedQuery, ignoreCase = true) }
}

private fun Ticket.isOpenTicket(): Boolean = ticketStatusTone(status) == com.example.supporthub.features.tickets.TicketStatusTone.OPEN
private fun Ticket.isPendingTicket(): Boolean = ticketStatusTone(status) == com.example.supporthub.features.tickets.TicketStatusTone.IN_PROGRESS
private fun Ticket.isResolvedTicket(): Boolean = ticketStatusTone(status) == com.example.supporthub.features.tickets.TicketStatusTone.RESOLVED

private fun Ticket.toAdminTicketListItem(): AdminTicketListItem {
    val normalizedAssignedAgentId = assignedAgentId?.trim().orEmpty().ifBlank { null }
    val normalizedAssignedAgentName = assignedAgentName?.trim().orEmpty().ifBlank { null }
    val normalizedAssignmentStatus = assignmentStatus?.trim().orEmpty().ifBlank { null }
    val assigned = normalizedAssignedAgentName ?: normalizedAssignedAgentId
    val department = assignedAgentDepartment?.takeIf { it.isNotBlank() }.orEmpty()

    return AdminTicketListItem(
        ticketId = ticketId,
        displayTicketId = displayTicketId.ifBlank {
            ticketId.take(6).uppercase(Locale.ENGLISH)
        }, subject = subject.ifBlank { "Untitled ticket" },
        requesterName = employeeName.ifBlank { employeeEmail.ifBlank { "Unknown requester" } },
        requesterInitials = employeeName.initialsFromName(),
        createdAtLabel = createdAt.toAdminTicketDateLabel(),
        categoryLabel = category.ifBlank { "General" },
        priorityLabel = priority.ifBlank { "Medium" }.uppercase(Locale.ENGLISH),
        priorityColor = ticketPriorityColor(priority),
        statusLabel = ticketStatusLabel(status),
        statusColor = ticketStatusColor(status),
        assignmentStatusLabel = if (assigned == null) "No Agent Assigned" else normalizedAssignmentStatus ?: "Assigned",
        assignmentAgentName = assigned ?: "No Agent Assigned",
        assignmentAgentDepartment = department.ifBlank { if (assigned == null) "" else "Support Team" },
        assignedAgentId = normalizedAssignedAgentId,
        requesterEmail = employeeEmail,
        requesterDepartment = department,
    )
}

private fun ticketPriorityColor(priority: String): Color = when (ticketPriorityTone(priority)) {
    com.example.supporthub.features.tickets.TicketPriorityTone.LOW -> Color(0xFF16A34A)
    com.example.supporthub.features.tickets.TicketPriorityTone.MEDIUM -> Color(0xFF2563EB)
    com.example.supporthub.features.tickets.TicketPriorityTone.CRITICAL -> Color(0xFFDC2626)
    com.example.supporthub.features.tickets.TicketPriorityTone.HIGH -> Color(0xFFF59E0B)
}

private fun ticketStatusColor(status: String): Color = when (ticketStatusTone(status)) {
    com.example.supporthub.features.tickets.TicketStatusTone.OPEN -> adminTicketsBlue
    com.example.supporthub.features.tickets.TicketStatusTone.IN_PROGRESS -> adminTicketsPending
    com.example.supporthub.features.tickets.TicketStatusTone.RESOLVED -> adminTicketsResolved
}

private fun Any?.toAdminTicketEpochMillis(): Long = when (this) {
    is Long -> this
    is Int -> toLong()
    is String -> toLongOrNull() ?: 0L
    else -> 0L
}

private fun Any?.toAdminTicketDateLabel(): String {
    val millis = toAdminTicketEpochMillis()
    if (millis <= 0L) return "Just now"
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(millis))
}

private fun String.initialsFromName(): String {
    val initials = trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") {
        it.first().uppercaseChar().toString()
    }
    return initials.ifBlank { take(2).uppercase(Locale.ENGLISH) }
}

private fun currentDateLabel(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
}