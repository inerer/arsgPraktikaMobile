package com.example.tea

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.tea.api.Api
import com.example.tea.database.DatabaseHelper
import com.example.tea.models.article.Article
import com.example.tea.models.article.ArticleDomain
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DraftActivity : AppCompatActivity() {

    lateinit var article : ArticleDomain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft)

        val arguments = intent.extras
        val id = arguments!!["id"].toString()

        val backBtn : Button = findViewById(R.id.back_from_draft_button);
        val publish : Button = findViewById(R.id.publish_draft_button)

        getArticle(id)

        backBtn.setOnClickListener {
            finish()
        }

        publish.setOnClickListener {
            val api = Api(this
            )

            val res = api.createArticle(article)
            if(res){

                val db = DatabaseHelper(this, null)
                db.deleteDraft(article.id)

                Toast.makeText(this, "Опубликовано!", Toast.LENGTH_SHORT).show()
                finish()
            }
            else{
                Toast.makeText(this, "Какие-то данные неверны", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun getArticle(id : String){
        val  db = DatabaseHelper(this, null)
        article = db.getArticle(id)

        val theme : TextView = findViewById(R.id.article_theme)
        val text : TextView = findViewById(R.id.article_text)
        val date : TextView = findViewById(R.id.article_date)
        val image : ImageView = findViewById(R.id.article_image)

        if (article != null) {
            theme.text = article.title
            text.text = article.description
            val text = "2022-01-06 20:30:45"
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(text, pattern)

            date.text = localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()

            image.setImageBitmap(convert(article.photo))
        }
        else{
            theme.text = "Не удалось загрузить"
        }
    }

    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap? {
        val decodedBytes: ByteArray = android.util.Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            android.util.Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}