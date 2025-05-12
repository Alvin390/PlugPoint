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
    internal fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getMessages(userId: String): StateFlow<List<ChatMessage>> {
        val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            println("Error: User not logged in")
            return messagesFlow
        }
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
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            println("Error: User not logged in")
            return
        }
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        db.collection("messages").add(message)
    }

    private val _searchResults = MutableStateFlow<List<ChatUser>>(emptyList())
    val searchResults: StateFlow<List<ChatUser>> = _searchResults
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)


    fun searchUsersByName(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        val lowercaseQuery = query.lowercase()
        db.collection("users")
            .whereGreaterThanOrEqualTo("name", lowercaseQuery)
            .whereLessThanOrEqualTo("name", lowercaseQuery + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error searching users: ${error.message}")
                    _searchResults.value = emptyList()
                    return@addSnapshotListener
                }
                _searchResults.value = snapshot?.toObjects(ChatUser::class.java) ?: emptyList()
            }
    }

    init {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            _chatUsers.value = snapshot?.toObjects(ChatUser::class.java) ?: emptyList()
        }
    }
}
