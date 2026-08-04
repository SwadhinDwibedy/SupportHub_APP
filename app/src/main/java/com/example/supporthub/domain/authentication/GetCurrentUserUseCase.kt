package com.example.supporthub.domain.authentication

import com.example.supporthub.features.authentication.repository.AuthRepository

class GetCurrentUserUseCase(
    private val repository: AuthRepository
) {

    suspend operator fun invoke() =
        repository.getCurrentUser()
}