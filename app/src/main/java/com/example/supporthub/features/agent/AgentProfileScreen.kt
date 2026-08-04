package com.example.supporthub.features.agent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supporthub.core.firebase.FirebaseCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.core.navigation.Routes
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepository
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepository
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val AgentProfileBackground = Color(0xFFF4F7FB)
private val AgentProfileSurface = Color.White
private val AgentProfileBorder = Color(0xFFE3EAF4)
private val AgentProfileText = Color(0xFF0F172A)
private val AgentProfileSubtext = Color(0xFF64748B)
private val AgentProfileAccent = Color(0xFF2563EB)
private val AgentProfileAccentSoft = Color(0xFFEAF2FF)
private val AgentProfileSuccess = Color(0xFF16A34A)
private val AgentProfileSuccessSoft = Color(0xFFE8F8EE)
private val AgentProfileAmber = Color(0xFFF97316)
private val AgentProfileAmberSoft = Color(0xFFFEEAD7)
private val AgentProfilePurple = Color(0xFF7C3AED)
private val AgentProfilePurpleSoft = Color(0xFFF0EAFE)
private val AgentProfilePink = Color(0xFFDB2777)
private val AgentProfilePinkSoft = Color(0xFFFCE7F3)
private val AgentProfileShadow = Color(0x120F172A)
private val AgentProfileCardShape = RoundedCornerShape(32.dp)

data class AgentProfileEditForm(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val jobTitle: String = "",
    val location: String = "",
    val workspaceName: String = "",
)

private data class AgentProfileEditState(
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

data class AgentProfileMetrics(
    val openTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val averageResponseLabel: String = "—",
)

data class AgentProfileData(
    val user: User,
    val metrics: AgentProfileMetrics = AgentProfileMetrics(),
)

interface AgentProfileRepository {
    fun observeProfile(): Flow<AgentProfileData?>
    suspend fun logout()
    suspend fun updateProfile(
        uid: String,
        updates: Map<String, Any?>,
    )
}

class FirebaseAgentProfileRepository(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val ticketRepository: TicketRepository = TicketRepositoryImpl(authRepository = authRepository),
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : AgentProfileRepository {

    override fun observeProfile(): Flow<AgentProfileData?> = callbackFlow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null || currentUser.uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val workspaceName = currentUser.workspaceName.trim()
        if (workspaceName.isBlank()) {
            trySend(AgentProfileData(user = currentUser))
            close()
            return@callbackFlow
        }

        val combinedFlow = observeUserDocument(currentUser.uid).combine(
            ticketRepository.observeAllTickets()
        ) { user, tickets ->
            val latestUser = user ?: currentUser
            AgentProfileData(
                user = latestUser,
                metrics = tickets.toAgentProfileMetrics(workspaceName = workspaceName, agentUid = latestUser.uid)
            )
        }

        val collectionJob = launch {
            combinedFlow.collect { value ->
                trySend(value)
            }
        }

        awaitClose {
            collectionJob.cancel()
        }
    }

    override suspend fun logout() {
        authRepository.logout()
    }

    override suspend fun updateProfile(uid: String, updates: Map<String, Any?>) {
        if (uid.isBlank()) return
        firestore.collection(FirebaseCollections.USERS).document(uid).update(updates)
    }

    private fun observeUserDocument(uid: String): Flow<User?> {
        if (uid.isBlank()) {
            return flowOf(null)
        }

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

class AgentProfileViewModel(
    private val repository: AgentProfileRepository = FirebaseAgentProfileRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentProfileUiState(isLoading = true))
    val uiState: StateFlow<AgentProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profileData ->
                if (profileData == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to load agent profile."
                        )
                    }
                } else {
                    _uiState.value = profileData.toProfileUiState(isLoading = false)
                }
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return runCatching {
            repository.logout()
        }
    }

    fun saveProfileEdits(uid: String, edits: AgentProfileEditForm, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.updateProfile(
                    uid = uid,
                    updates = mapOf(
                        "fullName" to edits.fullName.trim(),
                        "email" to edits.email.trim(),
                        "phone" to edits.phone.trim(),
                        "department" to edits.department.trim(),
                        "jobTitle" to edits.jobTitle.trim(),
                        "location" to edits.location.trim(),
                        "workspaceName" to edits.workspaceName.trim(),
                    )
                )
            }.onSuccess {
                onResult(true, "Profile updated successfully.")
            }.onFailure { throwable ->
                onResult(false, throwable.message ?: "Failed to update profile.")
            }
        }
    }
}

