package com.nyumbahub.app

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

private const val VERSION_URL = "https://yoursite.com/nyumbahub/version.json"

data class AppUpdate(
    val version: String,
    val versionCode: Int,
    val apkUrl: String,
    val releaseNotes: String,
    val forceUpdate: Boolean
)

suspend fun fetchUpdateInfo(): AppUpdate? = withContext(Dispatchers.IO) {
    try {
        val json = JSONObject(URL(VERSION_URL).readText())
        AppUpdate(
            version      = json.getString("version"),
            versionCode  = json.getInt("versionCode"),
            apkUrl       = json.getString("apkUrl"),
            releaseNotes = json.optString("releaseNotes", ""),
            forceUpdate  = json.optBoolean("forceUpdate", false)
        )
    } catch (e: Exception) { null }
}

fun downloadAndInstall(context: Context, apkUrl: String) {
    val fileName = "nyumbahub-update.apk"
    val file     = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    if (file.exists()) file.delete()

    val request = DownloadManager.Request(Uri.parse(apkUrl))
        .setTitle("Elmaz Update")
        .setDescription("Downloading update...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationUri(Uri.fromFile(file))

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = dm.enqueue(request)

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) == downloadId) {
                ctx.unregisterReceiver(this)
                installApk(ctx, file)
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED)
    } else {
        @Suppress("UnspecifiedRegisterReceiverFlag")
        context.registerReceiver(receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}

private fun installApk(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

