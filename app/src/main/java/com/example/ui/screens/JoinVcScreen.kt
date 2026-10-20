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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcTextField
import com.example.ui.theme.VcBackground
import com.example.ui.theme.VcElevatedPanel
import com.example.ui.theme.VcErrorRed
import com.example.ui.theme.VcPrimaryPurple
import com.example.ui.theme.VcSecondaryViolet
import com.example.ui.theme.VcSuccessGreen
import com.example.ui.theme.VcTextMuted
import com.example.ui.theme.VcTextPrimary
import com.example.ui.theme.VcTextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun JoinVcScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onJoinSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()

    var joinCodeInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    if (activeRoom != null) {
        onJoinSuccess()
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
                    text = "Join a VC",
                    fontSize = 22.sp,
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

        Spacer(modifier = Modifier.height(24.dp))

        // Input Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Meeting Code or Link",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                VcTextField(
                    value = joinCodeInput,
                    onValueChange = { joinCodeInput = it },
                    label = "Code e.g. 892104",
                    leadingIcon = Icons.Default.Link
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Example format: 892104 or https://vcconnect.app/room/892104",
                    fontSize = 12.sp,
                    color = VcTextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState is UiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VcPrimaryPurple)
                    }
                } else {
                    VcGradientButton(
                        text = "Join VC",
                        icon = Icons.Default.Link,
                        onClick = {
                            viewModel.joinVcRoomByCode(joinCodeInput)
                        }
                    )
                }

                if (uiState is UiState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = (uiState as UiState.Error).message,
                        color = VcErrorRed,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Before You Join" Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = VcPrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Before You Join",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VcTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item 1: Mic Permissions
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VcElevatedPanel),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = VcSecondaryViolet,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Microphone Permission",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VcTextPrimary
                        )
                        Text(
                            text = "Ensure mic permission is granted for clear voice chat.",
                            fontSize = 12.sp,
                            color = VcTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 2: Internet Connection
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VcElevatedPanel),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = VcSuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Stable Internet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VcTextPrimary
                        )
                        Text(
                            text = "A Wi-Fi or 4G/5G connection gives low-latency gaming voice.",
                            fontSize = 12.sp,
                            color = VcTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3: Quiet Environment
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VcElevatedPanel),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = VcPrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Quiet Environment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VcTextPrimary
                        )
                        Text(
                            text = "Use headphones or headset to avoid audio echo.",
                            fontSize = 12.sp,
                            color = VcTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { /* Help link */ }) {
                Text(
                    text = "Having trouble? Contact Support",
                    color = VcTextMuted,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
