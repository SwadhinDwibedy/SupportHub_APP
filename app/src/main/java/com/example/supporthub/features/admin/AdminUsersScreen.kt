package com.example.supporthub.features.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl

private enum class AdminUserFilter(val label: String, val testTagSuffix: String) {
    ALL_USERS("All", "all_users"),
    EMPLOYEES("Employees", "employees"),
    AGENTS("Agents", "agents"),
    ADMINS("Admins", "admins"),
    PENDING("Pending", "pending"),
    MANAGERS("Managers", "managers")
}

private val AdminUsersBackground = Color(0xFFFFFFFF)
private val AdminUsersSurface = Color(0xFFFFFFFF)
private val AdminUsersBorder = Color(0xFFECEFF3)
private val AdminUsersPrimary = Color(0xFF0F9D58)
private val AdminUsersTitle = Color(0xFF101828)
private val AdminUsersSubtitle = Color(0xFF667085)
private val AdminUsersSoftGreen = Color(0xFFEAF7EF)
private val AdminUsersSoftBlue = Color(0xFFEAF2FF)
private val AdminUsersSoftOrange = Color(0xFFFFF1E8)
private val AdminUsersSoftPurple = Color(0xFFF2EAFF)
private val AdminUsersSoftGray = Color(0xFFF2F4F7)
private val AdminUsersAdminChip = Color(0xFF0F9D58)
private val AdminUsersAgentChip = Color(0xFF3B82F6)
private val AdminUsersEmployeeChip = Color(0xFF98A2B3)
private val AdminUsersManagerChip = Color(0xFF7C3AED)
private val AdminUsersPendingChip = Color(0xFFF59E0B)

@Composable
fun AdminUsersScreen(
    onUserSelected: (User) -> Unit = {}
) {
    val approvalsViewModel: AdminApprovalsViewModel = viewModel(
        factory = AdminApprovalsViewModelFactory(AuthRepositoryImpl())
    )
    val uiState by approvalsViewModel.uiState.collectAsState()

    AdminUsersScreenContent(
        uiState = uiState,
        onRefresh = approvalsViewModel::loadPendingUsers,
        onApprove = approvalsViewModel::approveUser,
        onKeepPending = approvalsViewModel::keepPending,
        onUserSelected = onUserSelected
    )
}

@Composable
internal fun AdminUsersScreenContent(
    uiState: AdminApprovalsUiState,
    onRefresh: () -> Unit,
    onApprove: (String, Role) -> Unit,
    onKeepPending: (String) -> Unit,
    onUserSelected: (User) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var selectedFilter by remember { mutableStateOf(AdminUserFilter.ALL_USERS) }

    val workspaceUsers = uiState.workspaceUsers
    val pendingUsers = workspaceUsers.filter { user -> user.status.equals("pending", ignoreCase = true) }
    val activeUsers = workspaceUsers.filter { user -> !user.status.equals("pending", ignoreCase = true) }
    val queryText = searchQuery.text
    val visibleUsers = workspaceUsers
        .filter { user -> matchesFilter(user, selectedFilter) && matchesSearch(user, queryText) }
        .sortedBy { user -> user.fullName.lowercase() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminUsersBackground)
            .testTag("admin_users_screen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            AdminUsersHeaderSection(
                totalUsers = workspaceUsers.size,
                pendingUsers = pendingUsers.size,
                activeUsers = activeUsers.size,
                admins = workspaceUsers.count { matchesRole(it, Role.ADMIN) },
                onRefresh = onRefresh
            )
        }

        uiState.actionMessage?.let { message ->
            item {
                StatusMessageCard(
                    message = message,
                    containerColor = AdminUsersSoftGreen,
                    contentColor = AdminUsersTitle,
                    borderColor = AdminUsersBorder
                )
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                StatusMessageCard(
                    message = message,
                    containerColor = Color(0xFFFFF1F2),
                    contentColor = Color(0xFFB42318),
                    borderColor = Color(0xFFFECACA)
                )
            }
        }

        when {
            uiState.isLoading && uiState.workspaceUsers.isEmpty() -> item { LoadingStateCard() }
            else -> {
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }
                item {
                    RoleFilterTabs(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }
                item {
                    UsersDirectorySection(
                        users = visibleUsers,
                        onRefresh = onRefresh,
                        onUserSelected = onUserSelected
                    )
                }
                item {
                    PendingApprovalsSection(
                        pendingUsers = pendingUsers,
                        isLoading = uiState.isLoading,
                        onApprove = onApprove,
                        onKeepPending = onKeepPending
                    )
                }
                if (visibleUsers.isEmpty() && searchQuery.text.isNotBlank()) {
                    item { EmptyUsersState() }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersHeaderSection(
    totalUsers: Int,
    pendingUsers: Int,
    activeUsers: Int,
    admins: Int,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Users",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AdminUsersTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Manage everyone in your workspace",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AdminUsersSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminUsersPrimary),
                border = BorderStroke(1.dp, AdminUsersBorder)
            ) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Refresh", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SummaryCard(
                label = "Total users",
                value = totalUsers.toString(),
                accent = AdminUsersPrimary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Pending",
                value = pendingUsers.toString(),
                accent = AdminUsersPendingChip,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Active",
                value = activeUsers.toString(),
                accent = AdminUsersAgentChip,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Admins",
                value = admins.toString(),
                accent = AdminUsersManagerChip,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AdminUsersSubtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = AdminUsersTitle,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = AdminUsersSubtitle)
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search users",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = AdminUsersPrimary,
                    focusedTextColor = AdminUsersTitle,
                    unfocusedTextColor = AdminUsersTitle,
                    focusedPlaceholderColor = AdminUsersSubtitle,
                    unfocusedPlaceholderColor = AdminUsersSubtitle
                )
            )
        }
    }
}

