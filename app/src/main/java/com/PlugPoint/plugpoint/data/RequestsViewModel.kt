package com.PlugPoint.plugpoint.data

import androidx.lifecycle.ViewModel
import com.PlugPoint.plugpoint.models.Requests
import com.google.firebase.firestore.FirebaseFirestore

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
        firestore.collection("requests")
            .add(request)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
