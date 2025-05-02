package com.PlugPoint.plugpoint.models

class User_consumer {
    var firstName: String = ""
    var lastName: String = ""
    var companyName: String = ""
    var idNumber: String = ""
    var county: String = ""
    var category: String = ""
    var email: String = ""
    var password: String = ""
    var confirmPassword: String = ""
    var imageUri: String? = null // Field to store the image URI

    constructor(
        firstName: String,
        lastName: String,
        companyName: String,
        idNumber: String,
        county: String,
        category: String,
        email: String,
        password: String,
        confirmPassword: String,
        imageUri: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.companyName = companyName
        this.idNumber = idNumber
        this.county = county
        this.category = category
        this.email = email
        this.password = password
        this.confirmPassword = confirmPassword
        this.imageUri = imageUri
    }

    constructor()
}