package com.example.supporthub.features.employee

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Subject
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.repository.AuthRepositoryImpl
import com.example.supporthub.features.tickets.Ticket
import com.example.supporthub.features.tickets.TicketRepositoryImpl
import com.example.supporthub.features.tickets.TicketUiEffect
import com.example.supporthub.features.tickets.TicketUiState
import com.example.supporthub.features.tickets.TicketViewModel
import com.example.supporthub.features.tickets.TicketViewModelFactory
import com.example.supporthub.features.tickets.displayTicketId
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@RequiresApi(Build.VERSION_CODES.O)
private val employeeHomeZoneId: ZoneId = ZoneId.systemDefault()

@RequiresApi(Build.VERSION_CODES.O)
private val employeeHomeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "EEEE, d MMMM",
    Locale.ENGLISH
)

data class EmployeeHomeHeaderState(
    val dateLabel: String,
    val greeting: String
)

data class EmployeeHomeActionCardState(
    val title: String,
    val description: String,
    val primaryActionLabel: String,
)

data class EmployeeTicketSummaryState(
    val openCount: Int,
    val inProgressCount: Int,
    val resolvedCount: Int,
)

enum class EmployeeSummaryCardKind {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
}

data class EmployeeSummaryCardStyle(
    val label: String,
    val sidecapName: String,
    val accentColor: Color,
    val sidecapColor: Color,
    val borderColor: Color,
    val glowColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isGlassmorphism: Boolean = true,
    val whiteLeftPanelOnly: Boolean = true,
    val usesSoftBackground: Boolean = true,
    val showsSubtleTrendLine: Boolean = false,
    val usesReferenceCardShadow: Boolean = true,
    val usesThinLeftAccent: Boolean = true,
    val usesCapAccent: Boolean = false,
    val cornerRadiusDp: Int = 24,
    val leftAccentWidthDp: Int = 5,
    val capHeightDp: Int = 0,
    val contentEmphasisLevel: Int = 0,
)

enum class EmployeeRecentActivityIcon {
    TICKET_OPEN,
    TICKET_IN_PROGRESS,
    TICKET_RESOLVED,
}

data class EmployeeRecentActivityItem(
    val ticketId: String,
    val displayId: String,
    val title: String,
    val subtitle: String,
    val statusLabel: String,
    val icon: EmployeeRecentActivityIcon,
)

fun employeeSummaryCardStyle(kind: EmployeeSummaryCardKind): EmployeeSummaryCardStyle {
    return when (kind) {
        EmployeeSummaryCardKind.OPEN -> EmployeeSummaryCardStyle(
            label = "Open Tickets",
            sidecapName = "orange",
            accentColor = Color(0xFFF59E0B),
            sidecapColor = Color(0xFFF59E0B),
            borderColor = Color(0x140F172A),
            glowColor = Color(0x00000000),
            icon = Icons.Rounded.Description,
        )
        EmployeeSummaryCardKind.IN_PROGRESS -> EmployeeSummaryCardStyle(
            label = "In Progress",
            sidecapName = "blue",
            accentColor = Color(0xFF3B82F6),
            sidecapColor = Color(0xFF3B82F6),
            borderColor = Color(0x140F172A),
            glowColor = Color(0x00000000),
            icon = Icons.Rounded.Forum,
        )
        EmployeeSummaryCardKind.RESOLVED -> EmployeeSummaryCardStyle(
            label = "Resolved",
            sidecapName = "green",
            accentColor = Color(0xFF22C55E),
            sidecapColor = Color(0xFF22C55E),
            borderColor = Color(0x140F172A),
            glowColor = Color(0x00000000),
            icon = Icons.Rounded.CheckCircle,
        )
    }
}

