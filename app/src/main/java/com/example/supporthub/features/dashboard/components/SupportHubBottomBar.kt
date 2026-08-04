package com.example.supporthub.features.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun SupportHubBottomBar(
    items: List<BottomNavItem>,
    selectedRoute: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockColor = Color.White
    val activeColor = Color(0xFF57C7A5)
    val activeContainer = Color(0xFFEAF9F3)
    val inactiveColor = Color(0xFF6F7C77)
    val shadowColor = Color(0x18000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag("bottom_bar_shell_transparent"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .testTag("bottom_bar_dock")
                .then(Modifier.testTag("bottom_bar_dock_white"))
                .then(Modifier.testTag("bottom_bar_height_78dp"))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                ),
            shape = RoundedCornerShape(30.dp),
            color = dockColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    PremiumBottomBarItem(
                        item = item,
                        selected = item.route == selectedRoute,
                        activeColor = activeColor,
                        activeContainer = activeContainer,
                        inactiveColor = inactiveColor,
                        onClick = { onItemSelected(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.PremiumBottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    activeColor: Color,
    activeContainer: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val itemTag = if (selected) {
        "bottom_bar_item_selected_${item.label}"
    } else {
        "bottom_bar_item_unselected_${item.label}"
    }
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.96f
            selected -> 1f
            else -> 0.985f
        },
        animationSpec = spring(
            dampingRatio = 0.92f,
            stiffness = 900f
        ),
        label = "itemScale"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 58.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = 0.95f,
            stiffness = 900f
        ),
        label = "indicatorWidth"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) activeContainer else Color.Transparent,
        animationSpec = spring(
            dampingRatio = 0.95f,
            stiffness = 900f
        ),
        label = "indicatorColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(
            dampingRatio = 0.95f,
            stiffness = 900f
        ),
        label = "iconTint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(
            dampingRatio = 0.95f,
            stiffness = 900f
        ),
        label = "labelTint"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = 0.95f,
            stiffness = 900f
        ),
        label = "labelAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .testTag(itemTag)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale)
                .widthIn(min = 0.dp)
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick
                )
                .padding(horizontal = 3.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .testTag(if (selected) "bottom_bar_selected_indicator_${item.label}" else "bottom_bar_unselected_indicator_${item.label}")
                    .widthIn(min = indicatorWidth)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
                    .padding(horizontal = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.label,
                fontSize = 10.sp,
                lineHeight = 10.5.sp,
                letterSpacing = 0.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = labelTint,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .testTag("bottom_bar_item_label_${item.label}")
                    .alpha(labelAlpha)
                    .padding(horizontal = 2.dp)
            )
        }
    }
}
