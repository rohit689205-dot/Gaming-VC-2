package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoomMember
import com.example.ui.components.VcAvatar
import com.example.ui.components.VcCard
import com.example.ui.theme.VcBackground
import com.example.ui.theme.VcBorderSubtle
import com.example.ui.theme.VcElevatedPanel
import com.example.ui.theme.VcErrorRed
import com.example.ui.theme.VcPrimaryPurple
import com.example.ui.theme.VcSecondaryViolet
import com.example.ui.theme.VcSuccessGreen
import com.example.ui.theme.VcSurfaceCard
import com.example.ui.theme.VcTextMuted
import com.example.ui.theme.VcTextPrimary
import com.example.ui.theme.VcTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LiveRoomScreen(
    viewModel: MainViewModel,
    onLeaveRoomClick: () -> Unit
) {
    val context = LocalContext.current
    val activeRoom by viewModel.activeRoom.collectAsState()
    val members by viewModel.activeRoomMembers.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val isMuted by viewModel.voiceStreamManager.isMuted.collectAsState()
    val isCameraOn by viewModel.voiceStreamManager.isCameraOn.collectAsState()
    val isSpeaking by viewModel.voiceStreamManager.isSpeaking.collectAsState()

    var showMemberListModal by remember { mutableStateOf(false) }

    val roomTitle = activeRoom?.roomName ?: "Live Voice Room"
    val roomCode = activeRoom?.inviteCode ?: "892104"
    val callTime = viewModel.getFormattedCallDuration()

    // Ensure local user appears in participant list if empty
    val displayMembers = if (members.isNotEmpty()) members else listOf(
        RoomMember(
            id = user?.id ?: "local",
            userId = user?.id ?: "local",
            displayName = user?.displayName ?: "You",
            isMuted = isMuted,
            isSpeaking = isSpeaking
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            // Live Room Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onLeaveRoomClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VcElevatedPanel)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Minimize",
                        tint = VcTextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = roomTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VcTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = callTime,
                            fontSize = 13.sp,
                            color = VcTextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VcSuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "• ${displayMembers.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VcSuccessGreen
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showMemberListModal = !showMemberListModal },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VcElevatedPanel)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = VcTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Participant Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayMembers) { member ->
                    val isLocal = member.userId == user?.id
                    val memberMuted = if (isLocal) isMuted else member.isMuted
                    val memberSpeaking = if (isLocal) isSpeaking else member.isSpeaking

                    ParticipantCard(
                        member = member,
                        isLocalUser = isLocal,
                        isMuted = memberMuted,
                        isSpeaking = memberSpeaking,
                        isCameraOn = isLocal && isCameraOn
                    )
                }
            }
        }

        // Floating Audio Control Bar at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            VcCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = VcElevatedPanel
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Button
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) VcErrorRed else VcPrimaryPurple)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Toggle Mute",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Camera Toggle
                    IconButton(
                        onClick = { viewModel.toggleCamera() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isCameraOn) VcSecondaryViolet else VcSurfaceCard)
                    ) {
                        Icon(
                            imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Toggle Camera",
                            tint = if (isCameraOn) Color.White else VcTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Switch Camera
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Switching camera view", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(VcSurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = VcTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Share Invitation
                    IconButton(
                        onClick = {
                            val msg = "Join my voice room '$roomTitle' on VC Connect! Code: $roomCode"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, msg)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Invite to VC"))
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(VcSurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Invite",
                            tint = VcTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Leave / End Call Button
                    FloatingActionButton(
                        onClick = {
                            viewModel.leaveActiveRoom()
                            onLeaveRoomClick()
                        },
                        containerColor = VcErrorRed,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Leave Call",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantCard(
    member: RoomMember,
    isLocalUser: Boolean,
    isMuted: Boolean,
    isSpeaking: Boolean,
    isCameraOn: Boolean
) {
    VcCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        backgroundColor = VcSurfaceCard
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                VcAvatar(
                    name = member.displayName.ifBlank { "Player" },
                    isSpeaking = isSpeaking,
                    isMuted = isMuted,
                    size = 64.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName.ifBlank { "Gamer" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VcTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isLocalUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VcPrimaryPurple.copy(alpha = 0.3f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "You",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VcPrimaryPurple
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isMuted) "Muted" else if (isSpeaking) "Speaking..." else "Connected",
                    fontSize = 12.sp,
                    color = if (isSpeaking) VcSuccessGreen else VcTextMuted
                )
            }
        }
    }
}
