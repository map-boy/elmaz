package com.nyumbahub.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.nyumbahub.core.ui.theme.NyumbaHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) subscribeToTopics()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        lifecycleScope.launch {
            val update = fetchUpdateInfo()
            val currentCode = packageManager
                .getPackageInfo(packageName, 0)
                .longVersionCode
                .toInt()
            if (update != null && update.versionCode > currentCode) {
                showUpdateDialog(update)
            }
        }

        setContent {
            NyumbaHubTheme {
                var showExitDialog by remember { mutableStateOf(false) }
                BackHandler { showExitDialog = true }
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title            = { Text("Exit App") },
                        text             = { Text("Are you sure you want to exit?") },
                        confirmButton    = {
                            TextButton(onClick = { finish() }) { Text("EXIT") }
                        },
                        dismissButton    = {
                            TextButton(onClick = { showExitDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                NyumbaHubNavGraph()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    subscribeToTopics()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            subscribeToTopics()
        }
    }

    private fun subscribeToTopics() {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic("all_users")
            subscribeToTopic("listings")
            subscribeToTopic("motors")
            subscribeToTopic("promotions")
        }
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null && token.isNotEmpty()) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .update("fcmToken", token)
            }
        }
    }

    private fun showUpdateDialog(update: AppUpdate) {
        val builder = android.app.AlertDialog.Builder(this)
            .setTitle("Update Available — v${update.version}")
            .setMessage(update.releaseNotes.ifBlank { "A new version of Elmaz is available." })
            .setPositiveButton("Update Now") { _, _ ->
                downloadAndInstall(this, update.apkUrl)
            }
        if (!update.forceUpdate) builder.setNegativeButton("Later", null)
        val dialog = builder.create()
        dialog.setCancelable(!update.forceUpdate)
        dialog.show()
    }
}

