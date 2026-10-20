package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FriendRepository
import com.example.model.FriendRequest
import com.example.model.Friendship
import com.example.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val friendRepo = FriendRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRequest>> = _incomingRequests.asStateFlow()

    private val _outgoingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val outgoingRequests: StateFlow<List<FriendRequest>> = _outgoingRequests.asStateFlow()

    private val _friendsList = MutableStateFlow<List<Friendship>>(emptyList())
    val friendsList: StateFlow<List<Friendship>> = _friendsList.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    private var allUsersJob: Job? = null
    private var incomingJob: Job? = null
    private var outgoingJob: Job? = null
    private var friendsJob: Job? = null

    private var rawFriendsList: List<Friendship> = emptyList()

    private fun updateFriendsWithUserProfiles() {
        val usersMap = _allUsers.value.associateBy { it.id }
        _friendsList.value = rawFriendsList.map { friendship ->
            val userProfile = usersMap[friendship.friendUserId]
            if (userProfile != null) {
                friendship.copy(
                    isOnline = userProfile.isOnline,
                    activityStatus = userProfile.activityStatus,
                    lastSeenText = userProfile.lastSeenText,
                    lastActive = userProfile.lastActive ?: friendship.lastActive,
                    friendDisplayName = userProfile.displayName.ifBlank { friendship.friendDisplayName },
                    friendAvatarUrl = userProfile.avatarUrl.ifBlank { friendship.friendAvatarUrl }
                )
            } else {
                friendship
            }
        }
    }

    fun initializeUser(userId: String) {
        if (userId.isBlank()) return

        allUsersJob?.cancel()
        allUsersJob = viewModelScope.launch {
            friendRepo.observeAllUsers(userId).collectLatest { users ->
                _allUsers.value = users
                filterSearchResults(_searchQuery.value)
                updateFriendsWithUserProfiles()
            }
        }

        incomingJob?.cancel()
        incomingJob = viewModelScope.launch {
            friendRepo.observeIncomingRequests(userId).collectLatest { list ->
                _incomingRequests.value = list
            }
        }

        outgoingJob?.cancel()
        outgoingJob = viewModelScope.launch {
            friendRepo.observeOutgoingRequests(userId).collectLatest { list ->
                _outgoingRequests.value = list
            }
        }

        friendsJob?.cancel()
        friendsJob = viewModelScope.launch {
            friendRepo.observeFriends(userId).collectLatest { list ->
                rawFriendsList = list
                updateFriendsWithUserProfiles()
            }
        }
    }

    fun onSearchQueryChanged(query: String, currentUserId: String) {
        _searchQuery.value = query
        filterSearchResults(query)
    }

    private fun filterSearchResults(query: String) {
        val cleanQuery = query.trim().lowercase().removePrefix("@")
        val users = _allUsers.value
        _searchResults.value = if (cleanQuery.isBlank()) {
            users
        } else {
            users.filter { user ->
                user.username.lowercase().contains(cleanQuery) ||
                user.displayName.lowercase().contains(cleanQuery) ||
                user.id.lowercase() == cleanQuery
            }
        }
    }


    fun sendFriendRequest(sender: UserProfile, recipient: UserProfile) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = friendRepo.sendFriendRequest(sender, recipient)
            result.onSuccess {
                _uiState.value = UiState.Success("Friend request sent to ${recipient.displayName}!")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed sending friend request")
            }
        }
    }

    fun acceptRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = friendRepo.acceptFriendRequest(request)
            result.onSuccess {
                _uiState.value = UiState.Success("Friend request accepted!")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed accepting request")
            }
        }
    }

    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = friendRepo.declineFriendRequest(requestId)
            result.onSuccess {
                _uiState.value = UiState.Success("Request declined")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed declining request")
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = friendRepo.cancelFriendRequest(requestId)
            result.onSuccess {
                _uiState.value = UiState.Success("Request cancelled")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed cancelling request")
            }
        }
    }

    fun removeFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = friendRepo.removeFriend(currentUserId, friendId)
            result.onSuccess {
                _uiState.value = UiState.Success("Friend removed")
            }.onFailure { err ->
                _uiState.value = UiState.Error(err.message ?: "Failed removing friend")
            }
        }
    }

    fun updateUserStatus(currentUserId: String, isOnline: Boolean, activityStatus: String, lastSeenText: String = "Just now") {
        viewModelScope.launch {
            friendRepo.updateUserStatus(currentUserId, isOnline, activityStatus, lastSeenText)
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }
}

