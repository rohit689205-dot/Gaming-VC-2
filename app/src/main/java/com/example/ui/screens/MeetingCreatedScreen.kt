package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcOutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcOutlinedButton
import com.example.ui.theme.VcBackground
import com.example.ui.theme.VcElevatedPanel
import com.example.ui.theme.VcPrimaryPurple
import com.example.ui.theme.VcSecondaryViolet
import com.example.ui.theme.VcSuccessGreen
import com.example.ui.theme.VcTextMuted
import com.example.ui.theme.VcTextPrimary
import com.example.ui.theme.VcTextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MeetingCreatedScreen(
    viewModel: MainViewModel,
    onJoinMeetingNow: () -> Unit,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val createdRoomInfo by viewModel.createdRoomCreatedInfo.collectAsState()

    val roomCode = createdRoomInfo?.inviteCode ?: "892104"
    val roomName = createdRoomInfo?.roomName ?: "Gaming Squad VC"
    val meetingUrl = "https://vcconnect.app/room/$roomCode"
    val fullInviteMessage = "Join my voice room '$roomName' on VC Connect!\nMeeting Code: $roomCode\nLink: $meetingUrl"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Success Graphic
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(VcPrimaryPurple, VcSecondaryViolet)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Meeting Created!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = VcTextPrimary
        )

        Text(
            text = "Share this code or link with your participants",
            fontSize = 14.sp,
            color = VcTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Meeting Code Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MEETING CODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = roomCode,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VcPrimaryPurple,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Meeting Code", roomCode))
                        Toast.makeText(context, "Code copied: $roomCode", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = VcSecondaryViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Copy Code", color = VcTextPrimary, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meeting Link Card
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = VcSecondaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Meeting Link",
                            fontSize = 12.sp,
                            color = VcTextMuted
                        )
                        Text(
                            text = meetingUrl,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VcTextPrimary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Meeting Link", meetingUrl))
                        Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Link",
                        tint = VcPrimaryPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VcOutlinedButton(
                text = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, fullInviteMessage)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share VC Invitation")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.weight(1f)
            )

            VcOutlinedButton(
                text = "Copy All",
                icon = Icons.Default.ContentCopy,
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("VC Invitation", fullInviteMessage))
                    Toast.makeText(context, "Invitation copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        VcGradientButton(
            text = "Join Meeting Now",
            icon = Icons.Default.PlayArrow,
            onClick = {
                createdRoomInfo?.let { room ->
                    viewModel.connectToLiveRoom(room)
                    viewModel.clearCreatedRoomInfo()
                    onJoinMeetingNow()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                viewModel.clearCreatedRoomInfo()
                onCloseClick()
            }
        ) {
            Text(
                text = "Close",
                color = VcTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
