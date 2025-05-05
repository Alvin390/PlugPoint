package com.PlugPoint.plugpoint.models


class Commodity {
    var name: String = ""
    var quantity: String = ""
    var cost = ""
    var currency: String = ""
    var imageUri: String? = null
    var booked: Boolean = false

    constructor(
        name: String,
        quantity: String,
        cost: String,
        currency: String,
        imageUri: String?
    ) {
        this.name = name
        this.quantity = quantity
        this.cost = cost
        this.currency = currency
        this.imageUri = imageUri
    }

    constructor()

    fun updateBooked(isBooked: Boolean): Commodity {
        this.booked = isBooked
        return this
    }
}