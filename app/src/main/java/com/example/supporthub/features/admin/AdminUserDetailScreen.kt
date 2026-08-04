package com.example.supporthub.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.core.demo.DemoDataProvider
import com.example.supporthub.core.demo.USE_DEMO_DATA
import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class AdminUserDetailMetrics(
    val openTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val totalTickets: Int = 0,
)

data class AdminUserDetailData(
    val user: User,
    val tickets: List<Ticket> = emptyList(),
) {
    val metrics: AdminUserDetailMetrics = AdminUserDetailMetrics(
        openTickets = tickets.count { !it.status.equals("resolved", ignoreCase = true) },
        resolvedTickets = tickets.count { it.status.equals("resolved", ignoreCase = true) },
        totalTickets = tickets.size,
    )
}

data class AdminUserDetailUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val tickets: List<Ticket> = emptyList(),
    val errorMessage: String? = null
)

interface AdminUserDetailRepository {
    fun observeUserDetail(uid: String): Flow<AdminUserDetailData?>
}

class FirebaseAdminUserDetailRepository(
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = AuthRepositoryImpl()),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : AdminUserDetailRepository {

    override fun observeUserDetail(uid: String): Flow<AdminUserDetailData?> {
        if (uid.isBlank()) {
            return flowOf(null)
        }

        return observeUserDocument(uid).combine(ticketRepository.observeEmployeeTickets(uid)) { user, tickets ->
            user?.let { AdminUserDetailData(user = it, tickets = tickets) }
        }
    }

    private fun observeUserDocument(uid: String): Flow<User?> = callbackFlow {
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

class AdminUserDetailViewModel(
    private val uid: String,
    private val repository: AdminUserDetailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserDetailUiState(isLoading = true))
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        load()
    }

    fun load() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = AdminUserDetailUiState(isLoading = true)
            repository.observeUserDetail(uid).collect { data ->
                _uiState.value = when {
                    data == null -> AdminUserDetailUiState(
                        isLoading = false,
                        errorMessage = "Unable to load user details."
                    )
                    else -> AdminUserDetailUiState(
                        isLoading = false,
                        user = data.user,
                        tickets = data.tickets,
                        errorMessage = null
                    )
                }
            }
        }
    }
}

class AdminUserDetailViewModelFactory(
    private val uid: String,
    private val repository: AdminUserDetailRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserDetailViewModel(uid, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AdminUserDetailScreen(
    uid: String,
    onNavigateBack: () -> Unit = {},
    repository: AdminUserDetailRepository = FirebaseAdminUserDetailRepository()
) {
    val viewModel: AdminUserDetailViewModel = viewModel(
        factory = AdminUserDetailViewModelFactory(uid, repository)
    )
    val state by viewModel.uiState.collectAsState()
    val currentUser = state.user

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("admin_user_detail_screen")
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            currentUser == null -> ErrorState(onNavigateBack = onNavigateBack)
            else -> AdminUserDetailContent(
                user = currentUser,
                tickets = state.tickets,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun AdminUserDetailContent(
    user: User,
    tickets: List<Ticket>,
    onNavigateBack: () -> Unit
) {
    val initials = remember(user.fullName, user.email) { buildAdminAvatarInitials(user.fullName, user.email) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
                Text(
                    text = "User Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0F9D58), Color(0xFF3B82F6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = user.fullName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = user.email, color = Color(0xFF667085), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Divider(color = Color(0xFFECEFF3))

                    DetailRow(icon = Icons.Outlined.Badge, label = "Role", value = user.role.orEmpty())
                    DetailRow(icon = Icons.Outlined.Business, label = "Department", value = user.department)
                    DetailRow(icon = Icons.Outlined.Email, label = "Email", value = user.email)
                    DetailRow(icon = Icons.Outlined.Phone, label = "Phone", value = user.phone.ifBlank { "—" })
                }
            }
        }

        item {
            MetricRow(user = user, ticketCount = tickets.size)
        }

        item {
            Text(
                text = "Tickets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (tickets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "No tickets found",
                        modifier = Modifier.padding(20.dp),
                        color = Color(0xFF667085),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            items(tickets.size) { index ->
                TicketCard(ticket = tickets[index])
            }
        }
    }
}

@Composable
private fun MetricRow(user: User, ticketCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        DetailMetricCard(label = "Tickets", value = ticketCount.toString(), modifier = Modifier.weight(1f))
        DetailMetricCard(label = "Status", value = user.status.ifBlank { "—" }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = Color(0xFF667085), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = value, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF667085))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color(0xFF667085), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = value, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ticket.subject, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = ticket.category, color = Color(0xFF667085), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                AssistChip(onClick = {}, label = { Text(text = ticket.status, maxLines = 1, overflow = TextOverflow.Ellipsis) })
            }
            Text(text = ticket.description, color = Color(0xFF667085), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ErrorState(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Unable to load user details.", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onNavigateBack) {
            Text(text = "Go back", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
