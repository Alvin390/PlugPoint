package com.PlugPoint.plugpoint.data

import com.PlugPoint.plugpoint.models.Commodity
import com.google.firebase.firestore.FirebaseFirestore

class CommodityViewModel {
    private val firestore = FirebaseFirestore.getInstance()

    fun addCommodityToFirestore(
        commodity: Commodity,
        userId: String, // Ensure this is the supplier's userId
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userCommoditiesRef = firestore.collection("suppliers").document(userId).collection("commodities")

        val commodityId = userCommoditiesRef.document().id // Generate a unique ID for the commodity
        commodity.id = commodityId // Set the ID in the commodity object
        userCommoditiesRef.document(commodityId).set(commodity)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun fetchCommoditiesFromFirestore(
        userId: String, // Ensure this is the logged-in supplier's userId
        onSuccess: (List<Commodity>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userCommoditiesRef = firestore.collection("suppliers").document(userId).collection("commodities")

        userCommoditiesRef.get()
            .addOnSuccessListener { snapshot ->
                val commodities = snapshot.documents.mapNotNull { document ->
                    document.toObject(Commodity::class.java)?.apply { id = document.id }
                }
                onSuccess(commodities)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}