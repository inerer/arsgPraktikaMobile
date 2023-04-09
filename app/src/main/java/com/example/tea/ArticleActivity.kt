package com.example.tea

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tea.api.Api
import com.example.tea.models.article.Article
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ArticleActivity : AppCompatActivity() {

    // on below line we are creating
    // a variable for qr encoder.
    lateinit var pdfBitmap : Bitmap
    lateinit var bmp :  Bitmap

    lateinit var article: Article

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        val arguments = intent.extras
        val id = arguments!!["id"].toString()

        val backBtn : Button = findViewById(R.id.back_from_article_button);
        val pdfBtn : Button = findViewById(R.id.pdf_article_button)

        getArticle(id)

        backBtn.setOnClickListener {
            finish()
        }

        pdfBtn.setOnClickListener {
            pdfBtn.setOnClickListener {
                if (checkPermission()) {
                    generatePDF();
                } else {
                    requestPermission();
                }
            }
        }

    }

    fun getArticle(id : String){
        val  api : Api = Api(this)
        article = api.getArticle(id)!!

        val theme : TextView = findViewById(R.id.article_theme)
        val text : TextView = findViewById(R.id.article_text)
        val date : TextView = findViewById(R.id.article_date)
        val image : ImageView = findViewById(R.id.article_image)

        if (article != null) {
            theme.text = article!!.title
            text.text = article!!.description
            try {
                val text = article.dateOfPublication
                val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val localDateTime = LocalDateTime.parse(text, pattern)

                date.text = localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()
            }
            catch (e : java.lang.Exception){
                val text = article.dateOfPublication
                val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val localDateTime = LocalDateTime.parse(text, pattern)

                date.text = localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()
            }

            image.setImageBitmap(convert(article!!.photo))
        }
        else{
            theme.text = "Не удалось загрузить"
        }
    }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun generatePDF() {

            bmp = article?.let { convert(it.photo) }!!
            pdfBitmap =  Bitmap.createScaledBitmap(bmp, 300, 400, false)
            // creating an object variable
            // for our PDF document.
            val pdfDocument = PdfDocument()

            // two variables for paint "paint" is used
            // for drawing shapes and we will use "title"
            // for adding text in our PDF file.
            val paint = Paint()
            val title = Paint()

            // we are adding page info to our PDF file
            // in which we will be passing our pageWidth,
            // pageHeight and number of pages and after that
            // we are calling it to create our PDF.
            val mypageInfo = PdfDocument.PageInfo.Builder(792, 1120, 1).create()

            // below line is used for setting
            // start page for our PDF file.
            val myPage = pdfDocument.startPage(mypageInfo)

            // creating a variable for canvas
            // from our page of PDF.
            val canvas = myPage.canvas

            // below line is used to draw our image on our PDF file.
            // the first parameter of our drawbitmap method is
            // our bitmap
            // second parameter is position from left
            // third parameter is position from top and last
            // one is our variable for paint.
            canvas.drawBitmap(pdfBitmap, 56F, 40F, paint)


            canvas.drawText(article!!.title, 209F, 100F, title);
            canvas.drawText(article!!.description, 209F, 80F, title);

            // below line is used for adding typeface for
            // our text which we will be adding in our PDF file.
            title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // below line is used for setting text size
            // which we will be displaying in our PDF file.
            title.setTextSize(15F)

            // below line is sued for setting color
            // of our text inside our PDF file.
            title.color = ContextCompat.getColor(this, R.color.black)

            title.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            title.color = ContextCompat.getColor(this, R.color.black)
            title.setTextSize(15F)

            // after adding all attributes to our
            // PDF file we will be finishing our page.
            pdfDocument.finishPage(myPage)

            // below line is used to set the name of
            // our PDF file and its path.
            val file = File(Environment.getExternalStorageDirectory(), "${article.title}.pdf")
            try {
                // after creating a file name we will
                // write our PDF file to that location.
                pdfDocument.writeTo(FileOutputStream(file))

                // below line is to print toast message
                // on completion of PDF generation.
                Toast.makeText(
                    this,
                    "PDF file generated successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                // below line is used
                // to handle error
                e.printStackTrace()
            }
            // after storing our pdf to that
            // location we are closing our PDF file.
            pdfDocument.close()
        }

        fun checkPermission(): Boolean {
            // checking of permissions.
            val permission1 =
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            val permission2 =
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
        }

        fun requestPermission() {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                200
            )
        }

        @Throws(IllegalArgumentException::class)
        fun convert(base64Str: String): Bitmap? {
            val decodedBytes: ByteArray = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT
            )
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 200) {
                if (grantResults.size > 0) {

                    // after requesting permissions we are showing
                    // users a toast message of permission granted.
                    val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (writeStorage && readStorage) {
                        Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

}