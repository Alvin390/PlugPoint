package com.PlugPoint.plugpoint.ui.theme.screens.chat_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.data.ChatViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.PlugPoint.plugpoint.models.ChatMessage
import com.PlugPoint.plugpoint.utilis.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen2(navController: NavController, chatViewModel: ChatViewModel, userId: String) {
    val messages by chatViewModel.getMessages(userId).collectAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
        val currentUserId = chatViewModel.getCurrentUserId()
        if (currentUserId == null) {
            LaunchedEffect(Unit) {
                navController.navigate("login") // Redirect to login screen
            }
            return
        }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WindowInsets.navigationBars.asPaddingValues())
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                items(messages) { message ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(message.text)
                        Text(
                            formatTimestamp(message.timestamp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            LaunchedEffect(messages) {
                listState.animateScrollToItem(messages.size - 1)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") }
                )
                Button(onClick = {
                    chatViewModel.sendMessage(userId, messageText)
                    messageText = ""
                }) {
                    Text("Send")
                }
            }
        }
    }
}
