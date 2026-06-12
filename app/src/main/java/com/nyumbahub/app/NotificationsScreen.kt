package com.nyumbahub.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "general",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Real-time listener instead of one-shot get()
    DisposableEffect(user?.uid) {
        if (user == null) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
        val reg: ListenerRegistration = FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(user.uid)
            .collection("items")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val fetched = snap?.documents?.mapNotNull { doc ->
                    try {
                        AppNotification(
                            id        = doc.id,
                            title     = doc.getString("title") ?: "",
                            body      = doc.getString("body") ?: "",
                            type      = doc.getString("type") ?: "general",
                            isRead    = doc.getBoolean("isRead") ?: false,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                // Never fall back to hardcoded demo data - show empty state if none
                notifications = fetched
                isLoading = false
            }
        onDispose { reg.remove() }
    }

    fun markAllRead() {
        user?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            notifications.filter { !it.isRead }.forEach { notif ->
                db.collection("notifications").document(uid)
                    .collection("items").document(notif.id)
                    .update("isRead", true)
            }
        }
        notifications = notifications.map { it.copy(isRead = true) }
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Notifications", fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Surface(shape = CircleShape, color = OrangeAccent) {
                                Text(
                                    "$unreadCount", color = Color.White, fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { markAllRead() }) {
                            Text("Mark all read", color = NavyPrimary, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
            }
            user == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("Sign in to see notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
            notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("No notifications yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "You will be notified about inquiries and updates here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationCard(notif = notif, onTap = {
                            notifications = notifications.map {
                                if (it.id == notif.id) it.copy(isRead = true) else it
                            }
                            user?.uid?.let { uid ->
                                FirebaseFirestore.getInstance()
                                    .collection("notifications").document(uid)
                                    .collection("items").document(notif.id)
                                    .update("isRead", true)
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification, onTap: () -> Unit) {
    val icon: ImageVector = when (notif.type) {
        "inquiry"    -> Icons.Default.Email
        "price_drop" -> Icons.Default.TrendingDown
        "listing"    -> Icons.Default.Home
        "chat"       -> Icons.Default.Chat
        "verified"   -> Icons.Default.Verified
        else         -> Icons.Default.Notifications
    }
    val iconColor = when (notif.type) {
        "inquiry"    -> NavyPrimary
        "price_drop" -> Color(0xFF2E7D32)
        "listing"    -> OrangeAccent
        "chat"       -> Color(0xFF0288D1)
        "verified"   -> Color(0xFF6A1B9A)
        else         -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isRead) Color.White else Color(0xFFF0F4FF)
        ),
        elevation = CardDefaults.cardElevation(if (notif.isRead) 1.dp else 3.dp),
        onClick = onTap
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        notif.title,
                        fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notif.isRead) {
                        Box(modifier = Modifier.size(8.dp).background(OrangeAccent, CircleShape))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(notif.body, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(formatNotifTime(notif.createdAt), fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}

private fun formatNotifTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000    -> "Just now"
        diff < 3600000  -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else            -> "${diff / 86400000}d ago"
    }
}