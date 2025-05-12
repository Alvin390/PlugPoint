package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.models.Requests
import com.PlugPoint.plugpoint.models.UserConsumer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Enhanced request item with display names
data class RequestWithNames(
    val request: Requests,
    var consumerName: String = "",
    var commodityName: String = "",
    var isCompleted: Boolean = false
)

class SupplierRequestsViewModel : ViewModel() {
    private val _acceptedRequests = MutableStateFlow<List<RequestWithNames>>(emptyList())
    val acceptedRequests: StateFlow<List<RequestWithNames>> = _acceptedRequests
    private val firestore = FirebaseFirestore.getInstance()
    private val _requests = MutableStateFlow<List<RequestWithNames>>(emptyList())
    val requests: StateFlow<List<RequestWithNames>> = _requests

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading


    fun fetchRequestsForSupplier(supplierId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                println("Fetching requests for supplier ID: '$supplierId'")

                // Now, let's do our query with the snapshot listener
                firestore.collection("requests")
                    // Check if supplierId is empty in the database as shown in logs
                    .whereEqualTo("supplierId", if (supplierId.isBlank()) "" else supplierId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            println("Error in snapshot listener: ${error.message}")
                            _isLoading.value = false
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            println("Snapshot exists, is empty: ${snapshot.isEmpty}")
                            if (!snapshot.isEmpty) {
                                val requestList = snapshot.toObjects(Requests::class.java)
                                println("Fetched ${requestList.size} requests")

                                // Convert to RequestWithNames objects
                                val requestsWithNames = requestList.map { RequestWithNames(request = it) }
                                _requests.value = requestsWithNames

                                // Load names for each request
                                requestsWithNames.forEach { requestWithNames ->
                                    loadConsumerName(requestWithNames)
                                    loadCommodityName(requestWithNames)
                                }
                            } else {
                                println("No documents match the query")
                                _requests.value = emptyList()
                            }
                        } else {
                            println("Snapshot is null")
                            _requests.value = emptyList()
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                println("Exception in fetchRequestsForSupplier: ${e.message}")
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    private fun loadConsumerName(requestWithNames: RequestWithNames) {
        val consumerId = requestWithNames.request.consumerId
        if (consumerId.isBlank()) {
            requestWithNames.consumerName = "Unknown"
            return
        }

        firestore.collection("users_consumer")
            .whereEqualTo("id", consumerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val consumer = snapshot.documents.firstOrNull()?.toObject(UserConsumer::class.java)
                if (consumer != null) {
                    requestWithNames.consumerName = "${consumer.firstName} ${consumer.lastName}"
                    // Notify UI of changes
                    _requests.value = _requests.value.toList()
                } else {
                    requestWithNames.consumerName = "Unknown Consumer"
                    _requests.value = _requests.value.toList()
                }
            }
            .addOnFailureListener {
                requestWithNames.consumerName = "Error Loading"
                _requests.value = _requests.value.toList()
            }
    }

    private fun loadCommodityName(requestWithNames: RequestWithNames) {
        val commodityId = requestWithNames.request.commodityId
        if (commodityId.isBlank()) {
            requestWithNames.commodityName = "Unknown"
            return
        }

        firestore.collection("commodities")
            .document(commodityId)
            .get()
            .addOnSuccessListener { document ->
                val commodity = document.toObject(Commodity::class.java)
                if (commodity != null) {
                    requestWithNames.commodityName = commodity.name
                    // Notify UI of changes
                    _requests.value = _requests.value.toList()
                } else {
                    requestWithNames.commodityName = "Unknown Commodity"
                    _requests.value = _requests.value.toList()
                }
            }
            .addOnFailureListener {
                requestWithNames.commodityName = "Error Loading"
                _requests.value = _requests.value.toList()
            }
    }

    // For testing: add a test request to verify writing works
    fun addTestRequest(supplierId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val testRequest = Requests(
            consumerId = "test-consumer-id",
            supplierId = supplierId,  // Use the provided supplierId
            commodityId = "MToEzhUPczUjEaz0GVHi",  // Use a real commodity ID from your database
            quantity = 5,
            totalCost = 100.0,
            paymentMethod = "MPESA",
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("requests")
            .add(testRequest)
            .addOnSuccessListener {
                println("Test request added successfully")
                onSuccess()
            }
            .addOnFailureListener {
                println("Failed to add test request: ${it.message}")
                onFailure(it)
            }
    }

    // Special method to fix empty supplierId in the database
    fun fixEmptySupplierIds(newSupplierId: String, onComplete: (Int) -> Unit) {
        firestore.collection("requests")
            .whereEqualTo("supplierId", "")
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                println("Found $count documents with empty supplierId")

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "supplierId", newSupplierId)
                }

                if (count > 0) {
                    batch.commit()
                        .addOnSuccessListener {
                            println("Updated $count documents with new supplierId")
                            onComplete(count)
                        }
                        .addOnFailureListener { e ->
                            println("Failed to update documents: ${e.message}")
                            onComplete(0)
                        }
                } else {
                    onComplete(0)
                }
            }
            .addOnFailureListener { e ->
                println("Failed to query documents: ${e.message}")
                onComplete(0)
            }
    }

    fun acceptRequest(requestWithNames: RequestWithNames, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("accepted_requests")
            .add(requestWithNames.request)
            .addOnSuccessListener {
                deleteRequest(requestWithNames.request, onSuccess, onFailure)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun declineRequest(requestWithNames: RequestWithNames, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        deleteRequest(requestWithNames.request, onSuccess, onFailure)
    }

    private fun deleteRequest(request: Requests, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("requests")
            .whereEqualTo("consumerId", request.consumerId)
            .whereEqualTo("commodityId", request.commodityId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { it.reference.delete() }
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun fetchConsumerPhoneNumber(consumerId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users_consumer")
            .whereEqualTo("id", consumerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val phoneNumber = snapshot.documents.firstOrNull()?.getString("phoneNumber")
                if (phoneNumber != null) {
                    onSuccess(phoneNumber)
                } else {
                    onFailure(Exception("Phone number not found for consumerId: $consumerId"))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}