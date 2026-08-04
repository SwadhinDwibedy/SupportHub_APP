package com.example.supporthub.features.authentication.model

data class Workspace(

    val workspaceName: String = "",

    val normalizedWorkspaceKey: String = "",

    val ownerUid: String = "",

    val status: String = "active",

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)