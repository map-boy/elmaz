package com.nyumbahub.feature.chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryListScreen(onInquiryClick: (String) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    var inquiries by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        if (user == null) { isLoading = false; return@LaunchedEffect }
        FirebaseFirestore.getInstance()
            .collection("inquiries")
            .whereEqualTo("ownerId", user.uid)
            .get()
            .addOnSuccessListener { snap ->
                inquiries = snap.documents.map { doc ->
                    (doc.data ?: emptyMap<String, Any>()).toMutableMap().also { it["_id"] = doc.id }
                }.sortedByDescending { it["createdAt"] as? Long ?: 0L }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inquiries") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            inquiries.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Email, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No inquiries yet", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Inquiries on your listings appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inquiries) { inq ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onInquiryClick(inq["_id"] as? String ?: "") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inq["listingTitle"] as? String ?: "Property Inquiry",
                                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("From: ${inq["senderName"] as? String ?: "Unknown"}",
                                    fontSize = 12.sp, color = Color.Gray)
                                Text(inq["lastMessage"] as? String ?: "New inquiry",
                                    fontSize = 12.sp, color = Color.DarkGray, maxLines = 1)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = OrangeAccent.copy(alpha = 0.15f)) {
                                Text("New", fontSize = 10.sp, color = OrangeAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
