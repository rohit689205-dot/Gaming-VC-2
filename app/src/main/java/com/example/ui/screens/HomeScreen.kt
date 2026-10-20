package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcOutlinedButton
import com.example.ui.components.VcTopBar
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
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onCreateVcClick: () -> Unit,
    onJoinVcClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val history by viewModel.meetingHistory.collectAsState()

    val userName = user?.displayName?.ifBlank { "Gamer" } ?: "Gamer"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
    ) {
        VcTopBar(
            title = "VC Connect",
            avatarName = userName,
            avatarUrl = user?.avatarUrl,
            onProfileClick = onProfileClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Welcome Back Hero Banner
                VcCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back,",
                                fontSize = 14.sp,
                                color = VcTextSecondary
                            )
                            Text(
                                text = userName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = VcTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start or join a voice meeting with your team.",
                                fontSize = 13.sp,
                                color = VcTextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(VcPrimaryPurple, VcSecondaryViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main Action Cards
                // 1. Create a VC
                VcCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCreateVcClick
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VcElevatedPanel),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = VcPrimaryPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Create a VC",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VcTextPrimary
                                )
                                Text(
                                    text = "Start your own voice meeting room",
                                    fontSize = 13.sp,
                                    color = VcTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        VcGradientButton(
                            text = "Create VC",
                            icon = Icons.Default.Videocam,
                            onClick = onCreateVcClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Join a VC
                VcCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onJoinVcClick
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VcElevatedPanel),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = VcSecondaryViolet,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Join a VC",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VcTextPrimary
                                )
                                Text(
                                    text = "Enter a meeting code or link to connect",
                                    fontSize = 13.sp,
                                    color = VcTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        VcOutlinedButton(
                            text = "Join VC",
                            icon = Icons.Default.Link,
                            onClick = onJoinVcClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Activity Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VcTextPrimary
                    )
                    Text(
                        text = "See All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VcPrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recent Activity List Items
            if (history.isEmpty()) {
                item {
                    VcCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VcTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No recent meetings yet",
                                fontSize = 15.sp,
                                color = VcTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Create or join a VC to get started!",
                                fontSize = 13.sp,
                                color = VcTextMuted
                            )
                        }
                    }
                }
            } else {
                items(history.take(4)) { item ->
                    VcCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(VcElevatedPanel),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = VcPrimaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.roomName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VcTextPrimary
                                    )
                                    Text(
                                        text = "Host: ${item.hostName} • ${item.durationMinutes} min",
                                        fontSize = 13.sp,
                                        color = VcTextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VcElevatedPanel)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.status,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.status == "Active") VcSuccessGreen else VcTextMuted
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
