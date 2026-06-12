package com.nyumbahub.feature.profile.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

private const val PREFS_NAME = "nyumbahub_prefs"
private const val KEY_BIOMETRICS = "biometrics_enabled"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    email: String,
    onBack: () -> Unit,
    onChangePassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // Load saved biometric preference
    var biometrics by remember { mutableStateOf(prefs.getBoolean(KEY_BIOMETRICS, false)) }
    var biometricStatus by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS

    fun launchBiometric(onSuccess: () -> Unit, onFail: () -> Unit) {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFail()
                }
                override fun onAuthenticationFailed() {
                    onFail()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Elmaz Security")
            .setSubtitle("Confirm your identity")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(info)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Biometrics Not Available") },
            text = { Text("Your device does not support biometric authentication or none is enrolled. Please set up fingerprint or face unlock in your device settings.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)) {
                Text("Email", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Text(email.ifEmpty { "Not signed in" }, style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Password", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(4.dp))
                    Text("**********", style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = onChangePassword) {
                    Icon(Icons.Default.Edit, contentDescription = "Change password", tint = OrangeAccent)
                }
            }
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null,
                            tint = NavyPrimary, modifier = Modifier.size(22.dp))
                        Text("Biometric Login", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (!canAuthenticate) "Not available on this device"
                        else if (biometrics) "Enabled - fingerprint/face unlock active"
                        else "Use fingerprint or face to log in",
                        fontSize = 12.sp,
                        color = if (!canAuthenticate) Color.Red.copy(alpha = 0.7f) else Color.Gray
                    )
                    if (biometricStatus.isNotEmpty()) {
                        Text(
                            biometricStatus, fontSize = 12.sp,
                            color = if (biometricStatus.contains("enabled")) Color(0xFF2E7D32) else Color.Red
                        )
                    }
                }
                Switch(
                    checked = biometrics,
                    onCheckedChange = { newValue ->
                        if (!canAuthenticate) {
                            showDialog = true
                            return@Switch
                        }
                        launchBiometric(
                            onSuccess = {
                                biometrics = newValue
                                // Persist the preference
                                prefs.edit().putBoolean(KEY_BIOMETRICS, newValue).apply()
                                biometricStatus = if (newValue) "Biometrics enabled successfully"
                                                  else "Biometrics disabled"
                            },
                            onFail = {
                                biometricStatus = "Authentication failed. Try again."
                            }
                        )
                    },
                    enabled = canAuthenticate,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OrangeAccent
                    )
                )
            }
            HorizontalDivider()

            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF4FF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null,
                        tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        "Biometric login uses your device fingerprint or face recognition. Your biometric data never leaves your device.",
                        fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
