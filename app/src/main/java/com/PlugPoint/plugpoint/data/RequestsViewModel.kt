package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.PlugPoint.plugpoint.models.Commodity
import com.PlugPoint.plugpoint.models.Requests
import com.PlugPoint.plugpoint.models.UserConsumer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RequestsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    fun calculateTotalCost(quantity: Int?, costPerUnit: Double): Double {
        return (quantity ?: 0) * costPerUnit
    }

    // Function to extract numeric value from cost string
    fun extractNumericCost(costString: String): Double {
        // Remove all non-numeric characters except decimal point
        val numericString = costString.replace("[^\\d.]".toRegex(), "")
        return numericString.toDoubleOrNull() ?: 0.0
    }

    // Function to determine currency symbol
    fun extractCurrencySymbol(costString: String, defaultCurrency: String): String {
        return when {
            costString.contains("$") -> "$"
            costString.contains("Ksh") -> "Ksh"
            else -> defaultCurrency
        }
    }

    fun saveRequest(request: Requests, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                if (request.consumerId.isBlank()) {
                    throw Exception("Consumer ID is blank")
                }
                // Check if consumer exists by document ID
                val consumerDoc = firestore.collection("users_consumer")
                    .document(request.consumerId)
                    .get()
                    .await()
                if (!consumerDoc.exists()) {
                    throw Exception("No consumer found for consumerId ${request.consumerId}")
                }

                if (request.commodityId.isBlank()) {
                    throw Exception("Commodity ID is blank")
                }
                val commodityDoc = firestore.collection("commodities")
                    .document(request.commodityId)
                    .get()
                    .await()
                if (!commodityDoc.exists()) {
                    throw Exception("No commodity found for commodityId ${request.commodityId}")
                }

                firestore.collection("requests")
                    .add(request)
                    .await()
                println("saveRequest: Request saved successfully for consumerId ${request.consumerId}, commodityId ${request.commodityId}")
                onSuccess()
            } catch (e: Exception) {
                println("saveRequest: Failed to save request: ${e.message}")
                onFailure(e)
            }
        }
    }
}
