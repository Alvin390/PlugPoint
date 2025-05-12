package com.PlugPoint.plugpoint.models

data class Requests(
    var consumerId: String = "",
    var supplierId: String = "",
    var commodityId: String = "",
    var quantity: Int = 0,
    var totalCost: Double = 0.0,
    var paymentMethod: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

