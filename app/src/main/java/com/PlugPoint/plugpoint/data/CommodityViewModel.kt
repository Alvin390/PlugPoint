import android.net.Uri
import androidx.lifecycle.ViewModel
import com.PlugPoint.plugpoint.data.ImgurUploadState
import com.PlugPoint.plugpoint.models.Commodity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.PlugPoint.plugpoint.data.ImgurViewModel
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.PlugPoint.plugpoint.utilis.FirestoreCollections

class CommodityViewModel(private val imgurViewModel: ImgurViewModel): ViewModel() {
    companion object { private const val PAGE_SIZE = 20 }

    private val firestore = FirebaseFirestore.getInstance()
    sealed class UiState<out T> {
        object Idle : UiState<Nothing>()
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _commodities = MutableStateFlow<UiState<List<Commodity>>>(UiState.Idle)
    val commodities: StateFlow<UiState<List<Commodity>>> get() = _commodities
    private var lastVisibleSnapshot: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLastPage = false
    private var loadJob: Job? = null

    fun addCommodityToFirestore(
        commodity: Commodity,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        imageUri: Uri?,
        context: android.content.Context
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Only update imageUri if a new image is picked
                if (imageUri != null) {
                    commodity.imageUri = uploadImageIfNeeded(imageUri, context)
                }
                // else, keep the existing commodity.imageUri (do not overwrite with null)

                // Save commodity to Firestore
                val userCommoditiesRef = firestore.collection(FirestoreCollections.SUPPLIERS).document(userId).collection(FirestoreCollections.COMMODITIES_SUB)
                val commodityId = userCommoditiesRef.document().id
                commodity.id = commodityId
                userCommoditiesRef.document(commodityId).set(commodity).await()

                loadFirstPage(userId)
                onSuccess()
            } catch (exception: Exception) {
                onFailure(exception)
            }
        }
    }

    fun loadFirstPage(userId: String) {
        loadJob?.cancel()
        _commodities.value = UiState.Loading
        lastVisibleSnapshot = null
        isLastPage = false
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseQuery = firestore.collection(FirestoreCollections.SUPPLIERS)
                    .document(userId)
                    .collection(FirestoreCollections.COMMODITIES_SUB)
                    .orderBy("name")
                    .limit(PAGE_SIZE.toLong())
                val snapshot = baseQuery.get().await()
                val newItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Commodity::class.java)?.apply { id = doc.id }
                }
                lastVisibleSnapshot = snapshot.documents.lastOrNull()
                if (snapshot.size() < PAGE_SIZE) {
                    isLastPage = true
                }
                _commodities.value = UiState.Success(newItems)
            } catch (e: Exception) {
                _commodities.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Legacy wrapper to maintain compatibility with older UI code.
     * Delegates to [loadFirstPage].
     */
    fun fetchCommoditiesFromFirestore(userId: String) {
        loadFirstPage(userId)
    }

    fun loadNextPage(userId: String) {
        if (isLastPage) return
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseQuery = firestore.collection(FirestoreCollections.SUPPLIERS)
                    .document(userId)
                    .collection(FirestoreCollections.COMMODITIES_SUB)
                    .orderBy("name")
                    .limit(PAGE_SIZE.toLong())
                val query = lastVisibleSnapshot?.let { baseQuery.startAfter(it) } ?: baseQuery
                val snapshot = query.get().await()

                val newItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Commodity::class.java)?.apply { id = doc.id }
                }
                lastVisibleSnapshot = snapshot.documents.lastOrNull()
                if (snapshot.size() < PAGE_SIZE) {
                    isLastPage = true
                }
                val oldList = (commodities.value as? UiState.Success)?.data ?: emptyList()
                _commodities.value = UiState.Success(oldList + newItems)
            } catch (e: Exception) {
                _commodities.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteCommodityFromFirestore(
        userId: String,
        commodityId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val commodityRef = firestore.collection(FirestoreCollections.SUPPLIERS).document(userId).collection(FirestoreCollections.COMMODITIES_SUB).document(commodityId)
        commodityRef.delete()
            .addOnSuccessListener {
                loadFirstPage(userId) // Refresh the list after deletion
                onSuccess()
            }
            .addOnFailureListener { exception -> onFailure(exception) }
        val currentList = (_commodities.value as? UiState.Success)?.data ?: emptyList()
        _commodities.value = UiState.Success(currentList.filter { it.id != commodityId })
    }

    fun updateCommodityInFirestore(
        userId: String,
        commodityId: String,
        updatedCommodity: Commodity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        context: android.content.Context
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Only upload and update imageUri if a new image is picked
                if (!updatedCommodity.imageUri.isNullOrBlank() && updatedCommodity.imageUri!!.startsWith("content://")) {
                    updatedCommodity.imageUri = imgurViewModel.uploadImageAndGetUrl(
                        Uri.parse(updatedCommodity.imageUri), context, authorization = ImgurViewModel.AUTHORIZATION)
                }
                // else, keep the existing imageUri (do not overwrite with null)

                // Update commodity in Firestore
                val commodityRef = firestore.collection(FirestoreCollections.SUPPLIERS).document(userId).collection(FirestoreCollections.COMMODITIES_SUB).document(commodityId)
                commodityRef.set(updatedCommodity).await()

                loadFirstPage(userId)
                onSuccess()
            } catch (exception: Exception) {
                onFailure(exception)
            }
        }
    }

    // Deprecated: Use loadFirstPage instead
    private fun refreshCommodities(userId: String) {}


    fun updateCommodities(updatedList: List<Commodity>) {
        _commodities.value = UiState.Success(updatedList)
    }
    private suspend fun uploadImageIfNeeded(
        imageUri: Uri?,
        context: android.content.Context
    ): String? {
        if (imageUri == null) return null
        val compressedFile = com.PlugPoint.plugpoint.utilis.ImageCompressUtils.compressUri(context, imageUri)
        return imgurViewModel.uploadImageAndGetUrl(android.net.Uri.fromFile(compressedFile), context, authorization = ImgurViewModel.AUTHORIZATION)
    }
}
