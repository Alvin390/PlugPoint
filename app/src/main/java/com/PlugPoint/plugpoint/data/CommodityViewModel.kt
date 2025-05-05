package com.PlugPoint.plugpoint.data

import com.PlugPoint.plugpoint.models.Commodity
import com.google.firebase.database.FirebaseDatabase

class CommodityViewModel {
    fun addCommodityToFirebase(
        commodity: Commodity,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val userCommoditiesRef = database.getReference("users/$userId/commodities")

        val commodityId = userCommoditiesRef.push().key // Generate a unique ID for the commodity
        if (commodityId != null) {
            userCommoditiesRef.child(commodityId).setValue(commodity)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        } else {
            onFailure(Exception("Failed to generate commodity ID"))
        }
    }
}