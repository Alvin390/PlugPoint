package com.PlugPoint.plugpoint.ui.theme.screens.commodity_view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.data.CommodityShowViewModel
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerTopBar
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar

@Composable
fun CommodityView(
    navController: NavController,
    supplierId: String,
    searcherRole: String,
    viewModel: CommodityShowViewModel = viewModel()
) {
    val commodities = viewModel.commodities.collectAsState().value

    LaunchedEffect(supplierId) {
        viewModel.fetchCommoditiesForSupplier(supplierId)
    }

    Scaffold(
        topBar = {
            if (searcherRole == "consumer") ConsumerTopBar() else SupplierTopBar()
        },
        bottomBar = {
            if (searcherRole == "consumer") ConsumerBottomNavBar(navController, supplierId)
            else SupplierBottomNavBar(navController, supplierId)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(commodities) { commodity ->
                    CommodityListItem(
                        commodity = commodity,
                        onClick = {
                            if (searcherRole == "consumer") {
                                // Placeholder for click logic
                            }
                        },
                        clickable = searcherRole == "consumer"
                    )
                }
            }
        }
    }
}

@Composable
fun CommodityListItem(commodity: Commodity, onClick: () -> Unit, clickable: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickable) { onClick() }
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = commodity.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Quantity: ${commodity.quantity}", style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "${commodity.cost} ${commodity.currency}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}
