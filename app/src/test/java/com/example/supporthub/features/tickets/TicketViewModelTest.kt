package com.example.supporthub.features.tickets

import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `submit exposes validation errors and does not call repository when draft is invalid`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeTicketRepository()
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.onSubjectChanged("   ")
            viewModel.onDescriptionChanged("  ")
            viewModel.submitTicket()

            val state = viewModel.uiState.value
            assertEquals("Subject cannot be empty.", state.subjectError)
            assertEquals("Description cannot be empty.", state.descriptionError)
            assertEquals("Category must be selected.", state.categoryError)
            assertEquals("Priority must be selected.", state.priorityError)
            assertEquals(0, repository.createTicketCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `submit emits success effect and clears form when repository succeeds`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeTicketRepository(
                createTicketResult = Result.success(Unit),
                currentUser = sampleUser()
            )
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.onSubjectChanged("Payroll issue")
            viewModel.onDescriptionChanged("Cannot access payroll portal")
            viewModel.onCategorySelected("Access")
            viewModel.onPrioritySelected("High")

            viewModel.submitTicket()

            val state = viewModel.uiState.value
            assertFalse(state.isSubmitting)
            assertFalse(state.isFormVisible)
            assertEquals("", state.subject)
            assertEquals("", state.description)
            assertEquals(null, state.selectedCategory)
            assertEquals(null, state.selectedPriority)
            assertEquals(TicketUiEffect.CloseWithSuccess("Ticket created successfully."), state.effect)
            assertEquals(1, repository.createTicketCalls)
            assertEquals("Payroll issue", repository.lastDraft?.subject)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `submit exposes repository error when create ticket fails`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeTicketRepository(
                createTicketResult = Result.failure(IllegalStateException("Firestore unavailable")),
                currentUser = sampleUser()
            )
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.onSubjectChanged("Payroll issue")
            viewModel.onDescriptionChanged("Cannot access payroll portal")
            viewModel.onCategorySelected("Access")
            viewModel.onPrioritySelected("High")

            viewModel.submitTicket()

            val state = viewModel.uiState.value
            assertFalse(state.isSubmitting)
            assertEquals("Firestore unavailable", state.submitError)
            assertEquals(TicketUiEffect.None, state.effect)
            assertEquals(1, repository.createTicketCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `search query filters tickets by subject id and category`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeTicketRepository(
                ticketsFlow = MutableStateFlow(
                    listOf(
                        sampleTicket(ticketId = "4025", subject = "Request new monitor for design station", category = "IT", status = TICKET_STATUS_OPEN),
                        sampleTicket(ticketId = "4019", subject = "VPN access not working on macOS Sonoma", category = "IT", status = "IN_PROGRESS"),
                        sampleTicket(ticketId = "4012", subject = "Unlock conference room calendar", category = "HR", status = "RESOLVED"),
                    )
                )
            )
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.start("employee-1")
            advanceUntilIdle()
            viewModel.onTicketSearchChanged("vpn")

            val state = viewModel.uiState.value
            assertEquals("vpn", state.ticketSearchQuery)
            assertEquals(1, state.visibleTickets.size)
            assertEquals("4019", state.visibleTickets.single().ticketId)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `status tab filters visible tickets`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeTicketRepository(
                ticketsFlow = MutableStateFlow(
                    listOf(
                        sampleTicket(ticketId = "4025", status = TICKET_STATUS_OPEN),
                        sampleTicket(ticketId = "4019", status = "IN_PROGRESS"),
                        sampleTicket(ticketId = "4012", status = "RESOLVED"),
                    )
                )
            )
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.start("employee-1")
            advanceUntilIdle()
            viewModel.onTicketStatusFilterSelected(TicketStatusFilter.RESOLVED)

            val state = viewModel.uiState.value
            assertEquals(TicketStatusFilter.RESOLVED, state.selectedTicketStatusFilter)
            assertEquals(listOf("4012"), state.visibleTickets.map(Ticket::ticketId))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `load ticket detail exposes selected ticket and assignment info`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val detailedTicket = sampleTicket(
                ticketId = "4019",
                subject = "VPN access not working on macOS Sonoma",
                category = "IT Support",
                status = "IN_PROGRESS",
            ).copy(
                description = "Unable to connect after upgrade.",
                assignedAgentId = "agent-7",
                assignedAgentName = "Alex Rivera"
            )
            val repository = FakeTicketRepository(ticketDetail = detailedTicket)
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.loadTicketDetail("4019")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingTicketDetail)
            assertEquals(detailedTicket, state.selectedTicketDetail)
            assertEquals("Alex Rivera", state.selectedTicketDetail?.assignedAgentName)
            assertEquals(1, repository.getTicketByIdCalls)
            assertEquals("4019", repository.lastRequestedTicketId)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `load ticket detail shows fallback state when no agent is assigned`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val detailedTicket = sampleTicket(ticketId = "4025").copy(
                assignedAgentId = null,
                assignedAgentName = null
            )
            val repository = FakeTicketRepository(ticketDetail = detailedTicket)
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.loadTicketDetail("4025")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(detailedTicket, state.selectedTicketDetail)
            assertTrue(state.selectedTicketDetail?.assignedAgentName.isNullOrBlank())
            assertEquals(null, state.ticketDetailError)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `load ticket detail exposes repository failure`() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = FakeTicketRepository(
                ticketDetailResult = Result.failure(IllegalStateException("Ticket not found"))
            )
            val viewModel = TicketViewModel(repository) { block -> runSuspend(block) }

            viewModel.loadTicketDetail("missing-ticket")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingTicketDetail)
            assertEquals(null, state.selectedTicketDetail)
            assertEquals("Ticket not found", state.ticketDetailError)
            assertEquals(1, repository.getTicketByIdCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `display ticket id returns first four characters when id is long`() {
        val ticket = sampleTicket(ticketId = "ABCDEF123456")

        assertEquals("ABCD", ticket.displayTicketId)
    }

    @Test
    fun `display ticket id keeps shorter ids unchanged`() {
        val ticket = sampleTicket(ticketId = "4012")

        assertEquals("4012", ticket.displayTicketId)
    }

    @Test
    fun `priority cap style maps low medium high and critical to expected tones`() {
        assertEquals(TicketPriorityTone.LOW, ticketPriorityTone("low"))
        assertEquals(TicketPriorityTone.MEDIUM, ticketPriorityTone("Medium"))
        assertEquals(TicketPriorityTone.HIGH, ticketPriorityTone("HIGH"))
        assertEquals(TicketPriorityTone.CRITICAL, ticketPriorityTone("critical"))
    }

    @Test
    fun `status pill label maps open in progress and resolved correctly`() {
        assertEquals("OPEN", ticketStatusLabel(TICKET_STATUS_OPEN))
        assertEquals("IN PROGRESS", ticketStatusLabel("IN_PROGRESS"))
        assertEquals("RESOLVED", ticketStatusLabel("resolved"))
    }

    @Test
    fun `status pill tone maps open in progress and resolved correctly`() {
        assertEquals(TicketStatusTone.OPEN, ticketStatusTone(TICKET_STATUS_OPEN))
        assertEquals(TicketStatusTone.IN_PROGRESS, ticketStatusTone("IN_PROGRESS"))
        assertEquals(TicketStatusTone.RESOLVED, ticketStatusTone("resolved"))
    }

    private fun sampleUser(): User = User(
        uid = "employee-1",
        fullName = "Sarah Johnson",
        email = "sarah@example.com",
        workspaceName = "Acme",
        status = "active",
        requestedRole = "employee",
        approvedRole = "employee"
    )

    private fun sampleTicket(
        ticketId: String = "4025",
        subject: String = "Request new monitor for design station",
        category: String = "IT",
        status: String = TICKET_STATUS_OPEN,
    ): Ticket = Ticket(
        ticketId = ticketId,
        employeeUid = "employee-1",
        employeeName = "Sarah Johnson",
        employeeEmail = "sarah@example.com",
        workspaceName = "Acme",
        subject = subject,
        description = "Sample description",
        category = category,
        priority = "High",
        status = status,
    )
}

