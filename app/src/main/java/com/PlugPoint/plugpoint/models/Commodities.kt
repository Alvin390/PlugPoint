package com.PlugPoint.plugpoint.models

data class Commodity(
    var id: String = "",
    var name: String = "",
    var quantity: String = "",
    var cost: String = "",
    var currency: String = "",
    var imageUri: String? = null,
    var booked: Boolean = false
) {
    fun updateBooked(isBooked: Boolean): Commodity {
        return this.copy(booked = isBooked)
    }
}
