package com.PlugPoint.plugpoint.ui.theme.screens.chat_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.ChatViewModel
import com.PlugPoint.plugpoint.models.ChatMessage
import kotlinx.coroutines.tasks.await

@Composable
fun ChatScreen2(
    navController: NavController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    userId: String
) {
    val messages by chatViewModel.messages.collectAsState()
    val userIdCurrent = authViewModel.getLoggedInUserId()
    var messageText by remember { mutableStateOf("") }

    // Find or create conversation ID
    LaunchedEffect(userId) {
        if (userIdCurrent == null) return@LaunchedEffect // Skip if not logged in
        val participants = listOf(userIdCurrent, userId).sorted() // Built-in sorted()
        val conversationQuery = FirebaseFirestore.getInstance()
            .collection("conversations")
            .whereEqualTo("participants", participants)
            .get()
            .await()
        val conversationId = if (conversationQuery.isEmpty) {
            val newConversation = hashMapOf(
                "participants" to participants,
                "lastMessage" to "",
                "lastMessageTime" to 0L,
                "lastMessageSenderId" to ""
            )
            val docRef = FirebaseFirestore.getInstance()
                .collection("conversations")
                .add(newConversation)
                .await()
            docRef.id
        } else {
            conversationQuery.documents.first().id
        }
        chatViewModel.listenForMessages(conversationId)
    }

    if (userIdCurrent == null) {
        Text(
            text = "You must be logged in to send messages.",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageItem(
                    message = message,
                    isCurrentUser = message.senderId == userIdCurrent
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        chatViewModel.sendMessage(userId, messageText)
                        messageText = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                fontSize = 16.sp
            )
        }
    }
}