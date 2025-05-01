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
import com.PlugPoint.plugpoint.R


@Composable
fun ConsumerProfileScreen() {
    Scaffold(
        topBar = { ConsumerTopBar() },
        bottomBar = { ConsumerBottomNavBar() }
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
            ProfileDetails()

            Spacer(modifier = Modifier.height(16.dp))

            // Clickable Feature Cards in a Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 cards per row
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    listOf(
                        "Applications Made" to R.drawable.applicationsmadeconsumer,
                        "Accepted Applications" to R.drawable.acceptedapplications,
                        "Saved Suppliers" to R.drawable.savedsuppliersconsumer,
                        "Edit Profile" to R.drawable.editprofileconsumer,
                        "Settings" to R.drawable.settingsconsumer,
                        "Logout" to R.drawable.logoutsupply
                    )
                ) { (title, imageRes) ->
                    FeatureCard(title, imageRes)
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
fun ProfileDetails() {
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
            Text("John Smith", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Consumer", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ConsumerBottomNavBar() {
    val items = listOf("My Profile", "Search", "Notifications", "Chat")
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Search,
        Icons.Default.Notifications,
        Icons.Default.MailOutline
    )
    var selectedIndex by remember { mutableStateOf(0) }

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
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
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
fun FeatureCard(title: String, @DrawableRes imageRes: Int) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable { /* TODO: Handle click */ },
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

@Preview
@Composable
private fun consumer_profile_preview() {
    ConsumerProfileScreen()
}
