package com.example.supporthub.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val isLoadingConversations: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val selectedConversationId: String? = null,
    val errorMessage: String? = null,
)

class ChatViewModel(
    private val repository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadConversations(userUid: String) {
        _uiState.value = _uiState.value.copy(isLoadingConversations = true, errorMessage = null)
    }

    fun loadMessages(conversationId: String) {
        _uiState.value = _uiState.value.copy(
            selectedConversationId = conversationId,
            isLoadingMessages = true,
            errorMessage = null,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class ChatViewModelFactory(
    private val repository: ChatRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repository) as T
    }
}
