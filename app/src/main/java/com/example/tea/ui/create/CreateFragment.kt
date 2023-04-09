package com.example.tea.ui.create

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tea.R
import com.example.tea.api.Api
import com.example.tea.database.DatabaseHelper
import com.example.tea.databinding.FragmentCreateBinding
import com.example.tea.databinding.FragmentHomeBinding
import com.example.tea.models.article.ArticleDomain
import com.example.tea.models.user.EditUser
import java.io.ByteArrayOutputStream

class CreateFragment : Fragment() {

    var imageBitmap: Bitmap? = null

    val articleDomain : ArticleDomain = ArticleDomain()
    var isDraft = false

    lateinit var image : ImageView
    lateinit var chooseBtn : Button
    lateinit var saveBtn : Button
    lateinit var cancelBtn : Button
    lateinit var draftButton : Button

    lateinit var tittle : EditText
    lateinit var description : EditText

    private var _binding: FragmentCreateBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val createViewModel =
            ViewModelProvider(this).get(CreateViewModel::class.java)

        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        val root: View = binding.root


        image = binding.createImage

        chooseBtn = binding.createChooseImage
        saveBtn = binding.createButton
        cancelBtn = binding.cancelCreateButton
        draftButton = binding.draftButton

        tittle = binding.titleEditText
        description = binding.descriptionEditText

        draftButton.setOnClickListener {
            isDraft = !isDraft
            if(isDraft){
                draftButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
            }
            else{
                draftButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
            }

        }

        cancelBtn.setOnClickListener{
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle("Отменить редактирование?")
                    .setMessage("Вы уверены?")
                    .setCancelable(true)
                    .setPositiveButton("Да") { _, _ ->
                        image.setImageBitmap(null)
                        tittle.setText("")
                        description.setText("")
                    }
                    .setNegativeButton(
                        "Нет, остаться"
                    ) { _, _ ->
                    }
                builder.create()
            }
        }

        chooseBtn.setOnClickListener{

           imagePickDialog()

        }

        saveBtn.setOnClickListener{
            val bitmap: Bitmap? =  imageBitmap

            if (bitmap != null) {
                val bos: ByteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                val image:ByteArray = bos.toByteArray()
                val base64Encoded = java.util.Base64.getEncoder().encodeToString(image)
                bitmap.recycle()

                articleDomain.photo = base64Encoded
            }

            articleDomain.title = tittle.text.toString()
            articleDomain.description = tittle.text.toString()

            if(isDraft){
                val db = DatabaseHelper(activity, null)
                db.addArticle(articleDomain)

                Toast.makeText(activity, "Сохранено как черновик", Toast.LENGTH_SHORT).show()

                image.setImageBitmap(null)
                tittle.setText("")
                description.setText("")
            }
            else{
                val api = Api(activity)

                val res = api.createArticle(articleDomain)
                if(res){
                    Toast.makeText(activity, "Сохранено", Toast.LENGTH_SHORT).show()
                    image.setImageBitmap(null)
                    tittle.setText("")
                    description.setText("")
                }
                else{
                    Toast.makeText(activity, "Какие-то данные неверны", Toast.LENGTH_SHORT).show()
                }
            }

        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1888 && resultCode == AppCompatActivity.RESULT_OK) {
            imageBitmap = data?.extras!!["data"] as Bitmap?
            image.setImageBitmap(imageBitmap)
        }
        if (resultCode == RESULT_OK && requestCode == 100) {
            val uri = data?.data
            if (uri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                imageBitmap = bitmap
                image.setImageBitmap(imageBitmap)
            }
        }
    }
    private fun checkPermission(): Boolean {
        return activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } === PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf<String>(Manifest.permission.CAMERA),
                200
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun imagePickDialog() {
        val options = arrayOf("Камера", "Галерея")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
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