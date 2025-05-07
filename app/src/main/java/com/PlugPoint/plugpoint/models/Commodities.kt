package com.PlugPoint.plugpoint.models


class Commodity {
    var id: String = "" // Add this property
    var name: String = ""
    var quantity: String = ""
    var cost = ""
    var currency: String = ""
    var imageUri: String? = null
    var booked: Boolean = false

    constructor(
        id: String,
        name: String,
        quantity: String,
        cost: String,
        currency: String,
        imageUri: String?
    ) {
        this.id = id
        this.name = name
        this.quantity = quantity
        this.cost = cost
        this.currency = currency
        this.imageUri = imageUri
    }

    constructor() {
        this.id = ""
        this.name = ""
        this.quantity = ""
        this.cost = ""
        this.currency = ""
        this.imageUri = null
        this.booked = false
    }

    fun updateBooked(isBooked: Boolean): Commodity {
        this.booked = isBooked
        return this
    }
}
