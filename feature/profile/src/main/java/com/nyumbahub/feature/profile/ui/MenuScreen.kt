package com.nyumbahub.feature.profile.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import com.nyumbahub.feature.profile.viewmodel.ProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onAdminClick: () -> Unit = {},
    onLogin: () -> Unit,
    onProfile: () -> Unit,
    onSubscription: () -> Unit,
    onMyListings: () -> Unit,
    onSaved: () -> Unit,
    onSecurity: () -> Unit,
    onNotifications: () -> Unit,
    onContactUs: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
    onSignOut: () -> Unit,
    onShareApp: () -> Unit,
    onValuation: () -> Unit = {},
    onAgents: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var adminPasswordError by remember { mutableStateOf("") }

    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false; adminPassword = ""; adminPasswordError = "" },
            title = { Text("Admin Access", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = adminPassword, onValueChange = { adminPassword = it; adminPasswordError = "" },
                        label = { Text("Password") }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), isError = adminPasswordError.isNotEmpty(),
                        supportingText = if (adminPasswordError.isNotEmpty()) {{ Text(adminPasswordError, color = Color.Red) }} else null)
                }
            },
            confirmButton = {
                Button(onClick = {
                    FirebaseFirestore.getInstance().collection("config").document("admin").get().addOnSuccessListener { doc ->
                        val correct = doc.getString("password") ?: ""
                        if (adminPassword == correct) { isAdminUnlocked = true; showAdminDialog = false; adminPassword = "" }
                        else adminPasswordError = "Incorrect password"
                    }.addOnFailureListener { adminPasswordError = "Network error" }
                }, colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) { Text("Unlock") }
            },
            dismissButton = { TextButton(onClick = { showAdminDialog = false; adminPassword = ""; adminPasswordError = "" }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            if (user != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = NavyPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user!!.displayName.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user!!.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(user!!.email, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = NavyPrimary)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onLogin() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null,
                        modifier = Modifier.size(56.dp), tint = NavyPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Sign in / Register", style = MaterialTheme.typography.titleMedium,
                        color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider()

            MenuRow(Icons.Default.Star,         "Subscription",       OrangeAccent) { onSubscription() }
            MenuRow(Icons.Default.Home,          "Property Valuation", NavyPrimary)  { onValuation() }
            MenuRow(Icons.Default.Person,        "Find an Agent",      NavyPrimary)  { onAgents() }
            MenuRow(Icons.Default.Home,          "My Listings",        NavyPrimary)  { onMyListings() }
            MenuRow(Icons.Default.Favorite,      "Saved Listings",     NavyPrimary)  { onSaved() }

            HorizontalDivider()

            MenuRow(Icons.Default.Lock,          "Security",           NavyPrimary)  { onSecurity() }
            MenuRow(Icons.Default.Notifications, "Notifications",      NavyPrimary)  { onNotifications() }

            HorizontalDivider()

            MenuRow(Icons.Default.Phone, "Contact Us (WhatsApp)", NavyPrimary) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/250798028184"))
                context.startActivity(intent)
            }
            MenuRow(Icons.Default.Info,  "Terms & Conditions", NavyPrimary) { onTerms() }
            MenuRow(Icons.Default.Lock,  "Privacy Policy",     NavyPrimary) { onPrivacy() }
            MenuRow(Icons.Default.Share, "Share App",          NavyPrimary) { onShareApp() }

            HorizontalDivider()
            MenuRow(Icons.Default.Settings, "Admin Panel", Color(0xFFE53935)) { onAdminClick() }

            if (user != null) {
                HorizontalDivider()
                MenuRow(Icons.Default.ExitToApp, "Sign Out", Color.Red) {
                    viewModel.signOut()
                    onSignOut()
                }
            }

            HorizontalDivider()
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }) {
                    Text("Powered by", fontSize = 11.sp, color = Color.Gray)
                    Text("VAF UBWENGE TECH", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
    HorizontalDivider(modifier = Modifier.padding(start = 54.dp))
}