fun buildEmployeeTicketSummaryState(tickets: List<Ticket>): EmployeeTicketSummaryState {
    var openCount = 0
    var inProgressCount = 0
    var resolvedCount = 0

    tickets.forEach { ticket ->
        when (ticket.status.trim().uppercase(Locale.ENGLISH)) {
            "IN_PROGRESS", "PENDING" -> inProgressCount += 1
            "RESOLVED", "CLOSED" -> resolvedCount += 1
            else -> openCount += 1
        }
    }

    return EmployeeTicketSummaryState(
        openCount = openCount,
        inProgressCount = inProgressCount,
        resolvedCount = resolvedCount,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildEmployeeHomeHeaderState(
    fullName: String,
    email: String = "",
    dateTime: ZonedDateTime,
): EmployeeHomeHeaderState {
    val displayName = resolveEmployeeDisplayName(
        fullName = fullName,
        email = email
    )

    val greetingPrefix = when (dateTime.hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    return EmployeeHomeHeaderState(
        dateLabel = dateTime
            .format(employeeHomeDateFormatter)
            .uppercase(Locale.ENGLISH),
        greeting = "$greetingPrefix, $displayName"
    )
}

fun buildEmployeeHomeActionCardState(
    userName: String,
    uiState: TicketUiState,
): EmployeeHomeActionCardState {
    return EmployeeHomeActionCardState(
        title = "Create a Ticket",
        description = "Need IT support or have a request? We are here to help.",
        primaryActionLabel = "New Ticket",
    )
}

fun buildEmployeeRecentActivityItems(tickets: List<Ticket>): List<EmployeeRecentActivityItem> {
    return tickets.take(5).map { ticket ->
        val normalizedStatus = ticket.status.trim().uppercase(Locale.ENGLISH)
        val statusLabel = when (normalizedStatus) {
            "IN_PROGRESS", "PENDING" -> "In Progress"
            "RESOLVED", "CLOSED" -> "Resolved"
            else -> "Open"
        }
        val displayId = ticket.displayTicketId.ifBlank { "----" }
        val icon = when (normalizedStatus) {
            "IN_PROGRESS", "PENDING" -> EmployeeRecentActivityIcon.TICKET_IN_PROGRESS
            "RESOLVED", "CLOSED" -> EmployeeRecentActivityIcon.TICKET_RESOLVED
            else -> EmployeeRecentActivityIcon.TICKET_OPEN
        }
        val title = "Ticket #$displayId"
        val subtitle = when {
            normalizedStatus in setOf("IN_PROGRESS", "PENDING") && !ticket.assignedAgentName.isNullOrBlank() -> ticket.assignedAgentName
            ticket.category.isNotBlank() -> ticket.category
            else -> ticket.subject.ifBlank { "Support update" }
        }

        EmployeeRecentActivityItem(
            ticketId = ticket.ticketId,
            displayId = displayId,
            title = title,
            subtitle = subtitle,
            statusLabel = statusLabel,
            icon = icon,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmployeeHomeScreen(
    navController: NavController? = null,
    authRepository: AuthRepositoryImpl = AuthRepositoryImpl(),
    clock: Clock = Clock.system(employeeHomeZoneId),
) {
    val ticketViewModel: TicketViewModel = viewModel(
        factory = TicketViewModelFactory(TicketRepositoryImpl(authRepository = authRepository))
    )

    val ticketUiState by ticketViewModel.uiState.collectAsState()

    val currentUser by produceState<User?>(initialValue = null) {
        value = authRepository.getCurrentUser()
    }

    val headerState = buildEmployeeHomeHeaderState(
        fullName = currentUser?.fullName.orEmpty(),
        email = currentUser?.email.orEmpty(),
        dateTime = ZonedDateTime.now(clock)
    )

    val actionCardState = buildEmployeeHomeActionCardState(
        userName = resolveEmployeeDisplayName(
            fullName = currentUser?.fullName.orEmpty(),
            email = currentUser?.email.orEmpty(),
        ),
        uiState = ticketUiState,
    )
    val ticketSummaryState = buildEmployeeTicketSummaryState(ticketUiState.tickets)

    LaunchedEffect(currentUser?.uid) {
        ticketViewModel.start(currentUser?.uid.orEmpty())
    }

    LaunchedEffect(ticketUiState.effect) {
        if (ticketUiState.effect is TicketUiEffect.CloseWithSuccess) {
            ticketViewModel.hideForm()
            ticketViewModel.consumeEffect()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .testTag("employee_home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EmployeeHomeTopHeader(
                    state = headerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            item {
                EmployeeHomeActionCard(
                    state = actionCardState,
                    onNewTicketClick = ticketViewModel::showForm
                )
            }

            item {
                EmployeeTicketSummaryCards(
                    state = ticketSummaryState,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                EmployeeTicketSection(
                    uiState = ticketUiState,
                    onSeeAllClick = {
                        navController?.navigate(EmployeeNavRoutes.Tickets) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        if (ticketUiState.isFormVisible) {
            CreateTicketBottomSheet(
                state = ticketUiState,
                onDismiss = ticketViewModel::hideForm,
                onSubjectChanged = ticketViewModel::onSubjectChanged,
                onDescriptionChanged = ticketViewModel::onDescriptionChanged,
                onCategorySelected = ticketViewModel::onCategorySelected,
                onPrioritySelected = ticketViewModel::onPrioritySelected,
                onSubmit = ticketViewModel::submitTicket,
            )
        }
    }
}

@Composable
internal fun EmployeeHomeTopHeader(
    state: EmployeeHomeHeaderState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(bottom = 2.dp)
            .testTag("employee_home_top_header"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = state.dateLabel,
            modifier = Modifier.testTag("employee_home_date"),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF8A94A6),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = state.greeting,
            modifier = Modifier.testTag("employee_home_greeting"),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF111827),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmployeeHomeActionCard(
    state: EmployeeHomeActionCardState,
    onNewTicketClick: () -> Unit,
) {
    CreateTicketHeroCard(
        title = state.title,
        subtitle = state.description,
        buttonText = state.primaryActionLabel,
        onClick = onNewTicketClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_new_ticket_card")
    )
}

@Composable
fun CreateTicketHeroCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = isSystemInDarkTheme()

    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF167C73), Color(0xFF0F5D57))
    } else {
        listOf(Color(0xFF1A8A83), Color(0xFF116B64))
    }

    var isPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "create_ticket_button_scale"
    )

    ElevatedCard(
        modifier = modifier.heightIn(min = 168.dp),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(colors = gradientColors),
                    shape = RoundedCornerShape(30.dp)
                )
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 19.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .height(48.dp)
                        .defaultMinSize(minWidth = 132.dp)
                        .scale(buttonScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        isPressed = false
                                    }
                                }
                            )
                        },
                    interactionSource = remember { MutableInteractionSource() },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF145F59)
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF145F59)
                        )
                        Text(
                            text = buttonText,
                            style = TextStyle(
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF145F59),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

fun employeeSummaryCardWidthFraction(isCompactLayout: Boolean): Float {
    return if (isCompactLayout) 1f else 0.29f
}

fun employeeSummaryCardSpacingDp(isCompactLayout: Boolean): Int {
    return if (isCompactLayout) 6 else 6
}

fun employeeSummaryCardHorizontalPaddingDp(): Int = 18

fun employeeSummaryCardTopPaddingDp(): Int = 18

fun employeeSummaryCardBottomPaddingDp(): Int = 12

@Composable
private fun EmployeeTicketSummaryCards(
    state: EmployeeTicketSummaryState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.testTag("employee_ticket_summary_cards")
    ) {
        val compactLayout = maxWidth < 360.dp
        val cardSpacing = employeeSummaryCardSpacingDp(compactLayout).dp

        if (compactLayout) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {

                EmployeeTicketSummaryCard(
                    count = state.openCount,
                    style = employeeSummaryCardStyle(EmployeeSummaryCardKind.OPEN),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_ticket_summary_open")
                )

                EmployeeTicketSummaryCard(
                    count = state.inProgressCount,
                    style = employeeSummaryCardStyle(EmployeeSummaryCardKind.IN_PROGRESS),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_ticket_summary_in_progress")
                )

                EmployeeTicketSummaryCard(
                    count = state.resolvedCount,
                    style = employeeSummaryCardStyle(EmployeeSummaryCardKind.RESOLVED),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_ticket_summary_resolved")
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {

                EmployeeTicketSummaryCard(
                    count = state.openCount,
                    style = employeeSummaryCardStyle(EmployeeSummaryCardKind.OPEN),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("employee_ticket_summary_open")
                )

                EmployeeTicketSummaryCard(
                    count = state.inProgressCount,
                    style = employeeSummaryCardStyle(EmployeeSummaryCardKind.IN_PROGRESS),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("employee_ticket_summary_in_progress")
                )

                EmployeeTicketSummaryCard(
                    count = state.resolvedCount,
                    style = employeeSummaryCardStyle(EmployeeSummaryCardKind.RESOLVED),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("employee_ticket_summary_resolved")
                )
            }
        }
    }
}

@Composable
private fun EmployeeTicketSummaryCard(
    count: Int,
    style: EmployeeSummaryCardStyle,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(style.cornerRadiusDp.dp)
    Card(
        modifier = modifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (style.usesSoftBackground) Color.White else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(1.dp, style.borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (style.usesReferenceCardShadow) 6.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .background(color = Color.White, shape = cardShape)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(style.sidecapColor)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = style.leftAccentWidthDp.dp)
                        .background(
                            color = if (style.usesSoftBackground) Color.White else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = style.cornerRadiusDp.dp,
                                bottomStart = 0.dp,
                                bottomEnd = style.cornerRadiusDp.dp
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .width(style.leftAccentWidthDp.dp)
                        .fillMaxSize()
                        .align(Alignment.CenterStart)
                        .testTag("employee_summary_sidecap_${style.sidecapName}")
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = employeeSummaryCardHorizontalPaddingDp().dp,
                        top = employeeSummaryCardTopPaddingDp().dp,
                        end = employeeSummaryCardHorizontalPaddingDp().dp,
                        bottom = employeeSummaryCardBottomPaddingDp().dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = style.accentColor.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = style.icon,
                                contentDescription = null,
                                tint = style.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (count == 1) "1 ticket" else "$count tickets",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF),
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}

@Composable
internal fun EmployeeTicketSection(
    uiState: TicketUiState,
    onSeeAllClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_recent_activity_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("employee_recent_activity_header"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A94A6)
            )
            Text(
                text = "SEE ALL",
                modifier = Modifier
                    .testTag("employee_recent_activity_see_all")
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onSeeAllClick() })
                    },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F8A6B)
            )
        }

        when {
            uiState.isLoadingTickets -> {
                CircularProgressIndicator()
            }

            uiState.tickets.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No activity yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Your latest ticket updates will appear here instantly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        buildEmployeeRecentActivityItems(uiState.tickets).forEachIndexed { index, item ->
                            if (index > 0) {
                                Divider(
                                    modifier = Modifier.testTag("employee_recent_activity_divider_${item.ticketId.ifBlank { index.toString() }}"),
                                    color = Color(0xFFE5E7EB),
                                    thickness = 1.dp
                                )
                            }
                            EmployeeTicketCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeTicketCard(item: EmployeeRecentActivityItem) {
    val itemId = item.ticketId.ifBlank { item.title }
    val accentColor = when (item.icon) {
        EmployeeRecentActivityIcon.TICKET_OPEN -> Color(0xFF0F8A6B)
        EmployeeRecentActivityIcon.TICKET_IN_PROGRESS -> Color(0xFF3B82F6)
        EmployeeRecentActivityIcon.TICKET_RESOLVED -> Color(0xFF22A06B)
    }
    val activityIcon = when (item.icon) {
        EmployeeRecentActivityIcon.TICKET_OPEN -> Icons.Rounded.Description
        EmployeeRecentActivityIcon.TICKET_IN_PROGRESS -> Icons.Rounded.Forum
        EmployeeRecentActivityIcon.TICKET_RESOLVED -> Icons.Rounded.CheckCircle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .testTag("employee_recent_activity_item_$itemId"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(14.dp),
            color = accentColor.copy(alpha = 0.10f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = activityIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (item.icon == EmployeeRecentActivityIcon.TICKET_IN_PROGRESS) Icons.Rounded.Label else Icons.Rounded.Category,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = item.statusLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreateTicketBottomSheet(
    state: TicketUiState,
    onDismiss: () -> Unit,
    onSubjectChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onPrioritySelected: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val sheetBackground = Color(0xFFF7FAF9)
    val cardBackground = Color.White
    val borderColor = Color(0xFFD9E4DE)
    val accent = Color(0xFF1A7F67)
    val accentDeep = Color(0xFF115C4A)
    val accentSoft = Color(0xFFEAF6F1)
    val accentSubtle = Color(0xFFF3FBF7)
    val titleColor = Color(0xFF122033)
    val subtitleColor = Color(0xFF617181)
    val mutedTextColor = Color(0xFF8C9AAA)
    val chipText = Color(0xFF33495B)
    val chipBorder = Color(0xFFD5E2DA)
    val selectedBg = Color(0xFFE3F4EC)
    val selectedText = accentDeep
    val descriptionLimit = 500
    val buttonGradient = listOf(Color(0xFF17A27D), accent)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = sheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 54.dp, height = 5.dp)
                    .background(
                        color = accent.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 500.dp, max = 780.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(30.dp)
                    ),
                shape = RoundedCornerShape(30.dp),
                color = cardBackground,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(accent, accentDeep)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(accentSubtle)
                                        .border(1.dp, Color(0xFFD6EBDD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = accentSoft,
                                        border = BorderStroke(1.dp, Color(0xFFD1EBDD))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "NEW TICKET",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = accentDeep
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Create Ticket",
                                        style = TextStyle(
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 28.sp
                                        ),
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible,
                                        softWrap = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("create_ticket_sheet_close"),
                                shape = CircleShape,
                                color = Color(0xFFF4FBF8),
                                border = BorderStroke(1.dp, borderColor),
                                onClick = onDismiss
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close create ticket sheet",
                                        tint = accent
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFFF8FCFA),
                            border = BorderStroke(1.dp, Color(0xFFE4EFE9))
                        ) {
                            Text(
                                text = "Describe the issue clearly and we’ll route it to the right team.",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                ),
                                color = subtitleColor,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }

                        Divider(
                            color = borderColor.copy(alpha = 0.7f),
                            thickness = 1.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Subject,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Subject",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor
                                )
                            }

                            OutlinedTextField(
                                value = state.subject,
                                onValueChange = onSubjectChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_ticket_subject"),
                                placeholder = {
                                    Text("Briefly describe the issue", color = mutedTextColor)
                                },
                                textStyle = TextStyle(
                                    color = Color.Black,
                                    fontSize = 16.sp
                                ),
                                supportingText = state.subjectError?.let { { Text(it) } },
                                isError = state.subjectError != null,
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = chipBorder,
                                    focusedContainerColor = Color(0xFFFCFEFD),
                                    unfocusedContainerColor = Color(0xFFF8FBFA),
                                    cursorColor = accent
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Category,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = "Category",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleColor
                                    )
                                }

                                Text(
                                    text = "Choose one",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedTextColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFF8FCFA))
                                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(
                                            state = rememberScrollState(),
                                            reverseScrolling = true
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    state.categoryOptions.reversed().forEach { category ->
                                        val isSelected = state.selectedCategory == category
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isSelected) selectedBg else Color.White,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) accent else chipBorder
                                            ),
                                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                                            onClick = { onCategorySelected(category) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 15.dp,
                                                    vertical = 10.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            color = if (isSelected) accent else Color(0xFFC3D1C8),
                                                            shape = CircleShape
                                                        )
                                                )
                                                Text(
                                                    text = category,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) selectedText else chipText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            state.categoryError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Flag,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Priority",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.priorityOptions.forEach { priority ->
                                    val isSelected = state.selectedPriority == priority
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) selectedBg else Color.White,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) accent else chipBorder
                                        ),
                                        onClick = { onPrioritySelected(priority) }
                                    ) {
                                        Text(
                                            text = priority,
                                            modifier = Modifier.padding(
                                                horizontal = 14.dp,
                                                vertical = 10.dp
                                            ),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) selectedText else chipText,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            state.priorityError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Description,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = "Description",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleColor
                                    )
                                }

                                Text(
                                    text = "${state.description.length}/$descriptionLimit",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = mutedTextColor
                                )
                            }

                            OutlinedTextField(
                                value = state.description,
                                onValueChange = {
                                    if (it.length <= descriptionLimit) onDescriptionChanged(it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_ticket_description"),
                                placeholder = {
                                    Text(
                                        "Add context, impact, and any relevant details",
                                        color = mutedTextColor
                                    )
                                },
                                textStyle = TextStyle(
                                    color = Color.Black,
                                    fontSize = 16.sp
                                ),
                                supportingText = state.descriptionError?.let { { Text(it) } },
                                isError = state.descriptionError != null,
                                minLines = 4,
                                maxLines = 7,
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = chipBorder,
                                    focusedContainerColor = Color(0xFFFCFEFD),
                                    unfocusedContainerColor = Color(0xFFF8FBFA),
                                    cursorColor = accent
                                )
                            )
                        }

                        state.submitError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            enabled = state.isSubmitEnabled,
                            onClick = onSubmit
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (state.isSubmitEnabled) {
                                                buttonGradient
                                            } else {
                                                listOf(Color(0xFFD1D5DB), Color(0xFFC7CDD6))
                                            }
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Submit Ticket",
                                        color = if (state.isSubmitEnabled) Color.White else Color(0xFF6B7280),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveEmployeeDisplayName(
    fullName: String,
    email: String,
): String {
    val trimmedName = fullName.trim()
    if (trimmedName.isNotBlank()) {
        return trimmedName.substringBefore(' ')
    }

    val emailPrefix = email.substringBefore('@').trim()
    if (emailPrefix.isNotBlank()) {
        return emailPrefix.substringBefore('.').substringBefore('_').substringBefore('-')
    }

    return "User"
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun employeeHomeCurrentDate(clock: Clock = Clock.system(employeeHomeZoneId)): LocalDate =
    LocalDate.now(clock)

@Composable
internal fun EmployeePageTemplate(
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