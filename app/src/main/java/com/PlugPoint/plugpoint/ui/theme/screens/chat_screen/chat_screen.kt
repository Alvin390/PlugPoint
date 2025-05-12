package com.PlugPoint.plugpoint.ui.theme.screens.chat_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.data.ChatViewModel
import com.PlugPoint.plugpoint.models.ChatUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, chatViewModel: ChatViewModel) {
    val chatUsers by chatViewModel.chatUsers.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WindowInsets.navigationBars.asPaddingValues())
        ) {
            items(chatUsers) { user ->
                ChatUserItem(user = user, onClick = {
                    navController.navigate("chat_screen_2/${user.id}")
                })
            }
        }
    }
}

@Composable
fun ChatUserItem(user: ChatUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(user.name, style = MaterialTheme.typography.bodyLarge)
    }
}
