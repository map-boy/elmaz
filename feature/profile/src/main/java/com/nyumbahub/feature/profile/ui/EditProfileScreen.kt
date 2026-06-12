package com.nyumbahub.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

val userRoles = listOf(
    "Buyer" to Icons.Default.Star,
    "Renter" to Icons.Default.Home,
    "Landlord" to Icons.Default.Home,
    "Agent" to Icons.Default.Person,
    "Car Seller" to Icons.Default.Star,
    "Developer" to Icons.Default.Home
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val auth     = FirebaseAuth.getInstance()
    val user     = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    var displayName  by remember { mutableStateOf(user?.displayName ?: "") }
    var phone        by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Buyer") }
    var isSaving     by remember { mutableStateOf(false) }
    var saveSuccess  by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    phone        = doc.getString("phone") ?: ""
                    selectedRole = doc.getString("role")  ?: "Buyer"
                    if (displayName.isEmpty()) displayName = doc.getString("displayName") ?: ""
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayName.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = OrangeAccent,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = "Change photo",
                                tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Email (read only)
            OutlinedTextField(
                value = user?.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Role
            Text("I am a", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                userRoles.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { (role, icon) ->
                            val isSelected = selectedRole == role
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) NavyPrimary else Color(0xFFF5F5F5)
                                ),
                                elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
                                onClick = { selectedRole = role }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(icon, contentDescription = null,
                                        tint = if (isSelected) Color.White else NavyPrimary,
                                        modifier = Modifier.size(20.dp))
                                    Text(role, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.Black)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = Color.Red, fontSize = 13.sp)
            }
            if (saveSuccess) {
                Text("Profile saved successfully!", color = Color(0xFF2E7D32), fontSize = 13.sp)
            }

            Button(
                onClick = {
                    isSaving = true
                    errorMsg = ""
                    saveSuccess = false
                    val uid = user?.uid ?: return@Button
                    val data = mapOf(
                        "displayName" to displayName,
                        "phone"       to phone,
                        "role"        to selectedRole,
                        "email"       to (user.email ?: ""),
                        "updatedAt"   to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(uid).set(data)
                        .addOnSuccessListener { doc ->
                            isSaving    = false
                            saveSuccess = true
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName).build()
                            user.updateProfile(profileUpdates)
                        }
                        .addOnFailureListener { e: Exception ->
                            isSaving = false
                            errorMsg = e.message ?: "Failed to save"
                        }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Save Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}





