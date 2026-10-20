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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcTextField
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
import com.example.ui.viewmodel.UiState

@Composable
fun CreateVcScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onMeetingCreatedSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val createdRoomInfo by viewModel.createdRoomCreatedInfo.collectAsState()

    var roomName by remember { mutableStateOf("Gaming Squad VC") }
    var isCameraEnabled by remember { mutableStateOf(true) }
    var isMicEnabled by remember { mutableStateOf(true) }
    var privacyMode by remember { mutableStateOf("link") } // "link" or "private"

    val scrollState = rememberScrollState()

    if (createdRoomInfo != null) {
        onMeetingCreatedSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VcElevatedPanel)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = VcTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Create a VC",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )
                Text(
                    text = "Set up your meeting details",
                    fontSize = 13.sp,
                    color = VcTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Meeting Name Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Meeting Name",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                VcTextField(
                    value = roomName,
                    onValueChange = { if (it.length <= 40) roomName = it },
                    label = "Room Title",
                    leadingIcon = Icons.Default.Videocam
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Suggested: Gaming Squad, Team Sync, Friends VC, Late Night Gaming",
                    fontSize = 12.sp,
                    color = VcTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera & Microphone Settings Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Media & Device Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Microphone Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VcElevatedPanel),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = VcPrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Microphone",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VcTextPrimary
                            )
                            Text(
                                text = if (isMicEnabled) "Mic active for meeting" else "Muted on join",
                                fontSize = 12.sp,
                                color = VcTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isMicEnabled,
                        onCheckedChange = { isMicEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VcPrimaryPurple,
                            checkedTrackColor = VcSecondaryViolet.copy(alpha = 0.4f),
                            uncheckedThumbColor = VcTextMuted,
                            uncheckedTrackColor = VcElevatedPanel
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VcElevatedPanel),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraFront,
                                contentDescription = null,
                                tint = VcSecondaryViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Camera Video",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VcTextPrimary
                            )
                            Text(
                                text = if (isCameraEnabled) "Video feed enabled" else "Voice-only mode",
                                fontSize = 12.sp,
                                color = VcTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isCameraEnabled,
                        onCheckedChange = { isCameraEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VcPrimaryPurple,
                            checkedTrackColor = VcSecondaryViolet.copy(alpha = 0.4f),
                            uncheckedThumbColor = VcTextMuted,
                            uncheckedTrackColor = VcElevatedPanel
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Mode Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Room Privacy",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Link Accessible Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (privacyMode == "link") VcElevatedPanel else VcSurfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (privacyMode == "link") VcPrimaryPurple else VcBorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { privacyMode = "link" }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = VcPrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Link-Accessible",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VcTextPrimary
                        )
                        Text(
                            text = "Anyone with the 6-digit code or link can join",
                            fontSize = 12.sp,
                            color = VcTextSecondary
                        )
                    }
                    if (privacyMode == "link") {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VcSuccessGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Private / Invite-Only Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (privacyMode == "private") VcElevatedPanel else VcSurfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (privacyMode == "private") VcPrimaryPurple else VcBorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { privacyMode = "private" }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VcSecondaryViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Private / Invite-Only",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VcTextPrimary
                        )
                        Text(
                            text = "Only authorized team members can join",
                            fontSize = 12.sp,
                            color = VcTextSecondary
                        )
                    }
                    if (privacyMode == "private") {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VcSuccessGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (uiState is UiState.Loading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VcPrimaryPurple)
            }
        } else {
            VcGradientButton(
                text = "Create Meeting",
                onClick = {
                    viewModel.createVcRoom(roomName, privacyMode)
                }
            )
        }

        if (uiState is UiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (uiState as UiState.Error).message,
                color = VcErrorRed,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
