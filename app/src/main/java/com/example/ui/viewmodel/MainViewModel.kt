package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.FriendRepository
import com.example.data.MeetingHistoryRepository
import com.example.data.RoomRepository
import com.example.model.MeetingHistoryItem
import com.example.model.RoomMember
import com.example.model.UserProfile
import com.example.model.VcRoom
import com.example.service.VoiceCallForegroundService
import com.example.webrtc.VoiceStreamManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String? = null) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    private val friendRepo = FriendRepository(application)
    private val roomRepo = RoomRepository(application)
    private val historyRepo = MeetingHistoryRepository(application)
    val voiceStreamManager = VoiceStreamManager(application)
    val credentialManager = CredentialManager.create(application)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _activeRoom = MutableStateFlow<VcRoom?>(null)
    val activeRoom: StateFlow<VcRoom?> = _activeRoom.asStateFlow()

    private val _activeRoomMembers = MutableStateFlow<List<RoomMember>>(emptyList())
    val activeRoomMembers: StateFlow<List<RoomMember>> = _activeRoomMembers.asStateFlow()

    private val _meetingHistory = MutableStateFlow<List<MeetingHistoryItem>>(emptyList())
    val meetingHistory: StateFlow<List<MeetingHistoryItem>> = _meetingHistory.asStateFlow()

    private val _createdRoomCreatedInfo = MutableStateFlow<VcRoom?>(null)
    val createdRoomCreatedInfo: StateFlow<VcRoom?> = _createdRoomCreatedInfo.asStateFlow()

    private var membersObserveJob: Job? = null
    private var historyObserveJob: Job? = null
    private var callDurationSeconds = 0
    private var timerJob: Job? = null

    init {
        checkCurrentAuthSession()
        observeVoiceStreamState()
        purgeGuestData()
    }

    fun purgeGuestData() {
        viewModelScope.launch {
            val result = friendRepo.deleteAllGuestData()
            result.onSuccess { count ->
                if (count > 0) {
                    Log.i("MainViewModel", "Purged $count guest records from database")
                }
            }
        }
    }

    private var heartbeatJob: Job? = null

    private fun startPresenceHeartbeat(userId: String) {
        heartbeatJob?.cancel()
        if (userId.isBlank()) return
        heartbeatJob = viewModelScope.launch {
            while (true) {
                friendRepo.sendHeartbeat(userId)
                delay(30_000L)
            }
        }
    }

    private fun checkCurrentAuthSession() {
        val user = authRepo.currentUser
        if (user != null) {
            viewModelScope.launch {
                val profile = authRepo.fetchUserProfile(user.uid) ?: UserProfile(
                    id = user.uid,
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Gamer",
                    email = user.email ?: ""
                )
                _currentUser.value = profile
                observeHistory(user.uid)
                startPresenceHeartbeat(profile.id)
            }
        }
    }

    private fun observeVoiceStreamState() {
        viewModelScope.launch {
            voiceStreamManager.isMuted.collectLatest { muted ->
                val room = _activeRoom.value
                val user = _currentUser.value
                if (room != null && user != null) {
                    roomRepo.updateMemberState(
                        roomId = room.roomId,
                        userId = user.id,
                        isMuted = muted,
                        isSpeaking = voiceStreamManager.isSpeaking.value
                    )
                }
            }
        }
        viewModelScope.launch {
            voiceStreamManager.isSpeaking.collectLatest { speaking ->
                val room = _activeRoom.value
                val user = _currentUser.value
                if (room != null && user != null) {
                    roomRepo.updateMemberState(
                        roomId = room.roomId,
                        userId = user.id,
                        isMuted = voiceStreamManager.isMuted.value,
                        isSpeaking = speaking
                    )
                }
            }
        }
    }

    private fun observeHistory(userId: String) {
        historyObserveJob?.cancel()
        historyObserveJob = viewModelScope.launch {
            historyRepo.observeMeetingHistory(userId).collectLatest { list ->
                _meetingHistory.value = list
            }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepo.signInWithGoogle(activity, credentialManager)
            result.onSuccess { profile ->
                _currentUser.value = profile
                _uiState.value = UiState.Success("Signed in with Google")
                observeHistory(profile.id)
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Google Sign-In failed")
            }
        }
    }

    fun signInAsGuest(guestName: String = "", username: String = "") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepo.signInAsGuest(guestName)
            result.onSuccess { profile ->
                val cleanUsername = when {
                    username.startsWith("@") -> username
                    username.isNotBlank() -> "@$username"
                    else -> "@guest_${profile.id.takeLast(4)}"
                }
                val updated = profile.copy(username = cleanUsername)
                authRepo.saveUserProfile(updated)
                _currentUser.value = updated
                _uiState.value = UiState.Success("Signed in as Guest")
                observeHistory(profile.id)
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Guest Sign-In failed")
            }
        }
    }

    fun createVcRoom(roomName: String, privacyMode: String) {
        val user = _currentUser.value ?: run {
            _uiState.value = UiState.Error("User not signed in.")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = roomRepo.createRoom(
                roomName = roomName,
                privacyMode = privacyMode,
                hostUserId = user.id,
                hostName = user.displayName
            )
            result.onSuccess { vcRoom ->
                _createdRoomCreatedInfo.value = vcRoom
                _uiState.value = UiState.Success("Room created successfully!")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Room creation failed")
            }
        }
    }

    fun joinVcRoomByCode(code: String) {
        val user = _currentUser.value ?: run {
            _uiState.value = UiState.Error("Please sign in first.")
            return
        }
        if (code.isBlank()) {
            _uiState.value = UiState.Error("Please enter a meeting code or link.")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = roomRepo.joinRoomByCode(
                inviteCode = code,
                userId = user.id,
                displayName = user.displayName
            )
            result.onSuccess { vcRoom ->
                connectToLiveRoom(vcRoom)
                _uiState.value = UiState.Success()
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed joining room")
            }
        }
    }

    fun connectToLiveRoom(vcRoom: VcRoom) {
        val user = _currentUser.value ?: return
        _activeRoom.value = vcRoom
        
        // Start LiveKit/WebRTC stream
        voiceStreamManager.connectToRoom(vcRoom.roomId, user.id, user.displayName)

        // Start Android Foreground Service for background voice audio
        VoiceCallForegroundService.startService(
            context = getApplication(),
            roomName = vcRoom.roomName,
            isMuted = voiceStreamManager.isMuted.value
        )

        // Listen for room members
        membersObserveJob?.cancel()
        membersObserveJob = viewModelScope.launch {
            roomRepo.observeRoomMembers(vcRoom.roomId).collectLatest { members ->
                _activeRoomMembers.value = members
            }
        }

        // Start Call Timer
        callDurationSeconds = 0
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                callDurationSeconds++
            }
        }
    }

    fun toggleMute() {
        val isMuted = voiceStreamManager.toggleMute()
        val room = _activeRoom.value
        if (room != null) {
            VoiceCallForegroundService.startService(getApplication(), room.roomName, isMuted)
        }
    }

    fun toggleCamera() {
        voiceStreamManager.toggleCamera()
    }

    fun leaveActiveRoom() {
        val room = _activeRoom.value
        val user = _currentUser.value
        if (room != null && user != null) {
            viewModelScope.launch {
                roomRepo.leaveRoom(room.roomId, user.id)
                historyRepo.addMeetingHistory(
                    userId = user.id,
                    item = MeetingHistoryItem(
                        roomId = room.roomId,
                        roomName = room.roomName,
                        hostUserId = room.hostUserId,
                        hostName = room.hostName,
                        inviteCode = room.inviteCode,
                        durationMinutes = (callDurationSeconds / 60).coerceAtLeast(1),
                        status = "Completed",
                        participantCount = _activeRoomMembers.value.size.coerceAtLeast(1)
                    )
                )
            }
        }

        VoiceCallForegroundService.stopService(getApplication())
        voiceStreamManager.disconnect()
        timerJob?.cancel()
        membersObserveJob?.cancel()
        _activeRoom.value = null
        _activeRoomMembers.value = emptyList()
    }

    fun updateProfileDisplayName(newName: String) {
        val user = _currentUser.value ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            val updated = user.copy(displayName = newName)
            if (authRepo.saveUserProfile(updated)) {
                _currentUser.value = updated
                _uiState.value = UiState.Success("Profile name updated!")
            }
        }
    }

    fun updateUserProfileDetails(
        displayName: String,
        username: String,
        activityStatus: String,
        isOnline: Boolean
    ) {
        val user = _currentUser.value ?: return
        val cleanUsername = if (username.startsWith("@")) username else if (username.isNotBlank()) "@$username" else user.username
        viewModelScope.launch {
            val updated = user.copy(
                displayName = displayName.ifBlank { user.displayName },
                username = cleanUsername,
                activityStatus = activityStatus,
                isOnline = isOnline,
                lastSeenText = if (isOnline) "Just now" else "10m ago"
            )
            if (authRepo.saveUserProfile(updated)) {
                _currentUser.value = updated
                _uiState.value = UiState.Success("Gamer profile & status updated!")
            }
        }
    }

    fun uploadProfilePhoto(imageUri: android.net.Uri) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepo.uploadProfilePhoto(user.id, imageUri)
            result.onSuccess { downloadUrl ->
                _currentUser.value = user.copy(avatarUrl = downloadUrl)
                _uiState.value = UiState.Success("Profile photo updated successfully!")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed to upload photo")
            }
        }
    }

    fun signOut() {

        leaveActiveRoom()
        viewModelScope.launch {
            authRepo.signOut(credentialManager)
            _currentUser.value = null
            _meetingHistory.value = emptyList()
            _uiState.value = UiState.Idle
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun clearCreatedRoomInfo() {
        _createdRoomCreatedInfo.value = null
    }

    fun getFormattedCallDuration(): String {
        val mins = callDurationSeconds / 60
        val secs = callDurationSeconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
