package com.example.tea.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.NavigationDrawer
import com.example.tea.NavigationActivity
import com.example.tea.R
import com.example.tea.api.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    lateinit var login : EditText
    lateinit var password : EditText
    lateinit var loginBtn : Button
    lateinit var saveCheckBox : CheckBox
    lateinit var registrationBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        lifecycleScope.launch(Dispatchers.IO) {
            tryLog()
        }

        Thread.sleep(1000)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login = findViewById<EditText>(R.id.editText_login)
        password = findViewById<EditText>(R.id.editText_password)

        loginBtn = findViewById<Button>(R.id.login_button);
        saveCheckBox = findViewById<CheckBox>(R.id.remind_checkbox)
        registrationBtn = findViewById<Button>(R.id.registration_button);

        loginBtn.setOnClickListener {
            val api = Api(this)
            val auth = api.login(login.text.toString(), password.text.toString(), saveCheckBox.isChecked)
            if(auth){
                val intent = Intent(this, NavigationActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
            }
        }

        registrationBtn.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

    }

    private fun tryLogin() : Boolean{
        val api = Api(this)
        var res : String? = api.getToken()

        return res != null
    }

    private fun tryLog(){
        if(tryLogin()){
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}