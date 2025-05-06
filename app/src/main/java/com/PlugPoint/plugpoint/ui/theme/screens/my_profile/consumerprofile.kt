package com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile




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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.PlugPoint.plugpoint.R
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.navigation.ROUTE_COMMODITY_LIST
import com.PlugPoint.plugpoint.navigation.ROUTE_NOTIFICATION
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_SUPPLIER
import kotlin.text.category
import kotlin.text.ifEmpty


@Composable
fun ConsumerProfileScreen(navController: NavController,
                          authViewModel: AuthViewModel= viewModel(),
                          userId: String) {
    authViewModel.fetchProfileDetails(userId, "consumer")
    val userConsumer by authViewModel.consumerDetails.collectAsState()

    LaunchedEffect(userId) {
        authViewModel.fetchProfileDetails(userId, "consumer")
    }
    Scaffold(
        topBar = { ConsumerTopBar() },
        bottomBar = { ConsumerBottomNavBar(navController) }
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

            if (userConsumer != null) {
                ProfileDetails(userConsumer = userConsumer!!)
            } else {
                Text("Loading...", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    listOf(
                        Triple("Applications Made", R.drawable.applicationsmadeconsumer, ROUTE_COMMODITY_LIST),
                        Triple("Accepted Applications", R.drawable.acceptedapplications, ROUTE_NOTIFICATION),
                        Triple("Saved Suppliers", R.drawable.savedsuppliersconsumer, ROUTE_SEARCH_CONSUMER),
                        Triple("Edit Profile", R.drawable.editprofileconsumer, ROUTE_PROFILE_CONSUMER),
                        Triple("Settings", R.drawable.settingsconsumer, ROUTE_SEARCH_SUPPLIER),
                        Triple("Logout", R.drawable.logoutsupply, ROUTE_PROFILE_SUPPLIER)
                    )
                ) { (title, imageRes, route) ->
                    FeatureCard(title, imageRes, navController, route)
                }
            }
        }
    }
}

@Composable
fun ConsumerTopBar() {
    val gradientColors = listOf(
        Color(0xFF0000FF), // blue2
        Color(0xFF1E90FF), // dodgerBlue
        Color(0xFF87CEEB), // skyBlue
        Color(0xFF00CED1)  // darkTurquoise
    )
    Box(
        modifier = Modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
            .fillMaxWidth()
            .background(Brush.horizontalGradient(gradientColors))
            .padding(vertical = 18.dp, horizontal = 16.dp)
            .padding(WindowInsets.statusBars.asPaddingValues()) // Add padding for the status bar
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
fun ProfileDetails(userConsumer: UserConsumer) {
    val name = "${userConsumer.firstName} ${userConsumer.lastName}"
    val county = userConsumer.county
    val category = userConsumer.category
    val userType = "Consumer" // Assuming this Composable is specifically for consumers

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
        Column(
            modifier = Modifier.weight(1f) // Allow text column to take available space
        ) {
            // Display Name with larger font and strong emphasis
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp, // Larger font size for name
                color = Color.Black // Ensure good contrast
            )

            Spacer(modifier = Modifier.height(4.dp)) // Add some vertical space

            // Use smaller text for secondary details and add labels
            Text(
                text = "Category: $category", // Category is relevant for consumers too
                fontSize = 14.sp,
                color = Color.DarkGray // Slightly less prominent color
            )
            Text(
                text = "County: $county",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp)) // Add more vertical space

            // Display user type dynamically
            Text(
                text = "$userType", // Use the dynamic userType variable
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary, // Use your theme's primary color
                fontWeight = FontWeight.Medium // Make it slightly bold
            )
        }
    }
}

@Composable
fun ConsumerBottomNavBar(navController: NavController) {
    val items = listOf("My Profile", "Search", "Notifications", "Chat")
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Search,
        Icons.Default.Notifications,
        Icons.Default.MailOutline
    )
    val routes = listOf(
        ROUTE_PROFILE_CONSUMER, // Navigate to "My Profile"
        ROUTE_SEARCH_CONSUMER,  // Navigate to "Search"
        null,                   // Notifications (not built yet)
        null                    // Chat (not built yet)
    )

    // Get the current route
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFADD8E6), // lightBlue
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
                selected = routes[index] == currentRoute,
                onClick = {
                    if (routes[index] != null && routes[index] != currentRoute) {
                        navController.navigate(routes[index]!!) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1E90FF),
                    selectedTextColor = Color(0xFF1E90FF),
                    indicatorColor = Color(0xFF87CEFA)
                )
            )
        }
    }
}

@Composable
fun FeatureCard(title: String, @DrawableRes imageRes: Int, navController: NavController, route: String) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable { navController.navigate(route) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F2FF))
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

//@Preview
//@Composable
//private fun consumer_profile_preview() {
//    ConsumerProfileScreen(rememberNavController(),userId = "sampleUserId",viewModel = AuthViewModel())
//}
