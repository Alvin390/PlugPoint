package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchSupplierAuthViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    sealed class User {
        data class Supplier(val user: UserSupplier) : User()
        data class Consumer(val user: UserConsumer) : User()
    }

    fun searchUsers(query: String, onError: (String) -> Unit = {}) {
        val normalizedQuery = query.trim().lowercase() // Normalize the query

        viewModelScope.launch {
            try {
                val suppliersDeferred = async {
                    firestore.collection("suppliers")
                        .whereGreaterThanOrEqualTo("searchableField", normalizedQuery)
                        .whereLessThanOrEqualTo("searchableField", "$normalizedQuery\uf8ff")
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            document.toObject(UserSupplier::class.java)?.let { user ->
                                user.imageUri = extractImgurId(user.imageUri) // Ensure Imgur ID
                                User.Supplier(user)
                            }
                        }
                }

                val consumersDeferred = async {
                    firestore.collection("consumers")
                        .whereGreaterThanOrEqualTo("searchableField", normalizedQuery)
                        .whereLessThanOrEqualTo("searchableField", "$normalizedQuery\uf8ff")
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            document.toObject(UserConsumer::class.java)?.let { user ->
                                user.imageUri = extractImgurId(user.imageUri) // Ensure Imgur ID
                                User.Consumer(user)
                            }
                        }
                }

                val suppliers = suppliersDeferred.await()
                val consumers = consumersDeferred.await()

                _searchResults.value = suppliers + consumers // Combine results
            } catch (exception: Exception) {
                onError("Error fetching search results: ${exception.message}")
                _searchResults.value = emptyList() // Clear results on error
            }
        }
    }

    // Helper function to extract Imgur ID from a URL
    private fun extractImgurId(imageUri: String?): String? {
        return imageUri?.substringAfterLast("/")?.substringBefore(".")
    }
}