package com.nyumbahub.feature.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.nyumbahub.core.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    val isLoading = MutableStateFlow(false)
    private var inquiryId: String = ""
    private var listener: ListenerRegistration? = null

    fun loadMessages(inquiryId: String) {
        this.inquiryId = inquiryId
        isLoading.value = true
        listener?.remove()
        listener = db.collection("inquiries")
            .document(inquiryId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    _messages.value = snap.documents.mapNotNull { doc ->
                        try {
                            Message(
                                id        = doc.id,
                                inquiryId = inquiryId,
                                senderId  = doc.getString("senderId") ?: "",
                                text      = doc.getString("text") ?: "",
                                sentAt    = doc.getLong("sentAt") ?: 0L,
                                readAt    = doc.getLong("readAt") ?: 0L
                            )
                        } catch (e: Exception) { null }
                    }
                }
                isLoading.value = false
            }
    }

    fun sendMessage(senderId: String, text: String) {
        if (inquiryId.isEmpty() || text.isBlank()) return
        val uid = senderId.ifEmpty { FirebaseAuth.getInstance().currentUser?.uid ?: return }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val data = mapOf(
                "inquiryId" to inquiryId,
                "senderId"  to uid,
                "text"      to text,
                "sentAt"    to now,
                "readAt"    to null
            )
            db.collection("inquiries").document(inquiryId)
                .collection("messages")
                .add(data)
                .addOnSuccessListener {
                    // Update inquiry with last message + increment unread for the other party
                    db.collection("inquiries").document(inquiryId).get()
                        .addOnSuccessListener { doc ->
                            val isOwner = doc.getString("ownerId") == uid
                            val unreadField = if (isOwner) "unreadBySender" else "unreadByOwner"
                            val current = doc.getLong(unreadField) ?: 0L
                            doc.reference.update(
                                mapOf(
                                    "lastMessage" to text,
                                    "updatedAt"   to now,
                                    unreadField   to current + 1
                                )
                            ).addOnFailureListener {
                                android.util.Log.e("ChatViewModel", "Failed to update inquiry", it)
                            }
                        }
                        .addOnFailureListener {
                            android.util.Log.e("ChatViewModel", "Failed to fetch inquiry for update", it)
                        }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
