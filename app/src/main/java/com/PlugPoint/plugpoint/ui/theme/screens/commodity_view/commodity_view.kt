package com.PlugPoint.plugpoint.ui.theme.screens.commodity_view

import CommodityShowViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.PlugPoint.plugpoint.data.RequestsViewModel
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.models.Requests
import com.PlugPoint.plugpoint.ui.theme.blue
import com.PlugPoint.plugpoint.ui.theme.blue1
import com.PlugPoint.plugpoint.ui.theme.green1
import com.PlugPoint.plugpoint.ui.theme.lightBlue
import com.PlugPoint.plugpoint.ui.theme.screens.commodity_list_supplier.CommodityListItem
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerTopBar
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.toString

// Updated CommodityView function with proper currency handling
// Updated CommodityView function with proper currency handling
@Composable
fun CommodityView(
    navController: NavController,
    supplierId: String,
    searcherRole: String,
    viewModel: CommodityShowViewModel = viewModel(),
    requestsViewModel: RequestsViewModel = viewModel()
) {
    val commodities = viewModel.commodities.collectAsState().value
    val showDialog = remember { mutableStateOf(false) }
    val selectedCommodity = remember { mutableStateOf<Commodity?>(null) }
    val consumerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val snackbarHostState = remember { SnackbarHostState() } // Add Snackbar for error feedback
    val coroutineScope = rememberCoroutineScope()

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // Add Snackbar host
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (commodities.isEmpty()) {
                Text("No commodities available", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(commodities) { commodity ->
                        CommodityListItem(
                            commodity = commodity,
                            onClick = {
                                if (searcherRole == "consumer") {
                                    selectedCommodity.value = commodity
                                    showDialog.value = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        selectedCommodity.value?.let { commodity ->
            RequestDialog(
                commodity = commodity,
                onDismiss = { showDialog.value = false },
                onConfirm = { quantity, paymentMethod ->
                    if (consumerId.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Request made !")
                        }
//                        return@RequestDialog
                    }
                    // Extract numeric part of the cost
                    val costPerUnit = extractNumericCost(commodity.cost)
                    val totalCost = requestsViewModel.calculateTotalCost(quantity, costPerUnit)
                    val request = Requests(
                        consumerId = consumerId,
                        supplierId = commodity.supplierId,
                        commodityId = commodity.id,
                        quantity = quantity,
                        totalCost = totalCost,
                        paymentMethod = paymentMethod,
                        currency = commodity.currency // Use commodity currency
                    )
                    requestsViewModel.saveRequest(
                        request,
                        onSuccess = {
                            showDialog.value = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Request submitted successfully")
                            }
                        },
                        onFailure = { e ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to submit request: ${e.message}")
                            }
                        }
                    )
                }
            )
        }
    }
}

// Helper function to extract numeric value from cost string
private fun extractNumericCost(costString: String): Double {
    // Remove all non-numeric characters except decimal point
    val numericString = costString.replace("[^\\d.]".toRegex(), "")
    return numericString.toDoubleOrNull() ?: 0.0
}

// Updated RequestDialog with proper currency handling
@Composable
fun RequestDialog(
    commodity: Commodity,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, paymentMethod: String) -> Unit
) {

val quantity = remember { mutableStateOf("") }
val paymentMethod = remember { mutableStateOf("Cash") }

// Extract currency symbol and numeric value
val currencySymbol = remember { commodity.currency }
val costPerUnit = remember { commodity.cost.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0 }
val totalCost = remember { mutableStateOf(0.0) }

AlertDialog(
onDismissRequest = { onDismiss() },
title = {
    Text(
        text = "Request ${commodity.name}",
        color = blue1,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
},
text = {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(lightBlue),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Price per unit: $currencySymbol ${costPerUnit.toString()}",
            color = blue,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quantity:", color = blue1, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = quantity.value,
                onValueChange = {
                    val sanitizedInput = it.replace("[^\\d]".toRegex(), "") // Remove non-numeric characters
                    quantity.value = sanitizedInput
                    totalCost.value = (sanitizedInput.toIntOrNull() ?: 0) * costPerUnit
                },
                placeholder = { Text("Enter quantity", color = blue1) },
                modifier = Modifier.width(120.dp),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Total Cost: $currencySymbol ${totalCost.value}",
            color = blue1,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = paymentMethod.value == "Cash",
                onClick = { paymentMethod.value = "Cash" },
                colors = RadioButtonDefaults.colors(selectedColor = blue1)
            )
            Text("Cash", color = blue1)
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = paymentMethod.value == "MPESA",
                onClick = { paymentMethod.value = "MPESA" },
                colors = RadioButtonDefaults.colors(selectedColor = blue1)
            )
            Text("MPESA", color = blue1)
        }
    }
},
confirmButton = {
    Button(
        onClick = {
            val quantityValue = quantity.value.toIntOrNull() ?: 0
            if (quantityValue > 0) {
                onConfirm(quantityValue, paymentMethod.value)
            } else {
                // Show error to user
                // You can use a Snackbar or Toast here
                println("Invalid quantity entered")
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = green1)
    ) {
        Text("Request", color = Color.White)
    }
},
dismissButton = {
    Button(
        onClick = { onDismiss() },
        colors = ButtonDefaults.buttonColors(containerColor = blue1)
    ) {
        Text("Cancel", color = Color.White)
    }
},
containerColor = lightBlue
)
}
