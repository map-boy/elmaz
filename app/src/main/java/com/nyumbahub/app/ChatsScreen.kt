package com.nyumbahub.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nyumbahub.core.ui.theme.NavyPrimary
import com.nyumbahub.core.ui.theme.OrangeAccent
import java.text.SimpleDateFormat
import java.util.*

data class ChatPreview(
    val inquiryId: String,
    val listingTitle: String,
    val otherPartyName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isOwner: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onChatClick: (String) -> Unit,
    onLoginRequired: () -> Unit,
    onExplore: () -> Unit,
    onPostAd: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    var chats by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(user?.uid) {
        if (user == null) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
        val db = FirebaseFirestore.getInstance()
        val uid = user.uid
        val senderMap = mutableMapOf<String, ChatPreview>()
        val ownerMap  = mutableMapOf<String, ChatPreview>()
        var senderReady = false
        var ownerReady  = false

        fun merge() {
            if (!senderReady || !ownerReady) return
            chats = (senderMap.values + ownerMap.values)
                .distinctBy { it.inquiryId }
                .sortedByDescending { it.timestamp }
            isLoading = false
        }

        val reg1: ListenerRegistration = db.collection("inquiries")
            .whereEqualTo("senderId", uid)
            .addSnapshotListener { snap, error ->
                if (error != null) { senderReady = true; merge(); return@addSnapshotListener }
                senderMap.clear()
                snap?.documents?.forEach { doc ->
                    try {
                        senderMap[doc.id] = ChatPreview(
                            inquiryId      = doc.id,
                            listingTitle   = doc.getString("listingTitle") ?: "Property Inquiry",
                            otherPartyName = doc.getString("ownerName") ?: "Owner",
                            lastMessage    = doc.getString("lastMessage") ?: "Inquiry sent",
                            timestamp      = doc.getLong("updatedAt") ?: doc.getLong("createdAt") ?: 0L,
                            unreadCount    = (doc.getLong("unreadBySender") ?: 0L).toInt(),
                            isOwner        = false
                        )
                    } catch (_: Exception) {}
                }
                senderReady = true
                merge()
            }

        val reg2: ListenerRegistration = db.collection("inquiries")
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snap, error ->
                if (error != null) { ownerReady = true; merge(); return@addSnapshotListener }
                ownerMap.clear()
                snap?.documents?.forEach { doc ->
                    try {
                        ownerMap[doc.id] = ChatPreview(
                            inquiryId      = doc.id,
                            listingTitle   = doc.getString("listingTitle") ?: "Property Inquiry",
                            otherPartyName = doc.getString("senderName") ?: "Inquirer",
                            lastMessage    = doc.getString("lastMessage") ?: "New inquiry",
                            timestamp      = doc.getLong("updatedAt") ?: doc.getLong("createdAt") ?: 0L,
                            unreadCount    = (doc.getLong("unreadByOwner") ?: 0L).toInt(),
                            isOwner        = true
                        )
                    } catch (_: Exception) {}
                }
                ownerReady = true
                merge()
            }

        onDispose { reg1.remove(); reg2.remove() }
    }

    val totalUnread = chats.sumOf { it.unreadCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Chats", fontWeight = FontWeight.Bold)
                        if (totalUnread > 0) {
                            Surface(shape = CircleShape, color = OrangeAccent) {
                                Text(
                                    "$totalUnread",
                                    color = Color.White, fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        when {
            user == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("Sign in to see your chats",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Your conversations will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF888888), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onLoginRequired,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) { Text("Sign In", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
            }
            chats.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("No chats yet",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Send an inquiry on any listing to start chatting",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF888888), textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onExplore,
                            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(25.dp)
                        ) { Text("Explore Listings", fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onPostAd,
                            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) { Text("Post an Ad", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chats, key = { it.inquiryId }) { chat ->
                        ChatCard(chat = chat, onClick = { onChatClick(chat.inquiryId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatCard(chat: ChatPreview, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chat.unreadCount > 0) Color(0xFFF0F4FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (chat.unreadCount > 0) 3.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (chat.isOwner) OrangeAccent else NavyPrimary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        chat.otherPartyName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chat.listingTitle,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        formatChatTime(chat.timestamp),
                        fontSize = 10.sp,
                        color = if (chat.unreadCount > 0) NavyPrimary else Color.LightGray
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(chat.otherPartyName, fontSize = 12.sp, color = Color.Gray)
                        Text(
                            chat.lastMessage,
                            fontSize = 12.sp,
                            color = if (chat.unreadCount > 0) Color.DarkGray else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    if (chat.unreadCount > 0) {
                        Surface(shape = CircleShape, color = OrangeAccent) {
                            Text(
                                "${chat.unreadCount}",
                                color = Color.White, fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000    -> "now"
        diff < 3600000  -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else            -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}