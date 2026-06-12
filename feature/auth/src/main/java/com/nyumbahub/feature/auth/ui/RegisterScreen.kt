package com.nyumbahub.feature.auth.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nyumbahub.core.ui.components.NyumbaHubLogo
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import com.nyumbahub.feature.auth.viewmodel.AuthUiState
import com.nyumbahub.feature.auth.viewmodel.AuthViewModel

private enum class RegisterMode { PICKER, USER, AGENT }

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mode by remember { mutableStateOf(RegisterMode.PICKER) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onNavigateToHome()
    }

    when (mode) {
        RegisterMode.PICKER -> RegisterPickerScreen(
            onUserClick  = { mode = RegisterMode.USER },
            onAgentClick = { mode = RegisterMode.AGENT }
        )
        RegisterMode.USER  -> UserRegisterForm(uiState, viewModel, onNavigateToLogin) { mode = RegisterMode.PICKER }
        RegisterMode.AGENT -> AgentRegisterForm(uiState, viewModel, onNavigateToLogin) { mode = RegisterMode.PICKER }
    }
}

@Composable
private fun RegisterPickerScreen(onUserClick: () -> Unit, onAgentClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(NavyPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NyumbaHubLogo(size = 72.dp)
            Spacer(Modifier.height(12.dp))
            Text("Join Elmaz", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Create your account", fontSize = 14.sp, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(40.dp))
            Text("Register as", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Color.White).clickable { onUserClick() }.padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(26.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Regular User", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                        Text("Browse, rent & buy properties", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = NavyPrimary.copy(alpha = 0.5f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(OrangeAccent).clickable { onAgentClick() }.padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Agent / Developer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text("List & manage properties professionally", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onUserClick) {
                Text("Already have an account? Sign In", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserRegisterForm(uiState: AuthUiState, viewModel: AuthViewModel, onNavigateToLogin: () -> Unit, onBack: () -> Unit) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Account") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            NyumbaHubLogo(size = 64.dp)
            Spacer(Modifier.height(12.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Join Elmaz today", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Email, null) })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) })
            Spacer(Modifier.height(20.dp))
            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = { viewModel.signUp(email, password, name) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) {
                if (uiState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Create Account", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateToLogin) { Text("Already have an account? Sign In") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentRegisterForm(uiState: AuthUiState, viewModel: AuthViewModel, onNavigateToLogin: () -> Unit, onBack: () -> Unit) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agency   by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agent Registration") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrangeAccent, titleContentColor = Color.White, navigationIconContentColor = Color.White))
        },
        containerColor = Color(0xFFFFF8F0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(OrangeAccent),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Agent Registration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Professional property listing account", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = OrangeAccent.copy(alpha = 0.1f)) {
                Text("KYC verification required after registration", fontSize = 11.sp, color = OrangeAccent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = agency, onValueChange = { agency = it }, label = { Text("Agency / Company Name") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Home, null) })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Phone, null) })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Email, null) })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password *") },
                modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) })
            Spacer(Modifier.height(16.dp))
            if (uiState is AuthUiState.Error) {
                Text((uiState as AuthUiState.Error).message, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = {
                    viewModel.signUp(email, password, name)
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .update(mapOf("role" to "commissioner", "agency" to agency, "phone" to phone))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)) {
                if (uiState is AuthUiState.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Register as Agent", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateToLogin) { Text("Already have an account? Sign In", color = OrangeAccent) }
        }
    }
}



