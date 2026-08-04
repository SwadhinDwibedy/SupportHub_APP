package com.example.supporthub.features.chat

import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.model.User

data class Conversation(
    val conversationId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantRoles: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Any? = null,
    val lastSenderUid: String = "",
    val unreadCountEmployee: Int = 0,
    val unreadCountAgent: Int = 0,
    val createdAt: Any? = null,
    val updatedAt: Any? = null,
)

data class ChatMessage(
    val messageId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderRole: String = "",
    val message: String = "",
    val messageType: String = ChatMessageType.TEXT.value,
    val timestamp: Any? = null,
    val read: Boolean = false,
)

enum class ChatMessageType(val value: String) {
    TEXT("TEXT")
}

fun User.chatDisplayName(): String = fullName.ifBlank { email }

fun Role.chatValue(): String = value
