package com.example.supporthub.features.authentication.repository

import com.example.supporthub.features.authentication.model.Role

data class RegistrationDecision(
    val approvedRole: String?,
    val status: String,
    val isWorkspaceOwner: Boolean
)

object WorkspaceRegistrationPolicy {

    private const val TAG = "WorkspaceRegistration"

    fun normalizeWorkspaceDisplayName(workspaceName: String): String {
        return workspaceName
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    fun normalizeWorkspaceKey(workspaceName: String): String {
        return normalizeWorkspaceDisplayName(workspaceName)
            .lowercase()
    }

    fun resolveRegistration(
        requestedRole: Role
    ): RegistrationDecision {
        return RegistrationDecision(
            approvedRole = null,
            status = "pending",
            isWorkspaceOwner = false
        )
    }

    fun logWorkspaceLookup(
        requestedWorkspaceName: String,
        normalizedWorkspaceKey: String,
        existingWorkspaceFound: Boolean
    ) {
        println(
            "$TAG: Workspace lookup requested='${requestedWorkspaceName}' normalized='${normalizedWorkspaceKey}' existingWorkspaceFound=${existingWorkspaceFound}"
        )
    }

    fun logWorkspaceCreation(
        requestedWorkspaceName: String,
        normalizedWorkspaceKey: String,
        ownerUid: String
    ) {
        println(
            "$TAG: Creating workspace requested='${requestedWorkspaceName}' normalized='${normalizedWorkspaceKey}' ownerUid=${ownerUid}"
        )
    }

    fun logRegistrationDecision(
        requestedWorkspaceName: String,
        normalizedWorkspaceKey: String,
        requestedRole: Role,
        decision: RegistrationDecision
    ) {
        println(
            "$TAG: Registration decision requested='${requestedWorkspaceName}' normalized='${normalizedWorkspaceKey}' role=${requestedRole.value} approvedRole=${decision.approvedRole} status=${decision.status} isWorkspaceOwner=${decision.isWorkspaceOwner}"
        )
    }
}
