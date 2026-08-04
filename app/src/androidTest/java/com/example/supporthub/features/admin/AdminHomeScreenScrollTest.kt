package com.example.supporthub.features.admin

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminHomeScreenScrollTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminHomeContent_exposesScrollActionForOverflowingDashboard() {
        composeTestRule.setContent {
            AdminHomeContent(
                headerState = AdminCommandCenterHeaderState(
                    dateLabel = "MONDAY, 21 JULY",
                    timeLabel = "09:00 AM",
                    title = "Command Center"
                ),
                metricsState = AdminDashboardMetricsState(
                    totalTicketsLabel = "120",
                    deltaLabel = "+12% vs last month",
                    openTicketsLabel = "18 open"
                ),
                organizationHealthState = OrganizationHealthState(
                    scoreLabel = "94",
                    summaryLabel = "Excellent • Uptime 99.98%",
                    totalUsersLabel = "342",
                    departmentsLabel = "8"
                ),
                todayTicketsState = TodayTicketsState(
                    createdLabel = "48",
                    resolvedLabel = "42",
                    pendingLabel = "6"
                ),
                recentUsersState = RecentUsersState(
                    items = listOf(
                        RecentUserItem(profileName = "Sarah Khan", dateLabel = "MONDAY, 21 JULY")
                    )
                ),
                recentTicketsState = RecentTicketsState(
                    items = listOf(
                        RecentTicketItem(
                            name = "Email not working",
                            documentId = "ABCD",
                            createdDateLabel = "MONDAY, 21 JULY",
                            statusLabel = "OPEN"
                        )
                    )
                )
            )
        }

        composeTestRule.onNodeWithTag("admin_home_scroll_container").assert(hasScrollAction())
    }
}
