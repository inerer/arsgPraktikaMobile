package com.example.tea

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tea.api.Api
import com.example.tea.database.DatabaseHelper
import com.example.tea.databinding.FragmentHomeBinding
import com.example.tea.models.article.Article
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var adapter: ArticleItemRecyclerViewAdapter
    private lateinit var articlesRv: RecyclerView

    lateinit var nothingShow : TextView

    lateinit var list : RecyclerView

    var articles : List<Article>? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nothingShow = findViewById(R.id.my_nothing_show)

        loadArticlesAsync()
    }

    private fun initAdapter(articles: List<Article>) {
        adapter = ArticleItemRecyclerViewAdapter(articles, this)
        articlesRv = findViewById(R.id.my_list)
        articlesRv.adapter = adapter
    }

    private fun loadArticlesAsync(){
        val th = loadArticles()
        th.start()
        th.join()

        // создаем адаптер
        if (articles != null) {
            initAdapter(articles!!)
        }
        else{
            nothingShow.text = "Нет публикаций"
            nothingShow.visibility = TextView.VISIBLE
            list.visibility = TextView.GONE
        }
    }

    private fun loadArticles(): Thread {

        val db = DatabaseHelper(this, null)
        var author = db.getGuest()

        if(author.id == 0){
            author = db.getProfile()
        }

        val thread = Thread{
            try {
                val httpUrlConnection = URL("http://188.164.136.18:8888" + "/api/Article/getArticleByAuthor" + author.login).openConnection() as HttpURLConnection
                httpUrlConnection.apply {
                    connectTimeout = 10000 // 10 seconds
                    requestMethod = "GET"
                    doInput = true
                }
                if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
                    articles = null
                }
                val streamReader = InputStreamReader(httpUrlConnection.inputStream)
                var text: String = ""
                streamReader.use {
                    text = it.readText()
                }

                val type = object : TypeToken<List<Article>>(){}.type

                articles = Gson().fromJson<List<Article>>(text, type)

                httpUrlConnection.disconnect()
            }
            catch (e : IOException){

            }
        }
        return thread
    }
}