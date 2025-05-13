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
    val imageUrl: String = "",
    var imageUri: String? = null,
    var phoneNumber: String = "",
    val id: String = ""
) : Comparable<UserSupplier> {
    override fun compareTo(other: UserSupplier): Int {
        return this.companyName.compareTo(other.companyName) // Sort by companyName
    }
}