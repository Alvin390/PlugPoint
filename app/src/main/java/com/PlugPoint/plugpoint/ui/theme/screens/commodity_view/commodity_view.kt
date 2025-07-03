package com.PlugPoint.plugpoint.ui.theme.screens.commodity_view

import android.util.Log
import CommodityShowViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.consumerprofile.ConsumerTopBar
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierBottomNavBar
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Updated CommodityView function with proper currency handling
// Updated CommodityView function with proper currency handling
@Composable
fun CommodityView(
    navController: NavController,
    supplierId: String,
    searcherRole: String,
    viewModel: CommodityShowViewModel = viewModel()
) {
    val commoditiesState = viewModel.commodities.collectAsState().value
    Log.d("PlugPointDebug", "searcherRole at start: $searcherRole")
    val listState = rememberLazyListState()
    // Detect when we should load next page (when the last visible item is within 5 items of the end)
    val shouldLoadNext by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = (commoditiesState as? CommodityShowViewModel.UiState.Success<List<Commodity>>)?.data?.size ?: 0
            lastVisible >= total - 4
        }
    }
    val showDialog = remember { mutableStateOf(false) }
    val selectedCommodity = remember { mutableStateOf<Commodity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() } // Add Snackbar for error feedback
    fun log(msg: String) = android.util.Log.d("PlugPointDebug", msg)

    LaunchedEffect(supplierId) {
        viewModel.loadFirstPage(supplierId)
    }

    // Trigger pagination when needed
    LaunchedEffect(shouldLoadNext) {
        if (shouldLoadNext && viewModel.hasMore()) {
            viewModel.loadNextPage(supplierId)
        }
    }

    // backward compatibility: ensure first page loaded
    // viewModel.fetchCommoditiesForSupplier(supplierId) // deprecated wrapper

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
            when (commoditiesState) {
                is CommodityShowViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CommodityShowViewModel.UiState.Error -> {
                    Text((commoditiesState as CommodityShowViewModel.UiState.Error).message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is CommodityShowViewModel.UiState.Success -> {
                    val commodities = (commoditiesState as CommodityShowViewModel.UiState.Success<List<Commodity>>).data
                    if (commodities.isEmpty()) {
                        Text("No commodities available", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            state = listState
                        ) {
                            items(commodities) { commodity ->
                                CommodityListItem(
                                    commodity = commodity,
                                    onClick = {
                                        Log.d("PlugPointDebug", "Commodity clicked: ${commodity.name}, id=${commodity.id}")
                                        if (searcherRole == "consumer") {
                                            selectedCommodity.value = commodity
                                            showDialog.value = true
                                            Log.d("PlugPointDebug", "showDialog set to true, selectedCommodity set")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
            // Show the RequestDialog if needed
            // RequestsViewModel for request creation
            val requestsViewModel: RequestsViewModel = viewModel()
            val auth = FirebaseAuth.getInstance()
            val consumerId = auth.currentUser?.uid ?: ""
            if (showDialog.value && selectedCommodity.value != null) {
                log("Rendering RequestDialog for commodity: ${selectedCommodity.value?.name}")
                RequestDialog(
                    commodity = selectedCommodity.value!!,
                    onDismiss = { showDialog.value = false },
                    onConfirm = { quantity, paymentMethod ->
                        val commodity = selectedCommodity.value!!
                        val totalCost = quantity * (commodity.cost.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0)
                        val request = Requests(
                            consumerId = consumerId,
                            supplierId = supplierId,
                            commodityId = commodity.id ?: "",
                            quantity = quantity,
                            totalCost = totalCost,
                            paymentMethod = paymentMethod,
                            currency = commodity.currency,
                            timestamp = System.currentTimeMillis()
                        )
                        requestsViewModel.saveRequest(
                            request = request,
                            onSuccess = {
                                showDialog.value = false
                                // Show success snackbar
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Request sent successfully!")
                                }
                            },
                            onFailure = { e ->
                                showDialog.value = false
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Failed to send request: ${e.message}")
                                }
                            }
                        )
                    }
                )
            }
            // Fallback visual indicator if dialog should be up but isn't
            if (showDialog.value && selectedCommodity.value == null) {
                log("showDialog true but selectedCommodity is null!")
                Text("[DEBUG] Dialog state true but no selected commodity", color = Color.Red)
            }
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
