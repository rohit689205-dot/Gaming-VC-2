package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.NavigationTab
import com.example.ui.components.VcBottomNavigationBar
import com.example.ui.components.VcTextField
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateVcScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JoinVcScreen
import com.example.ui.screens.LiveRoomScreen
import com.example.ui.screens.MeetingCreatedScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecentActivityScreen
import com.example.ui.theme.VcBackground
import com.example.ui.theme.VcConnectTheme
import com.example.ui.theme.VcPrimaryPurple
import com.example.ui.theme.VcSurfaceCard
import com.example.ui.theme.VcTextPrimary
import com.example.ui.theme.VcTextSecondary
import com.example.ui.viewmodel.FriendsViewModel
import com.example.ui.viewmodel.MainViewModel

enum class ScreenState {
    AUTH,
    MAIN_HOME,
    MAIN_FRIENDS,
    MAIN_RECENT,
    MAIN_PROFILE,
    CREATE_VC,
    MEETING_CREATED,
    JOIN_VC,
    LIVE_ROOM
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VcConnectTheme {
                VcConnectApp()
            }
        }
    }
}

@Composable
fun VcConnectApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()

    var currentScreen by remember { mutableStateOf(ScreenState.AUTH) }

    // Request permissions launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions granted
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    var showUsernamePrompt by remember { mutableStateOf(false) }
    var inputUsername by remember { mutableStateOf("") }

    val friendsViewModel: FriendsViewModel = viewModel()
    val incomingRequests by friendsViewModel.incomingRequests.collectAsState()

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { uid ->
            friendsViewModel.initializeUser(uid)
        }
    }

    // Sync auth state with screen state
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            currentScreen = ScreenState.AUTH
        } else {
            if (currentScreen == ScreenState.AUTH) {
                currentScreen = ScreenState.MAIN_HOME
            }
            if (currentUser?.username.isNullOrBlank()) {
                inputUsername = "@gamer_${currentUser?.id?.takeLast(4)}"
                showUsernamePrompt = true
            }
        }
    }

    // Auto navigate to live room when active
    LaunchedEffect(activeRoom) {
        if (activeRoom != null) {
            currentScreen = ScreenState.LIVE_ROOM
        }
    }

    // Handle Back Button navigation
    BackHandler(enabled = currentScreen != ScreenState.MAIN_HOME && currentScreen != ScreenState.AUTH) {
        when (currentScreen) {
            ScreenState.CREATE_VC, ScreenState.JOIN_VC, ScreenState.MEETING_CREATED -> {
                currentScreen = ScreenState.MAIN_HOME
            }
            ScreenState.LIVE_ROOM -> {
                currentScreen = ScreenState.MAIN_HOME
            }
            ScreenState.MAIN_FRIENDS, ScreenState.MAIN_RECENT, ScreenState.MAIN_PROFILE -> {
                currentScreen = ScreenState.MAIN_HOME
            }
            else -> {}
        }
    }

    val isMainTabScreen = currentScreen == ScreenState.MAIN_HOME ||
            currentScreen == ScreenState.MAIN_FRIENDS ||
            currentScreen == ScreenState.MAIN_RECENT ||
            currentScreen == ScreenState.MAIN_PROFILE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isMainTabScreen && currentUser != null) {
                val currentTab = when (currentScreen) {
                    ScreenState.MAIN_FRIENDS -> NavigationTab.FRIENDS
                    ScreenState.MAIN_RECENT -> NavigationTab.RECENT
                    ScreenState.MAIN_PROFILE -> NavigationTab.PROFILE
                    else -> NavigationTab.HOME
                }
                VcBottomNavigationBar(
                    currentTab = currentTab,
                    pendingRequestsCount = incomingRequests.size,
                    onTabSelected = { tab ->
                        currentScreen = when (tab) {
                            NavigationTab.HOME -> ScreenState.MAIN_HOME
                            NavigationTab.FRIENDS -> ScreenState.MAIN_FRIENDS
                            NavigationTab.RECENT -> ScreenState.MAIN_RECENT
                            NavigationTab.PROFILE -> ScreenState.MAIN_PROFILE
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VcBackground)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenState.AUTH -> {
                    AuthScreen(
                        viewModel = viewModel,
                        onAuthSuccess = { currentScreen = ScreenState.MAIN_HOME }
                    )
                }

                ScreenState.MAIN_HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onCreateVcClick = { currentScreen = ScreenState.CREATE_VC },
                        onJoinVcClick = { currentScreen = ScreenState.JOIN_VC },
                        onProfileClick = { currentScreen = ScreenState.MAIN_PROFILE }
                    )
                }

                ScreenState.MAIN_FRIENDS -> {
                    FriendsScreen(
                        mainViewModel = viewModel,
                        friendsViewModel = friendsViewModel,
                        onProfileClick = { currentScreen = ScreenState.MAIN_PROFILE }
                    )
                }

                ScreenState.MAIN_RECENT -> {
                    RecentActivityScreen(
                        viewModel = viewModel,
                        onRejoinClick = { code ->
                            viewModel.joinVcRoomByCode(code)
                        },
                        onProfileClick = { currentScreen = ScreenState.MAIN_PROFILE }
                    )
                }

                ScreenState.MAIN_PROFILE -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onSignOutClick = { currentScreen = ScreenState.AUTH }
                    )
                }

                ScreenState.CREATE_VC -> {
                    CreateVcScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = ScreenState.MAIN_HOME },
                        onMeetingCreatedSuccess = { currentScreen = ScreenState.MEETING_CREATED }
                    )
                }

                ScreenState.MEETING_CREATED -> {
                    MeetingCreatedScreen(
                        viewModel = viewModel,
                        onJoinMeetingNow = { currentScreen = ScreenState.LIVE_ROOM },
                        onCloseClick = { currentScreen = ScreenState.MAIN_HOME }
                    )
                }

                ScreenState.JOIN_VC -> {
                    JoinVcScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = ScreenState.MAIN_HOME },
                        onJoinSuccess = { currentScreen = ScreenState.LIVE_ROOM }
                    )
                }

                ScreenState.LIVE_ROOM -> {
                    LiveRoomScreen(
                        viewModel = viewModel,
                        onLeaveRoomClick = { currentScreen = ScreenState.MAIN_HOME }
                    )
                }
            }
        }
    }

    val currUser = currentUser
    if (showUsernamePrompt) {
        AlertDialog(
            onDismissRequest = { /* mandatory selection */ },
            title = {
                Text("Choose Your Gamer Tag", color = VcTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                VcTextField(
                    value = inputUsername,
                    onValueChange = { inputUsername = it },
                    label = "Unique Username (@username)"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalUsername = if (inputUsername.startsWith("@")) inputUsername else "@${inputUsername.trim()}"
                        if (currUser != null) {
                            viewModel.updateUserProfileDetails(
                                displayName = currUser.displayName,
                                username = finalUsername,
                                activityStatus = currUser.activityStatus,
                                isOnline = true
                            )
                        }
                        showUsernamePrompt = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VcPrimaryPurple)
                ) {
                    Text("Set Gamer Tag", color = Color.White)
                }
            },
            containerColor = VcSurfaceCard
        )
    }
}

