package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.UserProfile
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthenticationManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    val currentUser get() = auth.currentUser

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    suspend fun signInAsGuest(guestName: String = "Guest Gamer"): Result<UserProfile> {
        return try {
            val user = try {
                val result = auth.signInAnonymously().await()
                result.user
            } catch (e: Exception) {
                Log.w("AuthenticationManager", "Firebase anonymous auth failed/disabled, using instant guest session", e)
                null
            }

            val uid = user?.uid ?: "guest_${System.currentTimeMillis().toString().takeLast(6)}"
            val name = if (guestName.isNotBlank()) guestName else "Guest #${uid.takeLast(4).uppercase()}"

            val profile = UserProfile(
                id = uid,
                displayName = name,
                email = "guest_${uid.take(6)}@vcconnect.app",
                avatarUrl = ""
            )

            if (user != null) {
                saveUserProfile(profile)
            }
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("AuthenticationManager", "Guest sign in fallback", e)
            val fallbackUid = "guest_${System.currentTimeMillis().toString().takeLast(6)}"
            val fallbackProfile = UserProfile(
                id = fallbackUid,
                displayName = if (guestName.isNotBlank()) guestName else "Guest Gamer",
                email = "guest@vcconnect.app"
            )
            Result.success(fallbackProfile)
        }
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager
    ): Result<UserProfile> {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return Result.failure(Exception("Google Sign-In Web Client ID missing: ${e.message}"))
        }

        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: return Result.failure(Exception("Google Auth failed: null user"))

                val profile = UserProfile(
                    id = user.uid,
                    displayName = user.displayName ?: "Gamer",
                    email = user.email ?: "",
                    avatarUrl = user.photoUrl?.toString() ?: ""
                )
                saveUserProfile(profile)
                Result.success(profile)
            } else {
                Result.failure(Exception("Unexpected credential type returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthenticationManager", "Google Sign-In cancelled: ${e.message}")
            Result.failure(Exception("Sign-In cancelled"))
        } catch (e: Exception) {
            Log.e("AuthenticationManager", "Google Sign-In error", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(userId: String): UserProfile? {
        if (userId.startsWith("guest_")) {
            return UserProfile(
                id = userId,
                displayName = "Guest Gamer",
                email = "guest@vcconnect.app"
            )
        }
        val docPath = "users/$userId"
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) doc.toObject(UserProfile::class.java) else null
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        val uid = auth.currentUser?.uid ?: profile.id
        if (uid.isBlank()) return false
        val docPath = "users/$uid"
        return try {
            val data = mutableMapOf<String, Any>(
                "displayName" to profile.displayName,
                "username" to profile.username,
                "email" to profile.email,
                "avatarUrl" to profile.avatarUrl,
                "isOnline" to profile.isOnline,
                "activityStatus" to profile.activityStatus,
                "lastSeenText" to profile.lastSeenText,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(data).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            false
        }
    }

    suspend fun uploadProfilePhoto(userId: String, imageUri: android.net.Uri): Result<String> {
        if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))
        return try {
            val storageRef = try {
                val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
                storage.reference.child("profile_photos/$userId.jpg")
            } catch (e: Exception) {
                null
            }

            val downloadUrl = if (storageRef != null) {
                try {
                    storageRef.putFile(imageUri).await()
                    storageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    Log.w("AuthenticationManager", "Firebase Storage upload failed, fallback to base64: ${e.message}")
                    createBase64DataUrl(imageUri)
                }
            } else {
                createBase64DataUrl(imageUri)
            }

            if (downloadUrl.isNotBlank()) {
                val currentProfile = fetchUserProfile(userId) ?: UserProfile(id = userId)
                val updated = currentProfile.copy(avatarUrl = downloadUrl)
                saveUserProfile(updated)
                updateFriendshipAvatars(userId, downloadUrl)
                Result.success(downloadUrl)
            } else {
                Result.failure(Exception("Failed to process image"))
            }
        } catch (e: Exception) {
            Log.e("AuthenticationManager", "Error uploading profile photo", e)
            Result.failure(e)
        }
    }

    private fun createBase64DataUrl(imageUri: android.net.Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val base64Str = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64Str"
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun updateFriendshipAvatars(userId: String, avatarUrl: String) {
        try {
            val snapshot = db.collection("friendships")
                .whereEqualTo("friendUserId", userId)
                .get().await()
            for (doc in snapshot.documents) {
                db.collection("friendships").document(doc.id)
                    .update("friendAvatarUrl", avatarUrl).await()
            }
        } catch (e: Exception) {
            Log.w("AuthenticationManager", "Failed updating friendship avatars: ${e.message}")
        }
    }

    suspend fun signOut(credentialManager: CredentialManager) {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthenticationManager", "Failed clearing credential state", e)
        }
    }
}

