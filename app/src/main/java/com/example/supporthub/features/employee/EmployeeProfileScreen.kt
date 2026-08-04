package com.example.supporthub.features.employee

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Workspaces
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground

private val ProfileBackground = Color.White
private val ProfileCardColor = Color.White
private val ProfilePrimary = Color(0xFF2E6FE8)
private val ProfilePrimaryDark = Color(0xFF177C83)
private val ProfileSecondary = Color(0xFF7B8CA8)
private val ProfileTitle = Color(0xFF1A2233)
private val ProfileLabel = Color(0xFF95A3BA)
private val ProfileFieldFill = Color(0xFFF8FAFC)
private val ProfileIconTint = Color(0xFF8A9AB1)
private val ProfileAccent = Color(0xFF43B17A)
private val ProfileSuccess = Color(0xFF2F9E68)
private val ProfileSuccessDark = Color(0xFF1F7A4B)
private val ProfileSheetSurface = Color.White
private val ProfileSheetBorder = Color(0xFFE7ECF3)
private val ProfileSheetMuted = Color(0xFF64748B)
private val ProfileSheetText = Color(0xFF0F172A)
private val ProfileSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmployeeProfileScreen(
    repository: EmployeeProfileRepository = EmployeeProfileRepositoryImpl(),
    onNavigateBack: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val viewModel: EmployeeProfileViewModel = viewModel(
        factory = EmployeeProfileViewModelFactory(repository)
    )
    val state by viewModel.uiState.collectAsState()
    var isEditSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLogoutSuccess) {
        if (state.isLogoutSuccess) {
            onLoggedOut()
        }
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage?.isNotBlank() == true) {
            isEditSheetVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .testTag("employee_profile_screen"),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(color = ProfilePrimary)
            else -> EmployeeProfileContent(
                state = state,
                onNavigateBack = onNavigateBack,
                onEditClick = { isEditSheetVisible = true },
                onLogoutClick = viewModel::logout
            )
        }

        if (isEditSheetVisible) {
            EmployeeProfileEditSheet(
                state = state,
                onDismiss = { isEditSheetVisible = false },
                onSave = { viewModel.saveProfile() },
                onFullNameChanged = viewModel::onFullNameChanged,
                onPhoneChanged = viewModel::onPhoneChanged,
                onLocationChanged = viewModel::onLocationChanged,
                onJobTitleChanged = viewModel::onJobTitleChanged,
                onDepartmentChanged = viewModel::onDepartmentChanged
            )
        }
    }
}

@Composable
private fun EmployeeProfileContent(
    state: EmployeeProfileUiState,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileTopBar(
                    onNavigateBack = onNavigateBack,
                    onEditClick = onEditClick
                )
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileHeroCard(
                    state = state,
                    isEditing = false
                )
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileMetricsRow(state = state)
            }
        }

        item {
            ProfileSectionCard(
                title = "Personal Info",
                subtitle = "Contact details and identity",
                accentColor = ProfilePrimary
            ) {
                ProfileInfoRow(
                    icon = Icons.Outlined.Badge,
                    label = "Full Name",
                    value = state.fullName,
                    testTag = "employee_profile_full_name_value"
                )

                ProfileInfoDivider()

                ProfileInfoRow(
                    icon = Icons.Outlined.Email,
                    label = "Email",
                    value = state.email,
                    testTag = "employee_profile_email_value"
                )

                ProfileInfoDivider()

                ProfileInfoRow(
                    icon = Icons.Outlined.Phone,
                    label = "Phone Number",
                    value = state.phone,
                    testTag = "employee_profile_phone_value"
                )

                ProfileInfoDivider()

                ProfileInfoRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Location",
                    value = state.location,
                    testTag = "employee_profile_location_value"
                )
            }
        }

        item {
            ProfileSectionCard(
                title = "Work Info",
                subtitle = "Role and department details",
                accentColor = ProfileAccent
            ) {
                ProfileInfoRow(
                    icon = Icons.Outlined.Business,
                    label = "Job Title",
                    value = state.jobTitle,
                    testTag = "employee_profile_job_title_value"
                )

                ProfileInfoDivider()

                ProfileInfoRow(
                    icon = Icons.Outlined.Workspaces,
                    label = "Department",
                    value = state.department,
                    testTag = "employee_profile_department_value"
                )
            }
        }

        item {
            ProfileSectionCard(
                title = "Workspace",
                subtitle = "Assigned workspace and access context",
                accentColor = ProfilePrimaryDark
            ) {
                ProfileInfoRow(
                    icon = Icons.Outlined.Workspaces,
                    label = "Workspace",
                    value = state.workspaceName.ifBlank { "—" },
                    testTag = "employee_profile_workspace_value"
                )
            }
        }

        item {
            ProfileCardActionRow(
                title = "Recent Activity",
                subtitle = "View ticket activity and performance trends",
                onClick = {},
                testTag = "employee_profile_recent_activity_row"
            )
        }

        item {
            OutlinedButton(
                onClick = onLogoutClick,
                enabled = !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFE3EAF4)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE04848)
                )
            ) {
                if (state.isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE04848)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logout")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeProfileEditSheet(
    state: EmployeeProfileUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onJobTitleChanged: (String) -> Unit,
    onDepartmentChanged: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = ProfileSheetShape,
        containerColor = ProfileSheetSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD9E0EA))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ProfileSheetText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Update your personal information",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProfileSheetMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = ProfileSheetMuted
                    )
                }
            }

            ProfileSheetTextField(
                value = state.fullName,
                onValueChange = onFullNameChanged,
                label = "Full Name",
                leadingIcon = Icons.Outlined.Badge,
                errorMessage = state.fullNameError,
                enabled = !state.isSaving,
                testTag = "employee_profile_full_name_field"
            )

            ProfileSheetTextField(
                value = state.phone,
                onValueChange = {
                    val phone = it.filter(Char::isDigit)
                    if (phone.length <= 10) {
                        onPhoneChanged(phone)
                    }
                },
                label = "Phone Number",
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                enabled = !state.isSaving,
                testTag = "employee_profile_phone_field"
            )

            ProfileSheetTextField(
                value = state.location,
                onValueChange = onLocationChanged,
                label = "Location",
                leadingIcon = Icons.Outlined.LocationOn,
                enabled = !state.isSaving,
                testTag = "employee_profile_location_field"
            )

            ProfileSheetTextField(
                value = state.jobTitle,
                onValueChange = onJobTitleChanged,
                label = "Job Title",
                leadingIcon = Icons.Outlined.Business,
                enabled = !state.isSaving,
                testTag = "employee_profile_job_title_field"
            )

            ProfileSheetTextField(
                value = state.department,
                onValueChange = onDepartmentChanged,
                label = "Department",
                leadingIcon = Icons.Outlined.Workspaces,
                enabled = !state.isSaving,
                testTag = "employee_profile_department_field"
            )

            ProfileSheetTextField(
                value = state.email,
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                enabled = false,
                testTag = "employee_profile_email_field"
            )

            state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ProfileSheetBorder)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1.25f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfilePrimary)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
