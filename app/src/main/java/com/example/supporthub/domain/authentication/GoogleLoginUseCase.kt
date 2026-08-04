package com.example.supporthub.domain.authentication

import com.example.supporthub.features.authentication.repository.AuthRepository

class GoogleLoginUseCase(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(
        idToken: String
    ) = repository.signInWithGoogle(idToken)
}