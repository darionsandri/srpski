package com.srpski.app.platform

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Thin wrapper around Android TTS for Serbian.
 * Text is Latin-script; the Google Serbian voice reads it correctly.
 */
class Tts(context: Context) {

    private var ready = false
    private val tts = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ready = true
        }
    }.also {
        it.language = Locale("sr", "RS")
    }

    fun speak(text: String, slow: Boolean = false) {
        if (!ready) return
        tts.setSpeechRate(if (slow) 0.7f else 1.0f)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "srpski")
    }

    fun shutdown() = tts.shutdown()
}
