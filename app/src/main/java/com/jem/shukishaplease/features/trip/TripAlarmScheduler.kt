package com.jem.shukishaplease.features.trip

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

object TripAlarmScheduler {

    private const val ALARM_REQUEST_CODE = 9901

    fun scheduleBackupAlarm(context: Context, etaMinutes: Int, bufferMinutes: Int = 3) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ exact alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (ignored: Exception) {
                }
            }
        }

        val intent = Intent(context, TripAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMinutes = (etaMinutes - bufferMinutes).coerceAtLeast(1)
        val triggerTimeMs = System.currentTimeMillis() + (triggerMinutes * 60 * 1000L)

        try {
            val showIntent = Intent(context, TripActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                0,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTimeMs, showPendingIntent),
                pendingIntent
            )
            Log.d("TripAlarmScheduler", "Scheduled backup alarm in $triggerMinutes mins (Trigger at $triggerTimeMs)")
        } catch (e: Exception) {
            Log.e("TripAlarmScheduler", "Failed to schedule exact alarm: ${e.localizedMessage}")
        }
    }

    fun cancelBackupAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TripAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            Log.d("TripAlarmScheduler", "Cancelled backup alarm.")
        } catch (ignored: Exception) {
        }
    }
}
