package com.PlugPoint.plugpoint.ui.theme.screens.supplier_all_requests

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.PlugPoint.plugpoint.data.RequestWithNames
import com.PlugPoint.plugpoint.data.SupplierRequestsViewModel
import com.PlugPoint.plugpoint.ui.theme.blue1
import com.PlugPoint.plugpoint.ui.theme.green1
import com.PlugPoint.plugpoint.ui.theme.red
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@Composable
fun SupplierAllRequestsScreen(supplierId: String, viewModel: SupplierRequestsViewModel = viewModel()) {
    val requests by viewModel.requests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var selectedRequest by remember { mutableStateOf<RequestWithNames?>(null) }
    var isCalling by remember { mutableStateOf(false) }
    var callError by remember { mutableStateOf<String?>(null) }
    var addingTestRequest by remember { mutableStateOf(false) }
    var showFixDialog by remember { mutableStateOf(false) }
    var fixingEmptyIds by remember { mutableStateOf(false) }
    var fixResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(supplierId) {
        println("LaunchedEffect with supplierId: $supplierId")
        viewModel.fetchRequestsForSupplier(supplierId)
    }

    Scaffold(
        topBar = { SupplierTopBar() },
        floatingActionButton = {
            Column {
                // Fix empty supplier IDs FAB
                SmallFloatingActionButton(
                    onClick = { showFixDialog = true },
                    modifier = Modifier.padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Fix Empty IDs")
                }

                // Add Test Request FAB
                FloatingActionButton(
                    onClick = {
                        if (!addingTestRequest) {
                            addingTestRequest = true
                            viewModel.addTestRequest(
                                supplierId = supplierId,
                                onSuccess = {
                                    addingTestRequest = false
                                },
                                onFailure = {
                                    addingTestRequest = false
                                }
                            )
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Test Request")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading requests...")
                }
            } else if (requests.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No requests available", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Try adding a test request using the + button")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(requests) { requestWithNames ->
                        RequestItem(requestWithNames = requestWithNames, onClick = { selectedRequest = requestWithNames })
                    }
                }
            }

            // Loading indicator for adding test request
            if (addingTestRequest) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Card(
                        modifier = Modifier.align(Alignment.Center)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Adding test request...")
                        }
                    }
                }
            }
        }
    }

    // Fix empty supplier IDs dialog
    if (showFixDialog) {
        AlertDialog(
            onDismissRequest = { if (!fixingEmptyIds) showFixDialog = false },
            title = { Text("Fix Database Issues") },
            text = {
                Text("The logs showed that some requests have an empty supplier ID. " +
                        "Would you like to update these requests with your current supplier ID?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        fixingEmptyIds = true
                        viewModel.fixEmptySupplierIds(
                            newSupplierId = supplierId,
                            onComplete = { count ->
                                fixingEmptyIds = false
                                showFixDialog = false
                                if (count > 0) {
                                    fixResult = "Successfully updated $count request(s)."
                                } else {
                                    fixResult = "No requests needed to be updated."
                                }
                            }
                        )
                    },
                    enabled = !fixingEmptyIds
                ) {
                    if (fixingEmptyIds) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Fix Now")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFixDialog = false },
                    enabled = !fixingEmptyIds
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Fix result dialog
    fixResult?.let { result ->
        AlertDialog(
            onDismissRequest = { fixResult = null },
            title = { Text("Database Update") },
            text = { Text(result) },
            confirmButton = {
                Button(onClick = {
                    fixResult = null
                    // Refresh the requests after fixing IDs
                    viewModel.fetchRequestsForSupplier(supplierId)
                }) {
                    Text("OK")
                }
            }
        )
    }

    selectedRequest?.let { requestWithNames ->
        RequestDialog(
            requestWithNames = requestWithNames,
            onDismiss = { selectedRequest = null; callError = null },
            onCall = {
                isCalling = true
                callError = null
                viewModel.fetchConsumerPhoneNumber(
                    consumerId = requestWithNames.request.consumerId,
                    onSuccess = { phoneNumber ->
                        isCalling = false
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:$phoneNumber".toUri()
                        }
                        context.startActivity(intent)
                    },
                    onFailure = { e ->
                        isCalling = false
                        callError = "Failed to fetch phone number: ${e.message}"
                    }
                )
            },
            onAccept = {
                viewModel.acceptRequest(requestWithNames, onSuccess = { selectedRequest = null }, onFailure = {})
            },
            onDecline = {
                viewModel.declineRequest(requestWithNames, onSuccess = { selectedRequest = null }, onFailure = {})
            }
        )
        if (isCalling) {
            AlertDialog(
                onDismissRequest = { isCalling = false },
                title = { Text("Calling...") },
                text = { Text("Fetching consumer's phone number...") },
                confirmButton = {}
            )
        }
        callError?.let { errorMsg ->
            AlertDialog(
                onDismissRequest = { callError = null },
                title = { Text("Error") },
                text = { Text(errorMsg) },
                confirmButton = {
                    Button(onClick = { callError = null }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun RequestItem(requestWithNames: RequestWithNames, onClick: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Show consumer name if available, otherwise show "Loading..."
            Text(
                text = if (requestWithNames.consumerName.isNotBlank())
                    "Consumer: ${requestWithNames.consumerName}"
                else
                    "Consumer: Loading...",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Show commodity name if available, otherwise show "Loading..."
            Text(
                text = if (requestWithNames.commodityName.isNotBlank())
                    "Commodity: ${requestWithNames.commodityName}"
                else
                    "Commodity: Loading...",
                fontSize = 14.sp
            )

            Text("Quantity: ${requestWithNames.request.quantity}", fontSize = 14.sp)
            Text("Cost: ${currencyFormat.format(requestWithNames.request.totalCost)}", fontSize = 14.sp)
            Text("Payment: ${requestWithNames.request.paymentMethod}", fontSize = 14.sp)
            Text(
                "Date: ${dateFormat.format(Date(requestWithNames.request.timestamp))}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RequestDialog(
    requestWithNames: RequestWithNames,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Request Details", color = Color(0xFFFFA500), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Show consumer name if available, otherwise show "Loading..."
                Text(
                    text = if (requestWithNames.consumerName.isNotBlank())
                        "Consumer: ${requestWithNames.consumerName}"
                    else
                        "Consumer: Loading...",
                    fontSize = 16.sp
                )

                // Show commodity name if available, otherwise show "Loading..."
                Text(
                    text = if (requestWithNames.commodityName.isNotBlank())
                        "Commodity: ${requestWithNames.commodityName}"
                    else
                        "Commodity: Loading...",
                    fontSize = 16.sp
                )

                Text("Quantity: ${requestWithNames.request.quantity}", fontSize = 16.sp)
                Text("Cost: ${currencyFormat.format(requestWithNames.request.totalCost)}", fontSize = 16.sp)
                Text("Payment: ${requestWithNames.request.paymentMethod}", fontSize = 16.sp)
                Text(
                    "Date: ${dateFormat.format(Date(requestWithNames.request.timestamp))}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Row {
                IconButton(onClick = onCall) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = blue1)
                }
                IconButton(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = green1)
                }
                IconButton(onClick = onDecline) {
                    Icon(Icons.Default.Delete, contentDescription = "Decline", tint = red)
                }
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}