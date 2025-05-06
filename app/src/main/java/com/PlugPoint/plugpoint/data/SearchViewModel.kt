package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchSupplierAuthViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private var searchJob: Job? = null

    sealed class User {
        data class Supplier(val user: UserSupplier) : User()
        data class Consumer(val user: UserConsumer) : User()
    }

    fun searchUsers(query: String) {
        searchJob?.cancel() // Cancel any ongoing search
        searchJob = viewModelScope.launch {
            delay(300) // Debounce for 300ms
            val results = mutableListOf<User>()

            // Search suppliers
            firestore.collection("suppliers")
                .whereGreaterThanOrEqualTo("searchableField", query)
                .whereLessThanOrEqualTo("searchableField", query + "\uf8ff")
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach { document ->
                        val supplier = document.toObject(UserSupplier::class.java)
                        if (supplier != null) {
                            results.add(User.Supplier(supplier))
                            println("Supplier found: ${supplier.firstName} ${supplier.lastName}")
                        }
                    }
                    _searchResults.value = results
                    println("Total suppliers found: ${results.size}")
                }
                .addOnFailureListener { exception ->
                    println("Error fetching suppliers: ${exception.message}")
                }

            // Search consumers
            firestore.collection("consumers")
                .whereGreaterThanOrEqualTo("searchableField", query)
                .whereLessThanOrEqualTo("searchableField", query + "\uf8ff")
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach { document ->
                        val consumer = document.toObject(UserConsumer::class.java)
                        if (consumer != null) {
                            results.add(User.Consumer(consumer))
                            println("Consumer found: ${consumer.firstName} ${consumer.lastName}")
                        }
                    }
                    _searchResults.value = results
                    println("Total consumers found: ${results.size}")
                }
                .addOnFailureListener { exception ->
                    println("Error fetching consumers: ${exception.message}")
                }
        }
    }
}