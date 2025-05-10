package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.Commodity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommodityShowViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _commodities = MutableStateFlow<List<Commodity>>(emptyList())
    val commodities: StateFlow<List<Commodity>> = _commodities

    fun fetchCommoditiesForSupplier(supplierId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("commodities")
                    .whereEqualTo("supplierId", supplierId)
                    .get()
                    .await()
                _commodities.value = snapshot.toObjects(Commodity::class.java)
                if (_commodities.value.isEmpty()) {
                    println("No commodities found for supplierId: $supplierId")
                } else {
                    println("Fetched commodities: ${_commodities.value}")
                }
            } catch (e: Exception) {
                println("Error fetching commodities: ${e.message}")
                _commodities.value = emptyList()
            }
        }
    }
}
