package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

/**
 * Handles emergency siren synthesis, haptic vibration, and Gujarati Text-to-Speech
 */
class AmbulanceAudioController(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private var sirenJob: Job? = null
    private var isSirenPlaying = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val gujaratiLocale = Locale("gu", "IN")
            val result = textToSpeech?.setLanguage(gujaratiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Indian English or default locale
                textToSpeech?.setLanguage(Locale("en", "IN"))
            }
            textToSpeech?.setPitch(1.05f)
            textToSpeech?.setSpeechRate(0.95f)
            isTtsReady = true
        }
    }

    fun speakGujarati(text: String) {
        if (!isTtsReady || textToSpeech == null) return
        try {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AMBULANCE_ALERT_TTS")
        } catch (_: Exception) {}
    }

    /**
     * Synthesizes realistic alternating dual-tone emergency ambulance siren
     */
    fun startSiren(scope: CoroutineScope) {
        if (isSirenPlaying) return
        isSirenPlaying = true

        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 4)

            var audioTrack: AudioTrack? = null
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()

                val durationSeconds = 0.25
                val numSamples = (durationSeconds * sampleRate).toInt()
                val lowToneSamples = ShortArray(numSamples)
                val highToneSamples = ShortArray(numSamples)

                val freqLow = 650.0  // Hz
                val freqHigh = 980.0 // Hz

                for (i in 0 until numSamples) {
                    val angleLow = 2.0 * Math.PI * i / (sampleRate / freqLow)
                    lowToneSamples[i] = (sin(angleLow) * 12000).toInt().toShort()

                    val angleHigh = 2.0 * Math.PI * i / (sampleRate / freqHigh)
                    highToneSamples[i] = (sin(angleHigh) * 12000).toInt().toShort()
                }

                while (isActive && isSirenPlaying) {
                    audioTrack.write(highToneSamples, 0, numSamples)
                    audioTrack.write(lowToneSamples, 0, numSamples)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopSiren() {
        isSirenPlaying = false
        sirenJob?.cancel()
        sirenJob = null
    }

    fun triggerEmergencyHaptics() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 200, 100, 300, 100, 400)
                val amplitudes = intArrayOf(0, 200, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        stopSiren()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }
}
