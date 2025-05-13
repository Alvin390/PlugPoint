package com.PlugPoint.plugpoint.ui.theme.screens.chat_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.ChatViewModel
import com.PlugPoint.plugpoint.models.Conversation
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel
) {
    val conversations by chatViewModel.conversations.collectAsState()
    val userId = authViewModel.getLoggedInUserId()

    if (userId == null) {
        Text(
            text = "You must be logged in to view chats.",
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
        Text(
            text = "Chats",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (conversations.isEmpty()) {
            Text(
                text = "No conversations yet.",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        currentUserId = userId,
                        onClick = {
                            navController.navigate("chat_screen_2/${conversation.otherUserId}")
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = conversation.otherUserName.take(1),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.otherUserName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = conversation.lastMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatTimestamp(conversation.lastMessageTime),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time
    return when {
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
    }
}