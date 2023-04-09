package com.example.tea.api

import androidx.fragment.app.FragmentActivity
import com.example.tea.database.DatabaseHelper
import com.example.tea.models.article.Article
import com.example.tea.models.article.ArticleDomain
import com.example.tea.models.user.EditUser
import com.example.tea.models.user.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class Api(val context: FragmentActivity?) {

    private val ENDPOINT = "http://188.164.136.18:8888"  // Im using json-server running on my localhost and emulator
    private val ARTICLES = "/api/Article/getArticles"
    private val ARTICLE = "/api/Article/getArticleById"
    private val LOGIN = "/api/User/login"
    private val REGISTRATION = "/api/User/create"
    private val UPDATEUSER = "/api/User/update"
    private val GETCLIENT = "/api/User/getUser"
    private val CREATEARTICLE = "/api/Article/create"
    private val DATES = "/api/Article/getArticleByDates"
    private val client = OkHttpClient()

    fun getArticles() : List<Article>? {

        val httpUrlConnection = URL(ENDPOINT + ARTICLES).openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return null
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

        val type = object : TypeToken<List<Article>>(){}.type

        val articles = Gson().fromJson<List<Article>>(text, type)

        httpUrlConnection.disconnect()

        return articles
    }

    fun getArticles(search: String) : List<Article>? {

        val httpUrlConnection = URL(ENDPOINT + "/api/Article/getArticleByAuthor" + search).openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return null
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

        val type = object : TypeToken<List<Article>>(){}.type

        val articles = Gson().fromJson<List<Article>>(text, type)

        httpUrlConnection.disconnect()

        return articles
    }

    fun getArticles(startDate : String, endDate : String) : List<Article>? {

        val httpUrlConnection = URL(ENDPOINT + DATES + "?dateStart=$startDate&dateEnd=$endDate").openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return null
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

        val type = object : TypeToken<List<Article>>(){}.type

        val articles = Gson().fromJson<List<Article>>(text, type)

        httpUrlConnection.disconnect()

        return articles
    }

    fun login(login: String, password : String, save : Boolean) : Boolean {
        val httpUrlConnection = URL(ENDPOINT + LOGIN + "?Login=$login&Password=$password").openConnection() as HttpURLConnection
        val body = JSONObject().apply {
            put("", "")
        }
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "POST"
            doOutput = true
        }

        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return false
        }

        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var token: String = ""
        streamReader.use {
            token = it.readText()
            if(save){
                val db = DatabaseHelper(context,null)
                db.addProfile(login, password)
            }
            else{
                val db = DatabaseHelper(context,null)
                db.addGuest(login, password)
            }
        }

        httpUrlConnection.disconnect()

        return true
    }

    fun createArticle(articleDomain: ArticleDomain) : Boolean {

        articleDomain.user = getUser()!!.id
        articleDomain.dateOfPublication = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)

        val httpUrlConnection = URL(ENDPOINT + CREATEARTICLE).openConnection() as HttpURLConnection
        val json = Gson().toJson(articleDomain)

        val token = getToken()

        httpUrlConnection.setRequestProperty("Content-Type", "application/json")
        httpUrlConnection.setRequestProperty("Authorization", "bearer " + token)

        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "POST"
            doOutput = true
        }


        httpUrlConnection.outputStream.use { os ->
            val input: ByteArray = json.toByteArray()
            os.write(input, 0, input.size)
        }

        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return false
        }

        httpUrlConnection.disconnect()

        return true
    }

    fun registration(user : User, save : Boolean) : Boolean {
        val httpUrlConnection = URL(ENDPOINT + REGISTRATION).openConnection() as HttpURLConnection
        val json = Gson().toJson(user)
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "POST"
            doOutput = true
        }.addRequestProperty("Content-Type", "application/json")


        httpUrlConnection.outputStream.use { os ->
            val input: ByteArray = json.toByteArray()
            os.write(input, 0, input.size)
        }

        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return false
        }

        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var token: String = ""
        streamReader.use {
            token = it.readText()
            if(save){
                val db = DatabaseHelper(context,null)
                db.addProfile(user.login, user.password)
            }
            else{
                val db = DatabaseHelper(context,null)

            }
        }

        httpUrlConnection.disconnect()

        return true
    }


    fun updateUser(editUser : EditUser, id : Int) : Boolean {
        val httpUrlConnection = URL(ENDPOINT + UPDATEUSER + id).openConnection() as HttpURLConnection
        val json = Gson().toJson(editUser)
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "PUT"
            doOutput = true
        }.addRequestProperty("Content-Type", "application/json")


        httpUrlConnection.outputStream.use { os ->
            val input: ByteArray = json.toByteArray()
            os.write(input, 0, input.size)
        }

        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return false
        }

        httpUrlConnection.disconnect()

        return true
    }


    fun getToken() : String? {

        val db = DatabaseHelper(context,null)
        var user = db.getGuest()

        if(user.id == 0){
            user = db.getProfile()
        }

        val httpUrlConnection = URL(ENDPOINT + LOGIN + "?Login=${user.login}&Password=${user.password}").openConnection() as HttpURLConnection
        val body = JSONObject().apply {
            put("", "")
        }
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "POST"
            doOutput = true
        }

        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return null
        }

        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var token: String = ""
        streamReader.use {
            token = it.readText()
        }

        httpUrlConnection.disconnect()

        return token
    }


    fun getArticle(id : String) : Article? {

        val httpUrlConnection = URL(ENDPOINT + ARTICLE + id).openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return null
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

        val type = object : TypeToken<List<Article>>(){}.type

        val articles = Gson().fromJson<List<Article>>(text, type)

        httpUrlConnection.disconnect()

        return articles[0]
    }


    fun getUser() : User? {

        val token = getToken()

        val httpUrlConnection = URL(ENDPOINT + GETCLIENT + token).openConnection() as HttpURLConnection

        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true

        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return null
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

        val type = object : TypeToken<User>(){}.type

        val user = Gson().fromJson<User>(text, type)

        httpUrlConnection.disconnect()

        return user
    }

}