class AgentProfileViewModelFactory(
    private val repository: AgentProfileRepository = FirebaseAgentProfileRepository(),
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

data class AgentProfileUiState(
    val isLoading: Boolean = true,
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val jobTitle: String = "",
    val workspaceName: String = "",
    val department: String = "",
    val avatarInitials: String = "",
    val openTickets: Int = 0,
    val resolvedTickets: Int = 0,
    val averageResponseLabel: String = "—",
    val statusLabel: String = "LIVE",
    val isOnline: Boolean = true,
    val errorMessage: String? = null,
)

fun AgentProfileData.toProfileUiState(isLoading: Boolean): AgentProfileUiState {
    return AgentProfileUiState(
        isLoading = isLoading,
        uid = user.uid,
        fullName = user.fullName,
        email = user.email,
        phone = user.phone,
        location = user.location,
        jobTitle = user.jobTitle,
        workspaceName = user.workspaceName,
        department = user.department,
        avatarInitials = buildAgentProfileInitials(user.fullName, user.email),
        openTickets = metrics.openTickets,
        resolvedTickets = metrics.resolvedTickets,
        averageResponseLabel = metrics.averageResponseLabel,
        statusLabel = if (user.status.equals("active", ignoreCase = true)) "LIVE" else user.status.uppercase(),
        isOnline = user.status.equals("active", ignoreCase = true),
        errorMessage = null,
    )
}

private fun buildAgentProfileInitials(fullName: String, email: String): String {
    val nameInitials = fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    if (nameInitials.isNotBlank()) return nameInitials

    val emailInitial = email.trim().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    return emailInitial.ifBlank { "A" }
}

private fun List<Ticket>.toAgentProfileMetrics(workspaceName: String, agentUid: String): AgentProfileMetrics {
    val workspaceTickets = filter { ticket ->
        ticket.workspaceName.trim().equals(workspaceName, ignoreCase = true)
    }

    val agentTickets = workspaceTickets.filter { ticket ->
        ticket.assignedAgentId.equals(agentUid, ignoreCase = true) ||
            ticket.assignedAgentName.equals(agentUid, ignoreCase = true) ||
            ticket.assignedAgentEmail.equals(agentUid, ignoreCase = true)
    }

    val openTickets = agentTickets.count { ticket ->
        ticket.status.trim().uppercase() != "RESOLVED"
    }
    val resolvedTickets = agentTickets.count { ticket ->
        ticket.status.trim().equals("RESOLVED", ignoreCase = true)
    }
    val averageResponseLabel = if (agentTickets.isEmpty()) "—" else "2m 14s avg"

    return AgentProfileMetrics(
        openTickets = openTickets,
        resolvedTickets = resolvedTickets,
        averageResponseLabel = averageResponseLabel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentProfileScreen(navController: NavController) {
    val viewModel: AgentProfileViewModel = viewModel(factory = AgentProfileViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showEditSheet by remember { mutableStateOf(false) }
    var editForm by remember { mutableStateOf(AgentProfileEditForm()) }
    var editState by remember { mutableStateOf(AgentProfileEditState()) }
    var isLoggingOut by remember { mutableStateOf(false) }

    LaunchedEffect(showEditSheet, uiState.uid) {
        if (showEditSheet) {
            editForm = AgentProfileEditForm(
                fullName = uiState.fullName,
                email = uiState.email,
                phone = uiState.phone,
                department = uiState.department,
                jobTitle = uiState.jobTitle,
                location = uiState.location,
                workspaceName = uiState.workspaceName,
            )
        }
    }

    SupportHubPremiumBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AgentProfileBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag("agent_profile_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeader(statusLabel = uiState.statusLabel)
                ProfileHeroCard(
                    uiState = uiState,
                    onEditProfile = { showEditSheet = true }
                )
                ProfileMetricRow(uiState = uiState)
                ProfileInsightsCard(uiState = uiState)
                ProfileSection(
                    title = "CONTACT",
                    items = listOf(
                        "Email" to uiState.email.ifBlank { "—" },
                        "Phone" to uiState.phone.ifBlank { "—" },
                        "Location" to uiState.location.ifBlank { "—" }
                    )
                )
                ProfileSection(
                    title = "WORKSPACE",
                    items = listOf(
                        "Workspace" to uiState.workspaceName.ifBlank { "—" },
                        "Department" to uiState.department.ifBlank { "—" },
                        "Role" to uiState.jobTitle.ifBlank { "—" }
                    )
                )
                uiState.errorMessage?.let { errorMessage ->
                    ProfileNoticeCard(title = "Profile sync issue", message = errorMessage, accent = Color(0xFFFB7185), tint = Color(0xFFFFE7EC))
                }
                AvailabilityCard(isOnline = uiState.isOnline)
                LogoutAction(
                    isLoggingOut = isLoggingOut,
                    onLogout = {
                        if (isLoggingOut) return@LogoutAction
                        isLoggingOut = true
                        scope.launch {
                            val result = viewModel.logout()
                            isLoggingOut = false
                            result.onSuccess {
                                snackbarHostState.showSnackbar("Logged out successfully.")
                                navController.navigate(Routes.Login.route) {
                                    popUpTo(Routes.AgentDashboard.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }.onFailure { throwable ->
                                snackbarHostState.showSnackbar(throwable.message ?: "Failed to log out.")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )

            if (editState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x660F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = AgentProfileSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            Text(text = "Saving profile…", color = AgentProfileText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        EditProfileBottomSheet(
            form = editForm,
            onFormChange = { editForm = it },
            isSaving = editState.isSaving,
            onDismiss = { if (!editState.isSaving) showEditSheet = false },
            onSave = {
                val uid = uiState.uid
                if (uid.isBlank()) {
                    scope.launch { snackbarHostState.showSnackbar("Unable to save profile: missing user ID.") }
                    return@EditProfileBottomSheet
                }
                editState = editState.copy(isSaving = true, message = null, error = null)
                viewModel.saveProfileEdits(
                    uid = uid,
                    edits = editForm,
                ) { success, message ->
                    editState = editState.copy(isSaving = false, message = message, error = if (success) null else message)
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                    if (success) {
                        showEditSheet = false
                    }
                }
            }
        )
    }

}

@Composable
private fun ProfileHeader(statusLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.width(42.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = AgentProfileText,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("agent_profile_title")
            )
            Text(text = "Personal details and access status", style = MaterialTheme.typography.bodySmall, color = AgentProfileSubtext)
        }
        Surface(shape = RoundedCornerShape(999.dp), color = AgentProfileSuccessSoft) {
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = AgentProfileSuccess,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ProfileHeroCard(uiState: AgentProfileUiState, onEditProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AgentProfileCardShape,
        colors = CardDefaults.cardColors(containerColor = AgentProfileSurface),
        border = BorderStroke(1.dp, AgentProfileBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF8FBFF), Color(0xFFFFFFFF))
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AgentProfileAccent, AgentProfilePurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.avatarInitials,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("agent_profile_avatar_initials")
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = AgentProfileSuccess,
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 1.dp,
                    modifier = Modifier.testTag("agent_profile_live_chip")
                ) {
                    Text(
                        text = uiState.statusLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = uiState.fullName,
                style = MaterialTheme.typography.headlineSmall,
                color = AgentProfileText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("agent_profile_name"),
                textAlign = TextAlign.Center
            )
            Text(
                text = uiState.jobTitle,
                style = MaterialTheme.typography.titleMedium,
                color = AgentProfileSubtext,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("agent_profile_job_title")
            )
            Text(
                text = uiState.workspaceName,
                style = MaterialTheme.typography.bodyMedium,
                color = AgentProfileSubtext,
                modifier = Modifier.testTag("agent_profile_workspace")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                ProfilePill(label = "Enterprise Support", backgroundColor = AgentProfileAccentSoft, contentColor = AgentProfileAccent)
                ProfilePill(label = "Priority Queue", backgroundColor = AgentProfilePurpleSoft, contentColor = AgentProfilePurple)
                ProfilePill(label = "Trusted", backgroundColor = AgentProfileSuccessSoft, contentColor = AgentProfileSuccess)
            }

            Button(
                onClick = onEditProfile,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgentProfileAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Edit Profile")
            }
        }
    }
}

@Composable
private fun ProfileMetricRow(uiState: AgentProfileUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricTile(
            modifier = Modifier.weight(1f),
            value = uiState.resolvedTickets.toString(),
            label = "Resolved today",
            backgroundColor = AgentProfileAccentSoft,
            accentColor = AgentProfileAccent
        )
        MetricTile(
            modifier = Modifier.weight(1f),
            value = uiState.openTickets.toString(),
            label = "Open tickets",
            backgroundColor = AgentProfileSuccessSoft,
            accentColor = AgentProfileSuccess
        )
        MetricTile(
            modifier = Modifier.weight(1f),
            value = uiState.averageResponseLabel,
            label = "Avg response",
            backgroundColor = AgentProfileAmberSoft,
            accentColor = AgentProfileAmber
        )
    }
}

@Composable
private fun ProfileInsightsCard(uiState: AgentProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AgentProfileSurface),
        border = BorderStroke(1.dp, AgentProfileBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = AgentProfileAccent)
                Text("Profile snapshot", color = AgentProfileText, fontWeight = FontWeight.SemiBold)
            }
            InsightRow(label = "Workspace", value = uiState.workspaceName.ifBlank { "—" }, icon = Icons.Outlined.Work)
            InsightRow(label = "Department", value = uiState.department.ifBlank { "—" }, icon = Icons.Outlined.SupportAgent)
            InsightRow(label = "Phone", value = uiState.phone.ifBlank { "—" }, icon = Icons.Outlined.Phone)
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = AgentProfileAccentSoft,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AgentProfileAccent)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AgentProfileSubtext)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = AgentProfileText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    backgroundColor: Color,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AgentProfileSubtext
            )
        }
    }
}

@Composable
private fun ProfileSection(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AgentProfileSurface),
        border = BorderStroke(1.dp, AgentProfileBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = AgentProfileSubtext, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            items.forEachIndexed { index, (label, value) ->
                ProfileRow(label = label, value = value)
                if (index != items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AgentProfileBorder)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = AgentProfileSubtext, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = AgentProfileText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileNoticeCard(title: String, message: String, accent: Color, tint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = tint),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, color = accent, fontWeight = FontWeight.Bold)
            Text(text = message, color = AgentProfileText)
        }
    }
}

