package com.example.supporthub.features.authentication

import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.utils.StartupDestinationResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class StartupDestinationResolverTest {

    @Test
    fun `returns login when there is no authenticated firebase user`() {
        val destination = StartupDestinationResolver.resolve(user = null)

        assertEquals(StartupDestination.Login, destination)
    }

    @Test
    fun `returns complete profile when firestore user document is missing`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "user@example.com",
                authProvider = "google",
                workspaceName = ""
            )
        )

        assertEquals(StartupDestination.CompleteProfile, destination)
    }

    @Test
    fun `returns complete profile when required profile fields are blank`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "user@example.com",
                fullName = "",
                workspaceName = "Acme",
                status = "pending",
                requestedRole = ""
            )
        )

        assertEquals(StartupDestination.CompleteProfile, destination)
    }

    @Test
    fun `returns pending approval for pending users with completed profile`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "user@example.com",
                fullName = "Pending User",
                workspaceName = "Acme",
                status = "pending",
                requestedRole = "employee"
            )
        )

        assertEquals(StartupDestination.PendingApproval, destination)
    }

    @Test
    fun `returns employee dashboard for active employees`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "employee@example.com",
                fullName = "Employee",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "employee",
                approvedRole = "employee"
            )
        )

        assertEquals(StartupDestination.EmployeeDashboard, destination)
    }

    @Test
    fun `returns agent dashboard for active agents`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "agent@example.com",
                fullName = "Agent",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "agent",
                approvedRole = "agent"
            )
        )

        assertEquals(StartupDestination.AgentDashboard, destination)
    }

    @Test
    fun `returns admin dashboard for active admins`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "admin@example.com",
                fullName = "Admin",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "admin",
                approvedRole = "admin"
            )
        )

        assertEquals(StartupDestination.AdminDashboard, destination)
    }

    @Test
    fun `routes active users using requested role when approved role is missing`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "broken@example.com",
                fullName = "Broken User",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "employee",
                approvedRole = null
            )
        )

        assertEquals(StartupDestination.EmployeeDashboard, destination)
    }

    @Test
    fun `does not use requested role when approved role is employee but user status is pending`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "pending-employee@example.com",
                fullName = "Pending Employee",
                workspaceName = "Acme",
                status = "pending",
                requestedRole = "employee",
                approvedRole = "employee"
            )
        )

        assertEquals(StartupDestination.PendingApproval, destination)
    }

    @Test
    fun `routes active employee with approved employee role to employee dashboard`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "employee@example.com",
                fullName = "Employee",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "employee",
                approvedRole = "employee"
            )
        )

        assertEquals(StartupDestination.EmployeeDashboard, destination)
    }

    @Test
    fun `routes active agent with approved agent role to agent dashboard`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "agent@example.com",
                fullName = "Agent",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "agent",
                approvedRole = "agent"
            )
        )

        assertEquals(StartupDestination.AgentDashboard, destination)
    }

    @Test
    fun `routes active admin with approved admin role to admin dashboard`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "admin@example.com",
                fullName = "Admin",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "admin",
                approvedRole = "admin"
            )
        )

        assertEquals(StartupDestination.AdminDashboard, destination)
    }

    @Test
    fun `does not allow workspace owner flag to override approved employee role`() {
        val destination = StartupDestinationResolver.resolve(
            user = User(
                uid = "uid",
                email = "owner@example.com",
                fullName = "Owner",
                workspaceName = "Acme",
                status = "active",
                requestedRole = "employee",
                approvedRole = "employee",
                isWorkspaceOwner = true,
                role = "admin"
            )
        )

        assertEquals(StartupDestination.EmployeeDashboard, destination)
    }
}
