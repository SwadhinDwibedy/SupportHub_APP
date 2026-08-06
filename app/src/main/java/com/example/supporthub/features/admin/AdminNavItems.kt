package com.example.supporthub.features.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.SupervisorAccount
import com.example.supporthub.features.dashboard.components.BottomNavItem

object AdminNavRoutes {
    const val Home = "admin_home"
    const val Users = "admin_users"
    const val Tickets = "admin_tickets"
    const val Analytics = "admin_analytics"
    const val Profile = "admin_profile"
}

val adminBottomNavItems = listOf(
    BottomNavItem(label = "Home", icon = Icons.Outlined.Home, route = AdminNavRoutes.Home),
    BottomNavItem(label = "Users", icon = Icons.Outlined.SupervisorAccount, route = AdminNavRoutes.Users),
    BottomNavItem(label = "Tickets", icon = Icons.Outlined.ConfirmationNumber, route = AdminNavRoutes.Tickets),
//    BottomNavItem(label = "Analytics", icon = Icons.Outlined.Analytics, route = AdminNavRoutes.Analytics),
    BottomNavItem(label = "Profile", icon = Icons.Outlined.PersonOutline, route = AdminNavRoutes.Profile)
)
