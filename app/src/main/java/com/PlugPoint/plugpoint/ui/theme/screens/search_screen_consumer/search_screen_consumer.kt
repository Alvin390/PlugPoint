package com.PlugPoint.plugpoint.ui.theme.screens.search_screen_consumer



import UserRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerBottomNavBar

@Composable
fun SearchConsumerScreen(navController: NavController, viewModel: SearchSupplierAuthViewModel, userId: String) {
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            SearchBarUI(searchText) { query ->
                searchText = query.trim().lowercase() // Normalize query
                viewModel.searchUsers(searchText) // Trigger search on query change
            }
        },
        bottomBar = { ConsumerBottomNavBar(navController,userId) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (searchText.isNotEmpty() && searchResults.isEmpty()) {
                Text(
                    "No results found",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { user ->
                        UserRow(user = user, navController = navController, searcherRole = "consumer")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarUI(searchText: String, onSearchTextChanged: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(WindowInsets.statusBars.asPaddingValues()) // Add padding for the status bar
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search...", fontSize = 16.sp) },
            singleLine = true,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF1E90FF) // Blue color for consumer
            )
        )
    }
}

data class UserRowData(
    val name: String,
    val type: String,
    val imageUrl: String,
    val route: String
)

@Composable
fun UserRow(user: SearchSupplierAuthViewModel.User, navController: NavController) {
    val userRowData = when (user) {
        is SearchSupplierAuthViewModel.User.Supplier -> {
            UserRowData(
                name = "${user.user.firstName} ${user.user.lastName}",
                type = "Supplier",
                imageUrl = user.user.imageUrl,
                route = "supplier_view/${user.id}/consumer" // Pass searcher's role
            )
        }
        is SearchSupplierAuthViewModel.User.Consumer -> {
            UserRowData(
                name = "${user.user.firstName} ${user.user.lastName}",
                type = "Consumer",
                imageUrl = user.user.imageUrl,
                route = "consumer_view/${user.id}/consumer" // Pass searcher's role
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(userRowData.route) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = userRowData.imageUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = userRowData.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = userRowData.type,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
        }
    }
}

//@Preview
//@Composable
//private fun search_consumer_preview() {
//    SearchConsumerScreen(rememberNavController(), SearchSupplierAuthViewModel())
//}

