package com.example.supporthub.features.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground

private val AdminProfileBackground = Color(0xFFF8FAFC)
private val AdminProfileSurface = Color.White
private val AdminProfileBorder = Color(0xFFE5E7EB)
private val AdminProfileText = Color(0xFF0F172A)
private val AdminProfileSubtext = Color(0xFF64748B)
private val AdminProfileLabel = Color(0xFF334155)
private val AdminProfileFieldBorder = Color(0xFFD1D5DB)
private val AdminProfileFieldFocused = Color(0xFF0F766E)
private val AdminProfileAccent = Color(0xFF0F766E)
private val AdminProfileAccentSoft = Color(0xFFE7F8F2)
private val AdminProfileSuccess = Color(0xFF16A34A)
private val AdminProfileSuccessSoft = Color(0xFFEAF8EE)
private val AdminProfileDanger = Color(0xFFDC2626)
private val AdminProfileDangerSoft = Color(0xFFFEE2E2)
private val AdminProfileGold = Color(0xFFF59E0B)
private val AdminProfileBlue = Color(0xFF2563EB)
private val AdminProfileBlueSoft = Color(0xFFEAF0FF)
private val AdminProfileViolet = Color(0xFF7C3AED)
private val AdminProfileVioletSoft = Color(0xFFF2EAFE)
private val AdminProfileCardShape = RoundedCornerShape(30.dp)
private val AdminProfileSheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

