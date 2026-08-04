package com.example.supporthub.features.authentication.model

data class User(

    val uid: String = "",

    val fullName: String = "",

    val email: String = "",

    val phone: String = "",

    val location: String = "",

    val jobTitle: String = "",

    val department: String = "",

    val workspaceName: String = "",

    val requestedRole: String = "",

    val approvedRole: String? = null,

    val status: String = "pending",

    val isWorkspaceOwner: Boolean = false,

    val authProvider: String = "email",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    val lastLogin: Long = System.currentTimeMillis(),

    val role: String? = null,

    val avatarUrl: String? = null
)