@Composable
private fun RoleFilterTabs(
    selectedFilter: AdminUserFilter,
    onFilterSelected: (AdminUserFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(AdminUserFilter.entries.size) { index ->
            val filter = AdminUserFilter.entries[index]
            val selected = filter == selectedFilter
            val background by animateColorAsState(
                if (selected) AdminUsersPrimary else Color.White,
                label = "tab_bg"
            )
            val contentColor by animateColorAsState(
                if (selected) Color.White else AdminUsersPrimary,
                label = "tab_fg"
            )
            Surface(
                modifier = Modifier.clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(999.dp),
                color = background,
                border = BorderStroke(1.dp, if (selected) AdminUsersPrimary else AdminUsersBorder)
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun UsersDirectorySection(
    users: List<User>,
    onRefresh: () -> Unit,
    onUserSelected: (User) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Users",
                style = MaterialTheme.typography.titleLarge,
                color = AdminUsersTitle,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminUsersPrimary),
                border = BorderStroke(1.dp, AdminUsersBorder)
            ) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Refresh", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            users.forEach { user ->
                UserCard(user = user, onClick = { onUserSelected(user) })
            }
        }
    }
}

@Composable
private fun LoadingStateCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = AdminUsersPrimary)
            Text(
                text = "Loading users...",
                style = MaterialTheme.typography.titleMedium,
                color = AdminUsersTitle,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyUsersState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AdminUsersSoftGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SupervisorAccount,
                    contentDescription = null,
                    tint = AdminUsersSubtitle
                )
            }
            Text(
                text = "No users found",
                style = MaterialTheme.typography.titleLarge,
                color = AdminUsersTitle,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Try another search.",
                style = MaterialTheme.typography.bodyMedium,
                color = AdminUsersSubtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PendingApprovalsSection(
    pendingUsers: List<User>,
    isLoading: Boolean,
    onApprove: (String, Role) -> Unit,
    onKeepPending: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Pending Approvals",
            style = MaterialTheme.typography.titleLarge,
            color = AdminUsersTitle,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (pendingUsers.isEmpty()) {
            EmptyUsersState()
        } else {
            pendingUsers.forEach { user ->
                PendingApprovalCard(
                    user = user,
                    isLoading = isLoading,
                    onApprove = onApprove,
                    onKeepPending = onKeepPending
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Avatar(initials = buildUserInitials(user), modifier = Modifier.size(48.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = user.fullName.takeIf { it.isNotBlank() } ?: "Unknown user",
                    style = MaterialTheme.typography.titleMedium,
                    color = AdminUsersTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AdminUsersSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleChip(
                        label = roleLabel(user),
                        roleColor = roleColor(user),
                        backgroundColor = roleBackground(user)
                    )
                    if (user.department.isNotBlank()) {
                        SoftChip(
                            label = user.department,
                            backgroundColor = AdminUsersSoftGreen,
                            contentColor = AdminUsersPrimary
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AdminUsersSubtitle
            )
        }
    }
}

@Composable
private fun PendingApprovalCard(
    user: User,
    isLoading: Boolean,
    onApprove: (String, Role) -> Unit,
    onKeepPending: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Avatar(initials = buildUserInitials(user), modifier = Modifier.size(48.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = AdminUsersTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminUsersSubtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Requested role: ${roleLabel(user)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = AdminUsersPendingChip,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.SupervisorAccount,
                    contentDescription = null,
                    tint = AdminUsersSubtitle
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onApprove(user.uid, Role.ADMIN) },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminUsersPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Approve", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = { onApprove(user.uid, Role.AGENT) },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminUsersAgentChip,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Assign Role", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = { onKeepPending(user.uid) },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminUsersPendingChip,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Reject", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun Avatar(
    initials: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(AdminUsersPrimary, AdminUsersAgentChip))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RoleChip(
    label: String,
    roleColor: Color,
    backgroundColor: Color
) {
    Surface(shape = RoundedCornerShape(999.dp), color = backgroundColor) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = roleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SoftChip(
    label: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(shape = RoundedCornerShape(999.dp), color = backgroundColor) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusMessageCard(
    message: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildUserInitials(user: User): String = listOf(user.fullName, user.email)
    .joinToString(" ")
    .trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.take(1).uppercase() }
    .ifBlank { "U" }

private fun matchesRole(user: User, role: Role): Boolean = when (role) {
    Role.ADMIN -> user.role.equals("admin", ignoreCase = true) || user.approvedRole.equals("admin", ignoreCase = true)
    Role.AGENT -> user.role.equals("agent", ignoreCase = true) || user.approvedRole.equals("agent", ignoreCase = true)
    Role.EMPLOYEE -> user.role.equals("employee", ignoreCase = true) || user.approvedRole.equals("employee", ignoreCase = true)
}

private fun roleLabel(user: User): String = when {
    user.status.equals("pending", ignoreCase = true) -> "Pending"
    user.approvedRole.equals("admin", ignoreCase = true) -> "Admin"
    user.approvedRole.equals("agent", ignoreCase = true) -> "Agent"
    user.approvedRole.equals("manager", ignoreCase = true) -> "Manager"
    else -> "Employee"
}

private fun roleColor(user: User): Color = when {
    user.status.equals("pending", ignoreCase = true) -> AdminUsersPendingChip
    user.approvedRole.equals("admin", ignoreCase = true) -> AdminUsersAdminChip
    user.approvedRole.equals("agent", ignoreCase = true) -> AdminUsersAgentChip
    user.approvedRole.equals("manager", ignoreCase = true) -> AdminUsersManagerChip
    else -> AdminUsersEmployeeChip
}

private fun roleBackground(user: User): Color = when {
    user.status.equals("pending", ignoreCase = true) -> AdminUsersSoftOrange
    user.approvedRole.equals("admin", ignoreCase = true) -> AdminUsersSoftGreen
    user.approvedRole.equals("agent", ignoreCase = true) -> AdminUsersSoftBlue
    user.approvedRole.equals("manager", ignoreCase = true) -> AdminUsersSoftPurple
    else -> AdminUsersSoftGray
}

private fun matchesFilter(user: User, filter: AdminUserFilter): Boolean = when (filter) {
    AdminUserFilter.ALL_USERS -> true
    AdminUserFilter.EMPLOYEES -> user.role.equals("employee", ignoreCase = true) || user.approvedRole.equals("employee", ignoreCase = true)
    AdminUserFilter.AGENTS -> user.role.equals("agent", ignoreCase = true) || user.approvedRole.equals("agent", ignoreCase = true)
    AdminUserFilter.ADMINS -> user.role.equals("admin", ignoreCase = true) || user.approvedRole.equals("admin", ignoreCase = true)
    AdminUserFilter.PENDING -> user.status.equals("pending", ignoreCase = true)
    AdminUserFilter.MANAGERS -> user.role.equals("manager", ignoreCase = true) || user.approvedRole.equals("manager", ignoreCase = true)
}

private fun matchesSearch(user: User, query: String): Boolean {
    if (query.isBlank()) return true
    val needle = query.trim().lowercase()
    return listOf(user.fullName, user.email, user.department, user.role, user.approvedRole).any { it?.contains(needle, ignoreCase = true) == true }
}
