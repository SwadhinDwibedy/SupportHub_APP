package com.example.supporthub.features.authentication.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.supporthub.features.authentication.model.User
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground

private val PendingCard = Color(0xFFFDFEFE)
private val PendingPrimary = Color(0xFF0F8B7B)
private val PendingPrimaryText = Color(0xFF182033)
private val PendingSecondaryText = Color(0xFF8A94A6)
private val PendingInputFill = Color(0xFFF2F6F8)
private val PendingIconBackground = Color(0xFFE8F5F1)

@Composable
fun PendingApprovalScreen(
    user: User,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    SupportHubPremiumBackground(
        modifier = Modifier
            .statusBarsPadding()
            .imePadding()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(horizontal = 28.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = PendingCard.copy(alpha = 0.94f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(PendingIconBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = PendingPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Account Pending",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PendingPrimaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your administrator will review your request shortly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PendingSecondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PendingInputFill)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Workspace",
                            style = MaterialTheme.typography.labelMedium,
                            color = PendingSecondaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.workspaceName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = PendingPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Requested Role",
                            style = MaterialTheme.typography.labelMedium,
                            color = PendingSecondaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.requestedRole,
                            style = MaterialTheme.typography.bodyLarge,
                            color = PendingPrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = PendingSecondaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.status,
                            style = MaterialTheme.typography.bodyLarge,
                            color = PendingPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = onRefresh,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PendingPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        text = "  Check Again",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = onLogout,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PendingPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = PendingPrimary
                    )
                    Text(
                        text = "  Logout",
                        fontWeight = FontWeight.SemiBold,
                        color = PendingPrimary
                    )
                }
            }
        }
    }
}
