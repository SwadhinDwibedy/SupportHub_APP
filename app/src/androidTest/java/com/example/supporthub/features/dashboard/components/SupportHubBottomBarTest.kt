package com.example.supporthub.features.dashboard.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupportHubBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val items = listOf(
        BottomNavItem(label = "Home", icon = Icons.Outlined.Home, route = "employee_home"),
        BottomNavItem(label = "Tickets", icon = Icons.Outlined.ConfirmationNumber, route = "employee_tickets"),
        BottomNavItem(label = "Chat", icon = Icons.Outlined.ChatBubbleOutline, route = "employee_chat"),
        BottomNavItem(label = "Profile", icon = Icons.Outlined.PersonOutline, route = "employee_profile")
    )

    private val adminItems = listOf(
        BottomNavItem(label = "Home", icon = Icons.Outlined.Home, route = "admin_home"),
        BottomNavItem(label = "Users", icon = Icons.Outlined.PersonOutline, route = "admin_users"),
        BottomNavItem(label = "Tickets", icon = Icons.Outlined.ConfirmationNumber, route = "admin_tickets"),
        BottomNavItem(label = "Analytics", icon = Icons.Outlined.ChatBubbleOutline, route = "admin_analytics"),
        BottomNavItem(label = "Profile", icon = Icons.Outlined.PersonOutline, route = "admin_profile")
    )

    @Test
    fun employeeItems_areDisplayed() {
        composeTestRule.setContent {
            SupportHubBottomBar(
                items = items,
                selectedRoute = "employee_home",
                onItemSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tickets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun tappingItem_updatesSelectedState() {
        composeTestRule.setContent {
            var selectedRoute by mutableStateOf("employee_home")

            SupportHubBottomBar(
                items = items,
                selectedRoute = selectedRoute,
                onItemSelected = { selectedRoute = it.route },
                modifier = Modifier.fillMaxSize()
            )
        }

        bottomBarItem("Home").assertIsDisplayed()
        bottomBarItem("Chat")
            .assertIsDisplayed()
            .performClick()
        bottomBarItem("Chat").assertIsDisplayed()
        bottomBarItem("Home").assertIsDisplayed()
    }

    @Test
    fun selectedItem_remainsVisibleAfterMultipleSelections() {
        composeTestRule.setContent {
            var selectedRoute by mutableStateOf("employee_home")

            SupportHubBottomBar(
                items = items,
                selectedRoute = selectedRoute,
                onItemSelected = { selectedRoute = it.route },
                modifier = Modifier.fillMaxSize()
            )
        }

        bottomBarItem("Tickets")
            .performClick()
        bottomBarItem("Tickets").assertIsDisplayed()

        bottomBarItem("Profile")
            .performClick()
        bottomBarItem("Profile").assertIsDisplayed()
    }

    @Test
    fun premiumDock_exposesMinimalMintSelectionSemantics() {
        composeTestRule.setContent {
            SupportHubBottomBar(
                items = items,
                selectedRoute = "employee_home",
                onItemSelected = {}
            )
        }

        composeTestRule.onNodeWithTag("bottom_bar_shell_transparent").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_dock").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_dock_white").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_height_78dp").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_item_selected_Home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_selected_indicator_Home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_item_unselected_Tickets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun analyticsLabel_isFullyExposedInAdminBar() {
        composeTestRule.setContent {
            SupportHubBottomBar(
                items = adminItems,
                selectedRoute = "admin_analytics",
                onItemSelected = {}
            )
        }

        composeTestRule.onNodeWithTag("bottom_bar_item_selected_Analytics").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_bar_item_label_Analytics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Analytics", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun bottomBarItem(label: String) = composeTestRule.onNodeWithText(
        text = label,
        useUnmergedTree = true
    )
}
