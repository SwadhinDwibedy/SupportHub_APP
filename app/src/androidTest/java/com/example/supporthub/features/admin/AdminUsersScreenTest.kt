package com.example.supporthub.features.admin

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminUsersScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun usersContent_exposesScrollableWhiteCardLayoutWithSeparateOverviewPendingAndDirectoryPanels() {
        composeTestRule.setContent {
            AdminUsersScreenContent(
                uiState = AdminApprovalsUiState(
                    pendingUsers = listOf(
                        sampleUser(
                            uid = "pending-1",
                            fullName = "Sarah Klein",
                            email = "sarah.klein@example.com",
                            department = "Engineering",
                            requestedRole = Role.ADMIN.value,
                            status = "pending"
                        ),
                        sampleUser(
                            uid = "approved-1",
                            fullName = "Alex Rivera",
                            email = "alex.rivera@example.com",
                            department = "Support",
                            requestedRole = Role.AGENT.value,
                            status = "active",
                            approvedRole = Role.AGENT.value
                        ),
                        sampleUser(
                            uid = "approved-2",
                            fullName = "James Liu",
                            email = "james.liu@example.com",
                            department = "Engineering",
                            requestedRole = Role.EMPLOYEE.value,
                            status = "active",
                            approvedRole = Role.EMPLOYEE.value
                        )
                    )
                ),
                onRefresh = {},
                onApprove = { _: String, _: Role -> },
                onKeepPending = { _: String -> }
            )
        }

        composeTestRule.onNodeWithText("Workspace users").assertIsDisplayed()
        composeTestRule.onNodeWithText("A brighter admin surface with scrollable cards, soft whites, and clear access actions.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_overview_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_pending_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_pending_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_search_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_filter_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_filter_all_users").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_filter_admins").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_filter_agents").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_filter_employees").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_directory_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_directory_card_approved-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_users_directory_card_approved-2").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("admin_users_edit_action").assertCountEquals(0)
    }

    private fun sampleUser(
        uid: String,
        fullName: String,
        email: String,
        department: String,
        requestedRole: String,
        status: String,
        approvedRole: String? = null
    ): User = User(
        uid = uid,
        fullName = fullName,
        email = email,
        department = department,
        requestedRole = requestedRole,
        approvedRole = approvedRole,
        status = status,
        workspaceName = "SupportHub"
    )
}