@Composable
fun AdminProfileScreen(
    repository: AdminProfileRepository = FirebaseAdminProfileRepository(),
    onNavigateBack: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val viewModel: AdminProfileViewModel = viewModel(factory = AdminProfileViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditSheet by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLoggedOut()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            if (message.contains("successfully", ignoreCase = true)) snackbarHostState.showSnackbar("✓ Profile updated successfully")
            else snackbarHostState.showSnackbar(message)
        }
    }

    SupportHubPremiumBackground {
        Box(modifier = Modifier.fillMaxSize().background(AdminProfileBackground)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ProfileTopBar(onNavigateBack = onNavigateBack)
                PremiumProfileHeader(state = state)
                PremiumStatsSection(state = state)
                PersonalInformationCard(state = state)
                ProfessionalInformationCard(state = state)
//                AvailabilityCard(state = state)
                OutlinedButton(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, AdminProfileDanger.copy(alpha = 0.32f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AdminProfileDanger
                    )
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Sign Out")
                }
                Spacer(Modifier.height(92.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            )
            PremiumFloatingEditButton(
                onClick = { showEditSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }

    if (showEditSheet) {
        EditProfileBottomSheet(
            state = state,
            onDismiss = { showEditSheet = false },
            onSave = { viewModel.updateProfile(it) }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Sign out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; viewModel.logout() }) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
        }
        Text(
            "Admin Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun PremiumProfileHeader(state: AdminProfileUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AdminProfileCardShape,
        colors = CardDefaults.cardColors(containerColor = AdminProfileSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFFF5FBF8), Color.White)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarHero(initials = state.avatarInitials)
                    StatusDot(isOnline = state.isAvailable)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = state.fullName.ifBlank { "Admin Profile" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AdminProfileText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.jobTitle.ifBlank { "Workspace Administrator" },
                        color = AdminProfileSubtext,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.workspaceName.ifBlank { "Workspace" },
                            color = AdminProfileText,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("•", color = AdminProfileSubtext)
                        Text(
                            text = state.department.ifBlank { "Department" },
                            color = AdminProfileSubtext,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumChip("Admin", AdminProfileAccentSoft, AdminProfileAccent)
                PremiumChip(
                    if (state.isAvailable) "Active" else "Offline",
                    if (state.isAvailable) AdminProfileSuccessSoft else AdminProfileDangerSoft,
                    if (state.isAvailable) AdminProfileSuccess else AdminProfileDanger
                )
                PremiumChip("Workspace Owner", AdminProfileBlueSoft, AdminProfileBlue)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                PremiumContactInfoRow(Icons.Outlined.MailOutline, state.email.ifBlank { "email@workspace.com" })
            }
        }
    }
}

@Composable
private fun StatusDot(isOnline: Boolean) {
    Surface(
        modifier = Modifier.size(18.dp),
        shape = CircleShape,
        color = if (isOnline) AdminProfileSuccess else AdminProfileDanger,
        border = BorderStroke(3.dp, Color.White)
    ) {}
}

@Composable
private fun PremiumContactInfoRow(icon: ImageVector, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = AdminProfileAccentSoft) {
            Icon(icon, null, tint = AdminProfileAccent, modifier = Modifier.padding(8.dp))
        }
        Text(value, color = AdminProfileText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PremiumStatsSection(state: AdminProfileUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AdminProfileText
        )
        val cards = listOf(
            MetricCardData("Resolved Tickets", state.resolvedTicketsLabel, Icons.Outlined.Done, AdminProfileSuccessSoft, AdminProfileSuccess),
            MetricCardData("Open Tickets", state.openTicketsLabel, Icons.Outlined.QueryStats, AdminProfileBlueSoft, AdminProfileBlue),
            MetricCardData("Average Response", state.responseTimeLabel, Icons.Outlined.Business, AdminProfileVioletSoft, AdminProfileViolet),
            MetricCardData("Workspace Rank", state.workspaceRankLabel, Icons.Outlined.Workspaces, AdminProfileAccentSoft, AdminProfileAccent),
            MetricCardData("Customer Rating", state.csatLabel, Icons.Outlined.CheckCircle, AdminProfileGold.copy(alpha = 0.16f), AdminProfileGold),
            MetricCardData("Total Tickets", state.totalTicketsLabel, Icons.Outlined.Workspaces, AdminProfileAccentSoft, AdminProfileAccent)
        )
        cards.chunked(2).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowCards.forEach { card -> PremiumMetricCard(card, Modifier.weight(1f)) }
                if (rowCards.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PremiumMetricCard(card: MetricCardData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AdminProfileSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = card.softBackground) {
                Icon(
                    card.icon,
                    contentDescription = null,
                    tint = card.tint,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Text(
                card.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AdminProfileText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                card.title,
                color = AdminProfileSubtext,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PersonalInformationCard(state: AdminProfileUiState) {
    InfoCard(title = "Personal Information") {
        val items = listOf(
            ProfileInfoItem(Icons.Outlined.Person, "Full Name", state.fullName),
            ProfileInfoItem(Icons.Outlined.Phone, "Phone Number", state.phone),
            ProfileInfoItem(Icons.Outlined.LocationOn, "Location", state.location),
            ProfileInfoItem(Icons.Outlined.MailOutline, "Email", state.email),
            ProfileInfoItem(Icons.Outlined.HomeWork, "Workspace", state.workspaceName)
        )
        items.forEach { item -> PremiumInfoRow(item) }
    }
}

@Composable
private fun ProfessionalInformationCard(state: AdminProfileUiState) {
    InfoCard(title = "Professional Information") {
        val items = listOf(
            ProfileInfoItem(Icons.Outlined.Business, "Department", state.department),
            ProfileInfoItem(Icons.Outlined.Badge, "Job Title", state.jobTitle),
            ProfileInfoItem(Icons.Outlined.Workspaces, "Role", state.roleLabel),
            ProfileInfoItem(Icons.Outlined.CheckCircle, "Status", state.statusLabel),
            ProfileInfoItem(Icons.Outlined.QueryStats, "Joined Date", state.joinedDateLabel),
            ProfileInfoItem(Icons.Outlined.Fingerprint, "Employee ID", state.employeeId.ifBlank { "—" })
        )
        items.forEach { item -> PremiumInfoRow(item) }
    }
}
//
//@Composable
//private fun AvailabilityCard(state: AdminProfileUiState) {
//    InfoCard(title = "Availability") {
//        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
//            Surface(shape = CircleShape, color = if (state.isAvailable) AdminProfileSuccessSoft else AdminProfileDangerSoft) {
//                Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
//                    Surface(modifier = Modifier.size(16.dp), shape = CircleShape, color = if (state.isAvailable) AdminProfileSuccess else AdminProfileDanger) {}
//                }
//            }
//            Column(modifier = Modifier.weight(1f)) {
//                Text(if (state.isAvailable) "Online" else "Away", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//                Text(state.availabilityMessage, color = AdminProfileSubtext)
//            }
//            Switch(checked = state.isAvailable, onCheckedChange = {}, enabled = false)
//        }
//    }
//}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AdminProfileCardShape,
        colors = CardDefaults.cardColors(containerColor = AdminProfileSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = AdminProfileText)
            content()
        }
    }
}

@Composable
private fun PremiumInfoRow(item: ProfileInfoItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = AdminProfileAccentSoft) {
            Icon(item.icon, null, tint = item.tint, modifier = Modifier.padding(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = AdminProfileSubtext, style = MaterialTheme.typography.labelMedium)
            Text(
                item.value.ifBlank { "—" },
                color = AdminProfileText,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun PremiumFloatingEditButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AdminProfileAccent, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
    ) {
        Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(10.dp))
        Text("Edit Profile")
    }
}

@Composable
private fun AvatarHero(initials: String) {
    Surface(
        modifier = Modifier.size(108.dp),
        shape = CircleShape,
        color = AdminProfileAccentSoft,
        border = BorderStroke(1.dp, AdminProfileAccent.copy(alpha = 0.18f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                initials.ifBlank { "A" },
                fontWeight = FontWeight.Bold,
                color = AdminProfileAccent,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
private fun PremiumChip(label: String, background: Color, contentColor: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    state: AdminProfileUiState,
    onDismiss: () -> Unit,
    onSave: (AdminProfileUpdate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fullName by remember(state.fullName) { mutableStateOf(state.fullName) }
    var phone by remember(state.phone) { mutableStateOf(state.phone) }
    var department by remember(state.department) { mutableStateOf(state.department) }
    var jobTitle by remember(state.jobTitle) { mutableStateOf(state.jobTitle) }
    var location by remember(state.location) { mutableStateOf(state.location) }
    var avatarUrl by remember(state.avatarUrl) { mutableStateOf(state.avatarUrl) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var departmentError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    val phoneRegex = remember { Regex("^\\+?[0-9][0-9\\s-]{6,}$") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        scrimColor = Color(0xB3000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AdminProfileText
            )
            Text(
                "Update your profile information in a clean, white form layout.",
                style = MaterialTheme.typography.bodyMedium,
                color = AdminProfileSubtext
            )
            EditableField("Full Name", fullName, { fullName = it }, Icons.Outlined.Person, nameError)
            EditableField("Phone Number", phone, { phone = it }, Icons.Outlined.Phone, phoneError)
            EditableField("Department", department, { department = it }, Icons.Outlined.Business, departmentError)
            EditableField("Designation / Job Title", jobTitle, { jobTitle = it }, Icons.Outlined.Badge, titleError)
            EditableField("Location", location, { location = it }, Icons.Outlined.LocationOn, null)
            EditableField("Avatar URL (optional)", avatarUrl.orEmpty(), { avatarUrl = it }, Icons.Outlined.MailOutline, null)
            ReadOnlyField("Email", state.email)
            ReadOnlyField("Workspace", state.workspaceName)
            ReadOnlyField("Role", state.roleLabel)
            Button(
                onClick = {
                    nameError = if (fullName.isBlank()) "Name cannot be blank" else null
                    phoneError = if (!phoneRegex.matches(phone.trim())) "Enter a valid phone number" else null
                    departmentError = if (department.isBlank()) "Department cannot be blank" else null
                    titleError = if (jobTitle.isBlank()) "Designation cannot be blank" else null
                    if (nameError == null && phoneError == null && departmentError == null && titleError == null) {
                        onSave(AdminProfileUpdate(fullName = fullName.trim(), phone = phone.trim(), department = department.trim(), jobTitle = jobTitle.trim(), location = location.trim(), avatarUrl = avatarUrl?.trim()?.ifBlank { null }))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isUpdatingProfile,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminProfileAccent,
                    contentColor = Color.White
                )
            ) {
                if (state.isUpdatingProfile) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("Saving...")
                } else {
                    Text("Save Changes")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    error: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = AdminProfileSubtext) },
        leadingIcon = { Icon(icon, null, tint = AdminProfileAccent) },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = AdminProfileDanger) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedTextColor = AdminProfileText,
            unfocusedTextColor = AdminProfileText,
            disabledTextColor = AdminProfileText,
            focusedLabelColor = AdminProfileAccent,
            unfocusedLabelColor = AdminProfileSubtext,
            focusedLeadingIconColor = AdminProfileAccent,
            unfocusedLeadingIconColor = AdminProfileSubtext,
            focusedBorderColor = AdminProfileAccent,
            unfocusedBorderColor = AdminProfileFieldBorder,
            errorBorderColor = AdminProfileDanger,
            errorLabelColor = AdminProfileDanger,
            errorLeadingIconColor = AdminProfileDanger,
            errorTextColor = AdminProfileDanger,
            cursorColor = AdminProfileAccent
        )
    )
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label, color = AdminProfileSubtext) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            disabledContainerColor = Color(0xFFF8FAFC),
            focusedTextColor = AdminProfileText,
            unfocusedTextColor = AdminProfileText,
            disabledTextColor = AdminProfileText,
            focusedLabelColor = AdminProfileSubtext,
            unfocusedLabelColor = AdminProfileSubtext,
            focusedBorderColor = AdminProfileFieldBorder,
            unfocusedBorderColor = AdminProfileFieldBorder,
            disabledBorderColor = AdminProfileFieldBorder
        )
    )
}

private data class MetricCardData(val title: String, val value: String, val icon: ImageVector, val softBackground: Color, val tint: Color)
private data class ProfileInfoItem(val icon: ImageVector, val title: String, val value: String, val tint: Color = AdminProfileAccent)
