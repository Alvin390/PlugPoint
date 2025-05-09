import android.net.Uri
import androidx.lifecycle.ViewModel
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
import androidx.lifecycle.viewModelScope

class CommodityViewModel(private val imgurViewModel: ImgurViewModel): ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _commodities = MutableStateFlow<List<Commodity>>(emptyList())
    val commodities: StateFlow<List<Commodity>> get() = _commodities

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
                // Upload image to Imgur if an image URI is provided
// Replace the existing image upload logic with:
                commodity.imageUri = uploadImageIfNeeded(imageUri, context)

                // Save commodity to Firestore
                val userCommoditiesRef = firestore.collection("suppliers").document(userId).collection("commodities")
                val commodityId = userCommoditiesRef.document().id
                commodity.id = commodityId
                userCommoditiesRef.document(commodityId).set(commodity).await()

//                refreshCommodities(userId) // Unified refresh logic
                onSuccess()
            } catch (exception: Exception) {
                onFailure(exception)
            }
//            refreshCommodities(userId) // Unified refresh logic
        }
    }

    fun fetchCommoditiesFromFirestore(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
        _commodities.update { currentList ->
            currentList.filterNot { it.id == commodityId }
        }
        refreshCommodities(userId) // Unified refresh logic
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
                // Handle image upload if the image URI has changed
                if (updatedCommodity.imageUri != null) {
                    imgurViewModel.uploadImage(Uri.parse(updatedCommodity.imageUri), context, authorization = "511479d0432ec58")
                    val uploadState = imgurViewModel.uploadState.first()
                    when (uploadState) {
                        is ImgurUploadState.Success -> {
                            updatedCommodity.imageUri = uploadState.imageUrl
                        }
                        is ImgurUploadState.Error -> throw Exception(uploadState.message)
                        else -> throw Exception("Unexpected Imgur upload state")
                    }
                }

                // Update commodity in Firestore
                val commodityRef = firestore.collection("suppliers").document(userId).collection("commodities").document(commodityId)
                commodityRef.set(updatedCommodity).await()

//                refreshCommodities(userId) // Unified refresh logic
                onSuccess()
            } catch (exception: Exception) {
                onFailure(exception)
            }
//            refreshCommodities(userId) // Unified refresh logic
        }
    }

    private fun refreshCommodities(userId: String) {
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

    fun updateCommodities(updatedList: List<Commodity>) {
        _commodities.value = updatedList
    }
    private suspend fun uploadImageIfNeeded(
        imageUri: Uri?,
        context: android.content.Context
    ): String? {
        if (imageUri == null) return null
        imgurViewModel.uploadImage(imageUri, context, authorization = "511479d0432ec58")
        val uploadState = imgurViewModel.uploadState.first()
        return when (uploadState) {
            is ImgurUploadState.Success -> uploadState.imageUrl // Return the uploaded image URL
            is ImgurUploadState.Error -> throw Exception(uploadState.message)
            else -> null
        }
    }
}
