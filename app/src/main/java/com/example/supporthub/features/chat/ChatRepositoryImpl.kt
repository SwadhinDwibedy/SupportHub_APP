package com.example.supporthub.features.chat

import com.example.supporthub.core.firebase.ChatCollections
import com.example.supporthub.core.firebase.FirestoreManager
import com.example.supporthub.features.authentication.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore = FirestoreManager.firestore,
) : ChatRepository {

    override suspend fun createConversation(employeeUid: String, agentUid: String): Result<Conversation> {
        return Result.failure(UnsupportedOperationException("Chat backend is not implemented yet."))
    }

    override fun getConversations(userUid: String): Flow<List<Conversation>> = emptyFlow()

    override fun getMessages(conversationId: String): Flow<List<ChatMessage>> = emptyFlow()

    override suspend fun sendMessage(conversationId: String, message: ChatMessage): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Chat backend is not implemented yet."))
    }

    override suspend fun markMessagesAsRead(conversationId: String, userUid: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Chat backend is not implemented yet."))
    }

    override suspend fun updateLastMessage(conversationId: String, lastMessage: String, lastSenderUid: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Chat backend is not implemented yet."))
    }

    override suspend fun getCurrentUserProfile(): User? = null
}
