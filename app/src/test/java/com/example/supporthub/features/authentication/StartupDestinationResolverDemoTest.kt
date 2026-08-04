package com.example.supporthub.features.authentication

import com.example.supporthub.core.demo.DemoDataProvider
import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.utils.StartupDestinationResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class StartupDestinationResolverDemoTest {

    @Test
    fun `resolves demo employee to employee dashboard`() {
        val destination = StartupDestinationResolver.resolve(DemoDataProvider.demoEmployeeUser)

        assertEquals(StartupDestination.EmployeeDashboard, destination)
    }

    @Test
    fun `resolves demo agent to agent dashboard`() {
        val destination = StartupDestinationResolver.resolve(DemoDataProvider.demoAgentUser)

        assertEquals(StartupDestination.AgentDashboard, destination)
    }

    @Test
    fun `resolves demo admin to admin dashboard`() {
        val destination = StartupDestinationResolver.resolve(DemoDataProvider.demoAdminUser)

        assertEquals(StartupDestination.AdminDashboard, destination)
    }
}
