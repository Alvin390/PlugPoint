// In supplier_accepted_requests.kt

package com.PlugPoint.plugpoint.ui.theme.screens.supplier_accepted_requests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.PlugPoint.plugpoint.data.RequestWithNames
import com.PlugPoint.plugpoint.data.SupplierRequestsViewModel
import com.PlugPoint.plugpoint.ui.theme.green1
import com.PlugPoint.plugpoint.ui.theme.screens.my_profile.SupplierTopBar

@Composable
fun SupplierAcceptedRequestsScreen(
    supplierId: String,
    viewModel: SupplierRequestsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val requests by viewModel.acceptedRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedRequest by remember { mutableStateOf<RequestWithNames?>(null) }
    var showDeliveredDialog by remember { mutableStateOf(false) }
    var completedRequestId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(supplierId) {
        viewModel.fetchAcceptedRequestsForSupplier(supplierId)
    }

    Scaffold(
        topBar = { SupplierTopBar() }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (requests.isEmpty()) {
                Text("No accepted requests", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(requests) { requestWithNames ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            RequestItemAccepted(
                                requestWithNames = requestWithNames,
                                onClick = {
                                    if (!requestWithNames.isCompleted) {
                                        selectedRequest = requestWithNames
                                        showDeliveredDialog = true
                                    }
                                }
                            )
                            if (requestWithNames.isCompleted) {
                                CompletedStamp()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeliveredDialog && selectedRequest != null) {
        DeliveredDialog(
            onConfirm = {
                selectedRequest?.let {
                    viewModel.markRequestAsCompleted(it) {
                        completedRequestId = it.request.commodityId
                        showDeliveredDialog = false
                        selectedRequest = null
                    }
                }
            },
            onDismiss = {
                showDeliveredDialog = false
                selectedRequest = null
            }
        )
    }
}

@Composable
fun RequestItemAccepted(requestWithNames: RequestWithNames, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = !requestWithNames.isCompleted) { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Consumer: ${requestWithNames.consumerName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Commodity: ${requestWithNames.commodityName}",
                fontSize = 14.sp
            )
            Text("Quantity: ${requestWithNames.request.quantity}", fontSize = 14.sp)
            Text("Cost: ${requestWithNames.request.totalCost}", fontSize = 14.sp)
            Text("Payment: ${requestWithNames.request.paymentMethod}", fontSize = 14.sp)
        }
    }
}

@Composable
fun DeliveredDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = green1, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delivered", color = green1, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
        },
        text = { Text("Mark this request as completed?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = green1)) {
                Text("Mark as Completed")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CompletedStamp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "COMPLETED",
            color = Color(0xFF43A047),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.7f))
                .padding(16.dp)
        )
    }
}