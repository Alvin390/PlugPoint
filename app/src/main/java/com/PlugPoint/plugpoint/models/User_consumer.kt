package com.PlugPoint.plugpoint.models

import retrofit2.http.Url

data class UserConsumer(
    val firstName: String = "",
    val lastName: String = "",
    val companyName: String = "",
    var idNumber: String = "",
    var county: String = "",
    var category: String = "",
    var email: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    val imageUrl: String="",
    var imageUri: String? = null
)
