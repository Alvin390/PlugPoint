package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.PlugPoint.plugpoint.models.ChatMessage
import com.PlugPoint.plugpoint.models.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.sortedBy

class ChatViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        listenForConversations()
    }

    private fun listenForConversations() {
        val userId = authViewModel.getLoggedInUserId() ?: return
        db.collection("conversations")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _conversations.value = emptyList()
                    return@addSnapshotListener
                }
                viewModelScope.launch {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        val participants = doc.get("participants") as? List<String> ?: return@mapNotNull null
                        val otherUserId = participants.find { it != userId }
                        if (otherUserId.isNullOrBlank()) return@mapNotNull null // Prevent crash

                        val lastMessage = doc.getString("lastMessage") ?: ""
                        val lastMessageTime = doc.getLong("lastMessageTime") ?: 0L
                        val lastMessageSenderId = doc.getString("lastMessageSenderId") ?: ""

                        val userDoc = db.collection("users").document(otherUserId).get().await()
                        val firstName = userDoc.getString("firstName") ?: ""
                        val lastName = userDoc.getString("lastName") ?: ""
                        val otherUserName = "$firstName $lastName"

                        Conversation(
                            id = doc.id,
                            otherUserId = otherUserId,
                            otherUserName = otherUserName,
                            lastMessage = lastMessage,
                            lastMessageTime = lastMessageTime,
                            lastMessageSenderId = lastMessageSenderId
                        )
                    }
                    _conversations.value = conversations // Firestore handles sorting
                }
            }
    }

    fun listenForMessages(conversationId: String) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _messages.value = emptyList()
                    return@addSnapshotListener
                }
                val messages = snapshot.documents.mapNotNull { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                _messages.value = messages.sortedBy { it.timestamp }
            }
    }

    fun sendMessage(receiverId: String, text: String) {
        val senderId = authViewModel.getLoggedInUserId() ?: return
        viewModelScope.launch {
            try {
                val participants = listOf(senderId, receiverId).sorted()
                val conversationQuery = db.collection("conversations")
                    .whereEqualTo("participants", participants)
                    .get()
                    .await()

                val conversationId = if (conversationQuery.isEmpty) {
                    val newConversation = hashMapOf(
                        "participants" to participants,
                        "lastMessage" to text,
                        "lastMessageTime" to System.currentTimeMillis(),
                        "lastMessageSenderId" to senderId
                    )
                    val docRef = db.collection("conversations").add(newConversation).await()
                    docRef.id
                } else {
                    val doc = conversationQuery.documents.first()
                    doc.reference.update(
                        mapOf(
                            "lastMessage" to text,
                            "lastMessageTime" to System.currentTimeMillis(),
                            "lastMessageSenderId" to senderId
                        )
                    ).await()
                    doc.id
                }

                val message = hashMapOf(
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .add(message)
                    .await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}