package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.models.Requests
import com.PlugPoint.plugpoint.models.UserConsumer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.PlugPoint.plugpoint.utilis.FirestoreCollections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Enhanced request item with display names
data class RequestWithNames(
    val request: Requests,
    var consumerName: String = "",
    var commodityName: String = "",
    var currency: String = request.currency, // Added currency field
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

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData
    private val pageSize = 10
    private var lastDocument: DocumentSnapshot? = null

    // Firestore listener reference to prevent leaks
    private var requestsListener: ListenerRegistration? = null


    fun fetchRequestsForSupplier(supplierId: String) {
        _isLoading.value = true
        requestsListener?.remove()
        requestsListener = firestore.collection(FirestoreCollections.REQUESTS)
            .whereEqualTo("supplierId", supplierId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("fetchRequestsForSupplier: Error fetching requests: ${e.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Requests::class.java)?.let { request ->
                            RequestWithNames(request = request, consumerName = "Loading...", commodityName = "Loading...")
                        }
                    }
                    _requests.value = requests
                    _isLoading.value = false
                    lastDocument = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                    _hasMoreData.value = snapshot.documents.size == pageSize
                    requests.forEach { requestWithNames ->
                        loadConsumerName(requestWithNames)
                        loadCommodityName(requestWithNames)
                    }
                }
            }
    }

    fun loadMoreRequests(supplierId: String) {
        if (!_hasMoreData.value || _isLoading.value) return
        _isLoading.value = true
        lastDocument?.let { lastDoc ->
            firestore.collection(FirestoreCollections.REQUESTS)
                .whereEqualTo("supplierId", supplierId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(pageSize.toLong())
                .get()
                .addOnSuccessListener { snapshot ->
                    val newRequests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Requests::class.java)?.let { request ->
                            RequestWithNames(request = request, consumerName = "Loading...", commodityName = "Loading...")
                        }
                    }
                    _requests.value = _requests.value + newRequests
                    _isLoading.value = false
                    lastDocument = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                    _hasMoreData.value = snapshot.documents.size == pageSize
                    newRequests.forEach { requestWithNames ->
                        loadConsumerName(requestWithNames)
                        loadCommodityName(requestWithNames)
                    }
                }
                .addOnFailureListener { e ->
                    println("loadMoreRequests: Error loading more requests: ${e.message}")
                    _isLoading.value = false
                }
        }
    }

    fun loadConsumerName(requestWithNames: RequestWithNames) {
        val consumerId = requestWithNames.request.consumerId
        if (consumerId.isBlank()) {
            println("loadConsumerName: consumerId is blank for request ${requestWithNames.request}")
            updateRequestWithNames(requestWithNames) { it.consumerName = "Unknown" }
            return
        }

        firestore.collection(FirestoreCollections.CONSUMERS)
            .document(consumerId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    println("loadConsumerName: No consumer found for consumerId $consumerId")
                    updateRequestWithNames(requestWithNames) { it.consumerName = "Unknown Consumer" }
                } else {
                    val consumer = document.toObject(UserConsumer::class.java)
                    val name = consumer?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Consumer"
                    println("loadConsumerName: Found consumer $name for consumerId $consumerId")
                    updateRequestWithNames(requestWithNames) { it.consumerName = name }
                }
            }
            .addOnFailureListener { e ->
                println("loadConsumerName: Error fetching consumer for consumerId $consumerId: ${e.message}")
                updateRequestWithNames(requestWithNames) { it.consumerName = "Error Loading" }
            }
    }

    fun loadCommodityName(requestWithNames: RequestWithNames) {
        val commodityId = requestWithNames.request.commodityId
        if (commodityId.isBlank()) {
            println("loadCommodityName: commodityId is blank for request ${requestWithNames.request}")
            updateRequestWithNames(requestWithNames) { it.commodityName = "Unknown" }
            return
        }

        firestore.collection(FirestoreCollections.COMMODITIES_TOP)
            .document(commodityId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    println("loadCommodityName: No commodity found for commodityId $commodityId")
                    updateRequestWithNames(requestWithNames) { it.commodityName = "Unknown Commodity" }
                } else {
                    val commodity = document.toObject(Commodity::class.java)
                    val name = commodity?.name ?: "Unknown Commodity"
                    println("loadCommodityName: Found commodity $name for commodityId $commodityId")
                    updateRequestWithNames(requestWithNames) { it.commodityName = name }
                }
            }
            .addOnFailureListener { e ->
                println("loadCommodityName: Error fetching commodity for commodityId $commodityId: ${e.message}")
                updateRequestWithNames(requestWithNames) { it.commodityName = "Error Loading" }
            }
    }


    // For testing: add a test request to verify writing works
    fun addTestRequest(supplierId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                // Use existing consumer if available
                val consumerSnapshot = firestore.collection(FirestoreCollections.CONSUMERS)
                    .limit(1)
                    .get()
                    .await()
                val consumerId = if (consumerSnapshot.isEmpty) {
                    val testConsumer = UserConsumer(
                        firstName = "Test",
                        lastName = "Consumer",
                        email = "test@consumer.com",
                        id = firestore.collection(FirestoreCollections.CONSUMERS).document().id,
                        phoneNumber = "1234567890"
                    )
                    firestore.collection(FirestoreCollections.CONSUMERS)
                        .document(testConsumer.id)
                        .set(testConsumer)
                        .await()
                    println("addTestRequest: Created test consumer with ID ${testConsumer.id}")
                    testConsumer.id
                } else {
                    consumerSnapshot.documents.first().id
                }

                // Use existing commodity for the supplier if available
                val commoditySnapshot = firestore.collection(FirestoreCollections.COMMODITIES_TOP)
                    .whereEqualTo("supplierId", supplierId)
                    .limit(1)
                    .get()
                    .await()
                val commodityId = if (commoditySnapshot.isEmpty) {
                    val testCommodity = Commodity(
                        id = firestore.collection(FirestoreCollections.COMMODITIES_TOP).document().id,
                        name = "Test Commodity",
                        quantity = "100",
                        cost = "200",
                        currency = "USD",
                        supplierId = supplierId
                    )
                    firestore.collection(FirestoreCollections.COMMODITIES_TOP)
                        .document(testCommodity.id)
                        .set(testCommodity)
                        .await()
                    println("addTestRequest: Created test commodity with ID ${testCommodity.id}")
                    testCommodity.id
                } else {
                    commoditySnapshot.documents.first().id
                }

                val testRequest = Requests(
                    consumerId = consumerId,
                    supplierId = supplierId,
                    commodityId = commodityId,
                    quantity = 5,
                    totalCost = 1000.0,
                    paymentMethod = "MPESA",
                    currency = "USD",
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection(FirestoreCollections.REQUESTS)
                    .add(testRequest)
                    .await()
                println("addTestRequest: Test request added successfully with consumerId $consumerId, commodityId $commodityId")
                onSuccess()
            } catch (e: Exception) {
                println("addTestRequest: Failed to create test request: ${e.message}")
                onFailure(e)
            }
        }
    }
    // Special method to fix empty supplierId in the database
    fun fixEmptySupplierIds(newSupplierId: String, onComplete: (Int) -> Unit) {
        firestore.collection(FirestoreCollections.REQUESTS)
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

    fun acceptRequest(
        requestWithNames: RequestWithNames,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Build batch – create accepted request doc with denormalised names, delete originals
                val batch = firestore.batch()
                // Add accepted request with extra display fields
                val acceptedRef = firestore.collection(FirestoreCollections.ACCEPTED_REQUESTS).document()
                val r = requestWithNames.request
                val data = hashMapOf<String, Any?>(
                    "consumerId" to r.consumerId,
                    "supplierId" to r.supplierId,
                    "commodityId" to r.commodityId,
                    "quantity" to r.quantity,
                    "totalCost" to r.totalCost,
                    "paymentMethod" to r.paymentMethod,
                    "currency" to r.currency,
                    "timestamp" to r.timestamp,
                    "consumerName" to requestWithNames.consumerName,
                    "commodityName" to requestWithNames.commodityName
                )
                batch.set(acceptedRef, data)

                // Find original request docs
                val originals = firestore.collection(FirestoreCollections.REQUESTS)
                    .whereEqualTo("consumerId", requestWithNames.request.consumerId)
                    .whereEqualTo("commodityId", requestWithNames.request.commodityId)
                    .get().await()
                originals.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun declineRequest(
        requestWithNames: RequestWithNames,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val originals = firestore.collection(FirestoreCollections.REQUESTS)
                    .whereEqualTo("consumerId", requestWithNames.request.consumerId)
                    .whereEqualTo("commodityId", requestWithNames.request.commodityId)
                    .get().await()
                val batch = firestore.batch()
                originals.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    private fun deleteRequest( // deprecated, kept for binary compatibility but no longer used elsewhere
        request: Requests,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection(FirestoreCollections.REQUESTS)
            .whereEqualTo("consumerId", request.consumerId)
            .whereEqualTo("commodityId", request.commodityId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { it.reference.delete() }
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun fetchConsumerPhoneNumber(
        consumerId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection(FirestoreCollections.CONSUMERS)
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

    private fun updateRequestWithNames(
        requestWithNames: RequestWithNames,
        update: (RequestWithNames) -> Unit
    ) {
        update(requestWithNames)
        _requests.value = _requests.value.map {
            if (it.request == requestWithNames.request) requestWithNames.copy() else it
        }
    }
    fun cleanInvalidRequests(onComplete: (String) -> Unit) {
        firestore.collection(FirestoreCollections.REQUESTS)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                var deletedCount = 0

                snapshot.documents.forEach { doc ->
                    val request = doc.toObject(Requests::class.java) ?: return@forEach
                    // Delete requests with blank consumerId or known invalid IDs
                    if (request.consumerId.isBlank() ||
                        request.consumerId == "test-consumer-id" ||
                        request.commodityId == "test-commodity-id" ||
                        request.commodityId.isBlank()) {
                        batch.delete(doc.reference)
                        deletedCount++
                        println("cleanInvalidRequests: Deleting invalid request: $request")
                    }
                }

                if (deletedCount > 0) {
                    batch.commit()
                        .addOnSuccessListener {
                            onComplete("Deleted $deletedCount invalid requests")
                        }
                        .addOnFailureListener { e ->
                            onComplete("Failed to delete invalid requests: ${e.message}")
                        }
                } else {
                    onComplete("No invalid requests found")
                }
            }
            .addOnFailureListener { e ->
                onComplete("Failed to query requests: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        requestsListener?.remove()
    }
}
