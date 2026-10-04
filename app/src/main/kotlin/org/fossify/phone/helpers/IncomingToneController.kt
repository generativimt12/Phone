package org.fossify.phone.helpers

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.telecom.Call
import android.telecom.TelecomManager
import org.fossify.phone.extensions.getStateCompat

object IncomingToneController {
    private const val PREFS = "incoming_tone_control"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_URI = "uri"
    private var player: MediaPlayer? = null

    fun isEnabled(context: Context) = context.getSharedPreferences(PREFS, 0).getBoolean(KEY_ENABLED, true)
    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (!enabled) stop()
    }
    fun getUri(context: Context): Uri? = context.getSharedPreferences(PREFS, 0).getString(KEY_URI, null)?.let(Uri::parse)
    fun setUri(context: Context, uri: Uri?) { context.getSharedPreferences(PREFS, 0).edit().putString(KEY_URI, uri?.toString()).apply() }

    fun silenceSystemRinger(context: Context) {
        try { context.getSystemService(TelecomManager::class.java)?.silenceRinger() } catch (_: Exception) {}
    }

    fun start(context: Context, call: Call) {
        if (!isEnabled(context) || call.getStateCompat() != Call.STATE_RINGING) return
        stop()
        silenceSystemRinger(context)
        val uri = getUri(context) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) ?: Settings.System.DEFAULT_NOTIFICATION_URI
        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            mp.setDataSource(context, uri)
            mp.isLooping = true
            mp.setOnErrorListener { _, _, _ -> stop(); true }
            mp.prepare()
            mp.start()
            player = mp
        } catch (_: Exception) { stop() }
    }

    fun stop() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }
}
