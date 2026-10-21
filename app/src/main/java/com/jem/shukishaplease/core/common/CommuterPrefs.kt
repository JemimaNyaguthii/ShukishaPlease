package com.jem.shukishaplease.core.common

import android.content.Context
import android.content.SharedPreferences

data class TrustedContact(val name: String, val phone: String)

class CommuterPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shukisha_prefs", Context.MODE_PRIVATE)

    companion object {
        private val triggerLock = Any()
    }

    var userName: String
        get() = prefs.getString("pref_user_name", "Msafiri") ?: "Msafiri"
        set(value) = prefs.edit().putString("pref_user_name", value).apply()

    var preferredCategory: String
        get() = prefs.getString("pref_category", "All") ?: "All"
        set(value) = prefs.edit().putString("pref_category", value).apply()

    var preferredDifficulty: String
        get() = prefs.getString("pref_difficulty", "All") ?: "All"
        set(value) = prefs.edit().putString("pref_difficulty", value).apply()

    var isQuizEnabled: Boolean
        get() = prefs.getBoolean("pref_quiz_enabled", true)
        set(value) = prefs.edit().putBoolean("pref_quiz_enabled", value).apply()

    var totalPoints: Int
        get() = prefs.getInt("pref_total_points", 0)
        set(value) = prefs.edit().putInt("pref_total_points", value).apply()

    var streakDays: Int
        get() = prefs.getInt("pref_streak_days", 1)
        set(value) = prefs.edit().putInt("pref_streak_days", value).apply()

    var gamesCompleted: Int
        get() = prefs.getInt("pref_games_completed", 0)
        set(value) = prefs.edit().putInt("pref_games_completed", value).apply()

    var isTripActive: Boolean
        get() = prefs.getBoolean("pref_is_trip_active", false)
        set(value) = prefs.edit().putBoolean("pref_is_trip_active", value).apply()

    var alarmFired: Boolean
        get() = prefs.getBoolean("pref_alarm_fired", false)
        set(value) = prefs.edit().putBoolean("pref_alarm_fired", value).apply()

    fun tryClaimAlarmTrigger(): Boolean {
        synchronized(triggerLock) {
            if (!isTripActive) return false
            val alreadyFired = prefs.getBoolean("pref_alarm_fired", false)
            if (alreadyFired) return false
            prefs.edit().putBoolean("pref_alarm_fired", true).commit()
            return true
        }
    }

    var activeTripDestination: String
        get() = prefs.getString("pref_active_trip_dest", "") ?: ""
        set(value) = prefs.edit().putString("pref_active_trip_dest", value).apply()

    var activeTripLat: Double?
        get() {
            val str = prefs.getString("pref_active_trip_lat_str", null)
            val parsedDouble = str?.toDoubleOrNull()
            val lat = if (parsedDouble != null) {
                parsedDouble
            } else {
                try {
                    val floatVal = prefs.getFloat("pref_active_trip_lat", Float.NaN)
                    if (!floatVal.isNaN()) floatVal.toDouble() else null
                } catch (ignored: ClassCastException) {
                    null
                }
            }
            return if (lat != null && !lat.isNaN() && !lat.isInfinite() && lat in -90.0..90.0) lat else null
        }
        set(value) {
            if (value != null && !value.isNaN() && !value.isInfinite() && value in -90.0..90.0) {
                prefs.edit()
                    .putString("pref_active_trip_lat_str", value.toString())
                    .remove("pref_active_trip_lat")
                    .apply()
            } else {
                prefs.edit()
                    .remove("pref_active_trip_lat_str")
                    .remove("pref_active_trip_lat")
                    .apply()
            }
        }

    var activeTripLon: Double?
        get() {
            val str = prefs.getString("pref_active_trip_lon_str", null)
            val parsedDouble = str?.toDoubleOrNull()
            val lon = if (parsedDouble != null) {
                parsedDouble
            } else {
                try {
                    val floatVal = prefs.getFloat("pref_active_trip_lon", Float.NaN)
                    if (!floatVal.isNaN()) floatVal.toDouble() else null
                } catch (ignored: ClassCastException) {
                    null
                }
            }
            return if (lon != null && !lon.isNaN() && !lon.isInfinite() && lon in -180.0..180.0) lon else null
        }
        set(value) {
            if (value != null && !value.isNaN() && !value.isInfinite() && value in -180.0..180.0) {
                prefs.edit()
                    .putString("pref_active_trip_lon_str", value.toString())
                    .remove("pref_active_trip_lon")
                    .apply()
            } else {
                prefs.edit()
                    .remove("pref_active_trip_lon_str")
                    .remove("pref_active_trip_lon")
                    .apply()
            }
        }

    fun hasValidActiveTripDestination(): Boolean {
        return isTripActive && activeTripLat != null && activeTripLon != null
    }

    var activeTripStartTime: Long
        get() = prefs.getLong("pref_active_trip_start_time", 0L)
        set(value) = prefs.edit().putLong("pref_active_trip_start_time", value).apply()

    var activeTripAlertMinutes: Int
        get() = prefs.getInt("pref_active_trip_alert_min", 3)
        set(value) = prefs.edit().putInt("pref_active_trip_alert_min", value).apply()

    var lateNightHour: Int
        get() = prefs.getInt("pref_late_night_hour", 20)
        set(value) = prefs.edit().putInt("pref_late_night_hour", value).apply()

    var activeTripEtaMinutes: Int
        get() {
            val stored = prefs.getInt("pref_active_trip_eta_min", -1)
            if (stored > 0) return stored

            val dest = activeTripDestination
            return when {
                dest.contains("Nairobi", true) || dest.contains("CBD", true) -> 150
                dest.contains("Mombasa", true) -> 480
                dest.contains("Nanyuki", true) -> 180
                dest.contains("Nakuru", true) -> 160
                dest.contains("Karatina", true) -> 35
                dest.contains("Westlands", true) -> 20
                else -> 45
            }
        }
        set(value) = prefs.edit().putInt("pref_active_trip_eta_min", value).apply()

    fun getTrustedContacts(): List<TrustedContact> {
        val raw = prefs.getString("pref_trusted_contacts", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size == 2) TrustedContact(parts[0], parts[1]) else null
        }
    }

    fun saveTrustedContacts(contacts: List<TrustedContact>) {
        val raw = contacts.joinToString("||") { "${it.name}::${it.phone}" }
        prefs.edit().putString("pref_trusted_contacts", raw).apply()
    }

    fun clearActiveTrip() {
        isTripActive = false
        alarmFired = false
        activeTripDestination = ""
        activeTripLat = null
        activeTripLon = null
        activeTripStartTime = 0L
    }

    fun addPoints(points: Int) {
        totalPoints += points
        gamesCompleted += 1
    }
}
