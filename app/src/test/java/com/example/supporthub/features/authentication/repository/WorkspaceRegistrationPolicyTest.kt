package com.example.supporthub.features.authentication.repository

import com.example.supporthub.features.authentication.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceRegistrationPolicyTest {

    @Test
    fun `normalizeWorkspaceKey trims surrounding whitespace`() {
        assertEquals("acme", WorkspaceRegistrationPolicy.normalizeWorkspaceKey("  Acme  "))
    }

    @Test
    fun `normalizeWorkspaceKey collapses internal whitespace to avoid duplicate workspace documents`() {
        assertEquals("acme hq", WorkspaceRegistrationPolicy.normalizeWorkspaceKey("Acme   HQ"))
    }

    @Test
    fun `normalizeWorkspaceKey lowercases workspace names so lookup and creation share one document id`() {
        assertEquals("acmehq", WorkspaceRegistrationPolicy.normalizeWorkspaceKey("AcmeHQ"))
        assertEquals("acmehq", WorkspaceRegistrationPolicy.normalizeWorkspaceKey("acmehq"))
        assertEquals(
            WorkspaceRegistrationPolicy.normalizeWorkspaceKey(" Workspace A "),
            WorkspaceRegistrationPolicy.normalizeWorkspaceKey("workspace a")
        )
    }

    @Test
    fun `normalizeWorkspaceDisplayName preserves original casing while normalizing whitespace`() {
        assertEquals("Acme HQ", WorkspaceRegistrationPolicy.normalizeWorkspaceDisplayName("  Acme   HQ  "))
    }

    @Test
    fun `employee registration always remains pending non owner`() {
        val decision = WorkspaceRegistrationPolicy.resolveRegistration(requestedRole = Role.EMPLOYEE)

        assertNull(decision.approvedRole)
        assertEquals("pending", decision.status)
        assertFalse(decision.isWorkspaceOwner)
    }

    @Test
    fun `agent registration always remains pending non owner`() {
        val decision = WorkspaceRegistrationPolicy.resolveRegistration(requestedRole = Role.AGENT)

        assertNull(decision.approvedRole)
        assertEquals("pending", decision.status)
        assertFalse(decision.isWorkspaceOwner)
    }

    @Test
    fun `admin request registration always remains pending non owner`() {
        val decision = WorkspaceRegistrationPolicy.resolveRegistration(requestedRole = Role.ADMIN)

        assertNull(decision.approvedRole)
        assertEquals("pending", decision.status)
        assertFalse(decision.isWorkspaceOwner)
    }

    @Test
    fun `workspace existence no longer changes registration decision`() {
        val employeeDecision = WorkspaceRegistrationPolicy.resolveRegistration(requestedRole = Role.EMPLOYEE)
        val agentDecision = WorkspaceRegistrationPolicy.resolveRegistration(requestedRole = Role.AGENT)
        val adminDecision = WorkspaceRegistrationPolicy.resolveRegistration(requestedRole = Role.ADMIN)

        assertNull(employeeDecision.approvedRole)
        assertEquals("pending", employeeDecision.status)
        assertFalse(employeeDecision.isWorkspaceOwner)

        assertNull(agentDecision.approvedRole)
        assertEquals("pending", agentDecision.status)
        assertFalse(agentDecision.isWorkspaceOwner)

        assertNull(adminDecision.approvedRole)
        assertEquals("pending", adminDecision.status)
        assertFalse(adminDecision.isWorkspaceOwner)
    }
}
