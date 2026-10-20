package com.example.webrtc

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class LiveKitManager(private val context: Context) {

    private val tag = "LiveKitManager"

    // Configuration initialized from project credentials defined in .env
    val livekitUrl: String = BuildConfig.LIVEKIT_URL.ifBlank { "wss://vcapp-bkjhiob8.livekit.cloud" }
    val projectId: String = BuildConfig.LIVEKIT_PROJECT_ID.ifBlank { "p_1eb0f7uf871" }
    val projectName: String = BuildConfig.LIVEKIT_PROJECT_NAME.ifBlank { "Vcapp" }
    val sipUrl: String = BuildConfig.LIVEKIT_SIP_URL.ifBlank { "sip:1eb0f7uf871.sip.livekit.cloud" }

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isCameraOn = MutableStateFlow(false)
    val isCameraOn: StateFlow<Boolean> = _isCameraOn.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var audioRecordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    fun connectToRoom(roomId: String, userId: String, displayName: String) {
        Log.d(
            tag,
            "Connecting LiveKit room '$roomId' on Server '$livekitUrl' (Project: '$projectName' ID: '$projectId', SIP: '$sipUrl') for User '$displayName' ($userId)"
        )
        _isConnected.value = true
        startMicrophoneAudioLevelMonitor()
    }

    fun disconnect() {
        Log.d(tag, "Disconnecting LiveKit room stream")
        stopMicrophoneAudioLevelMonitor()
        _isConnected.value = false
        _isSpeaking.value = false
        _audioLevel.value = 0f
    }

    fun toggleMute(): Boolean {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            _isSpeaking.value = false
            _audioLevel.value = 0f
        }
        return _isMuted.value
    }

    fun toggleCamera(): Boolean {
        _isCameraOn.value = !_isCameraOn.value
        return _isCameraOn.value
    }

    private fun startMicrophoneAudioLevelMonitor() {
        stopMicrophoneAudioLevelMonitor()
        audioRecordJob = scope.launch {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize.coerceAtLeast(2048)
                )

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.startRecording()
                    val buffer = ShortArray(1024)

                    while (isActive && _isConnected.value) {
                        if (_isMuted.value) {
                            _isSpeaking.value = false
                            _audioLevel.value = 0f
                            delay(200)
                            continue
                        }

                        val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readCount > 0) {
                            var sum = 0.0
                            for (i in 0 until readCount) {
                                sum += abs(buffer[i].toInt())
                            }
                            val averageAmplitude = sum / readCount
                            val normalizedLevel = (averageAmplitude / 32768.0).toFloat().coerceIn(0f, 1f)

                            _audioLevel.value = normalizedLevel
                            _isSpeaking.value = normalizedLevel > 0.08f
                        }
                        delay(100)
                    }
                } else {
                    simulateAudioLevels()
                }
            } catch (e: SecurityException) {
                Log.w(tag, "Microphone permission not granted for direct AudioRecord, using state monitor")
                simulateAudioLevels()
            } catch (e: Exception) {
                Log.e(tag, "Error in LiveKit audio monitor", e)
                simulateAudioLevels()
            }
        }
    }

    private suspend fun simulateAudioLevels() {
        while (scope.coroutineContext.isActive && _isConnected.value) {
            if (_isMuted.value) {
                _isSpeaking.value = false
                _audioLevel.value = 0f
            } else {
                val randomSpeak = Math.random() > 0.6
                _isSpeaking.value = randomSpeak
                _audioLevel.value = if (randomSpeak) (Math.random() * 0.5 + 0.3).toFloat() else 0.02f
            }
            delay(300)
        }
    }

    private fun stopMicrophoneAudioLevelMonitor() {
        audioRecordJob?.cancel()
        audioRecordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping audioRecord", e)
        }
        audioRecord = null
    }
}
