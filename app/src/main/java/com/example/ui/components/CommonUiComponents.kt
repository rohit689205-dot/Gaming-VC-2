package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VcBackground
import com.example.ui.theme.VcBorderSubtle
import com.example.ui.theme.VcElevatedPanel
import com.example.ui.theme.VcPrimaryPurple
import com.example.ui.theme.VcSecondaryViolet
import com.example.ui.theme.VcSuccessGreen
import com.example.ui.theme.VcSurfaceCard
import com.example.ui.theme.VcTextMuted
import com.example.ui.theme.VcTextPrimary
import com.example.ui.theme.VcTextSecondary

@Composable
fun VcTopBar(
    title: String = "VC Connect",
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    avatarName: String = "User",
    avatarUrl: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(VcPrimaryPurple, VcSecondaryViolet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "VC Connect Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VcTextPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VcElevatedPanel)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = VcTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VcPrimaryPurple)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = avatarName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


@Composable
fun VcCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = VcSurfaceCard,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .border(1.dp, VcBorderSubtle, shape)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = backgroundColor,
        shape = shape
    ) {
        content()
    }
}

@Composable
fun VcGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = VcElevatedPanel
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .clip(buttonShape)
            .background(
                brush = if (enabled) Brush.horizontalGradient(
                    listOf(VcPrimaryPurple, VcSecondaryViolet)
                ) else Brush.linearGradient(listOf(VcElevatedPanel, VcElevatedPanel))
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VcOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val buttonShape = RoundedCornerShape(14.dp)
    OutlinedButton(
        onClick = onClick,
        shape = buttonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = VcTextPrimary
        ),
        border = BorderStroke(1.dp, VcBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(VcSurfaceCard)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VcSecondaryViolet,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = VcTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = VcTextSecondary) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = VcTextMuted) }
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VcSurfaceCard,
            unfocusedContainerColor = VcSurfaceCard,
            focusedBorderColor = VcPrimaryPurple,
            unfocusedBorderColor = VcBorderSubtle,
            focusedTextColor = VcTextPrimary,
            unfocusedTextColor = VcTextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun VcAvatar(
    name: String,
    avatarUrl: String? = null,
    isSpeaking: Boolean = false,
    isMuted: Boolean = false,
    size: Dp = 56.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size + 8.dp)
    ) {
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(size + 8.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(VcSuccessGreen.copy(alpha = 0.35f))
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(VcPrimaryPurple, VcSecondaryViolet))
                )
                .border(
                    width = if (isSpeaking) 2.5.dp else 1.dp,
                    color = if (isSpeaking) VcSuccessGreen else VcBorderSubtle,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "$name's profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = name.take(1).uppercase().ifBlank { "U" },
                    color = Color.White,
                    fontSize = (size.value * 0.4f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }


        if (isMuted) {
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .clip(CircleShape)
                    .background(Color(0xCCEF4444))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = "Muted",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
    }
}

enum class NavigationTab {
    HOME, FRIENDS, RECENT, PROFILE
}

@Composable
fun VcBottomNavigationBar(
    currentTab: NavigationTab,
    pendingRequestsCount: Int = 0,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = VcBackground,
        contentColor = VcTextSecondary,
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(width = 1.dp, color = VcBorderSubtle, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentTab == NavigationTab.HOME,
            onClick = { onTabSelected(NavigationTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VcPrimaryPurple,
                selectedTextColor = VcPrimaryPurple,
                unselectedIconColor = VcTextMuted,
                unselectedTextColor = VcTextMuted,
                indicatorColor = VcElevatedPanel
            )
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.FRIENDS,
            onClick = { onTabSelected(NavigationTab.FRIENDS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (pendingRequestsCount > 0) {
                            Badge(
                                containerColor = VcPrimaryPurple,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (pendingRequestsCount > 99) "99+" else pendingRequestsCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Group, contentDescription = "Friends")
                }
            },
            label = { Text("Friends") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VcPrimaryPurple,
                selectedTextColor = VcPrimaryPurple,
                unselectedIconColor = VcTextMuted,
                unselectedTextColor = VcTextMuted,
                indicatorColor = VcElevatedPanel
            )
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.RECENT,
            onClick = { onTabSelected(NavigationTab.RECENT) },
            icon = { Icon(Icons.Default.History, contentDescription = "Recent") },
            label = { Text("Recent") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VcPrimaryPurple,
                selectedTextColor = VcPrimaryPurple,
                unselectedIconColor = VcTextMuted,
                unselectedTextColor = VcTextMuted,
                indicatorColor = VcElevatedPanel
            )
        )
        NavigationBarItem(
            selected = currentTab == NavigationTab.PROFILE,
            onClick = { onTabSelected(NavigationTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VcPrimaryPurple,
                selectedTextColor = VcPrimaryPurple,
                unselectedIconColor = VcTextMuted,
                unselectedTextColor = VcTextMuted,
                indicatorColor = VcElevatedPanel
            )
        )
    }
}
