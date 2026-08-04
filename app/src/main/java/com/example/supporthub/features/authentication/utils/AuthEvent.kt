package com.example.supporthub.features.authentication.utils

import com.example.supporthub.features.authentication.model.User

sealed class AuthEvent {

    data object Idle : AuthEvent()

    data object Loading : AuthEvent()

    data class Success(
        val user: User
    ) : AuthEvent()

    data class Error(
        val message: String
    ) : AuthEvent()

}