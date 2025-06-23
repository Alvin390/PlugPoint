import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.utilis.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job

class CommodityShowViewModel : ViewModel() {
    companion object { private const val PAGE_SIZE = 20 }

    private val firestore = FirebaseFirestore.getInstance()
    sealed class UiState<out T> {
        object Idle : UiState<Nothing>()
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _commodities = MutableStateFlow<UiState<List<Commodity>>>(UiState.Idle)
    val commodities: StateFlow<UiState<List<Commodity>>> = _commodities
    private var lastVisibleSnapshot: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLastPage = false
    private var loadJob: Job? = null

    fun loadFirstPage(supplierId: String) {
        loadJob?.cancel()
        _commodities.value = UiState.Loading
        lastVisibleSnapshot = null
        isLastPage = false
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseQuery = firestore.collection(FirestoreCollections.SUPPLIERS)
                    .document(supplierId)
                    .collection(FirestoreCollections.COMMODITIES_SUB)
                    .orderBy("name")
                    .limit(PAGE_SIZE.toLong())
                val snapshot = baseQuery.get().await()
                val newItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Commodity::class.java)?.apply { id = doc.id }
                }
                lastVisibleSnapshot = snapshot.documents.lastOrNull()
                if (snapshot.size() < PAGE_SIZE) isLastPage = true
                _commodities.value = UiState.Success(newItems)
            } catch (e: Exception) {
                _commodities.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadNextPage(supplierId: String) {
        if (isLastPage) return
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseQuery = firestore.collection(FirestoreCollections.SUPPLIERS)
                    .document(supplierId)
                    .collection(FirestoreCollections.COMMODITIES_SUB)
                    .orderBy("name")
                    .limit(PAGE_SIZE.toLong())
                val query = lastVisibleSnapshot?.let { baseQuery.startAfter(it) } ?: baseQuery
                val snapshot = query.get().await()

                val newItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Commodity::class.java)?.apply { id = doc.id }
                }
                lastVisibleSnapshot = snapshot.documents.lastOrNull()
                if (snapshot.size() < PAGE_SIZE) isLastPage = true

                val oldList = (commodities.value as? UiState.Success)?.data ?: emptyList()
                _commodities.value = UiState.Success(oldList + newItems)
            } catch (e: Exception) {
                _commodities.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun hasMore(): Boolean = !isLastPage

    // Backward compatibility: delegate to loadFirstPage
    fun fetchCommoditiesForSupplier(supplierId: String) {
        loadFirstPage(supplierId)
    }


    // Optional: Add diagnostic method
    fun printSupplierCommodityStructure(supplierId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get the supplier document
                val supplierDoc = firestore.collection(FirestoreCollections.SUPPLIERS)
                    .document(supplierId)
                    .get()
                    .await()

                Log.d("SupplierDiagnostic", "Supplier Document Exists: ${supplierDoc.exists()}")
                if (supplierDoc.exists()) {
                    Log.d("SupplierDiagnostic", "Supplier Document Data: ${supplierDoc.data}")
                }

                // List all commodities for this supplier
                val commoditiesSnapshot = firestore.collection(FirestoreCollections.SUPPLIERS)
                    .document(supplierId)
                    .collection(FirestoreCollections.COMMODITIES_SUB)
                    .get()
                    .await()

                Log.d("SupplierDiagnostic", "Total Commodities: ${commoditiesSnapshot.size()}")

                commoditiesSnapshot.documents.forEach { document ->
                    Log.d("SupplierDiagnostic", "Commodity ID: ${document.id}")
                    Log.d("SupplierDiagnostic", "Commodity Data: ${document.data}")
                }
            } catch (e: Exception) {
                Log.e("SupplierDiagnostic", "Error examining supplier structure", e)
            }
        }
    }
}