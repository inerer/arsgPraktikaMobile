package com.example.tea.auth

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.tea.NavigationActivity
import com.example.tea.R
import com.example.tea.api.Api
import com.example.tea.models.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class RegistrationActivity : AppCompatActivity() {

    var imageBitmap: Bitmap? = null
    lateinit var imageView: ImageView

    private var imageUri: Uri? = null

    lateinit var cancelBtn : Button
    lateinit var registrationBtn : Button
    lateinit var chooseImageBtn : Button
    lateinit var maleButton : Button
    lateinit var female : Button

    lateinit var lastNameView : EditText
    lateinit var firstNameView : EditText
    lateinit var middleNameView : EditText
    lateinit var dateOfBirthView : EditText
    lateinit var loginView : EditText
    lateinit var passwordView : EditText
    lateinit var emailView : EditText

    lateinit var remindCheckBox : CheckBox

    val user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val myCalendar:Calendar = Calendar.getInstance()

        imageView = findViewById<ImageView>(R.id.image_registration)

        cancelBtn = findViewById<Button>(R.id.cancel_registration_button);
        registrationBtn = findViewById<Button>(R.id.registration_button);
        chooseImageBtn = findViewById<Button>(R.id.choose_image_button)

        maleButton = findViewById<Button>(R.id.registration_male_button)
        female = findViewById<Button>(R.id.registration_female_button)

        lastNameView = findViewById<EditText>(R.id.last_name_registration)
        firstNameView = findViewById<EditText>(R.id.first_name_registration)
        middleNameView = findViewById<EditText>(R.id.middle_name_registration)
        dateOfBirthView = findViewById<EditText>(R.id.birth_date_registration)
        loginView = findViewById<EditText>(R.id.login_registration)
        passwordView = findViewById<EditText>(R.id.password_registration)
        emailView = findViewById<EditText>(R.id.email_registration)

        remindCheckBox = findViewById<CheckBox>(R.id.remind_me_checkbox)

        val date =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = month
                myCalendar[Calendar.DAY_OF_MONTH] = day
                val myFormat = "yyyy-MM-dd"
                val dateFormat = SimpleDateFormat(myFormat, Locale.US)
                dateOfBirthView.setText(dateFormat.format(myCalendar.time))

            }

        dateOfBirthView.setOnClickListener {
            DatePickerDialog(this@RegistrationActivity,
                date,
                myCalendar[Calendar.YEAR] ,
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH] ).show()
        }

        maleButton.setOnClickListener {
            user.gender = "Male"
            maleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
            female.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)));
        }

        female.setOnClickListener {
            user.gender = "Female"
            maleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.black)));
            female.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
        }

        cancelBtn.setOnClickListener {
            finish()
        }

        chooseImageBtn.setOnClickListener{
        imagePickDialog()
        }

        registrationBtn.setOnClickListener {


            lifecycleScope.launch(Dispatchers.IO) {
                regAsync()
            }

            Thread.sleep(1000)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1888 && resultCode == RESULT_OK) {
            imageBitmap = data?.extras!!["data"] as Bitmap?
            imageView.setImageBitmap(imageBitmap)
        }
        if (resultCode == RESULT_OK && requestCode == 100) {
            val uri = data?.data
            if(uri!=null){
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                imageBitmap = bitmap
                imageView.setImageBitmap(imageBitmap)
            }
    } }

    private fun checkPermissionStorage(): Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) === PackageManager.PERMISSION_GRANTED
    }

    private  fun requestPermissionStorage(){
        ActivityCompat.requestPermissions(
            this, arrayOf<String>(Manifest.permission_group.STORAGE),
                200
        )
    }
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf<String>(Manifest.permission.CAMERA),
            200
        )
    }

    private fun regAsync(){

        user.login = loginView.text.toString()
        user.password = passwordView.text.toString()
        user.lastName = lastNameView.text.toString()
        user.firstName = firstNameView.text.toString()
        user.middleName = middleNameView.text.toString()
        user.dateOfBirth = dateOfBirthView.text.toString()
        user.email = emailView.text.toString()
        user.role = "User"

        val bitmap: Bitmap? =  imageBitmap

        if (bitmap != null) {
            val bos: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val image:ByteArray = bos.toByteArray()
            val base64Encoded = java.util.Base64.getEncoder().encodeToString(image)
            bitmap.recycle()

            user.photo = base64Encoded
        }

        val res = reg(user, remindCheckBox.isChecked)

        if(res){
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            Toast.makeText(this, "Неверные данные", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reg(user : User, save : Boolean) : Boolean{
        val api = Api(this)
        val res = api.registration(user, save)
        return res
    }

    private fun imagePickDialog() {
        val options = arrayOf("Камера", "Галерея")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Выберите изображение")
        builder.setItems(options) { dialogInterface, i ->
            if (i == 0) {
                pickFromCamera()
            } else {
                pickFromStorage()
            }
        }
        builder.create().show()
    }

    private fun pickFromCamera() {
        if (checkPermission()) {
            val camera = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(camera, 1888)

        } else {
            requestPermission();
        }
    }
    private fun pickFromStorage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }
}