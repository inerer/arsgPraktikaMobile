package com.example.tea.ui.profile

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.tea.EditProfileActivity
import com.example.tea.MainActivity

import com.example.tea.R
import com.example.tea.api.Api
import com.example.tea.databinding.FragmentProfileBinding
import com.example.tea.dialogs.LogoutFragment
import com.example.tea.models.user.User
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ProfileFragment : Fragment() {

    // on below line we are creating
    // a variable for bitmap
    lateinit var bitmap: Bitmap

    // on below line we are creating
    // a variable for qr encoder.
    lateinit var pdfBitmap : Bitmap
    lateinit var bmp :  Bitmap

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val user = getUser()

        val firstName: TextView = binding.profileFirstName
        val lastName : TextView = binding.profileLastName
        val middleName : TextView = binding.profileMiddleName
        val dateOfBirth : TextView = binding.profileBirthDate
        val login : TextView = binding.profileLogin
        val email : TextView = binding.profileEmail
        val image : ImageView = binding.profileImage

        val editButton: LinearLayout = binding.editProfileButton
        val exitButton : LinearLayout = binding.logoutProfileButton
        val pdfButton : LinearLayout = binding.printProfileButton

        val maleButton : Button = binding.profileManGender
        val femaleButton : Button = binding.profileFemaleGender
        val mainButton :Button =  binding.mainButton

        if (user != null ) {
            val text = user.dateOfBirth.replace("T", " ")
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(text, pattern)

            firstName.text = "Имя: " + user.firstName.toString()
            lastName.text = "Фамилия: " + user.lastName.toString()
            middleName.text = "Отчество: " + user.middleName.toString()
            dateOfBirth.text = "День рождения: " + localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()
            login.text = "Логин: " + user.login.toString()
            email.text = "Почта: " + user.email.toString()

            if(user.photo.length > 100){
                image.setImageBitmap(convert(user.photo))
            }

            if(user.gender == "Male"){
                maleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
                femaleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
            }
            if(user.gender == "Female"){
                maleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
                femaleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
            }
        }
        mainButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        editButton.setOnClickListener {

            if (user != null) {
                val intent = Intent(activity, EditProfileActivity::class.java)
                intent.putExtra("id", user.id)
                startActivity(intent)
            }

        }

        pdfButton.setOnClickListener {
            if (checkPermission()) {
                generatePDF();
            } else {
                requestPermission();
            }
        }

        exitButton.setOnClickListener{

            val myDialogFragment = LogoutFragment()
            val manager = activity?.supportFragmentManager
            val transaction: FragmentTransaction = manager!!.beginTransaction()
            myDialogFragment.show(transaction, "dialog")

        }

        return root
    }

    override fun onResume() {
        super.onResume()

        val user = getUser()


        val firstName: TextView = binding.profileFirstName
        val lastName : TextView = binding.profileLastName
        val middleName : TextView = binding.profileMiddleName
        val dateOfBirth : TextView = binding.profileBirthDate
        val login : TextView = binding.profileLogin
        val email : TextView = binding.profileEmail
        val image : ImageView = binding.profileImage

        val maleButton : Button = binding.profileManGender
        val femaleButton : Button = binding.profileFemaleGender

        if (user != null) {

            val text = user?.dateOfBirth?.replace("T", " ")
            val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(text, pattern)

            firstName.text = "Имя: " + user.firstName.toString()
            lastName.text = "Фамилия: " + user.lastName.toString()
            middleName.text = "Отчество: " + user.middleName.toString()
            dateOfBirth.text = "День рождения: " + localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()
            login.text = "Логин: " + user.login.toString()
            email.text = "Почта: " + user.email.toString()

            if(user.photo.length > 100){
                image.setImageBitmap(convert(user.photo))
            }

            if(user.gender == "Male"){
                maleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
                femaleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
            }
            if(user.gender == "Female"){
                maleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.white)));
                femaleButton.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.yellow)));
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getQrCodeBitmap(ssid: String): Bitmap {
        val size = 512 //pixels
        val qrCodeContent = ssid
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    private fun getUser() : User? {
        val api = Api(activity)
        val user = api.getUser()
        return user
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun generatePDF() {

        var user = getUser()

        val text = user?.dateOfBirth?.replace("T", " ")
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val localDateTime = LocalDateTime.parse(text, pattern)


        bmp = getQrCodeBitmap("${user?.lastName} ${user?.firstName} ${user?.middleName} ${localDateTime.dayOfMonth.toString() + " " + localDateTime.month + " " + localDateTime.year.toString()} ${user?.gender}")
        pdfBitmap =  getQrCodeBitmap("${user?.lastName} ${user?.firstName} ${user?.middleName} ${user?.dateOfBirth} ${user?.gender}")
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
        val mypageInfo = PageInfo.Builder(792, 1120, 1).create()

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

        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15F)

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.color = ContextCompat.getColor(requireContext(), R.color.black)

        title.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        title.color = ContextCompat.getColor(requireContext(), R.color.black)
        title.setTextSize(15F)

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage)

        // below line is used to set the name of
        // our PDF file and its path.
        val file = File(Environment.getExternalStorageDirectory(), "user.pdf")
        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(FileOutputStream(file))

            // below line is to print toast message
            // on completion of PDF generation.
            Toast.makeText(
                activity,
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

    private fun checkPermission(): Boolean {
        // checking of permissions.
        val permission1 =
            ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
        val permission2 =
            ContextCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf<String>(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
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
        if (requestCode == 200) {
            if (grantResults.size > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeStorage && readStorage) {
                    Toast.makeText(requireContext(), "Permission Granted..", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Permission Denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

