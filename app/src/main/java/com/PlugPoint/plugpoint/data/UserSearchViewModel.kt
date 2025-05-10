package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.UserConsumer
import com.PlugPoint.plugpoint.models.UserSupplier
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserSearchViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _selectedConsumer = MutableStateFlow<UserConsumer?>(null)
    val selectedConsumer: StateFlow<UserConsumer?> = _selectedConsumer

    private val _selectedSupplier = MutableStateFlow<UserSupplier?>(null)
    val selectedSupplier: StateFlow<UserSupplier?> = _selectedSupplier

    fun fetchConsumerDetails(userId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("consumers").document(userId).get().await()
                _selectedConsumer.value = document.toObject(UserConsumer::class.java)
            } catch (e: Exception) {
                _selectedConsumer.value = null
            }
        }
    }

    fun fetchSupplierDetails(userId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("suppliers").document(userId).get().await()
                _selectedSupplier.value = document.toObject(UserSupplier::class.java)
            } catch (e: Exception) {
                _selectedSupplier.value = null
            }
        }
    }
}
