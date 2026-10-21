package com.jem.shukishaplease.features.trip

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.jem.shukishaplease.MainActivity
import com.jem.shukishaplease.core.common.CommuterPrefs
import kotlinx.coroutines.*

class TripTrackingService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var destLat: Double = -1.2864
    private var destLon: Double = 36.8172
    private var destName: String = "Destination"
    private var lastAlertTime: Long = 0L

    private var lastLocationTimeMs: Long = 0L
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var watchdogJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        destName = intent?.getStringExtra("EXTRA_DEST_NAME") ?: "Destination"
        destLat = intent?.getDoubleExtra("EXTRA_DEST_LAT", -1.2864) ?: -1.2864
        destLon = intent?.getDoubleExtra("EXTRA_DEST_LON", 36.8172) ?: 36.8172

        val prefs = CommuterPrefs(applicationContext)
        prefs.isTripActive = true
        prefs.alarmFired = false
        prefs.activeTripDestination = destName
        prefs.activeTripLat = destLat
        prefs.activeTripLon = destLon
        if (prefs.activeTripStartTime == 0L) {
            prefs.activeTripStartTime = System.currentTimeMillis()
        }

        // Schedule initial backup exact alarm
        TripAlarmScheduler.scheduleBackupAlarm(applicationContext, prefs.activeTripEtaMinutes, prefs.activeTripAlertMinutes)

        val notification = createNotification("Shukisha is watching your trip to $destName")
        startForeground(NOTIFICATION_ID, notification)

        startLocationTracking()
        startGpsWatchdog()

        return START_STICKY
    }

    private fun startGpsWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = serviceScope.launch {
            while (isActive) {
                delay(5000)
                if (lastLocationTimeMs > 0L) {
                    val timeSinceUpdateMs = System.currentTimeMillis() - lastLocationTimeMs
                    if (timeSinceUpdateMs > 180000L) { // 3 minutes -> GPS LOST
                        updateNotification("⚠️ GPS signal lost! Alarm may be delayed.")
                        Log.w("TripTrackingService", "GPS Watchdog: No location updates for over 3 minutes.")
                    } else if (timeSinceUpdateMs > 60000L) { // 60 seconds -> GPS WEAK
                        updateNotification("⚠️ Weak GPS signal! Searching for satellite fix...")
                        Log.w("TripTrackingService", "GPS Watchdog: No location updates for over 60 seconds.")
                    }
                }
            }
        }
    }

    private fun startLocationTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                lastLocationTimeMs = System.currentTimeMillis()

                for (location in result.locations) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude,
                        location.longitude,
                        destLat,
                        destLon,
                        results
                    )
                    val distanceMeters = results[0]
                    val speed = if (location.hasSpeed() && location.speed > 0f) location.speed else 12f
                    val etaSeconds = distanceMeters / speed
                    val etaMinutes = etaSeconds / 60f

                    val prefs = CommuterPrefs(applicationContext)
                    prefs.activeTripEtaMinutes = etaMinutes.toInt().coerceAtLeast(1)

                    // Re-schedule backup exact alarm on each ETA update
                    if (!prefs.alarmFired) {
                        TripAlarmScheduler.scheduleBackupAlarm(applicationContext, etaMinutes.toInt(), prefs.activeTripAlertMinutes)
                    }

                    val distText = if (distanceMeters >= 1000f) "%.1f km".format(distanceMeters / 1000f) else "%.0f m".format(distanceMeters)
                    val etaText = "~%.0f min".format(etaMinutes.coerceAtLeast(1f))

                    val currentTime = System.currentTimeMillis()
                    val canTriggerAlert = (currentTime - lastAlertTime) > 45000L

                    if ((etaMinutes <= 3.0f || distanceMeters <= 500f) && canTriggerAlert) {
                        if (prefs.tryClaimAlarmTrigger()) {
                            lastAlertTime = currentTime
                            if (distanceMeters <= 150f || etaMinutes <= 1.0f) {
                                triggerAlertNotification("🚨 SHUKISHA HAPA! GET OFF NEXT!", "You are $distText ($etaText) from $destName. Alight now!")
                            } else {
                                triggerAlertNotification("⚠️ TUMEFIKA KARIBU! STOP APPROACHING", "Your stop $destName is $etaText ($distText) away. Get ready!")
                            }
                        } else {
                            updateNotification("Shukisha is watching your trip to $destName · $distText ($etaText remaining)")
                        }
                    } else {
                        updateNotification("Shukisha is watching your trip to $destName · $distText ($etaText remaining)")
                    }
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
        } catch (ignored: SecurityException) {
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shukisha Trip Tracking Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies commuters when approaching their alighting stop"
                enableVibration(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shukisha Please Active Trip")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_dialog_map)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun triggerAlertNotification(title: String, message: String) {
        val fullScreenIntent = Intent(this, IncomingCallAlertActivity::class.java).apply {
            putExtra("EXTRA_DEST_NAME", destName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_dialog_alert)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ALERT_NOTIFICATION_ID, alertNotification)

        try {
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            // Full screen intent fallback via notification
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        val prefs = CommuterPrefs(applicationContext)
        prefs.clearActiveTrip()
        TripAlarmScheduler.cancelBackupAlarm(applicationContext)

        if (::fusedClient.isInitialized && ::locationCallback.isInitialized) {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "shukisha_trip_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ALERT_NOTIFICATION_ID = 1002
    }
}
