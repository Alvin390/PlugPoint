package com.PlugPoint.plugpoint.data

import com.PlugPoint.plugpoint.models.Commodity
import com.google.firebase.database.FirebaseDatabase

class CommodityViewModel {
    fun addCommodityToFirebase(
        commodity: Commodity,
        userId: String, // Ensure this is the supplier's userId
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val userCommoditiesRef =
            database.getReference("suppliers/$userId/commodities") // Use "suppliers" node

        val commodityId = userCommoditiesRef.push().key // Generate a unique ID for the commodity
        if (commodityId != null) {
            userCommoditiesRef.child(commodityId).setValue(commodity)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
        } else {
            onFailure(Exception("Failed to generate commodity ID"))
        }
    }

    // Add this function in `CommodityViewModel`
    fun fetchCommoditiesFromFirebase(
        userId: String, // Ensure this is the logged-in supplier's userId
        onSuccess: (List<Commodity>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val userCommoditiesRef =
            database.getReference("suppliers/$userId/commodities") // Use "suppliers" node

        userCommoditiesRef.get()
            .addOnSuccessListener { snapshot ->
                val commodities = snapshot.children.mapNotNull { child ->
                    val commodity = child.getValue(Commodity::class.java)
                    commodity?.id = child.key ?: "" // Set the ID from the Firebase key
                    commodity
                }
                onSuccess(commodities)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteCommodityFromFirebase(
        userId: String,
        commodityId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val userCommoditiesRef =
            database.getReference("suppliers/$userId/commodities/$commodityId") // Correct path

        userCommoditiesRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
