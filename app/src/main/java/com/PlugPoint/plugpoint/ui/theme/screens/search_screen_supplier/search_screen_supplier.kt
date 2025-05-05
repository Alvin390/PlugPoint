package com.PlugPoint.plugpoint.ui.theme.screens.search_screen_supplier



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel
import com.PlugPoint.plugpoint.data.SearchSupplierAuthViewModel.User
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar

@Composable
fun Search_supply_screen(navController: NavController, userId: String, viewModel: SearchSupplierAuthViewModel) {
    var searchText by remember { mutableStateOf("") } // Use String instead of TextFieldValue
    val searchResults by viewModel.searchResults.collectAsState()

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
                .padding(WindowInsets.statusBars.asPaddingValues())
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold)
                Text(county)
                Text(category)
            }
        }
    }
    Scaffold(
        topBar = {
            SearchBarUI(searchText) { query ->
                searchText = query
                viewModel.searchUsers(query)
            }
        },
        bottomBar = { SupplierBottomNavBar(navController, userId) } // Pass userId here
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (searchResults.isEmpty()) {
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
fun SearchBarUI(searchText: String, onSearch: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = searchText, // Use String directly
            onValueChange = { onSearch(it) }, // Pass the updated string
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
                focusedBorderColor = Color(0xFFFF8C00)
            )
        )
    }
}

//@Composable
//fun SupplierBottomNavBar(navController: NavController) {
//    val items = listOf("My Profile", "Search", "Notifications", "Chat")
//    val icons = listOf(
//        Icons.Default.Person,
//        Icons.Default.Search,
//        Icons.Default.Notifications,
//        Icons.Default.MailOutline
//    )
//    val routes = listOf(
//        ROUTE_PROFILE_SUPPLIER, // Navigate to "My Profile"
//        ROUTE_SEARCH_SUPPLIER,  // Navigate to "Search"
//        null,                   // Notifications (not built yet)
//        null                    // Chat (not built yet)
//    )
//
//    // Get the current route
//    val currentRoute = navController.currentBackStackEntry?.destination?.route
//
//    NavigationBar(
//        containerColor = Color(0xFFFFDEAD),
//        contentColor = Color.Black,
//        tonalElevation = 8.dp
//    ) {
//        items.forEachIndexed { index, label ->
//            NavigationBarItem(
//                icon = {
//                    Icon(
//                        imageVector = icons[index],
//                        contentDescription = label
//                    )
//                },
//                label = { Text(label, fontSize = 12.sp) },
//                selected = routes[index] == currentRoute,
//                onClick = {
//                    if (routes[index] != null && routes[index] != currentRoute) {
//                        navController.navigate(routes[index]!!) {
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color(0xFFFF8C00),
//                    selectedTextColor = Color(0xFFFF8C00),
//                    indicatorColor = Color(0xFFFFEFD5),
//                    unselectedIconColor = Color.Gray,
//                    unselectedTextColor = Color.Gray
//                )
//            )
//        }
//    }
//}

@Preview
@Composable
private fun search_supply_preview() {
    val mockViewModel = SearchSupplierAuthViewModel()
    Search_supply_screen(rememberNavController(), userId = "sampleUserId", viewModel = mockViewModel)
}