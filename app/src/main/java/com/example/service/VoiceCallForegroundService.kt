package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class VoiceCallForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "vc_connect_voice_channel"
        const val NOTIFICATION_ID = 8801
        
        const val ACTION_START = "ACTION_START_VOICE_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_VOICE_SERVICE"
        const val ACTION_TOGGLE_MUTE = "ACTION_TOGGLE_MUTE"
        
        const val EXTRA_ROOM_NAME = "EXTRA_ROOM_NAME"
        const val EXTRA_IS_MUTED = "EXTRA_IS_MUTED"

        fun startService(context: Context, roomName: String, isMuted: Boolean = false) {
            val intent = Intent(context, VoiceCallForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ROOM_NAME, roomName)
                putExtra(EXTRA_IS_MUTED, isMuted)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VoiceCallForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var currentRoomName: String = "Voice Room"
    private var isMuted: Boolean = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                currentRoomName = intent.getStringExtra(EXTRA_ROOM_NAME) ?: "Voice Room"
                isMuted = intent.getBooleanExtra(EXTRA_IS_MUTED, false)
                startForegroundWithNotification()
            }
            ACTION_TOGGLE_MUTE -> {
                isMuted = !isMuted
                updateNotification()
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleMuteIntent = Intent(this, VoiceCallForegroundService::class.java).apply {
            action = ACTION_TOGGLE_MUTE
        }
        val toggleMutePendingIntent = PendingIntent.getService(
            this, 1, toggleMuteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val leaveIntent = Intent(this, VoiceCallForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val leavePendingIntent = PendingIntent.getService(
            this, 2, leaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val muteText = if (isMuted) "Unmute Mic" else "Mute Mic"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VC Connect — Active Call")
            .setContentText("Connected to $currentRoomName ${if (isMuted) "(Muted)" else "(Mic Live)"}")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(android.R.drawable.ic_lock_silent_mode, muteText, toggleMutePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Leave Call", leavePendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VC Connect Live Voice Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ongoing notification for VC Connect background voice chat"
                setSound(null, null)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
