package com.example.supporthub.features.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

fun interface TicketSubmissionLauncher {
    fun launch(block: suspend () -> Unit)
}

class TicketViewModel(
    private val repository: TicketRepository,
    private val submissionLauncher: TicketSubmissionLauncher? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    private var observeTicketsJob: Job? = null

    private fun launchSubmission(block: suspend () -> Unit) {
        submissionLauncher?.launch(block) ?: viewModelScope.launch { block() }
    }

    fun start(employeeUid: String) {
        if (employeeUid.isBlank()) {
            _uiState.value = _uiState.value.copy(
                tickets = emptyList(),
                isLoadingTickets = false,
                ticketsError = null,
                ticketSearchQuery = "",
                selectedTicketStatusFilter = TicketStatusFilter.ALL,
            )
            return
        }

        observeTicketsJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isLoadingTickets = true,
            ticketsError = null,
        )
        observeTicketsJob = viewModelScope.launch {
            repository.observeEmployeeTickets(employeeUid).collect { tickets ->
                _uiState.value = _uiState.value.copy(
                    tickets = tickets,
                    isLoadingTickets = false,
                    ticketsError = null,
                )
            }
        }
    }

    fun loadTicketDetail(ticketId: String) {
        if (ticketId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedTicketDetail = null,
                isLoadingTicketDetail = false,
                ticketDetailError = "Ticket id is missing."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingTicketDetail = true,
                ticketDetailError = null,
                selectedTicketDetail = null,
            )

            repository.getTicketById(ticketId).fold(
                onSuccess = { ticket ->
                    _uiState.value = _uiState.value.copy(
                        selectedTicketDetail = ticket,
                        isLoadingTicketDetail = false,
                        ticketDetailError = null,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        selectedTicketDetail = null,
                        isLoadingTicketDetail = false,
                        ticketDetailError = error.message ?: "Unable to load ticket details.",
                    )
                }
            )
        }
    }

    fun onTicketSearchChanged(value: String) {
        _uiState.value = _uiState.value.copy(ticketSearchQuery = value)
    }

    fun onTicketStatusFilterSelected(filter: TicketStatusFilter) {
        _uiState.value = _uiState.value.copy(selectedTicketStatusFilter = filter)
    }

    fun showForm() {
        _uiState.value = _uiState.value.copy(
            isFormVisible = true,
            submitError = null,
            effect = TicketUiEffect.None,
        )
    }

    fun hideForm() {
        _uiState.value = TicketUiState(
            tickets = _uiState.value.tickets,
            isLoadingTickets = _uiState.value.isLoadingTickets,
            ticketsError = _uiState.value.ticketsError,
            ticketSearchQuery = _uiState.value.ticketSearchQuery,
            selectedTicketStatusFilter = _uiState.value.selectedTicketStatusFilter,
        )
    }

    fun onSubjectChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            subject = value,
            subjectError = null,
            submitError = null,
            effect = TicketUiEffect.None,
        )
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value,
            descriptionError = null,
            submitError = null,
            effect = TicketUiEffect.None,
        )
    }

    fun onCategorySelected(value: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = value,
            categoryError = null,
            submitError = null,
            effect = TicketUiEffect.None,
        )
    }

    fun onPrioritySelected(value: String) {
        _uiState.value = _uiState.value.copy(
            selectedPriority = value,
            priorityError = null,
            submitError = null,
            effect = TicketUiEffect.None,
        )
    }

    fun submitTicket() {
        val validation = validateTicketDraft(
            subject = _uiState.value.subject,
            description = _uiState.value.description,
            selectedCategory = _uiState.value.selectedCategory,
            selectedPriority = _uiState.value.selectedPriority,
        )

        when (validation) {
            is TicketDraftValidationResult.Invalid -> {
                _uiState.value = _uiState.value.copy(
                    subjectError = validation.subjectError,
                    descriptionError = validation.descriptionError,
                    categoryError = validation.categoryError,
                    priorityError = validation.priorityError,
                    submitError = null,
                    isSubmitting = false,
                    effect = TicketUiEffect.None,
                )
            }

            is TicketDraftValidationResult.Valid -> {
                launchSubmission {
                    _uiState.value = _uiState.value.copy(
                        subject = validation.subject,
                        description = validation.description,
                        selectedCategory = validation.category,
                        selectedPriority = validation.priority,
                        subjectError = null,
                        descriptionError = null,
                        categoryError = null,
                        priorityError = null,
                        submitError = null,
                        isSubmitting = true,
                        effect = TicketUiEffect.None,
                    )

                    val result = repository.createTicket(
                        TicketDraft(
                            subject = validation.subject,
                            description = validation.description,
                            category = validation.category,
                            priority = validation.priority,
                        )
                    )

                    result.fold(
                        onSuccess = {
                            _uiState.value = TicketUiState(
                                tickets = _uiState.value.tickets,
                                isLoadingTickets = _uiState.value.isLoadingTickets,
                                ticketsError = _uiState.value.ticketsError,
                                ticketSearchQuery = _uiState.value.ticketSearchQuery,
                                selectedTicketStatusFilter = _uiState.value.selectedTicketStatusFilter,
                                effect = TicketUiEffect.CloseWithSuccess("Ticket created successfully."),
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                submitError = error.message ?: "Unable to create ticket.",
                                effect = TicketUiEffect.None,
                            )
                        }
                    )
                }
            }
        }
    }

    fun consumeEffect() {
        _uiState.value = _uiState.value.copy(effect = TicketUiEffect.None)
    }
}

class TicketViewModelFactory(
    private val repository: TicketRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TicketViewModel(repository) as T
    }
}
