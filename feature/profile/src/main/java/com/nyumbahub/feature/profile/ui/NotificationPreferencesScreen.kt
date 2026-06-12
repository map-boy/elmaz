package com.nyumbahub.feature.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(onBack: () -> Unit) {
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    var newListings     by remember { mutableStateOf(true) }
    var inquiryReplies  by remember { mutableStateOf(true) }
    var priceDrops      by remember { mutableStateOf(false) }
    var promotions      by remember { mutableStateOf(false) }
    var emailDigest     by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            SectionHeader("Push Notifications")
            NotifToggle("New listings matching my search", newListings)   { newListings = it }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            NotifToggle("Inquiry replies",                 inquiryReplies) { inquiryReplies = it }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            NotifToggle("Price drops on saved listings",   priceDrops)    { priceDrops = it }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            NotifToggle("Promotions and offers",           promotions)    { promotions = it }

            SectionHeader("Email")
            NotifToggle("Weekly digest", emailDigest) { emailDigest = it }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    user?.uid?.let { uid ->
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("notif_preferences").document(uid)
                            .set(mapOf(
                                "newListings"    to newListings,
                                "inquiryReplies" to inquiryReplies,
                                "priceDrops"     to priceDrops,
                                "promotions"     to promotions,
                                "emailDigest"    to emailDigest,
                                "updatedAt"      to System.currentTimeMillis()
                            ))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) { Text("Save Preferences", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun NotifToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = OrangeAccent
            )
        )
    }
}

