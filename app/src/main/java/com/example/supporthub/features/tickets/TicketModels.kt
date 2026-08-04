package com.example.supporthub.features.tickets

import com.example.supporthub.features.authentication.model.User
import java.util.Locale

const val TICKET_STATUS_OPEN = "OPEN"

val defaultTicketCategories = listOf(
    "Access",
    "Hardware",
    "Software",
    "Network",
    "Payroll",
    "Other"
)

val defaultTicketPriorities = listOf(
    "Low",
    "Medium",
    "High",
    "Critical"
)

data class TicketDraft(
    val subject: String,
    val description: String,
    val category: String,
    val priority: String,
)

data class Ticket(
    val ticketId: String = "",
    val employeeUid: String = "",
    val employeeName: String = "",
    val employeeEmail: String = "",
    val workspaceName: String = "",
    val subject: String = "",
    val description: String = "",
    val category: String = "",
    val priority: String = "",
    val status: String = TICKET_STATUS_OPEN,
    val assignedAgentId: String? = null,
    val assignedAgentName: String? = null,
    val assignedAgentEmail: String? = null,
    val assignedAgentRole: String? = null,
    val assignedAgentDepartment: String? = null,
    val assignedTimestamp: Any? = null,
    val assignedByAdmin: String? = null,
    val assignmentStatus: String? = null,
    val internalNotes: String? = null,
    val resolutionNotes: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null,
    val resolvedAt: Any? = null,
)

data class TicketSubmissionPayload(
    val employeeUid: String,
    val employeeName: String,
    val employeeEmail: String,
    val workspaceName: String,
    val subject: String,
    val description: String,
    val category: String,
    val priority: String,
)

data class AssignmentAgentOption(
    val uid: String,
    val fullName: String,
    val department: String,
    val activeTicketCount: Int,
    val isOnline: Boolean,
    val initials: String,
    val email: String,
)

data class TicketAssignmentUiState(
    val isLoadingAgents: Boolean = false,
    val isAssigningTicket: Boolean = false,
    val availableAgents: List<AssignmentAgentOption> = emptyList(),
    val selectedAgentId: String? = null,
    val isDropdownExpanded: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val showReassignSheet: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    val selectedAgent: AssignmentAgentOption?
        get() = availableAgents.firstOrNull { it.uid == selectedAgentId }
}

fun TicketDraft.toSubmissionPayload(user: User): TicketSubmissionPayload = TicketSubmissionPayload(
    employeeUid = user.uid,
    employeeName = user.fullName.trim(),
    employeeEmail = user.email.trim(),
    workspaceName = user.workspaceName.trim(),
    subject = subject,
    description = description,
    category = category,
    priority = priority,
)

sealed interface TicketDraftValidationResult {
    data class Valid(
        val subject: String,
        val description: String,
        val category: String,
        val priority: String,
    ) : TicketDraftValidationResult

    data class Invalid(
        val subjectError: String? = null,
        val descriptionError: String? = null,
        val categoryError: String? = null,
        val priorityError: String? = null,
    ) : TicketDraftValidationResult
}

fun validateTicketDraft(
    subject: String,
    description: String,
    selectedCategory: String?,
    selectedPriority: String?,
): TicketDraftValidationResult {
    val trimmedSubject = subject.trim()
    val trimmedDescription = description.trim()
    val category = selectedCategory?.trim().orEmpty()
    val priority = selectedPriority?.trim().orEmpty()

    val subjectError = if (trimmedSubject.isBlank()) "Subject cannot be empty." else null
    val descriptionError = if (trimmedDescription.isBlank()) "Description cannot be empty." else null
    val categoryError = if (category.isBlank()) "Category must be selected." else null
    val priorityError = if (priority.isBlank()) "Priority must be selected." else null

    if (subjectError != null || descriptionError != null || categoryError != null || priorityError != null) {
        return TicketDraftValidationResult.Invalid(
            subjectError = subjectError,
            descriptionError = descriptionError,
            categoryError = categoryError,
            priorityError = priorityError,
        )
    }

    return TicketDraftValidationResult.Valid(
        subject = trimmedSubject,
        description = trimmedDescription,
        category = category,
        priority = priority,
    )
}

