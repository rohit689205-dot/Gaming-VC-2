package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcAvatar
import com.example.ui.components.VcCard
import com.example.ui.components.VcTextField
import com.example.ui.theme.VcBackground
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
fun ProfileScreen(
    viewModel: MainViewModel,
    onSignOutClick: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadProfilePhoto(uri)
        }
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(user?.displayName ?: "") }
    var editUsernameInput by remember { mutableStateOf(user?.username ?: "") }
    var editActivityInput by remember { mutableStateOf(user?.activityStatus ?: "Playing Valorant") }
    var editIsOnline by remember { mutableStateOf(user?.isOnline ?: true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var noiseSuppressionEnabled by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile & Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VcTextPrimary
            )
            IconButton(
                onClick = { /* Settings options */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VcElevatedPanel)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = VcTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Header Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    VcAvatar(
                        name = user?.displayName ?: "User",
                        avatarUrl = user?.avatarUrl,
                        size = 80.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(VcPrimaryPurple)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap photo to change",
                    fontSize = 11.sp,
                    color = VcTextMuted,
                    fontWeight = FontWeight.Medium
                )


                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.displayName?.ifBlank { "Gamer" } ?: "Gamer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )

                Text(
                    text = user?.username?.ifBlank { "@gamer" } ?: "@gamer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VcSecondaryViolet
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.activityStatus?.ifBlank { "In Voice Lobby" } ?: "In Voice Lobby",
                    fontSize = 12.sp,
                    color = VcSuccessGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        editNameInput = user?.displayName ?: ""
                        editUsernameInput = user?.username ?: ""
                        editActivityInput = user?.activityStatus ?: "Playing Valorant"
                        editIsOnline = user?.isOnline ?: true
                        showEditProfileDialog = true
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = VcPrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit Gamer Profile",
                        color = VcTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio & Video Settings Group
        Text(
            text = "AUDIO & VIDEO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = VcTextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRowToggle(
                    icon = Icons.Default.Mic,
                    title = "AI Noise Suppression",
                    subtitle = "Filter out background gaming keyboard and fan noise",
                    checked = noiseSuppressionEnabled,
                    onCheckedChange = { noiseSuppressionEnabled = it }
                )

                SettingRowItem(
                    icon = Icons.Default.Videocam,
                    title = "Default Camera Quality",
                    value = "HD 720p",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Application Settings Group
        Text(
            text = "PREFERENCES & SECURITY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = VcTextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRowToggle(
                    icon = Icons.Default.Notifications,
                    title = "Voice Call Notifications",
                    subtitle = "Receive room invites and background call notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                SettingRowItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & Security",
                    value = "Encrypted",
                    onClick = {}
                )

                SettingRowItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Purge Guest Accounts Data",
                    value = "Clean",
                    onClick = {
                        viewModel.purgeGuestData()
                    }
                )

                SettingRowItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    value = "v1.0.0",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        Button(
            onClick = {
                viewModel.signOut()
                onSignOutClick()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = VcErrorRed.copy(alpha = 0.15f),
                contentColor = VcErrorRed
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = VcErrorRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    color = VcErrorRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(text = "Edit Gamer Profile & Status", color = VcTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    VcTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = "Display Name"
                    )
                    VcTextField(
                        value = editUsernameInput,
                        onValueChange = { editUsernameInput = it },
                        label = "Unique Gamer Tag (@username)"
                    )
                    VcTextField(
                        value = editActivityInput,
                        onValueChange = { editActivityInput = it },
                        label = "Currently Playing / Activity Status"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Online Presence", color = VcTextPrimary, fontSize = 14.sp)
                        Switch(
                            checked = editIsOnline,
                            onCheckedChange = { editIsOnline = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VcPrimaryPurple,
                                checkedTrackColor = VcSecondaryViolet.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfileDetails(
                            displayName = editNameInput,
                            username = editUsernameInput,
                            activityStatus = editActivityInput,
                            isOnline = editIsOnline
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VcPrimaryPurple)
                ) {
                    Text("Save Profile", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = VcTextSecondary)
                }
            },
            containerColor = VcSurfaceCard
        )
    }
}

@Composable
fun SettingRowToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(VcElevatedPanel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VcPrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VcTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = VcTextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VcPrimaryPurple,
                checkedTrackColor = VcSecondaryViolet.copy(alpha = 0.4f),
                uncheckedThumbColor = VcTextMuted,
                uncheckedTrackColor = VcElevatedPanel
            )
        )
    }
}

@Composable
fun SettingRowItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = VcSecondaryViolet,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = VcTextPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = VcTextMuted
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = VcTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
