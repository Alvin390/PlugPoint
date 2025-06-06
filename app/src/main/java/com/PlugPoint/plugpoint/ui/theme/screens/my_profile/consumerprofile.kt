package com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile




import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.PlugPoint.plugpoint.data.AuthViewModel
import com.PlugPoint.plugpoint.data.ImgurUploadState
import com.PlugPoint.plugpoint.data.ImgurViewModel
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.navigation.ROUTE_COMMODITY_LIST
import com.PlugPoint.plugpoint.navigation.ROUTE_LOGIN
import com.PlugPoint.plugpoint.navigation.ROUTE_NOTIFICATION
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_PROFILE_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_CONSUMER
import com.PlugPoint.plugpoint.navigation.ROUTE_SEARCH_SUPPLIER
import com.PlugPoint.plugpoint.navigation.ROUTE_SETTINGS
import com.PlugPoint.plugpoint.networks.ImgurAPI
import com.PlugPoint.plugpoint.networks.ImgurAPIFactory
import com.PlugPoint.plugpoint.ui.theme.gray
import com.PlugPoint.plugpoint.ui.theme.red
import com.PlugPoint.plugpoint.utilis.ImgurViewModelFactory
import kotlin.text.category
import kotlin.text.ifEmpty


@Composable
fun ConsumerProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userId: String
) {
    authViewModel.fetchProfileDetails(userId, "consumer")
    val userConsumer by authViewModel.consumerDetails.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) } // Controlled by card click

    // Handle back press
    BackHandler {
        showLogoutDialog = true
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logoutUser {
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(0) // Clear the back stack
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = red)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = gray)
                ) {
                    Text("No")
                }
            }
        )
    }

    LaunchedEffect(userId) {
        authViewModel.fetchProfileDetails(userId, "consumer")
    }

    Scaffold(
        topBar = { ConsumerTopBar() },
        bottomBar = { ConsumerBottomNavBar(navController, userId) }
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
                        Triple("Requests Made", R.drawable.applicationsmadeconsumer, null),
                        Triple("Accepted Requests", R.drawable.acceptedapplications, null),
                        Triple("Saved Suppliers", R.drawable.savedsuppliersconsumer, null),
                        Triple("Edit Profile", R.drawable.editprofileconsumer, ROUTE_PROFILE_CONSUMER),
                        Triple("Settings", R.drawable.settingsconsumer, ROUTE_SETTINGS),
                        Triple("Logout", R.drawable.logoutsupply, ROUTE_LOGIN)
                    )
                ) { (title, imageRes, route) ->
                    FeatureCard(
                        title = title,
                        imageRes = imageRes,
                        navController = navController,
                        route = route,
                        onEditProfileClick = {
                            if (title == "Edit Profile" && userConsumer != null) {
                                showEditDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog && userConsumer != null) {
        EditConsumerProfileDialog(
            userConsumer = userConsumer!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedData, imageUri ->
                authViewModel.updateUserDetails(
                    userId = userId,
                    userType = "consumer",
                    updatedData = updatedData,
                    imageUri = imageUri,
                    onUpdateSuccess = { showEditDialog = false },
                    onUpdateFailure = { error -> println("Error: $error") }
                )
            }
        )
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
    val imageUrl = userConsumer.imageUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
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
fun ConsumerBottomNavBar(navController: NavController, userId: String) {
    val items = listOf("My Profile", "Search", "Notifications", "Chat")
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Search,
        Icons.Default.Notifications,
        Icons.Default.MailOutline
    )
    val routes = listOf(
        "$ROUTE_PROFILE_CONSUMER/$userId", // Full route for "My Profile"
        "$ROUTE_SEARCH_CONSUMER/$userId", // Full route for "Search"
        null,                             // Notifications (not built yet)
        "chat_screen"
    )

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
                selected = routes[index] == currentRoute, // Match full route
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
fun FeatureCard(
    title: String,
    @DrawableRes imageRes: Int,
    navController: NavController,
    route: String?, // Make this nullable
    onEditProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable {
                if (title == "Edit Profile") {
                    onEditProfileClick()
                } else if (route != null) { // Only navigate if route is not null
                    navController.navigate(route)
                }
            },
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

@Composable
fun EditConsumerProfileDialog(
    userConsumer: UserConsumer,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>, Uri?) -> Unit // Updated to accept Uri? instead of String?
) {
    var firstName by remember { mutableStateOf(userConsumer.firstName) }
    var lastName by remember { mutableStateOf(userConsumer.lastName) }
    var county by remember { mutableStateOf(userConsumer.county) }
    var category by remember { mutableStateOf(userConsumer.category) }
    var email by remember { mutableStateOf(userConsumer.email) }
    var phoneNumber by remember { mutableStateOf(userConsumer.phoneNumber) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imgurViewModel: ImgurViewModel = viewModel(factory = ImgurViewModelFactory(ImgurAPIFactory.create()))

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        imgurViewModel.uploadImage(uri, context, "Client-ID 7d779f374d40497")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") })
                TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") })
                TextField(value = county, onValueChange = { county = it }, label = { Text("County") })
                TextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Upload Photo")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedData = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "county" to county,
                    "category" to category,
                    "email" to email,
                    "phoneNumber" to phoneNumber
                )
                onSave(updatedData, imageUri) // Pass Uri? directly
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
//@Preview
//@Composable
//private fun consumer_profile_preview() {
//    ConsumerProfileScreen(rememberNavController(),userId = "sampleUserId",viewModel = AuthViewModel())
//}


