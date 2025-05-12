package com.PlugPoint.plugpoint.utilis

import com.google.firebase.firestore.FirebaseFirestore
import com.PlugPoint.plugpoint.models.Requests

fun saveRequestToFirestore(request: Requests, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    FirebaseFirestore.getInstance().collection("requests")
        .add(request)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}