private fun runSuspend(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking {
    block()
}

private class FakeTicketRepository(
    private val createTicketResult: Result<Unit> = Result.success(Unit),
    private val currentUser: User? = null,
    private val ticketsFlow: Flow<List<Ticket>> = flowOf(emptyList()),
    private val ticketDetail: Ticket? = null,
    private val ticketDetailResult: Result<Ticket> = ticketDetail?.let { Result.success(it) }
        ?: Result.failure(IllegalStateException("Ticket not found")),
) : TicketRepository {

    var createTicketCalls: Int = 0
    var lastDraft: TicketDraft? = null
    var getTicketByIdCalls: Int = 0
    var lastRequestedTicketId: String? = null

    override suspend fun getCurrentUserProfile(): User? = currentUser

    override suspend fun createTicket(draft: TicketDraft): Result<Unit> {
        createTicketCalls += 1
        lastDraft = draft
        return createTicketResult
    }

    override fun observeEmployeeTickets(employeeUid: String): Flow<List<Ticket>> = ticketsFlow

    override fun observeAllTickets(): Flow<List<Ticket>> = ticketsFlow

    override suspend fun getTicketById(ticketId: String): Result<Ticket> {
        getTicketByIdCalls += 1
        lastRequestedTicketId = ticketId
        return ticketDetailResult
    }

    override suspend fun getWorkspaceUsers(workspaceName: String): Result<List<User>> =
        Result.failure(UnsupportedOperationException("Not used in test"))

    override suspend fun assignTicket(ticketId: String, assignment: TicketAssignmentUpdate): Result<Unit> =
        Result.failure(UnsupportedOperationException("Not used in test"))
}
