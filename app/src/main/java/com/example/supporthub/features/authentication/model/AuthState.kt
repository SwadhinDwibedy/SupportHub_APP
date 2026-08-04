package com.example.supporthub.features.authentication.model

enum class AuthState(val value: String) {
    ACTIVE("active"),
    PENDING("pending"),
    REJECTED("rejected"),
    DISABLED("disabled")
}