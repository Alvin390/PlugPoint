package com.PlugPoint.plugpoint.data

import com.PlugPoint.plugpoint.utilis.FirestoreCollections
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    // Firestore listener references to prevent leaks
    private var conversationsListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null

    init {
        listenForConversations()
    }

    private fun listenForConversations() {
        viewModelScope.launch {
            val userId = authViewModel.getLoggedInUserId() ?: return@launch
            conversationsListener?.remove()
            conversationsListener = db.collection(FirestoreCollections.CONVERSATIONS)
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

                            val otherUserName = fetchUserName(otherUserId)

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
    }

    fun listenForMessages(conversationId: String) {
        messagesListener?.remove()
        messagesListener = db.collection(FirestoreCollections.CONVERSATIONS)
            .document(conversationId)
            .collection(FirestoreCollections.MESSAGES)
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
    viewModelScope.launch {
        val senderId = authViewModel.getLoggedInUserId() ?: return@launch
        val participants = listOf(senderId, receiverId).sorted()
        val conversationId = participants.joinToString("_")
        try {
            val convoRef = db.collection(FirestoreCollections.CONVERSATIONS).document(conversationId)
            val convoSnap = convoRef.get().await()

            if (!convoSnap.exists()) {
                convoRef.set(
                    mapOf(
                        "participants" to participants,
                        "lastMessage" to text,
                        "lastMessageTime" to System.currentTimeMillis(),
                            "lastMessageSenderId" to senderId
                        )
                    ).await()
                } else {
                    convoRef.update(
                        mapOf(
                            "lastMessage" to text,
                            "lastMessageTime" to System.currentTimeMillis(),
                            "lastMessageSenderId" to senderId
                        )
                    ).await()
                }

                val message = hashMapOf(
                    "senderId" to senderId,
                    "receiverId" to receiverId,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection(FirestoreCollections.CONVERSATIONS)
                    .document(conversationId)
                    .collection(FirestoreCollections.MESSAGES)
                    .add(message)
                    .await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun fetchUserName(uid: String): String = withContext(Dispatchers.IO) {
        // Try suppliers
        val supplierDoc = db.collection(FirestoreCollections.SUPPLIERS).document(uid).get().await()
        if (supplierDoc.exists()) {
            val first = supplierDoc.getString("firstName") ?: ""
            val last = supplierDoc.getString("lastName") ?: ""
            return@withContext "$first $last"
        }
        val consumerDoc = db.collection(FirestoreCollections.CONSUMERS).document(uid).get().await()
        if (consumerDoc.exists()) {
            val first = consumerDoc.getString("firstName") ?: ""
            val last = consumerDoc.getString("lastName") ?: ""
            return@withContext "$first $last"
        }
        return@withContext "Unknown"
    }

    override fun onCleared() {
        super.onCleared()
        conversationsListener?.remove()
        messagesListener?.remove()
    }
}