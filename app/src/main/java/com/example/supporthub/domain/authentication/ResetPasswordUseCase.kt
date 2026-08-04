package com.example.supporthub.domain.authentication

import com.example.supporthub.features.authentication.repository.AuthRepository

class ResetPasswordUseCase(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(
        email: String
    ) = repository.sendPasswordResetEmail(email)
}