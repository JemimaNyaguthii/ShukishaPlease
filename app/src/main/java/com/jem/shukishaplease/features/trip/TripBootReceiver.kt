package com.jem.shukishaplease.features.trip

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.jem.shukishaplease.core.common.CommuterPrefs

class TripBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = CommuterPrefs(context)

            // Re-check location permission
            val hasLocationPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasLocationPerm) {
                Log.w("TripBootReceiver", "Cannot restore active trip: Location permission denied.")
                return
            }

            // Check valid active trip state & coordinates
            if (!prefs.hasValidActiveTripDestination() || prefs.activeTripDestination.isBlank()) {
                Log.w("TripBootReceiver", "No valid active trip destination to restore.")
                return
            }

            // Reject trips older than 24 hours (86,400,000 ms)
            val startTime = prefs.activeTripStartTime
            val currentTime = System.currentTimeMillis()
            if (startTime > 0L && (currentTime - startTime) > 86400000L) {
                Log.w("TripBootReceiver", "Active trip expired (> 24 hours). Clearing trip.")
                prefs.clearActiveTrip()
                return
            }

            val destLat = prefs.activeTripLat ?: return
            val destLon = prefs.activeTripLon ?: return

            Log.d("TripBootReceiver", "Restoring active trip to ${prefs.activeTripDestination} after reboot...")

            // Re-schedule backup exact alarm
            if (!prefs.alarmFired) {
                TripAlarmScheduler.scheduleBackupAlarm(context, prefs.activeTripEtaMinutes, prefs.activeTripAlertMinutes)
            }

            val serviceIntent = Intent(context, TripTrackingService::class.java).apply {
                putExtra("EXTRA_DEST_NAME", prefs.activeTripDestination)
                putExtra("EXTRA_DEST_LAT", destLat)
                putExtra("EXTRA_DEST_LON", destLon)
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("TripBootReceiver", "Failed to start foreground service on boot: ${e.localizedMessage}")
            }
        }
    }
}
