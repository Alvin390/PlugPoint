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
import com.PlugPoint.plugpoint.ui.theme.tomato
import kotlin.sequences.ifEmpty
import kotlin.text.category

@Composable
fun SupplierProfileScreen(navController: NavController, viewModel: AuthViewModel, userId: String) {
    val userSupplier by viewModel.supplierDetails.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchProfileDetails(userId, "supplier")
    }

    LaunchedEffect(Unit) {
        println("Rendering SupplierProfileScreen")
    }

    LaunchedEffect(userId) {
        viewModel.fetchProfileDetails(userId, "supplier")
        println("Fetching profile details for userId: $userId")
    }

    if (userSupplier != null) {
        println("UserSupplier state updated: $userSupplier")
        ProfileDetails(userSupplier!!)
    } else {
        println("UserSupplier is null, showing Loading...")
        CircularProgressIndicator()
    }
    Scaffold(
        topBar = { SupplierTopBar() },
        bottomBar = { SupplierBottomNavBar(navController, userId) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(WindowInsets.statusBars.asPaddingValues())
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Details
            if (userSupplier != null) {
                println("UserSupplier state updated: $userSupplier")
                ProfileDetails(userSupplier!!)
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally),color= tomato)
                Text("Loading...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                                "Commodities" -> navController.navigate("$ROUTE_COMMODITY_LIST/$userId")
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
    val name = remember(userSupplier) { "${userSupplier.firstName} ${userSupplier.lastName}" }
    val county = remember(userSupplier) { userSupplier.county.ifEmpty { "Unknown County" } }
    val category = remember(userSupplier) { userSupplier.category.ifEmpty { "Unknown Category" } }
    val company = remember(userSupplier) { userSupplier.companyName.ifEmpty { "No Company Company" } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
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
            Text(name, fontWeight = FontWeight.Bold)
            Text(company)
            Text(category)
            Text(county)
            Text("Supplier", color = Color.Gray)

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
            .padding(WindowInsets.statusBars.asPaddingValues())
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
fun SupplierBottomNavBar(navController: NavController, userId: String) {
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
                onClick = { // Replace with the actual user ID
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
    SupplierProfileScreen(rememberNavController(), viewModel = AuthViewModel(), userId = "userId")
}