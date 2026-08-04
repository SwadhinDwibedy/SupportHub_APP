package com.example.supporthub.features.chat

import com.example.supporthub.features.authentication.model.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun createConversation(employeeUid: String, agentUid: String): Result<Conversation>
    fun getConversations(userUid: String): Flow<List<Conversation>>
    fun getMessages(conversationId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(conversationId: String, message: ChatMessage): Result<Unit>
    suspend fun markMessagesAsRead(conversationId: String, userUid: String): Result<Unit>
    suspend fun updateLastMessage(conversationId: String, lastMessage: String, lastSenderUid: String): Result<Unit>
    suspend fun getCurrentUserProfile(): User?
}
