import com.PlugPoint.plugpoint.data.ImgurUploadState
import com.PlugPoint.plugpoint.models.Commodity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.PlugPoint.plugpoint.data.ImgurViewModel
import kotlinx.coroutines.flow.first

class CommodityViewModel(private val imgurViewModel: ImgurViewModel) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _commodities = MutableStateFlow<List<Commodity>>(emptyList())
    val commodities: StateFlow<List<Commodity>> get() = _commodities

    fun addCommodityToFirestore(
        commodity: Commodity,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Upload image to Imgur
                val imgurResponse = imgurViewModel.uploadImage(commodity.imageUri!!).first()
                if (imgurResponse is ImgurUploadState.Success) {
                    commodity.imageUri = imgurResponse.imageUrl // Update commodity with Imgur URL

                    // Save commodity to Firestore
                    val userCommoditiesRef = firestore.collection("suppliers").document(userId).collection("commodities")
                    val commodityId = userCommoditiesRef.document().id
                    commodity.id = commodityId
                    userCommoditiesRef.document(commodityId).set(commodity).await()

                    fetchCommoditiesFromFirestore(userId) // Refresh the list after adding
                    onSuccess()
                } else {
                    throw Exception("Image upload failed")
                }
            } catch (exception: Exception) {
                onFailure(exception)
            }
        }
    }

    fun fetchCommoditiesFromFirestore(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("suppliers").document(userId).collection("commodities").get().await()
                val commodities = snapshot.documents.mapNotNull { document ->
                    document.toObject(Commodity::class.java)?.apply { id = document.id }
                }
                _commodities.update { commodities }
            } catch (exception: Exception) {
                // Handle error if needed
            }
        }
    }

    fun deleteCommodityFromFirestore(
        userId: String,
        commodityId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val commodityRef = firestore.collection("suppliers").document(userId).collection("commodities").document(commodityId)
        commodityRef.delete()
            .addOnSuccessListener {
                fetchCommoditiesFromFirestore(userId) // Refresh the list after deletion
                onSuccess()
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun updateCommodityInFirestore(
        userId: String,
        commodityId: String,
        updatedCommodity: Commodity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val commodityRef = firestore.collection("suppliers").document(userId).collection("commodities").document(commodityId)
        commodityRef.set(updatedCommodity)
            .addOnSuccessListener {
                fetchCommoditiesFromFirestore(userId) // Refresh the list after updating
                onSuccess()
            }
            .addOnFailureListener { onFailure }
    }
}