@Composable
private fun AvailabilityCard(isOnline: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AgentProfileSurface),
        border = BorderStroke(1.dp, AgentProfileBorder)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isOnline) AgentProfileSuccessSoft else AgentProfileAmberSoft,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOnline) Icons.Outlined.SupportAgent else Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) AgentProfileSuccess else AgentProfileAmber
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOnline) "Available for assignments" else "Currently offline",
                    color = AgentProfileText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isOnline) "Live status synced from your account" else "Your status is paused in the workspace",
                    color = AgentProfileSubtext,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LogoutAction(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFDC2626),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFDC2626),
            disabledContentColor = Color.White.copy(alpha = 0.75f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp
        ),
        enabled = !isLoggingOut
    ) {
        if (isLoggingOut) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = if (isLoggingOut) "Logging out" else "Logout")
    }
}

@Composable
private fun ProfilePill(label: String, backgroundColor: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = backgroundColor) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditProfileBottomSheet(
    form: AgentProfileEditForm,
    onFormChange: (AgentProfileEditForm) -> Unit,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        scrimColor = Color(0x660F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Edit Profile",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Update your agent identity and workspace details",
                        color = Color.Black.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Surface(shape = CircleShape, color = AgentProfileAccentSoft, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = AgentProfileAccent)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditableProfileField("Full Name", form.fullName, onValueChange = { onFormChange(form.copy(fullName = it)) }, icon = Icons.Outlined.Person)
                EditableProfileField("Phone Number", form.phone, onValueChange = { onFormChange(form.copy(phone = it)) }, icon = Icons.Outlined.Phone)
                EditableProfileField("Department", form.department, onValueChange = { onFormChange(form.copy(department = it)) }, icon = Icons.Outlined.SupportAgent)
                EditableProfileField("Job Title", form.jobTitle, onValueChange = { onFormChange(form.copy(jobTitle = it)) }, icon = Icons.Outlined.Work)
                EditableProfileField("Location", form.location, onValueChange = { onFormChange(form.copy(location = it)) }, icon = Icons.Outlined.LocationOn)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, AgentProfileBorder)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgentProfileSuccess)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving")
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        enabled = enabled,
        readOnly = !enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Color(0xFF0F172A),
            unfocusedTextColor = Color(0xFF0F172A),
            cursorColor = AgentProfileAccent,
            focusedBorderColor = AgentProfileAccent,
            unfocusedBorderColor = AgentProfileBorder,
            focusedLabelColor = AgentProfileAccent,
            unfocusedLabelColor = AgentProfileSubtext,
            focusedLeadingIconColor = AgentProfileAccent,
            unfocusedLeadingIconColor = AgentProfileSubtext
        )
    )
}
