package com.example.supporthub.features.admin

import androidx.compose.ui.graphics.Color
import com.example.supporthub.ui.theme.SupportHubFontFamilies
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AdminHomeHeaderFormatterTest {

    private val zoneId = ZoneId.of("Asia/Calcutta")

    @Test
    fun `build returns uppercase day date and time labels for command center header`() {
        val dateTime = ZonedDateTime.of(2026, 8, 13, 9, 30, 0, 0, zoneId)

        val state = buildAdminCommandCenterHeaderState(dateTime)

        assertEquals("THURSDAY, 13 AUGUST", state.dateLabel)
        assertEquals("09:30 AM", state.timeLabel)
        assertEquals("Command Center", state.title)
    }

    @Test
    fun `build header separates date and time for requested admin command center layout`() {
        val dateTime = ZonedDateTime.of(2026, 8, 15, 10, 5, 0, 0, zoneId)

        val state = buildAdminCommandCenterHeaderState(dateTime)

        assertEquals("SATURDAY, 15 AUGUST", state.dateLabel)
        assertEquals("10:05 AM", state.timeLabel)
        assertEquals("Command Center", state.title)
    }

    @Test
    fun `support hub font families expose poppins for admin header`() {
        assertEquals(SupportHubFontFamilies.poppins, adminCommandCenterFontFamily())
    }

    @Test
    fun `dashboard card layout tokens match refined design`() {
        val spec = adminDashboardCardSpec()

        assertEquals(28, spec.cornerRadius.value.toInt())
        assertEquals(Color(0xFF0F766E), spec.containerColor)
        assertEquals(22, spec.horizontalPadding.value.toInt())
        assertEquals(24, spec.verticalPadding.value.toInt())
        assertEquals(120, spec.heroCircleSize.value.toInt())
        assertEquals(52, spec.secondaryCircleSize.value.toInt())
    }
}
