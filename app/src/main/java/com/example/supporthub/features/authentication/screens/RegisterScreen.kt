package com.example.supporthub.features.authentication.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.supporthub.R
import com.example.supporthub.features.authentication.components.RoleSelector
import com.example.supporthub.features.authentication.model.Role
import com.example.supporthub.features.authentication.utils.AuthDestination
import com.example.supporthub.features.authentication.utils.GoogleSignInHelper
import com.example.supporthub.features.authentication.viewmodel.AuthViewModel
import com.example.supporthub.features.shared.ui.SupportHubPremiumBackground
import kotlinx.coroutines.launch

private val RegisterCard = Color(0xFFFDFEFE)
private val RegisterInputFill = Color(0xFFF2F6F8)
private val RegisterPrimary = Color(0xFF0F8B7B)
private val RegisterPrimaryDarkText = Color(0xFF182033)
private val RegisterSecondaryText = Color(0xFF8A94A6)
private val RegisterDivider = Color(0xFFE6EAEE)
private val RegisterRoleCard = Color(0xFFF7FBFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onPendingApproval: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleCompleteProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleHelper = remember {
        GoogleSignInHelper(context)
    }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var workspaceName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(Role.EMPLOYEE) }

    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            AuthDestination.CompleteProfile -> {
                viewModel.consumeDestination()
                onGoogleCompleteProfile()
            }

            AuthDestination.PendingApproval -> {
                viewModel.consumeDestination()
                onPendingApproval()
            }

            AuthDestination.WorkspaceLoading -> {
                viewModel.consumeDestination()
                onRegisterSuccess()
            }

            null -> Unit
        }
    }

    SupportHubPremiumBackground(
        modifier = Modifier
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                color = RegisterCard.copy(alpha = 0.94f),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create workspace",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RegisterPrimaryDarkText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Set up your team account with a clean, premium onboarding flow",
                        color = RegisterSecondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = {
                            Text("Full name", color = RegisterSecondaryText)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = RegisterSecondaryText)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RegisterInputFill,
                            unfocusedContainerColor = RegisterInputFill,
                            disabledContainerColor = RegisterInputFill,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            cursorColor = RegisterPrimary,
                            focusedTextColor = RegisterPrimaryDarkText,
                            unfocusedTextColor = RegisterPrimaryDarkText,
                            focusedPlaceholderColor = RegisterSecondaryText,
                            unfocusedPlaceholderColor = RegisterSecondaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = { email = it },
                        placeholder = {
                            Text("Email address", color = RegisterSecondaryText)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = RegisterSecondaryText)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RegisterInputFill,
                            unfocusedContainerColor = RegisterInputFill,
                            disabledContainerColor = RegisterInputFill,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            cursorColor = RegisterPrimary,
                            focusedTextColor = RegisterPrimaryDarkText,
                            unfocusedTextColor = RegisterPrimaryDarkText,
                            focusedPlaceholderColor = RegisterSecondaryText,
                            unfocusedPlaceholderColor = RegisterSecondaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = workspaceName,
                        onValueChange = { workspaceName = it },
                        placeholder = {
                            Text("Workspace name", color = RegisterSecondaryText)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Business, null, tint = RegisterSecondaryText)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RegisterInputFill,
                            unfocusedContainerColor = RegisterInputFill,
                            disabledContainerColor = RegisterInputFill,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            cursorColor = RegisterPrimary,
                            focusedTextColor = RegisterPrimaryDarkText,
                            unfocusedTextColor = RegisterPrimaryDarkText,
                            focusedPlaceholderColor = RegisterSecondaryText,
                            unfocusedPlaceholderColor = RegisterSecondaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = RegisterRoleCard,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, RegisterDivider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Select your role",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = RegisterPrimaryDarkText
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Choose how you will use the workspace. This only updates the UI selection and keeps the same existing flow.",
                                color = RegisterSecondaryText,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            RoleSelector(
                                selectedRole = selectedRole,
                                onRoleSelected = { selectedRole = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = { password = it },
                        placeholder = {
                            Text("Password", color = RegisterSecondaryText)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = RegisterSecondaryText)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = null,
                                    tint = RegisterSecondaryText
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RegisterInputFill,
                            unfocusedContainerColor = RegisterInputFill,
                            disabledContainerColor = RegisterInputFill,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            cursorColor = RegisterPrimary,
                            focusedTextColor = RegisterPrimaryDarkText,
                            unfocusedTextColor = RegisterPrimaryDarkText,
                            focusedPlaceholderColor = RegisterSecondaryText,
                            unfocusedPlaceholderColor = RegisterSecondaryText
                        ),
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = {
                            Text("Confirm password", color = RegisterSecondaryText)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = RegisterSecondaryText)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    if (confirmVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = null,
                                    tint = RegisterSecondaryText
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = RegisterInputFill,
                            unfocusedContainerColor = RegisterInputFill,
                            disabledContainerColor = RegisterInputFill,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            cursorColor = RegisterPrimary,
                            focusedTextColor = RegisterPrimaryDarkText,
                            unfocusedTextColor = RegisterPrimaryDarkText,
                            focusedPlaceholderColor = RegisterSecondaryText,
                            unfocusedPlaceholderColor = RegisterSecondaryText
                        ),
                        visualTransformation = if (confirmVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading,
                        onClick = {
                            when {
                                fullName.isBlank() -> {}
                                email.isBlank() -> {}
                                workspaceName.isBlank() -> {}
                                password.isBlank() -> {}
                                password != confirmPassword -> {}
                                else -> {
                                    viewModel.register(
                                        fullName = fullName,
                                        email = email,
                                        password = password,
                                        workspaceName = workspaceName,
                                        role = selectedRole
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RegisterPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = RegisterPrimary.copy(alpha = 0.7f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creating...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    uiState.error?.let {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = RegisterDivider
                        )
                        Text(
                            text = "Or continue with",
                            color = RegisterSecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = RegisterDivider
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        onClick = {
                            scope.launch {
                                val idToken = googleHelper.signIn()
                                if (idToken != null) {
                                    viewModel.googleLogin(idToken)
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.92f),
                            contentColor = RegisterPrimaryDarkText
                        ),
                        border = BorderStroke(1.dp, RegisterDivider)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = RegisterPrimaryDarkText
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = RegisterSecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = onLoginClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Login",
                                color = RegisterPrimary,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
