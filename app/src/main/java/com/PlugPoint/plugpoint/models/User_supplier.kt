package com.PlugPoint.plugpoint.models

data class UserSupplier(
    var firstName: String = "",
    var lastName: String = "",
    var companyName: String = "",
    var idNumber: String = "",
    var county: String = "",
    var category: String = "",
    var email: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var imageUri: String? = null // Field to store the image URI
)
