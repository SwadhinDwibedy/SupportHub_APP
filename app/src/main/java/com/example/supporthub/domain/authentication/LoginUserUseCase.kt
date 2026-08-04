package com.example.supporthub.domain.authentication

import com.example.supporthub.features.authentication.repository.AuthRepository

class LoginUserUseCase(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ) = repository.loginUser(
        email,
        password
    )
}