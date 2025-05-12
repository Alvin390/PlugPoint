package com.PlugPoint.plugpoint.ui.theme.screens.supplier_view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.R
import com.PlugPoint.plugpoint.data.UserSearchViewModel
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerTopBar

@Composable
fun SupplierView(
    navController: NavController,
    userId: String,
    searcherRole: String,
    viewModel: UserSearchViewModel = viewModel()
) {
    val supplier by viewModel.selectedSupplier.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchSupplierDetails(userId)
    }

    Scaffold(
        topBar = {
            if (searcherRole == "consumer") ConsumerTopBar() else SupplierTopBar()
        },
        bottomBar = {
            if (searcherRole == "consumer") ConsumerBottomNavBar(navController, userId)
            else SupplierBottomNavBar(navController, userId)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
        ) {
            supplier?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = supplier?.imageUrl ?: R.drawable.profile_placeholder
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Profile Details
                    Column {
                        Text("${it.firstName} ${it.lastName}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Company: ${it.companyName}", fontSize = 14.sp, color = Color.Gray)
                        Text("Category: ${it.category}", fontSize = 14.sp, color = Color.Gray)
                        Text("County: ${it.county}", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        title = "Commodities",
                        iconRes = R.drawable.commodities,
                        onClick = {
                            navController.navigate("commodity_view/$userId/$searcherRole")
                        }
                    )
                    FeatureCard(title = "Chat", iconRes = R.drawable.chat, onClick = {
                        navController.navigate("chat_screen_2/${supplier?.id}")
                    })
                    FeatureCard(title = "Call", iconRes = R.drawable.dial) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:${it.phoneNumber}".toUri()
                        }
                        navController.context.startActivity(intent)
                    }
                }
            } ?: Text("Loading...", color = Color.Gray)
        }
    }
}

@Composable
fun FeatureCard(title: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5EE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = iconRes), contentDescription = title)
            Spacer(modifier = Modifier.height(3.dp))
            Text(title, fontSize = 14.sp)
        }
    }
}

