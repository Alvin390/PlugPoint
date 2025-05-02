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

@Composable
fun Search_supply_screen(navController: NavController) {
    Scaffold(
        topBar = { SearchBarUI() },
        bottomBar = { SupplierBottomNavBarSearch() }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
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

@Composable
fun SupplierBottomNavBarSearch() {
    val items = listOf("My Profile", "Search", "Notifications", "Chat")
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Search,
        Icons.Default.Notifications,
        Icons.Default.MailOutline
    )
    var selectedIndex by remember { mutableStateOf(1) }

    NavigationBar(
        containerColor = Color(0xFFFFDEAD),
        contentColor = Color.Black,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, label ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = label
                    )
                },
                label = { Text(label, fontSize = 12.sp) },
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF8C00),
                    selectedTextColor = Color(0xFFFF8C00),
                    indicatorColor = Color(0xFFFFEFD5)
                )
            )
        }
    }
}

@Preview
@Composable
private fun search_supply_preview() {
    Search_supply_screen(rememberNavController())
}
