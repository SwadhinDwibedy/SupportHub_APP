package com.example.supporthub.features.agent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgentProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_shows_agent_identity_summary_actions_and_logout() {
        composeTestRule.setContent {
            AgentProfileScreen()
        }

        composeTestRule.onNodeWithTag("agent_profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("AR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex Rivera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Senior Support Agent").assertIsDisplayed()
        composeTestRule.onNodeWithText("SupportHub North America").assertIsDisplayed()
        composeTestRule.onNodeWithText("LIVE").assertIsDisplayed()
        composeTestRule.onNodeWithText("24 resolved today").assertIsDisplayed()
        composeTestRule.onNodeWithText("8 open").assertIsDisplayed()
        composeTestRule.onNodeWithText("2m 14s avg response").assertIsDisplayed()
        composeTestRule.onNodeWithText("CONTACT").assertIsDisplayed()
        composeTestRule.onNodeWithText("WORKLOAD").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOG OUT").assertIsDisplayed()
    }
}
