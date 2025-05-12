import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.data.ChatViewModel
import com.PlugPoint.plugpoint.models.ChatUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, chatViewModel: ChatViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val chatUsers by chatViewModel.chatUsers.collectAsState(initial = emptyList())
    val searchResults by chatViewModel.searchResults.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()

    @Composable
    fun ChatUserItem(user: ChatUser, onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick() }
        ) {
            Text(text = user.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = user.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WindowInsets.navigationBars.asPaddingValues())
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    chatViewModel.searchUsersByName(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search by name") }
            )
            if (isLoading) {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            } else if (!errorMessage.isNullOrEmpty()) {
                Text("Error: $errorMessage", color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val usersToDisplay = if (searchQuery.isBlank()) chatUsers else searchResults
                    if (usersToDisplay.isEmpty()) {
                        item {
                            Text("No results found", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(usersToDisplay) { user ->
                            ChatUserItem(user = user, onClick = {
                                navController.navigate("chat_screen_2/${user.id}")
                            })
                        }
                    }
                }
            }
        }
    }
}
