package com.nyumbahub.feature.auth.ui

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.nyumbahub.core.ui.components.NyumbaHubLogo
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import com.nyumbahub.feature.auth.viewmodel.AuthUiState
import com.nyumbahub.feature.auth.viewmodel.AuthViewModel

private const val PREFS_NAME = "nyumbahub_prefs"
private const val KEY_BIOMETRICS = "biometrics_enabled"
private enum class LoginMode { PICKER, USER, AGENT }

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mode by remember { mutableStateOf(LoginMode.PICKER) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onNavigateToHome()
    }

    when (mode) {
        LoginMode.PICKER -> LoginPickerScreen(
            onUserClick  = { mode = LoginMode.USER },
            onAgentClick = { mode = LoginMode.AGENT }
        )
        LoginMode.USER -> UserLoginForm(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToRegister = onNavigateToRegister,
            onBack = { mode = LoginMode.PICKER }
        )
        LoginMode.AGENT -> AgentLoginForm(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToRegister = onNavigateToRegister,
            onBack = { mode = LoginMode.PICKER }
        )
    }
}

@Composable
private fun LoginPickerScreen(onUserClick: () -> Unit, onAgentClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(NavyPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            NyumbaHubLogo(size = 80.dp)
            Spacer(Modifier.height(12.dp))
            Text("Elmaz", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Find your perfect home in Rwanda", fontSize = 14.sp, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(48.dp))
            Text("Sign in as", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(20.dp))
            LoginTypeCard(
                icon = Icons.Default.Person,
                title = "Regular User",
                subtitle = "Browse & rent properties",
                color = Color.White,
                textColor = NavyPrimary,
                onClick = onUserClick
            )
            Spacer(Modifier.height(12.dp))
            LoginTypeCard(
                icon = Icons.Default.Star,
                title = "Agent / Commissioner",
                subtitle = "List & manage properties professionally",
                color = OrangeAccent,
                textColor = Color.White,
                onClick = onAgentClick
            )
        }
    }
}

@Composable
private fun LoginTypeCard(
    icon: ImageVector, title: String, subtitle: String,
    color: Color, textColor: Color, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                    .background(textColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                Text(subtitle, fontSize = 12.sp, color = textColor.copy(alpha = 0.75f))
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun GoogleSignInButton(onToken: (String) -> Unit, isLoading: Boolean) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
                account.idToken?.let { onToken(it) }
            } catch (_: Exception) {}
        }
    }
    OutlinedButton(
        onClick = {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("669202374973-prnfajk5n9i56f31d4thgufc84pnk858.apps.googleusercontent.com")
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(context, gso)
            client.signOut().addOnCompleteListener { launcher.launch(client.signInIntent) }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFDB4437), modifier = Modifier.size(20.dp))
                Text("Continue with Google", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun BiometricLoginButton(onSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val biometricsEnabled = prefs.getBoolean(KEY_BIOMETRICS, false)
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS

    if (!biometricsEnabled || !canAuthenticate) return

    var error by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val activity = context as? FragmentActivity ?: return@OutlinedButton
                val executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            onSuccess()
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            error = errString.toString()
                        }
                        override fun onAuthenticationFailed() {
                            error = "Authentication failed. Try again."
                        }
                    }
                )
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Elmaz Login")
                    .setSubtitle("Use biometrics to sign in")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()
                prompt.authenticate(info)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(22.dp))
                Text("Sign in with Biometrics", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }
        if (error.isNotEmpty()) {
            Text(error, color = Color.Red, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserLoginForm(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showReset by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    if (resetSent) Text("Reset email sent! Check your inbox.")
                    else {
                        Text("Enter your email to receive a reset link.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = resetEmail, onValueChange = { resetEmail = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                if (!resetSent) TextButton(onClick = { viewModel.sendPasswordReset(resetEmail); resetSent = true }) { Text("Send") }
                else TextButton(onClick = { showReset = false; resetSent = false }) { Text("OK") }
            },
            dismissButton = { if (!resetSent) TextButton(onClick = { showReset = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign In") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NyumbaHubLogo(size = 64.dp)
            Spacer(Modifier.height(12.dp))
            Text("Welcome back", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Sign in to your account", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(32.dp))

            GoogleSignInButton(
                onToken = { token ->
                    googleLoading = true
                    val credential = GoogleAuthProvider.getCredential(token, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener { googleLoading = false }
                        .addOnFailureListener { googleLoading = false }
                },
                isLoading = googleLoading
            )
            Spacer(Modifier.height(8.dp))
            BiometricLoginButton(onSuccess = { /* already logged in via Firebase session */ })
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("or", fontSize = 12.sp, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, null) })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) })
            TextButton(onClick = { showReset = true }, modifier = Modifier.align(Alignment.End)) {
                Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
            }
            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { viewModel.signIn(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                if (uiState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Sign In", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateToRegister) { Text("Don't have an account? Register") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentLoginForm(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var googleLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent / Commissioner Sign In") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeAccent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFFFF8F0)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Agent / Commissioner Portal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Professional property management", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = OrangeAccent.copy(alpha = 0.1f)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                    Text("KYC verified accounts only", fontSize = 12.sp, color = OrangeAccent, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(28.dp))

            GoogleSignInButton(
                onToken = { token ->
                    googleLoading = true
                    val credential = GoogleAuthProvider.getCredential(token, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener { googleLoading = false }
                        .addOnFailureListener { googleLoading = false }
                },
                isLoading = googleLoading
            )
            Spacer(Modifier.height(8.dp))
            BiometricLoginButton(onSuccess = { /* already logged in via Firebase session */ })
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("or", fontSize = 12.sp, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Agent Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, null) })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) })
            Spacer(Modifier.height(4.dp))
            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { viewModel.signIn(email, password) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                if (uiState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Sign In as Agent", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateToRegister) { Text("Register as Agent / Commissioner", color = OrangeAccent) }
        }
    }
}

