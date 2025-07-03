package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.google.firebase.firestore.FirebaseFirestore
import com.PlugPoint.plugpoint.utilis.FirestoreCollections
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job

class SearchSupplierAuthViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    sealed class UiState<out T> {
        object Idle : UiState<Nothing>()
        object Loading : UiState<Nothing>()
        data class Success<T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    private val _searchResults = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<User>>> = _searchResults
    private var searchJob: Job? = null

    sealed class User {
        data class Supplier(
            val user: UserSupplier,
            val id: String // Add id field
        ) : User()

        data class Consumer(
            val user: UserConsumer,
            val id: String // Add id field
        ) : User()
    }

    fun searchUsers(query: String, onError: (String) -> Unit = {}) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = UiState.Idle
            return
        }
        _searchResults.value = UiState.Loading
        val normalizedQuery = query.trim().lowercase()

        searchJob = viewModelScope.launch {
            try {
                val suppliersDeferred = async {
                    firestore.collection(FirestoreCollections.SUPPLIERS)
                        .orderBy("firstName")
                        .limit(20)
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            document.toObject(UserSupplier::class.java)?.let { user ->
                                User.Supplier(user = user, id = document.id)
                            }
                        }
                }

                val consumersDeferred = async {
                    firestore.collection(FirestoreCollections.CONSUMERS)
                        .orderBy("firstName")
                        .limit(20)
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            document.toObject(UserConsumer::class.java)?.let { user ->
                                User.Consumer(user = user, id = document.id)
                            }
                        }
                }

                val suppliers = suppliersDeferred.await()
                val consumers = consumersDeferred.await()

                android.util.Log.d("PlugPointSearch", "Suppliers found: ${suppliers.size}")
                android.util.Log.d("PlugPointSearch", "Consumers found: ${consumers.size}")

                val allUsers = suppliers + consumers

                val filtered = allUsers.filter { user ->
                    when (user) {
                        is User.Supplier -> {
                            with(user.user) {
                                firstName.contains(normalizedQuery, ignoreCase = true) ||
                                        lastName.contains(normalizedQuery, ignoreCase = true) ||
                                        companyName.contains(normalizedQuery, ignoreCase = true) ||
                                        category.contains(normalizedQuery, ignoreCase = true) ||
                                        county.contains(normalizedQuery, ignoreCase = true)
                            }
                        }
                        is User.Consumer -> {
                            with(user.user) {
                                firstName.contains(normalizedQuery, ignoreCase = true) ||
                                        lastName.contains(normalizedQuery, ignoreCase = true) ||
                                        companyName.contains(normalizedQuery, ignoreCase = true) ||
                                        category.contains(normalizedQuery, ignoreCase = true) ||
                                        county.contains(normalizedQuery, ignoreCase = true)
                            }
                        }
                    }
                }
                android.util.Log.d("PlugPointSearch", "Filtered users: ${filtered.size}")
                _searchResults.value = UiState.Success(filtered)
            } catch (e: Exception) {
                android.util.Log.e("PlugPointSearch", "Error fetching users: ${e.message}", e)
                onError("Error fetching users: ${e.message}")
                _searchResults.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }


    // Helper function to extract Imgur ID from a URL
    private fun extractImgurId(imageUri: String?): String? {
        return imageUri?.substringAfterLast("/")?.substringBefore(".")
    }
}