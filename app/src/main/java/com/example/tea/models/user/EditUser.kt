package com.example.tea.models.user

class EditUser(
    var firstName: String = "",
    var lastName: String = "",
    var middleName : String = "",
    var login : String? = null,
    var email: String? = null,
    var dateOfBirth : String = "",
    var photo : String = "",
    var role : String = "",
    var gender : String = "",
    var password : String = "",
    var confirmPassword : String = ""
)