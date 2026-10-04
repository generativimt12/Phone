package org.fossify.phone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import org.fossify.phone.helpers.CallManager

class CallButtonActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val call = CallManager.getPrimaryCall()
        if (call != null && CallManager.getState() == Call.STATE_RINGING) {
            CallManager.accept()
        } else {
            startActivity(Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                type = "vnd.android.cursor.dir/calls"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
        finish()
    }
}
