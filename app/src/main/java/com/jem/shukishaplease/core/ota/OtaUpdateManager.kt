package com.jem.shukishaplease.core.ota

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.jem.shukishaplease.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class OtaUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

class OtaUpdateManager(private val context: Context) {

    suspend fun checkForUpdates(manifestUrl: String): OtaUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(manifestUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val jsonString = reader.use { it.readText() }
                val obj = JSONObject(jsonString)

                val latestVersionCode = obj.getInt("versionCode")
                val latestVersionName = obj.optString("versionName", "1.0.0")
                val apkUrl = obj.optString("apkUrl", "")
                val releaseNotes = obj.optString("releaseNotes", "Bug fixes and performance improvements.")

                val currentVersionCode = BuildConfig.VERSION_CODE
                val isAvailable = latestVersionCode > currentVersionCode

                OtaUpdateInfo(
                    versionCode = latestVersionCode,
                    versionName = latestVersionName,
                    apkUrl = apkUrl,
                    releaseNotes = releaseNotes,
                    isUpdateAvailable = isAvailable
                )
            } else {
                OtaUpdateInfo(0, "", "", "", false)
            }
        } catch (e: Exception) {
            Log.e("OtaUpdateManager", "OTA check failed: ${e.localizedMessage}")
            OtaUpdateInfo(0, "", "", "", false)
        }
    }

    fun downloadAndInstallApk(apkUrl: String, fileName: String = "shukisha_update.apk") {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) {
                file.delete()
            }

            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("Shukisha Please OTA Update")
                .setDescription("Downloading latest update...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        try {
                            context.unregisterReceiver(this)
                        } catch (ignored: Exception) {
                        }
                        installApk(file)
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            Log.e("OtaUpdateManager", "Download failed: ${e.localizedMessage}")
        }
    }

    fun installApk(file: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("OtaUpdateManager", "Install failed: ${e.localizedMessage}")
        }
    }
}
