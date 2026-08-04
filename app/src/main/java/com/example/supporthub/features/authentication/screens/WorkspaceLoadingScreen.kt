package com.example.supporthub.features.authentication.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground
import java.util.Calendar
import java.util.Locale

private val WorkspaceCard = Color(0xFFFDFEFE)
private val WorkspacePrimary = Color(0xFF0F8B7B)
private val WorkspacePrimaryText = Color(0xFF182033)
private val WorkspaceSecondaryText = Color(0xFF8A94A6)
private val WorkspaceAvatarBackground = Color(0xFFE7F5F1)

@Composable
fun WorkspaceLoadingScreen(
    user: User,
    onFinished: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "workspace_loading")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_alpha"
    )

    LaunchedEffect(user.uid, user.status, user.approvedRole) {
        when {
            user.status.equals("pending", ignoreCase = true) -> onFinished("pending")
            user.status.equals("active", ignoreCase = true) && user.approvedRole.equals("employee", ignoreCase = true) -> onFinished("employee")
            user.status.equals("active", ignoreCase = true) && user.approvedRole.equals("agent", ignoreCase = true) -> onFinished("agent")
            user.status.equals("active", ignoreCase = true) && user.approvedRole.equals("admin", ignoreCase = true) -> onFinished("admin")
            else -> onFinished("login")
        }
    }

    val displayName = user.fullName.ifBlank { user.email.substringBefore('@') }
        .trim()
        .ifBlank { "User" }
    val firstLetter = displayName.first().uppercaseChar().toString()
    val greeting = currentGreeting()
    val subtitle = loadingSubtitleFor(user)

    SupportHubPremiumBackground(
        modifier = Modifier
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(32.dp),
                color = WorkspaceCard.copy(alpha = 0.95f),
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .background(WorkspaceAvatarBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstLetter,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = WorkspacePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = WorkspacePrimaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkspacePrimaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WorkspaceSecondaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = WorkspacePrimary,
                        trackColor = WorkspacePrimary.copy(alpha = 0.18f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = subtitle,
                        modifier = Modifier.alpha(alpha),
                        style = MaterialTheme.typography.bodyLarge,
                        color = WorkspaceSecondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun currentGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

private fun loadingSubtitleFor(user: User): String {
    val workspace = user.workspaceName.trim()
    return when {
        user.status.equals("pending", ignoreCase = true) -> {
            if (workspace.isNotEmpty()) {
                "Checking your access request for $workspace."
            } else {
                "Checking your access request."
            }
        }
        user.status.equals("active", ignoreCase = true) && user.approvedRole.equals("admin", ignoreCase = true) -> {
            if (workspace.isNotEmpty()) {
                "Preparing admin controls for $workspace."
            } else {
                "Preparing your admin workspace."
            }
        }
        user.status.equals("active", ignoreCase = true) && user.approvedRole.equals("agent", ignoreCase = true) -> {
            if (workspace.isNotEmpty()) {
                "Loading support queues for $workspace."
            } else {
                "Loading your support workspace."
            }
        }
        user.status.equals("active", ignoreCase = true) && user.approvedRole.equals("employee", ignoreCase = true) -> {
            if (workspace.isNotEmpty()) {
                "Loading your employee workspace at $workspace."
            } else {
                "Loading your employee workspace."
            }
        }
        workspace.isNotEmpty() -> "Organising your workspace at $workspace."
        else -> "Organising your SupportHub workspace."
    }
}
