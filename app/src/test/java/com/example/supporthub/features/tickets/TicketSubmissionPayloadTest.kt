package com.example.supporthub.features.tickets

import com.example.supporthub.features.authentication.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TicketSubmissionPayloadTest {

    @Test
    fun `draft maps to firestore payload using logged in user profile`() {
        val draft = TicketDraft(
            subject = "Payroll issue",
            description = "Cannot access payroll portal.",
            category = "Access",
            priority = "High"
        )
        val user = User(
            uid = "employee-1",
            fullName = "Sarah Johnson",
            email = "sarah@example.com",
            workspaceName = "Acme",
            status = "active",
            requestedRole = "employee",
            approvedRole = "employee"
        )

        val payload = draft.toSubmissionPayload(user)

        assertEquals(
            TicketSubmissionPayload(
                employeeUid = "employee-1",
                employeeName = "Sarah Johnson",
                employeeEmail = "sarah@example.com",
                workspaceName = "Acme",
                subject = "Payroll issue",
                description = "Cannot access payroll portal.",
                category = "Access",
                priority = "High"
            ),
            payload
        )
    }

    @Test
    fun `firestore create payload contains exact requested ticket fields`() {
        val payload = TicketSubmissionPayload(
            employeeUid = "employee-1",
            employeeName = "Sarah Johnson",
            employeeEmail = "sarah@example.com",
            workspaceName = "Acme",
            subject = "Payroll issue",
            description = "Cannot access payroll portal.",
            category = "Access",
            priority = "High"
        )

        val data = buildCreateTicketData(
            ticketId = "ticket-123",
            payload = payload,
            timestamp = "server-ts"
        )

        assertEquals("ticket-123", data["ticketId"])
        assertEquals("employee-1", data["employeeUid"])
        assertEquals("Sarah Johnson", data["employeeName"])
        assertEquals("sarah@example.com", data["employeeEmail"])
        assertEquals("Acme", data["workspaceName"])
        assertEquals("Payroll issue", data["subject"])
        assertEquals("Cannot access payroll portal.", data["description"])
        assertEquals("Access", data["category"])
        assertEquals("High", data["priority"])
        assertEquals(TICKET_STATUS_OPEN, data["status"])
        assertNull(data["assignedAgentId"])
        assertNull(data["assignedAgentName"])
        assertNull(data["assignedAgentEmail"])
        assertNull(data["assignedAgentRole"])
        assertNull(data["assignedTimestamp"])
        assertNull(data["assignedByAdmin"])
        assertNull(data["assignmentStatus"])
        assertEquals("server-ts", data["createdAt"])
        assertEquals("server-ts", data["updatedAt"])
        assertNull(data["resolvedAt"])
        assertEquals(
            setOf(
                "ticketId",
                "employeeUid",
                "employeeName",
                "employeeEmail",
                "workspaceName",
                "subject",
                "description",
                "category",
                "priority",
                "status",
                "assignedAgentId",
                "assignedAgentName",
                "assignedAgentEmail",
                "assignedAgentRole",
                "assignedAgentDepartment",
                "assignedTimestamp",
                "assignedByAdmin",
                "assignmentStatus",
                "createdAt",
                "updatedAt",
                "resolvedAt"
            ),
            data.keys
        )
    }
}
