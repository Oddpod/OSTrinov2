package com.odd.ostrino

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context


class ScreenLockBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(arg0: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            // do stuff
        }
    }
}