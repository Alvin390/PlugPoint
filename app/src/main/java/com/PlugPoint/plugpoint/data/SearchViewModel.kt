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
        val normalizedQuery = query.trim().lowercase()

        viewModelScope.launch {
            try {
                val suppliersDeferred = async {
                    firestore.collection("suppliers")
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            document.toObject(UserSupplier::class.java)?.let { user ->
                                User.Supplier(user = user, id = document.id) // Pass document.id as id
                            }
                        }
                }

                val consumersDeferred = async {
                    firestore.collection("consumers")
                        .get()
                        .await()
                        .documents.mapNotNull { document ->
                            document.toObject(UserConsumer::class.java)?.let { user ->
                                User.Consumer(user = user, id = document.id) // Pass document.id as id
                            }
                        }
                }

                val suppliers = suppliersDeferred.await()
                val consumers = consumersDeferred.await()

                val allUsers = suppliers + consumers

                _searchResults.value = allUsers.filter { user ->
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

            } catch (e: Exception) {
                onError("Error fetching users: ${e.message}")
                _searchResults.value = emptyList()
            }
        }
    }


    // Helper function to extract Imgur ID from a URL
    private fun extractImgurId(imageUri: String?): String? {
        return imageUri?.substringAfterLast("/")?.substringBefore(".")
    }
}