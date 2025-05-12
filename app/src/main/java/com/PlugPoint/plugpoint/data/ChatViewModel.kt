package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.PlugPoint.plugpoint.models.ChatMessage
import com.PlugPoint.plugpoint.models.ChatUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    fun getMessages(userId: String): StateFlow<List<ChatMessage>> {
        val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
        val currentUserId = getCurrentUserId()
        db.collection("messages")
            .whereIn("receiverId", listOf(userId, currentUserId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error fetching messages: ${error.message}")
                    return@addSnapshotListener
                }
                messagesFlow.value = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
            }
        return messagesFlow
    }

    fun sendMessage(receiverId: String, text: String) {
        val currentUserId = getCurrentUserId() // Implement this method to fetch the logged-in user's ID
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        db.collection("messages").add(message)
    }

    init {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            _chatUsers.value = snapshot?.toObjects(ChatUser::class.java) ?: emptyList()
        }
    }
}
