package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcCard
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
fun RecentActivityScreen(
    viewModel: MainViewModel,
    onRejoinClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val history by viewModel.meetingHistory.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // "All", "Upcoming", "Completed"

    val filteredHistory = when (selectedFilter) {
        "Upcoming" -> history.filter { it.status == "Upcoming" }
        "Completed" -> history.filter { it.status == "Completed" }
        else -> history
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
    ) {
        VcTopBar(
            title = "VC Connect",
            avatarName = user?.displayName ?: "User",
            onProfileClick = onProfileClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Recent Activity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )
                Text(
                    text = "Your past and upcoming voice meetings",
                    fontSize = 13.sp,
                    color = VcTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Tabs Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Upcoming", "Completed").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) VcPrimaryPurple else VcSurfaceCard)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) VcPrimaryPurple else VcBorderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else VcTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (filteredHistory.isEmpty()) {
                item {
                    VcCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VcTextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No meetings found for '$selectedFilter'",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = VcTextPrimary
                            )
                            Text(
                                text = "Your voice room logs will automatically record here.",
                                fontSize = 13.sp,
                                color = VcTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredHistory) { item ->
                    VcCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(VcElevatedPanel),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = VcPrimaryPurple,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.roomName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VcTextPrimary
                                        )
                                        Text(
                                            text = "Host: ${item.hostName} • Code: ${item.inviteCode}",
                                            fontSize = 12.sp,
                                            color = VcTextSecondary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VcElevatedPanel)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = item.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.status == "Active") VcSuccessGreen else VcTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duration: ${item.durationMinutes} mins • ${item.participantCount} users",
                                    fontSize = 12.sp,
                                    color = VcTextMuted
                                )

                                Button(
                                    onClick = { onRejoinClick(item.inviteCode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VcElevatedPanel,
                                        contentColor = VcPrimaryPurple
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Rejoin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
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
