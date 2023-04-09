package com.example.tea

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.tea.api.Api
import com.example.tea.models.user.EditUser
import com.example.tea.models.user.User
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    var imageBitmap: Bitmap? = null

    lateinit var image : ImageView

    lateinit var saveBtn : Button
    lateinit var cancelBtn : Button
    lateinit var chooseImageBtn : Button
    lateinit var maleBtn : Button
    lateinit var femaleBtn : Button

    lateinit var lastNameView : EditText
    lateinit var firstNameView : EditText
    lateinit var middleNameView : EditText
    lateinit var oldPasswordView : EditText
    lateinit var newPasswordView : EditText
    lateinit var emailView : EditText

    val editUser = EditUser()
    var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS

        val arguments = intent.extras
        val id = arguments!!["id"].toString()

        val api = Api(this)

        val user = api.getUser()



        if (user != null) {
            userId = user.id
            editUser.role = user.role
            editUser.gender = user.gender
            editUser.email = user.email
            editUser.dateOfBirth = user.dateOfBirth
            editUser.photo = user.photo
            editUser.middleName = user.middleName
            editUser.firstName = user.firstName
            editUser.lastName = user.lastName
        }

        image = findViewById<ImageView>(R.id.edit_profile_image)

        cancelBtn = findViewById<Button>(R.id.cancel_edit_profile_button);
        saveBtn = findViewById<Button>(R.id.save_edit_profile_button);
        chooseImageBtn = findViewById<Button>(R.id.choose_image_edit_profile)

        maleBtn = findViewById<Button>(R.id.male_button_edit_profile)
        femaleBtn = findViewById<Button>(R.id.female_button_edit_profile)

        lastNameView = findViewById<EditText>(R.id.last_name_edit_profile)
        firstNameView = findViewById<EditText>(R.id.first_name_edit_profile)
        middleNameView = findViewById<EditText>(R.id.middle_name_edit_profile)
        oldPasswordView = findViewById<EditText>(R.id.beb1)
        newPasswordView = findViewById<EditText>(R.id.beb2)
        emailView = findViewById<EditText>(R.id.email_edit_profile)

        if(editUser.photo.length > 100){
            image.setImageBitmap(convert(editUser.photo))
        }

        lastNameView.setText(editUser.lastName)
        middleNameView.setText(editUser.middleName)
        firstNameView.setText(editUser.firstName)
        emailView.setText(editUser.email)

        if(editUser.gender == "Male"){
            maleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
            femaleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
        }
        else{
            maleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
            femaleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
        }

        maleBtn.setOnClickListener {
            editUser.gender = "Male"
            maleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
            femaleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
        }

        femaleBtn.setOnClickListener {
            editUser.gender = "Female"
            maleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
            femaleBtn.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
        }

        chooseImageBtn.setOnClickListener{

            imagePickDialog()

        }

        cancelBtn.setOnClickListener {
            finish()
        }

        saveBtn.setOnClickListener {
            editAsync()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1888 && resultCode == RESULT_OK) {
            imageBitmap = data?.extras!!["data"] as Bitmap?
            image.setImageBitmap(imageBitmap)
        }
        if (resultCode == RESULT_OK && requestCode == 100) {
            val uri = data?.data
            if (uri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                imageBitmap = bitmap
                image.setImageBitmap(imageBitmap)
            }
        }
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

    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap? {
        val decodedBytes: ByteArray = android.util.Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            android.util.Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun editAsync(){

        if(emailView.text.toString() == editUser.email){
            editUser.email = null
        }
        else{
            editUser.email = emailView.text.toString()
        }

        editUser.middleName = middleNameView.text.toString()
        editUser.firstName = firstNameView.text.toString()
        editUser.lastName = lastNameView.text.toString()
        editUser.password = oldPasswordView.text.toString()
        editUser.confirmPassword = newPasswordView.text.toString()

        val bitmap: Bitmap? =  imageBitmap

        if (bitmap != null) {
            val bos: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            val image:ByteArray = bos.toByteArray()
            val base64Encoded = java.util.Base64.getEncoder().encodeToString(image)
            bitmap.recycle()

            editUser.photo = base64Encoded
        }

        val res = edit(editUser, userId)

        if(res){
            finish()
        }
        else{
            Toast.makeText(this, "Неверные данные", Toast.LENGTH_SHORT).show()
        }
    }

    private fun edit(editUser: EditUser, id : Int) : Boolean{
        val api = Api(this)
        val res = api.updateUser(editUser, id)
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