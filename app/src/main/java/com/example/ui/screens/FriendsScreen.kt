package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.FriendRequest
import com.example.model.Friendship
import com.example.model.UserProfile
import com.example.ui.components.VcAvatar
import com.example.ui.components.VcCard
import com.example.ui.components.VcGradientButton
import com.example.ui.components.VcOutlinedButton
import com.example.ui.components.VcTextField
import com.example.ui.components.VcTopBar
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
import com.example.ui.viewmodel.FriendsViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

enum class FriendsTab {
    MY_FRIENDS, REQUESTS_RECEIVED, REQUESTS_SENT, FIND_FRIENDS
}

@Composable
fun FriendsScreen(
    mainViewModel: MainViewModel,
    friendsViewModel: FriendsViewModel = viewModel(),
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by mainViewModel.currentUser.collectAsState()

    val searchQuery by friendsViewModel.searchQuery.collectAsState()
    val searchResults by friendsViewModel.searchResults.collectAsState()
    val incomingRequests by friendsViewModel.incomingRequests.collectAsState()
    val outgoingRequests by friendsViewModel.outgoingRequests.collectAsState()
    val friendsList by friendsViewModel.friendsList.collectAsState()
    val uiState by friendsViewModel.uiState.collectAsState()

    var activeTab by remember { mutableStateOf(FriendsTab.MY_FRIENDS) }
    var friendToRemove by remember { mutableStateOf<Friendship?>(null) }

    val userId = currentUser?.id ?: "guest"

    LaunchedEffect(userId) {
        friendsViewModel.initializeUser(userId)
    }

    LaunchedEffect(activeTab, userId) {
        if (activeTab == FriendsTab.FIND_FRIENDS) {
            friendsViewModel.onSearchQueryChanged(searchQuery, userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VcBackground)
    ) {
        VcTopBar(
            title = "VC Connect",
            avatarName = currentUser?.displayName ?: "User",
            onProfileClick = onProfileClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Friends & Community",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VcTextPrimary
            )
            Text(
                text = "Connect with teammates and launch instant voice rooms",
                fontSize = 13.sp,
                color = VcTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Tab Headers Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabChip(
                    text = "Friends (${friendsList.size})",
                    selected = activeTab == FriendsTab.MY_FRIENDS,
                    onClick = { activeTab = FriendsTab.MY_FRIENDS },
                    modifier = Modifier.weight(1f)
                )
                TabChip(
                    text = "Received (${incomingRequests.size})",
                    selected = activeTab == FriendsTab.REQUESTS_RECEIVED,
                    onClick = { activeTab = FriendsTab.REQUESTS_RECEIVED },
                    modifier = Modifier.weight(1f)
                )
                TabChip(
                    text = "Sent (${outgoingRequests.size})",
                    selected = activeTab == FriendsTab.REQUESTS_SENT,
                    onClick = { activeTab = FriendsTab.REQUESTS_SENT },
                    modifier = Modifier.weight(1f)
                )
                TabChip(
                    text = "Find",
                    selected = activeTab == FriendsTab.FIND_FRIENDS,
                    onClick = { activeTab = FriendsTab.FIND_FRIENDS },
                    modifier = Modifier.weight(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Tab Content
            when (activeTab) {
                FriendsTab.MY_FRIENDS -> {
                    MyFriendsTabContent(
                        friends = friendsList,
                        onInviteToVc = { friend ->
                            // Create room & open Share Sheet
                            mainViewModel.createVcRoom(
                                roomName = "${currentUser?.displayName ?: "Gamer"}'s Room",
                                privacyMode = "link"
                            )
                            val inviteLink = "https://vcconnect.app/room/invite"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Hey ${friend.friendDisplayName}! Join my voice room on VC Connect: $inviteLink")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Invite ${friend.friendDisplayName} to VC"))
                        },
                        onRemoveClick = { friend ->
                            friendToRemove = friend
                        }
                    )
                }

                FriendsTab.REQUESTS_RECEIVED -> {
                    RequestsReceivedTabContent(
                        requests = incomingRequests,
                        onAccept = { req -> friendsViewModel.acceptRequest(req) },
                        onDecline = { reqId -> friendsViewModel.declineRequest(reqId) }
                    )
                }

                FriendsTab.REQUESTS_SENT -> {
                    RequestsSentTabContent(
                        requests = outgoingRequests,
                        onCancel = { reqId -> friendsViewModel.cancelRequest(reqId) }
                    )
                }

                FriendsTab.FIND_FRIENDS -> {
                    FindFriendsTabContent(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        outgoingRequests = outgoingRequests,
                        onQueryChanged = { friendsViewModel.onSearchQueryChanged(it, userId) },
                        onAddFriendClick = { recipient ->
                            val senderProfile = currentUser ?: UserProfile(
                                id = userId,
                                displayName = "Gamer",
                                username = "@gamer"
                            )
                            friendsViewModel.sendFriendRequest(senderProfile, recipient)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Confirmation Dialog for Friend Removal
    if (friendToRemove != null) {
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = {
                Text("Remove Friend", color = VcTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to remove ${friendToRemove?.friendDisplayName} from your friends list?",
                    color = VcTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        friendToRemove?.let { friend ->
                            friendsViewModel.removeFriend(userId, friend.friendUserId)
                        }
                        friendToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VcErrorRed)
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { friendToRemove = null }) {
                    Text("Cancel", color = VcTextSecondary)
                }
            },
            containerColor = VcSurfaceCard
        )
    }
}

@Composable
fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) VcPrimaryPurple else VcSurfaceCard)
            .border(
                width = 1.dp,
                color = if (selected) VcPrimaryPurple else VcBorderSubtle,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else VcTextSecondary
        )
    }
}

@Composable
fun MyFriendsTabContent(
    friends: List<Friendship>,
    onInviteToVc: (Friendship) -> Unit,
    onRemoveClick: (Friendship) -> Unit
) {
    if (friends.isEmpty()) {
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = VcTextMuted,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No friends added yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )
                Text(
                    text = "Use the 'Find' tab to search teammates and send friend requests!",
                    fontSize = 13.sp,
                    color = VcTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(friends) { friend ->
                VcCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                VcAvatar(
                                    name = friend.friendDisplayName,
                                    avatarUrl = friend.friendAvatarUrl,
                                    size = 44.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (friend.isOnline) VcSuccessGreen else VcTextMuted)
                                        .border(2.dp, VcSurfaceCard, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = friend.friendDisplayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VcTextPrimary
                                )
                                Text(
                                    text = friend.friendUsername.ifBlank { "@gamer" },
                                    fontSize = 12.sp,
                                    color = VcSecondaryViolet
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (friend.isOnline) VcSuccessGreen else VcTextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (friend.isOnline) {
                                            if (friend.activityStatus.isNotBlank()) friend.activityStatus else "Online • In VC Lobby"
                                        } else {
                                            val lastActiveStr = formatLastActiveTimestamp(friend.lastActive, friend.lastSeenText)
                                            "Offline • Last active $lastActiveStr"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (friend.isOnline) VcSuccessGreen else VcTextMuted
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onInviteToVc(friend) },
                                colors = ButtonDefaults.buttonColors(containerColor = VcPrimaryPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(onClick = { onRemoveClick(friend) }) {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Remove Friend",
                                    tint = VcTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestsReceivedTabContent(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (String) -> Unit
) {
    if (requests.isEmpty()) {
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No pending friend requests",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )
                Text(
                    text = "Incoming requests from other players will appear here.",
                    fontSize = 13.sp,
                    color = VcTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(requests) { req ->
                VcCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VcAvatar(name = req.senderName, avatarUrl = req.senderAvatar, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = req.senderName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VcTextPrimary
                                )
                                Text(
                                    text = req.senderUsername.ifBlank { "@gamer" },
                                    fontSize = 12.sp,
                                    color = VcTextMuted
                                )
                            }
                        }

                        Row {
                            Button(
                                onClick = { onAccept(req) },
                                colors = ButtonDefaults.buttonColors(containerColor = VcSuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedButton(
                                onClick = { onDecline(req.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = VcErrorRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestsSentTabContent(
    requests: List<FriendRequest>,
    onCancel: (String) -> Unit
) {
    if (requests.isEmpty()) {
        VcCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No sent requests",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VcTextPrimary
                )
                Text(
                    text = "Requests you send to other gamers will be tracked here.",
                    fontSize = 13.sp,
                    color = VcTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(requests) { req ->
                VcCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VcAvatar(name = req.recipientName, avatarUrl = req.recipientAvatar, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = req.recipientName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VcTextPrimary
                                )
                                Text(
                                    text = "Pending approval",
                                    fontSize = 12.sp,
                                    color = VcSecondaryViolet
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onCancel(req.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp, color = VcTextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FindFriendsTabContent(
    searchQuery: String,
    searchResults: List<UserProfile>,
    outgoingRequests: List<FriendRequest>,
    onQueryChanged: (String) -> Unit,
    onAddFriendClick: (UserProfile) -> Unit
) {
    Column {
        VcTextField(
            value = searchQuery,
            onValueChange = onQueryChanged,
            label = "Search by @username, name, or User ID",
            leadingIcon = Icons.Default.Search,
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = VcTextMuted
                        )
                    }
                }
            } else null
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
            VcCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching players found for '$searchQuery'",
                        fontSize = 14.sp,
                        color = VcTextMuted
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(searchResults) { user ->
                    val isAlreadyPending = outgoingRequests.any { it.recipientId == user.id }

                    VcCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box {
                                    VcAvatar(
                                        name = user.displayName,
                                        avatarUrl = user.avatarUrl,
                                        size = 44.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (user.isOnline) VcSuccessGreen else VcTextMuted)
                                            .border(2.dp, VcSurfaceCard, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName.ifBlank { "Gamer" },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VcTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (user.isOnline) VcSuccessGreen.copy(alpha = 0.2f) else VcElevatedPanel)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (user.isOnline) "Online" else "Offline",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (user.isOnline) VcSuccessGreen else VcTextMuted
                                            )
                                        }
                                    }
                                    Text(
                                        text = user.username.ifBlank { "@gamer" },
                                        fontSize = 12.sp,
                                        color = VcTextSecondary
                                    )
                                    if (user.activityStatus.isNotBlank()) {
                                        Text(
                                            text = user.activityStatus,
                                            fontSize = 11.sp,
                                            color = if (user.isOnline) VcSuccessGreen else VcTextMuted
                                        )
                                    }
                                }
                            }

                            if (isAlreadyPending) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VcElevatedPanel)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Pending",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VcSecondaryViolet
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onAddFriendClick(user) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VcPrimaryPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Friend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatLastActiveTimestamp(timestamp: com.google.firebase.Timestamp?, fallback: String): String {
    if (timestamp == null) return fallback.ifBlank { "recently" }
    val diff = System.currentTimeMillis() - timestamp.toDate().time
    if (diff <= 0) return "Just now"
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "a while ago"
    }
}
