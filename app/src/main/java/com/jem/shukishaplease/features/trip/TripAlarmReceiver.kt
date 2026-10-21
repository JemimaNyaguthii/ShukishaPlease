package com.jem.shukishaplease.features.trip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jem.shukishaplease.core.common.CommuterPrefs

class TripAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = CommuterPrefs(context)
        Log.d("TripAlarmReceiver", "Backup alarm fired! AlarmFired: ${prefs.alarmFired}, TripActive: ${prefs.isTripActive}")

        if (prefs.tryClaimAlarmTrigger()) {
            val fullScreenIntent = Intent(context, IncomingCallAlertActivity::class.java).apply {
                putExtra("EXTRA_DEST_NAME", prefs.activeTripDestination)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(fullScreenIntent)
        }
    }
}
