package com.example.tea.models.article

import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

class Article(
    @SerializedName("Id")
    var id: Int = 0,
    @SerializedName("Title")
    var title: String = "",
    @SerializedName("Description")
    var description : String = "",
    @SerializedName("FirstName")
    var firstName: String = "firstName",
    @SerializedName("LastName")
    var lastName: String = "lastName",
    @SerializedName("MiddleName")
    var middleName: String = "middleName",
    @SerializedName("Login")
    var login: String = "login",
    @SerializedName("Email")
    var email: String = "email",
    @SerializedName("DateOfPublication")
    var dateOfPublication: String = "",
    @SerializedName("Photo")
    var photo: String = "photo",
    @SerializedName("Gender")
    var gender: String = "gender"
)