package com.PlugPoint.plugpoint.ui.theme.screens.my_profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.R
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.models.UserSupplier
import com.PlugPoint.plugpoint.navigation.ROUTE_COMMODITY_LIST
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_SUPPLIER
import kotlin.sequences.ifEmpty
import kotlin.text.category

@Composable
fun SupplierProfileScreen(navController: NavController, viewModel: AuthViewModel, userId: String) {
    val userSupplier by viewModel.supplierDetails.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchProfileDetails(userId, "supplier")
    }
    Scaffold(
        topBar = { SupplierTopBar() },
        bottomBar = { SupplierBottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Details
            if (userSupplier != null) {
                ProfileDetails(userSupplier!!)
            } else {
                Text("Loading...", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Clickable Feature Cards in a Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    listOf(
                        "Commodities" to R.drawable.commodities,
                        "Accepted Applications" to R.drawable.acceptedapplications,
                        "All Applications" to R.drawable.applications,
                        "Edit Profile" to R.drawable.editprofilesupply,
                        "Settings" to R.drawable.settingssupply,
                        "Logout" to R.drawable.logoutsupply
                    )
                ) { (title, imageRes) ->
                    FeatureCard(
                        title = title,
                        imageRes = imageRes,
                        onClick = {
                            when (title) {
                                "Commodities" -> navController.navigate(ROUTE_COMMODITY_LIST)
                                "Accepted Applications" -> { /* Add navigation or logic here */ }
                                "All Applications" -> { /* Add navigation or logic here */ }
                                "Edit Profile" -> { /* Add navigation or logic here */ }
                                "Settings" -> { /* Add navigation or logic here */ }
                                "Logout" -> { /* Add logout logic here */ }
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun ProfileDetails(userSupplier: UserSupplier) {
    val name = "${userSupplier.firstName} ${userSupplier.lastName}"
    val companyName = userSupplier.companyName.ifEmpty { "No Company" }
    val county = userSupplier.county
    val category = userSupplier.category
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_placeholder),
            contentDescription = "Profile",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(companyName, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(county, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(category, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Consumer", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun SupplierTopBar() {
    val gradientColors = listOf(
        Color(0xFFFFA500), // orange
        Color(0xFFFF8C00), // darkOrange
        Color(0xFFFF7F50), // coral
        Color(0xFFFF6347)  // tomatoOrange
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(gradientColors))
            .padding(vertical = 18.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "PlugPoint",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Cursive
        )
    }
}

@Composable
fun SupplierBottomNavBar(navController: NavController) {
    val items = listOf("My Profile", "Search", "Notifications", "Chat")
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Search,
        Icons.Default.Notifications,
        Icons.Default.MailOutline
    )
    val routes = listOf(
        ROUTE_PROFILE_SUPPLIER, // Navigate to "My Profile"
        ROUTE_SEARCH_SUPPLIER,  // Navigate to "Search"
        null,                   // Notifications (not built yet)
        null                    // Chat (not built yet)
    )

    val currentRoute = navController.currentBackStackEntry?.destination?.route

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
                selected = routes[index] != null && currentRoute?.startsWith(routes[index] ?: "") == true,
                onClick = {
                    val userId = "sampleUserId" // Replace with the actual user ID
                    if (routes[index] != null && routes[index] != currentRoute) {
                        navController.navigate("${routes[index]}/$userId") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF8C00),
                    selectedTextColor = Color(0xFFFF8C00),
                    indicatorColor = Color(0xFFFFEFD5),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                )
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    @DrawableRes imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5EE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview
@Composable
private fun supplier_profile_preview() {
    SupplierProfileScreen(rememberNavController(), viewModel = AuthViewModel(), userId = "sampleUserId")
}