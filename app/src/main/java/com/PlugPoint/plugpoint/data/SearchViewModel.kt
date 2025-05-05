package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.java


class SearchSupplierAuthViewModel : ViewModel() {
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
            val database = FirebaseDatabase.getInstance().reference
            val results = mutableListOf<User>()

            // Search suppliers
            val supplierQuery = database.child("suppliers")
                .orderByChild("searchableField")
                .startAt(query)
                .endAt(query + "\uf8ff")
            supplierQuery.get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { child ->
                    val supplier = child.getValue(UserSupplier::class.java)
                    if (supplier != null) {
                        results.add(User.Supplier(supplier))
                        println("Supplier found: ${supplier.firstName} ${supplier.lastName}")
                    }
                }
                _searchResults.value = results
                println("Total suppliers found: ${results.size}")
            }.addOnFailureListener { exception ->
                println("Error fetching suppliers: ${exception.message}")
            }

            // Search consumers
            val consumerQuery = database.child("consumers")
                .orderByChild("searchableField")
                .startAt(query)
                .endAt(query + "\uf8ff")
            consumerQuery.get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { child ->
                    val consumer = child.getValue(UserConsumer::class.java)
                    if (consumer != null) {
                        results.add(User.Consumer(consumer))
                        println("Consumer found: ${consumer.firstName} ${consumer.lastName}")
                    }
                }
                _searchResults.value = results
                println("Total consumers found: ${results.size}")
            }.addOnFailureListener { exception ->
                println("Error fetching consumers: ${exception.message}")
            }
        }
    }
}