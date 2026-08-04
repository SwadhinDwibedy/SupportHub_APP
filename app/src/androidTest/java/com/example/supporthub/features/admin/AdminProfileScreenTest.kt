package com.example.supporthub.features.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_matchesCompactCardLayout_and_reactsToRealtimeProfileUpdates() {
        val repository = FakeRealtimeAdminProfileRepository(initialState = sampleProfile())

        composeTestRule.setContent {
            AdminProfileScreen(
                repository = repository,
                onNavigateBack = {},
                onLoggedOut = {}
            )
        }

        composeTestRule.onNodeWithTag("admin_profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("AR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex Rivera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Senior IT Support Specialist").assertIsDisplayed()
        composeTestRule.onNodeWithText("IT Support · Network Team").assertIsDisplayed()
        composeTestRule.onNodeWithText("Online").assertIsDisplayed()
        composeTestRule.onNodeWithText("1,240").assertIsDisplayed()
        composeTestRule.onNodeWithText("RESOLVED").assertIsDisplayed()
        composeTestRule.onNodeWithText("4.9").assertIsDisplayed()
        composeTestRule.onNodeWithText("CSAT").assertIsDisplayed()
        composeTestRule.onNodeWithText("2m").assertIsDisplayed()
        composeTestRule.onNodeWithText("RESPONSE").assertIsDisplayed()
        composeTestRule.onNodeWithText("THIS WEEK").assertIsDisplayed()
        composeTestRule.onNodeWithText("On Shift").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tuesday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wednesday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Thursday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Friday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network").assertIsDisplayed()
        composeTestRule.onNodeWithText("Software").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hardware").assertIsDisplayed()
        composeTestRule.onNodeWithText("Onboarding").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log out").assertIsDisplayed()

        repository.emit(
            sampleProfile(
                fullName = "Ari Rivera",
                workspaceName = "IT Support · Network Team",
                status = "active"
            )
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ari Rivera").assertIsDisplayed()
    }

    private fun sampleProfile(
        fullName: String = "Alex Rivera",
        workspaceName: String = "IT Support · Network Team",
        status: String = "active"
    ): AdminProfileData {
        val user = User(
            uid = "admin-1",
            fullName = fullName,
            email = "alex.rivera@supporthub.io",
            phone = "+1 512-555-0147",
            location = "Austin, TX",
            jobTitle = "Senior IT Support Specialist",
            department = "IT Support",
            workspaceName = workspaceName,
            requestedRole = Role.ADMIN.value,
            approvedRole = Role.ADMIN.value,
            status = status,
            authProvider = "google"
        )

        return AdminProfileData(
            user = user,
            summary = AdminProfileSummary(
                totalUsers = 1240,
                totalTickets = 2400,
                pendingApprovals = 18,
                activeAgents = 64
            )
        )
    }
}

private class FakeRealtimeAdminProfileRepository(
    initialState: AdminProfileData
) : AdminProfileRepository {

    private val profileState = MutableStateFlow<AdminProfileData?>(initialState)

    fun emit(state: AdminProfileData) {
        profileState.value = state
    }

    override fun observeProfile() = profileState

    override suspend fun updateProfile(update: AdminProfileUpdate) {
        val current = profileState.value ?: return

        profileState.value = current.copy(
            user = current.user.copy(
                fullName = update.fullName,
                phone = update.phone
            )
        )
    }

    override suspend fun logout() {
        error("logout should not be called in this test")
    }
}
