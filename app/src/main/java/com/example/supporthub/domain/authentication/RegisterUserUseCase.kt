package com.example.supporthub.domain.authentication

import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.repository.AuthRepository

class RegisterUserUseCase(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        workspaceName: String,
        role: Role
    ) = repository.registerUser(
        fullName,
        email,
        password,
        workspaceName,
        role
    )
}