import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel.User
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.search_screen_consumer.SearchBarUI

@Composable
fun SearchScreenSupplier(navController: NavController, viewModel: SearchSupplierAuthViewModel) {
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            SearchBarUI(searchText) { query ->
                searchText = query.trim().lowercase() // Normalize query
                viewModel.searchUsers(searchText) // Trigger search on query change
            }
        },
        bottomBar = { ConsumerBottomNavBar(navController) }
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
                        UserRow(user = user)
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
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFFFF6347) // Blue color for consumer
            )
        )
    }
}
    @Composable
    fun UserRow(user: User) {
    val name = when (user) {
        is User.Supplier -> "${user.user.firstName} ${user.user.lastName}"
        is User.Consumer -> "${user.user.firstName} ${user.user.lastName}"
    }
    val county = when (user) {
        is User.Supplier -> user.user.county
        is User.Consumer -> user.user.county
    }
    val category = when (user) {
        is User.Supplier -> user.user.category
        is User.Consumer -> user.user.category
    }
    val imageUri = when (user) {
        is User.Supplier -> user.user.imageUri
        is User.Consumer -> user.user.imageUri
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!imageUri.isNullOrEmpty()) {
            // Use Imgur URL for profile picture
            Image(
                painter = rememberAsyncImagePainter("https://i.imgur.com/$imageUri.jpg"),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold)
            Text(county)
            Text(category)
        }
    }
}
