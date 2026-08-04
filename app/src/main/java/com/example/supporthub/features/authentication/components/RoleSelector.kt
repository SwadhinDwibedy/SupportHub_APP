package com.example.supporthub.features.authentication.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.supporthub.features.authentication.model.Role
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.ui.draw.clip

data class RoleStyle(
    val background: Color,
    val border: Color,
    val text: Color
)

private val EmployeeBlue = Color(0xFF2563EB)
private val AgentGreen = Color(0xFF16A34A)
private val AdminPurple = Color(0xFF7C3AED)

private val BorderColor = Color(0xFFE5E7EB)
private val CardColor = Color.White
private val TitleColor = Color(0xFF6B7280)
private val TextColor = Color(0xFF111827)

private fun Role.icon(): ImageVector =
    when (this) {
        Role.EMPLOYEE -> Icons.Outlined.Person
        Role.AGENT -> Icons.Outlined.SupportAgent
        Role.ADMIN -> Icons.Outlined.AdminPanelSettings
    }

private fun Role.accent(): Color =
    when (this) {
        Role.EMPLOYEE -> EmployeeBlue
        Role.AGENT -> AgentGreen
        Role.ADMIN -> AdminPurple
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelector(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit
) {

    var showSheet by remember {
        mutableStateOf(false)
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (showSheet) 180f else 0f,
        label = ""
    )

    val accent by animateColorAsState(
        targetValue = selectedRole.accent(),
        label = ""
    )

    Column {

        Text(
            text = "Role Selector",
            style = MaterialTheme.typography.labelMedium,
            color = TitleColor,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {
                    showSheet = true
                },

            shape = RoundedCornerShape(20.dp),

            colors = CardDefaults.elevatedCardColors(
                containerColor = CardColor
            ),

            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),

                verticalAlignment = Alignment.CenterVertically

            ) {

                Icon(
                    imageVector = selectedRole.icon(),
                    contentDescription = null,
                    tint = accent
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = selectedRole.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextColor,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(arrowRotation),
                    tint = Color.Gray
                )

            }

        }

        if (showSheet) {

            ModalBottomSheet(

                onDismissRequest = {
                    showSheet = false
                },

                containerColor = Color.White,

                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                )

            ) {

                RoleBottomSheetContent(
                    selectedRole,
                    onRoleSelected
                ) {
                    showSheet = false
                }

            }

        }

    }

}
@Composable
private fun RoleBottomSheetContent(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit,
    dismiss: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .navigationBarsPadding()
            .padding(bottom = 18.dp)
    ) {

        // Drag Handle

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 10.dp)
                .size(width = 48.dp, height = 5.dp)
                .clip(CircleShape)
                .background(Color(0xFFD9D9D9))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose Role",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Role.entries.forEachIndexed { index, role ->

            RoleItem(
                role = role,
                selected = role == selectedRole
            ) {

                onRoleSelected(role)
                dismiss()

            }

            if (index != Role.entries.lastIndex) {

                HorizontalDivider(
                    thickness = 0.6.dp,
                    color = Color(0xFFF1F3F5)
                )

            }

        }

        Spacer(modifier = Modifier.height(24.dp))

    }

}

@Composable
private fun RoleItem(
    role: Role,
    selected: Boolean,
    onClick: () -> Unit
) {

    val accent = role.accent()

    Surface(

        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = LocalIndication.current,
                interactionSource = remember {
                    MutableInteractionSource()
                }
            ) {
                onClick()
            },

        color = Color.Transparent

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Surface(

                shape = CircleShape,

                color =
                    if (selected)
                        accent.copy(alpha = .12f)
                    else
                        Color(0xFFF5F6F7)

            ) {

                Icon(

                    imageVector = role.icon(),

                    contentDescription = null,

                    tint =
                        if (selected)
                            accent
                        else
                            Color(0xFF9CA3AF),

                    modifier = Modifier.padding(12.dp)

                )

            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(

                    text = role.value,

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight =
                        if (selected)
                            FontWeight.Bold
                        else
                            FontWeight.SemiBold,

                    color = TextColor

                )

            }

            if (selected) {

                Surface(

                    shape = CircleShape,

                    color = accent

                ) {

                    Icon(

                        imageVector = Icons.Rounded.Check,

                        contentDescription = null,

                        tint = Color.White,

                        modifier = Modifier.padding(6.dp)

                    )

                }

            }

        }

    }

}