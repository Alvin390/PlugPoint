package com.PlugPoint.plugpoint.ui.theme.screens.search_screen_supplier


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_SUPPLIER
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar

@Composable
fun Search_supply_screen(navController: NavController,userId: String) {
    Scaffold(
        topBar = { SearchBarUI() },
        bottomBar = { SupplierBottomNavBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(WindowInsets.statusBars.asPaddingValues()) // Add padding for the status bar
        )
    }
}

@Composable
fun SearchBarUI() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(WindowInsets.statusBars.asPaddingValues()) // Add padding for the status bar
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
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
    Search_supply_screen(rememberNavController(),userId = "sampleUserId")
}
