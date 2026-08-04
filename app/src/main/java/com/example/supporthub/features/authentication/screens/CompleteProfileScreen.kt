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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.supporthub.core.firebase.FirebaseAuthManager
import com.example.supporthub.features.authentication.components.RoleSelector
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.utils.AuthDestination
import com.example.supporthub.features.authentication.viewmodel.AuthViewModel

private val CompleteProfileBackground = Color(0xFFF8FAFC)
private val CompleteProfileCard = Color(0xFFFDFEFE)
private val CompleteProfilePrimary = Color(0xFF0F8B7B)
private val CompleteProfilePrimaryText = Color(0xFF182033)
private val CompleteProfileSecondaryText = Color(0xFF8A94A6)
private val CompleteProfileInputFill = Color(0xFFF2F6F8)
private val CompleteProfileBlueSpot = Color(0xFFCBE2FF)
private val CompleteProfileMintSpot = Color(0xFFBEECDD)
private val CompleteProfilePurpleSpot = Color(0xFFE4D7FF)
private val CompleteProfileIconBackground = Color(0xFFE8F5F1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    viewModel: AuthViewModel,
    onCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val firebaseEmail = FirebaseAuthManager.auth.currentUser?.email.orEmpty()
    var fullName by remember(uiState.user?.fullName) {
        mutableStateOf(uiState.user?.fullName.orEmpty())
    }
    var email by remember(uiState.user?.email, firebaseEmail) {
        mutableStateOf(uiState.user?.email?.takeIf { it.isNotBlank() } ?: firebaseEmail)
    }
    var workspaceName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(Role.EMPLOYEE) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.user?.email, firebaseEmail) {
        val resolvedEmail = uiState.user?.email?.takeIf { it.isNotBlank() } ?: firebaseEmail
        if (resolvedEmail.isNotBlank() && email != resolvedEmail) {
            email = resolvedEmail
        }
    }

    LaunchedEffect(uiState.destination) {
        if (uiState.destination == AuthDestination.WorkspaceLoading) {
            viewModel.consumeDestination()
            onCompleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), CompleteProfileBackground, Color(0xFFFFFFFF))
                )
            )
            .statusBarsPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-60).dp, y = 58.dp)
                .clip(CircleShape)
                .background(CompleteProfileBlueSpot.copy(alpha = 0.45f))
                .blur(110.dp)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 170.dp, y = 120.dp)
                .clip(CircleShape)
                .background(CompleteProfilePurpleSpot.copy(alpha = 0.34f))
                .blur(110.dp)
        )

        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 80.dp, y = 320.dp)
                .clip(CircleShape)
                .background(CompleteProfileMintSpot.copy(alpha = 0.35f))
                .blur(120.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CompleteProfileCard.copy(alpha = 0.94f))
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
                            .background(CompleteProfileIconBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = CompleteProfilePrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Complete your profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CompleteProfilePrimaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Just a few more details to finish setting up your SupportHub account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CompleteProfileSecondaryText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            localError = null
                        },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Full Name",
                                color = CompleteProfileSecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = CompleteProfileSecondaryText
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = authTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Work Email",
                                color = CompleteProfileSecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = CompleteProfileSecondaryText
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CompleteProfileSecondaryText
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        colors = authTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = workspaceName,
                        onValueChange = {
                            workspaceName = it
                            localError = null
                        },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Company Name",
                                color = CompleteProfileSecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = CompleteProfileSecondaryText
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = authTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Choose Your Role",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CompleteProfilePrimaryText,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RoleSelector(
                        selectedRole = selectedRole,
                        onRoleSelected = {
                            selectedRole = it
                            localError = null
                        }
                    )

                    val displayError = localError ?: uiState.error
                    if (!displayError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = displayError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !uiState.isLoading,
                        onClick = {
                            val trimmedName = fullName.trim()
                            val trimmedWorkspace = workspaceName.trim()
                            val resolvedEmail = email.ifBlank {
                                uiState.user?.email?.takeIf { it.isNotBlank() }
                                    ?: FirebaseAuthManager.auth.currentUser?.email.orEmpty()
                            }
    
                            if (email != resolvedEmail) {
                                email = resolvedEmail
                            }
    
                            localError = when {
                                trimmedName.isBlank() -> "Please enter your full name."
                                trimmedWorkspace.isBlank() -> "Please enter your company name."
                                resolvedEmail.isBlank() -> "Authenticated Google email is unavailable. Please sign in again."
                                else -> null
                            }
    
                            if (localError == null) {
                                viewModel.completeGoogleProfile(
                                    fullName = trimmedName,
                                    email = resolvedEmail,
                                    workspaceName = trimmedWorkspace,
                                    role = selectedRole
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CompleteProfilePrimary)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Continue",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CompleteProfileInputFill,
    unfocusedContainerColor = CompleteProfileInputFill,
    disabledContainerColor = CompleteProfileInputFill,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    cursorColor = CompleteProfilePrimary,
    focusedTextColor = CompleteProfilePrimaryText,
    unfocusedTextColor = CompleteProfilePrimaryText,
    disabledTextColor = CompleteProfilePrimaryText,
    focusedPlaceholderColor = CompleteProfileSecondaryText,
    unfocusedPlaceholderColor = CompleteProfileSecondaryText,
    disabledPlaceholderColor = CompleteProfileSecondaryText,
    focusedLeadingIconColor = CompleteProfileSecondaryText,
    unfocusedLeadingIconColor = CompleteProfileSecondaryText,
    disabledLeadingIconColor = CompleteProfileSecondaryText,
    focusedTrailingIconColor = CompleteProfileSecondaryText,
    unfocusedTrailingIconColor = CompleteProfileSecondaryText,
    disabledTrailingIconColor = CompleteProfileSecondaryText
)
