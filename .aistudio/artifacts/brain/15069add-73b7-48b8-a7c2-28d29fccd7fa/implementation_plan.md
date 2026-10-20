# VC Connect — Friend Request System & Social Network

VC Connect is being expanded to include a complete, real-time Social Friend System allowing gamers to discover teammates by unique handles, manage incoming/outgoing friend requests, build mutual friend lists, and launch 1-tap voice room invitations.

## User Review & Confirmed Decisions

> [!IMPORTANT]
> The following user preferences were explicitly confirmed:

- **Username Requirement**: Users choose a unique `@username` upon first login / profile completion.
- **VC Room Invitations**: Tapping "Invite to VC" from the My Friends list creates an active VC room and opens the system Share sheet with a valid invitation link.
- **Visual Integration**: 100% dark gaming aesthetic (#05060B background, #10131B surface cards, #6755FF purple accents) matching all existing screens.

---

## 1. Overview & Core Features

1. **4-Tab Responsive Friends Screen**:
   - **My Friends**: Active accepted friends list with online/offline indicators, 1-tap "Invite to VC", and "Remove Friend" with confirmation.
   - **Requests Received**: Incoming pending requests displaying sender avatar, handle, request date, with "Accept" and "Decline" buttons.
   - **Requests Sent**: Outgoing pending requests with "Cancel Request" action.
   - **Find Friends**: Search bar querying registered users by `@username`, user ID, or display name, displaying "Add Friend" or "Pending" status.

2. **Firestore Social Data Architecture**:
   - `/users/{userId}`: `username`, `displayName`, `avatarUrl`, `isOnline`
   - `/friendRequests/{requestId}`: `senderId`, `senderName`, `senderUsername`, `recipientId`, `recipientName`, `recipientUsername`, `status` (`pending`/`accepted`/`declined`), `createdAt`
   - `/friendships/{friendshipId}`: `user1Id`, `user2Id`, `user1Name`, `user2Name`, `user1Username`, `user2Username`, `createdAt`

3. **Real-time Synchronization**:
   - Firestore snapshot listeners updating incoming/outgoing requests and friend lists instantly across devices.

---

## 2. Technical Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Friends Navigation Tab Bar                      │
│ ┌────────────────┐ ┌───────────────────┐ ┌─────────────┐ ┌────────────┐ │
│ │ My Friends     │ │ Requests Received │ │ Requests    │ │ Find       │ │
│ │ List          │ │ List              │ │ Sent List   │ │ Friends    │ │
│ └───────┬────────┘ └─────────┬─────────┘ └──────┬──────┘ └─────┬──────┘ │
└─────────┼────────────────────┼──────────────────┼──────────────┼────────┘
          │                    │                  │              │
┌─────────▼────────────────────▼──────────────────▼──────────────▼────────┐
│                          FriendsViewModel                               │
│ ┌───────────────────┐ ┌──────────────────────┐ ┌─────────────────────┐ │
│ │ Observe Friends   │ │ Handle Requests      │ │ User Search Engine  │ │
│ └─────────┬─────────┘ └──────────┬───────────┘ └──────────┬──────────┘ │
└───────────┼──────────────────────┼────────────────────────┼────────────┘
            │                      │                        │
┌───────────▼──────────────────────▼────────────────────────▼────────────┐
│                          FriendRepository                              │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │ Firestore Collections: /friendRequests, /friendships, /users      │ │
│ └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Implementation Steps

1. **Firestore Schema IR & Security Rules**:
   - Update `firebase-blueprint.json` and `firestore.rules` for `/friendRequests`, `/friendships`, and `/users` handles.
2. **Data & Repository Layer**:
   - Create `FriendRepository.kt` handling user search, request creation, accept/decline transactions, and real-time listeners.
3. **UI Components & ViewModels**:
   - Create `FriendsViewModel.kt` and `FriendsScreen.kt` featuring 4 tabs, search input, user cards, and confirmation dialogs.
4. **Navigation Integration**:
   - Add Friends tab to `VcBottomNavigationBar` in `MainActivity.kt`.
5. **Verification**:
   - Run `compile_applet` and `lint_applet` to confirm zero build or syntax errors.
