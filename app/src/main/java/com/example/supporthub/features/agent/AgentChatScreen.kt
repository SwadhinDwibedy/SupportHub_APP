package com.example.supporthub.features.agent

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.supporthub.features.chat.Conversation
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground
import com.example.supporthub.ui.theme.Emerald50
import com.example.supporthub.ui.theme.Emerald500
import com.example.supporthub.ui.theme.Emerald700
import com.example.supporthub.ui.theme.Gray100
import com.example.supporthub.ui.theme.Gray200
import com.example.supporthub.ui.theme.Gray300
import com.example.supporthub.ui.theme.Gray400
import com.example.supporthub.ui.theme.Gray500
import com.example.supporthub.ui.theme.Gray600
import com.example.supporthub.ui.theme.Gray900
import com.example.supporthub.ui.theme.White
import kotlin.math.min

private val AgentChatBorder = Color(0xFFE3ECF3)
private val AgentOnline = Emerald500
private val AgentOnlineMuted = Gray300

@Composable
fun AgentChatScreen() {
    val queryState = remember { androidx.compose.runtime.mutableStateOf("") }
    val allConversations = remember { emptyList<Conversation>() }
    val conversations = remember(queryState.value, allConversations) {
        if (queryState.value.isBlank()) {
            allConversations
        } else {
            allConversations.filter { conversation ->
                conversation.matchesAgentSearch(queryState.value)
            }
        }
    }

    SupportHubPremiumBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChatListHeader(
                title = "Chats",
                subtitle = "Manage employee conversations",
                showRefresh = true,
                onRefresh = { }
            )

            SearchBar(
                placeholder = "Search conversations",
                value = queryState.value,
                onValueChange = { queryState.value = it }
            )

            when {
                false -> LoadingPlaceholder()
                conversations.isEmpty() -> AgentEmptyState()
                else -> ConversationList(conversations = conversations, onConversationClick = { })
            }
        }
    }
}

@Composable
private fun ChatListHeader(
    title: String,
    subtitle: String,
    showRefresh: Boolean,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Gray900)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Gray600)
            }
            if (showRefresh) {
                Surface(shape = CircleShape, color = White, border = BorderStroke(1.dp, AgentChatBorder)) {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh chats", tint = Emerald700)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder, color = Gray500) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = Gray500) },
        textStyle = androidx.compose.ui.text.TextStyle(color = Gray900),
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun ConversationList(
    conversations: List<Conversation>,
    onConversationClick: (Conversation) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(conversations) { conversation ->
            AgentConversationCard(conversation = conversation, onClick = { onConversationClick(conversation) })
        }
    }
}

@Composable
private fun AgentConversationCard(
    conversation: Conversation,
    onClick: () -> Unit,
) {
    val unreadCount = conversation.unreadCountAgent
    val cardAlpha by animateFloatAsState(targetValue = 1f, animationSpec = tween(220), label = "agentChatCard")

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            ConversationAvatar(label = conversation.participantNames.values.firstOrNull().orEmpty())
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.participantNames.values.firstOrNull().orEmpty().ifBlank { "Employee" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900,
                        modifier = Modifier.weight(1f)
                    )
                    if (unreadCount > 0) {
                        UnreadBadge(count = unreadCount)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoleBadge(label = conversation.participantRoles.values.firstOrNull().orEmpty().ifBlank { "Employee" }, backgroundColor = Emerald50, textColor = Emerald700)
                    OnlineIndicator(online = false)
                }

                Text(
                    text = conversation.lastMessage.ifBlank { "Employee conversations will appear here." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.lastSenderUid.takeIf { it.isNotBlank() }?.let { "Last sender: $it" } ?: "Waiting for activity",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gray400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = conversation.lastMessageTime.toUiTime(), style = MaterialTheme.typography.labelMedium, color = Gray500)
                }
            }
        }
    }
}

@Composable
private fun AgentEmptyState() {
    EmptyStateCard(
        title = "No conversations available",
        subtitle = "Employee conversations will appear here when available.",
        icon = Icons.Outlined.SupportAgent
    )
}

@Composable
private fun LoadingPlaceholder() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Gray100) { Box(modifier = Modifier.size(54.dp)) }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Gray100) { Box(modifier = Modifier.fillMaxWidth().height(16.dp)) }
                        Surface(shape = RoundedCornerShape(8.dp), color = Gray100) { Box(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp)) }
                    }
                }
                if (it < 2) Divider(color = Gray200)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = CircleShape, color = Emerald50) {
                Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Emerald700, modifier = Modifier.size(36.dp))
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = Gray900, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Gray600)
        }
    }
}

@Composable
private fun ConversationAvatar(label: String) {
    Surface(shape = CircleShape, color = Emerald50, border = BorderStroke(1.dp, AgentChatBorder)) {
        Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
            Text(text = label.take(1).uppercase().ifBlank { "E" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Emerald700)
        }
    }
}

@Composable
private fun RoleBadge(
    label: String,
    backgroundColor: Color,
    textColor: Color,
) {
    Surface(shape = RoundedCornerShape(999.dp), color = backgroundColor) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun OnlineIndicator(online: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color = if (online) AgentOnline else AgentOnlineMuted, shape = CircleShape))
        Text(text = if (online) "Online" else "Offline", style = MaterialTheme.typography.labelSmall, color = Gray400)
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Surface(shape = RoundedCornerShape(999.dp), color = Emerald700) {
        Text(text = min(count, 99).toString(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = White)
    }
}

private fun Conversation.matchesAgentSearch(query: String): Boolean {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isBlank()) return true
    return participantNames.values.any { it.lowercase().contains(normalizedQuery) } ||
        participantRoles.values.any { it.lowercase().contains(normalizedQuery) } ||
        lastMessage.lowercase().contains(normalizedQuery)
}

private fun Any?.toUiTime(): String {
    return when (this) {
        null -> "Just now"
        is String -> if (isNotBlank()) this else "Just now"
        else -> "Just now"
    }
}
