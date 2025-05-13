package com.PlugPoint.plugpoint.models

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
    val imageUrl: String = "",
    var imageUri: String? = null,
    var phoneNumber: String = "",
    val id: String = ""
) : Comparable<UserConsumer> {
    override fun compareTo(other: UserConsumer): Int {
        return this.firstName.compareTo(other.firstName) // Sort by firstName
    }
}