private fun ProfileSheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    errorMessage: String? = null,
    testTag: String
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ProfileSheetText
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            enabled = enabled,
            isError = errorMessage != null,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF8FAFC),
                focusedBorderColor = ProfilePrimary,
                unfocusedBorderColor = ProfileSheetBorder,
                disabledBorderColor = ProfileSheetBorder,
                focusedTextColor = ProfileSheetText,
                unfocusedTextColor = ProfileSheetText,
                disabledTextColor = ProfileSheetMuted,
                cursorColor = ProfilePrimary,
                focusedLeadingIconColor = ProfilePrimary,
                unfocusedLeadingIconColor = ProfileSheetMuted,
                disabledLeadingIconColor = ProfileSheetMuted,
                focusedPlaceholderColor = ProfileSheetMuted,
                unfocusedPlaceholderColor = ProfileSheetMuted,
                disabledPlaceholderColor = ProfileSheetMuted,
                errorBorderColor = Color(0xFFDC2626),
                errorCursorColor = Color(0xFFDC2626),
                errorLeadingIconColor = Color(0xFFDC2626),
                errorTextColor = Color(0xFFDC2626)
            )
        )
        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFDC2626)
            )
        }
    }
}

@Composable
private fun ProfileTopBar(
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onNavigateBack)
                .testTag("employee_profile_back_button"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5EBF3)),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = ProfileTitle
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ProfileTitle,
                modifier = Modifier.testTag("employee_profile_title")
            )
            Text(
                text = "Overview",
                style = MaterialTheme.typography.labelMedium,
                color = ProfileSecondary
            )
        }

        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onEditClick)
                .testTag("employee_profile_edit_button"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5EBF3)),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = ProfilePrimary
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    state: EmployeeProfileUiState,
    isEditing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ProfilePrimary, ProfileAccent))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.avatarInitials.ifBlank { "P" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ProfileTitle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.jobTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ProfileSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.workspaceName,
                style = MaterialTheme.typography.labelLarge,
                color = if (isEditing) ProfileSuccessDark else ProfilePrimaryDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileMetricsRow(state: EmployeeProfileUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ProfileMetricCard(
            label = "Open Tickets",
            value = state.openTickets.toString(),
            tint = ProfilePrimary,
            modifier = Modifier.weight(1f)
        )
        ProfileMetricCard(
            label = "Resolved",
            value = state.resolvedTickets.toString(),
            tint = ProfileAccent,
            modifier = Modifier.weight(1f)
        )
        ProfileMetricCard(
            label = "Rating",
            value = state.rating,
            tint = ProfileSuccess,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileMetricCard(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = label, color = ProfileSecondary, style = MaterialTheme.typography.labelMedium)
            Text(text = value, color = tint, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileTitle
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfileSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = ProfileFieldFill) {
            Icon(icon, null, tint = ProfileIconTint, modifier = Modifier.padding(10.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = ProfileLabel, style = MaterialTheme.typography.labelMedium)
            Text(text = value, color = ProfileTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileInfoDivider() {
    HorizontalDivider(color = Color(0xFFF1F4F8), thickness = 1.dp)
}

@Composable
private fun ProfileCardActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE7EEF8)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = ProfileTitle, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = ProfileSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = ProfileIconTint
            )
        }
    }
}
