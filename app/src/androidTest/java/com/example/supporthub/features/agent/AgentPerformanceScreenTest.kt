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
class AgentPerformanceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun performanceScreen_showsAnalyticsDashboardSections() {
        composeTestRule.setContent {
            AgentPerformanceScreen()
        }

        composeTestRule.onNodeWithTag("agent_performance_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Performance").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_tab_day").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_tab_week").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_tab_month").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_summary_resolved_value").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_summary_resolved_label").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_summary_csat_value").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_summary_csat_label").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_productivity_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_productivity_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_completion_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_completion_ring").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Contact").assertIsDisplayed()
        composeTestRule.onNodeWithText("SLA Met").assertIsDisplayed()
        composeTestRule.onNodeWithText("Escalation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_metric_avg_response").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_metric_handle_time").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_metric_tickets_hour").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_metric_reopen_rate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_leaderboard_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_performance_leaderboard_item_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex Rivera").assertIsDisplayed()
        composeTestRule.onNodeWithText("142").assertIsDisplayed()
        composeTestRule.onNodeWithText("LEADERBOARD — THIS WEEK").assertIsDisplayed()
    }
}
