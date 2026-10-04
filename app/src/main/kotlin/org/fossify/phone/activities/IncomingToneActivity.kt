package org.fossify.phone.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import org.fossify.commons.activities.SimpleActivity
import org.fossify.phone.helpers.IncomingToneController

class IncomingToneActivity : SimpleActivity() {
    private lateinit var status: TextView

    private val pickTone = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            IncomingToneController.setUri(this, uri)
            updateStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 32)
        }
        val title = TextView(this).apply {
            text = "צלצול שיחה נכנסת"
            textSize = 24f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 16)
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        status = TextView(this).apply { textSize = 16f; setPadding(0, 20, 0, 20) }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        val enable = TextView(this).apply {
            text = "הפעלת הצלצול העצמאי"
            textSize = 18f
            setPadding(0, 28, 0, 28)
            setOnClickListener {
                IncomingToneController.setEnabled(this@IncomingToneActivity, !IncomingToneController.isEnabled(this@IncomingToneActivity))
                updateStatus()
            }
        }
        root.addView(enable, LinearLayout.LayoutParams(-1, -2))
        val choose = TextView(this).apply {
            text = "בחירת קובץ צלצול מהמכשיר"
            textSize = 18f
            setPadding(0, 28, 0, 28)
            setOnClickListener { pickTone.launch(arrayOf("audio/*")) }
        }
        root.addView(choose, LinearLayout.LayoutParams(-1, -2))
        val test = TextView(this).apply {
            text = "בדיקת הצלצול"
            textSize = 18f
            setPadding(0, 28, 0, 28)
            setOnClickListener {
                if (IncomingToneController.isEnabled(this@IncomingToneActivity)) {
                    val uri = IncomingToneController.getUri(this@IncomingToneActivity) ?: Settings.System.DEFAULT_RINGTONE_URI
                    try {
                        val player = android.media.MediaPlayer.create(this@IncomingToneActivity, uri)
                        player?.setOnCompletionListener { it.release() }
                        player?.start()
                    } catch (_: Exception) {}
                }
            }
        }
        root.addView(test, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        updateStatus()
    }

    private fun updateStatus() {
        val enabled = if (IncomingToneController.isEnabled(this)) "פעיל" else "כבוי"
        val uri = IncomingToneController.getUri(this)
        status.text = "מצב: $enabled\nצליל: ${uri?.lastPathSegment ?: "ברירת המחדל"}"
    }
}
