package com.example.supporthub.features.agent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Timeline
import com.example.supporthub.features.dashboard.components.BottomNavItem

object AgentNavRoutes {
    const val Home = "agent_home"
    const val Queue = "agent_queue"
    const val Chat = "agent_chat"
    const val Performance = "agent_performance"

    const val Profile = "agent_profile"
}

val agentBottomNavItems = listOf(
    BottomNavItem(label = "Home", icon = Icons.Outlined.Home, route = AgentNavRoutes.Home),
    BottomNavItem(label = "Queue", icon = Icons.Outlined.SupportAgent, route = AgentNavRoutes.Queue),
//    BottomNavItem(label = "Chat", icon = Icons.Outlined.ChatBubbleOutline, route = AgentNavRoutes.Chat),
    BottomNavItem(label = "Perf", icon = Icons.Outlined.Timeline, route = AgentNavRoutes.Performance),
    BottomNavItem(label = "Profile", icon = Icons.Outlined.PersonOutline, route = AgentNavRoutes.Profile)
)
