package com.example.tea.models.user

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.Date

class User(
    var id: Int = 0,
    var firstName: String = "",
    var lastName: String = "",
    var middleName : String = "",
    var login : String = "",
    var email: String = "",
    var dateOfBirth : String = "",
    var photo : String = "",
    var role : String = "",
    var gender : String = "",
    var password : String = ""
)