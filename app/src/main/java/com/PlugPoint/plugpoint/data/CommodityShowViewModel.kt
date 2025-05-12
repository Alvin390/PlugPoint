import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.Commodity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommodityShowViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _commodities = MutableStateFlow<List<Commodity>>(emptyList())
    val commodities: StateFlow<List<Commodity>> = _commodities

    fun fetchCommoditiesForSupplier(supplierId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch from the nested commodities collection under the specific supplier
                val snapshot = firestore.collection("suppliers")
                    .document(supplierId)
                    .collection("commodities")
                    .get()
                    .await()

                // Detailed logging
                Log.d("CommodityFetch", "Total commodities found: ${snapshot.size()}")

                // Convert documents to Commodity objects
                val fetchedCommodities = snapshot.documents.mapNotNull { document ->
                    document.toObject(Commodity::class.java)?.apply {
                        id = document.id
                    }
                }

                // Update state flow
                _commodities.value = fetchedCommodities

                // Logging
                Log.d("CommodityFetch", "Fetched Commodities: $fetchedCommodities")
            } catch (e: Exception) {
                Log.e("CommodityFetch", "Error fetching commodities", e)
                _commodities.value = emptyList()
            }
        }
    }

    // Optional: Add diagnostic method
    fun printSupplierCommodityStructure(supplierId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get the supplier document
                val supplierDoc = firestore.collection("suppliers")
                    .document(supplierId)
                    .get()
                    .await()

                Log.d("SupplierDiagnostic", "Supplier Document Exists: ${supplierDoc.exists()}")
                if (supplierDoc.exists()) {
                    Log.d("SupplierDiagnostic", "Supplier Document Data: ${supplierDoc.data}")
                }

                // List all commodities for this supplier
                val commoditiesSnapshot = firestore.collection("suppliers")
                    .document(supplierId)
                    .collection("commodities")
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