enum class TicketStatusFilter(
    val label: String,
) {
    ALL("All"),
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
}

enum class TicketPriorityTone {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

enum class TicketStatusTone {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
}

val Ticket.displayTicketId: String
    get() = ticketId.trim().takeIf { it.isNotBlank() }?.take(4).orEmpty()

fun ticketPriorityTone(priority: String): TicketPriorityTone {
    return when (priority.trim().uppercase(Locale.ENGLISH)) {
        "LOW" -> TicketPriorityTone.LOW
        "MEDIUM" -> TicketPriorityTone.MEDIUM
        "CRITICAL" -> TicketPriorityTone.CRITICAL
        else -> TicketPriorityTone.HIGH
    }
}

fun ticketStatusTone(status: String): TicketStatusTone {
    return when (status.trim().uppercase(Locale.ENGLISH)) {
        "IN_PROGRESS" -> TicketStatusTone.IN_PROGRESS
        "RESOLVED" -> TicketStatusTone.RESOLVED
        else -> TicketStatusTone.OPEN
    }
}

fun ticketStatusLabel(status: String): String {
    return when (ticketStatusTone(status)) {
        TicketStatusTone.OPEN -> TICKET_STATUS_OPEN
        TicketStatusTone.IN_PROGRESS -> "IN PROGRESS"
        TicketStatusTone.RESOLVED -> "RESOLVED"
    }
}

fun Ticket.matchesStatusFilter(filter: TicketStatusFilter): Boolean {
    val normalizedStatus = status.trim().uppercase(Locale.ENGLISH)
    return when (filter) {
        TicketStatusFilter.ALL -> true
        TicketStatusFilter.OPEN -> normalizedStatus == TICKET_STATUS_OPEN
        TicketStatusFilter.IN_PROGRESS -> normalizedStatus == "IN_PROGRESS"
        TicketStatusFilter.RESOLVED -> normalizedStatus == "RESOLVED"
    }
}

fun Ticket.matchesSearchQuery(query: String): Boolean {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) {
        return true
    }

    val searchableFields = listOf(ticketId, subject, category, status)
    return searchableFields.any { field ->
        field.contains(normalizedQuery, ignoreCase = true)
    }
}

sealed interface TicketUiEffect {
    data object None : TicketUiEffect
    data class CloseWithSuccess(val message: String) : TicketUiEffect
}

data class TicketUiState(
    val subject: String = "",
    val description: String = "",
    val selectedCategory: String? = null,
    val selectedPriority: String? = null,
    val categoryOptions: List<String> = defaultTicketCategories,
    val priorityOptions: List<String> = defaultTicketPriorities,
    val subjectError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val priorityError: String? = null,
    val submitError: String? = null,
    val ticketsError: String? = null,
    val ticketDetailError: String? = null,
    val isSubmitting: Boolean = false,
    val isFormVisible: Boolean = false,
    val isLoadingTickets: Boolean = false,
    val isLoadingTicketDetail: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val selectedTicketDetail: Ticket? = null,
    val ticketSearchQuery: String = "",
    val selectedTicketStatusFilter: TicketStatusFilter = TicketStatusFilter.ALL,
    val assignmentUiState: TicketAssignmentUiState = TicketAssignmentUiState(),
    val effect: TicketUiEffect = TicketUiEffect.None,
) {
    val isSubmitEnabled: Boolean
        get() = !isSubmitting &&
            subject.trim().isNotBlank() &&
            description.trim().isNotBlank() &&
            !selectedCategory.isNullOrBlank() &&
            !selectedPriority.isNullOrBlank()

    val visibleTickets: List<Ticket>
        get() = tickets.filter { ticket ->
            ticket.matchesStatusFilter(selectedTicketStatusFilter) &&
                ticket.matchesSearchQuery(ticketSearchQuery)
        }
}
