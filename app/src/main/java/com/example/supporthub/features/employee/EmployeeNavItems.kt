package com.example.supporthub.features.employee

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import com.example.supporthub.features.dashboard.components.BottomNavItem

object EmployeeNavRoutes {
    const val Home = "employee_home"
    const val Tickets = "employee_tickets"
    const val TicketDetails = "employee_ticket_details"
    const val TicketIdArg = "ticketId"
    const val TicketDetailsRoute = "$TicketDetails/{$TicketIdArg}"
    const val Chat = "employee_chat"
    const val Profile = "employee_profile"

    fun ticketDetails(ticketId: String): String = "$TicketDetails/$ticketId"
}

val employeeBottomNavItems = listOf(
    BottomNavItem(label = "Home", icon = Icons.Outlined.Home, route = EmployeeNavRoutes.Home),
    BottomNavItem(label = "Tickets", icon = Icons.Outlined.ConfirmationNumber, route = EmployeeNavRoutes.Tickets),
    BottomNavItem(label = "Chat", icon = Icons.Outlined.ChatBubbleOutline, route = EmployeeNavRoutes.Chat),
    BottomNavItem(label = "Profile", icon = Icons.Outlined.PersonOutline, route = EmployeeNavRoutes.Profile)
)
