package com.example.tea.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.tea.R
import com.example.tea.auth.LoginActivity
import com.example.tea.database.DatabaseHelper

class LogoutFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Выйти из приложения?")
                .setMessage("Вы уверены?")
                .setCancelable(true)
                .setPositiveButton("Да") { _, _ ->
                    val db = DatabaseHelper(context,null)
                    db.deleteProfile()
                    db.deleteGuest()
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton(
                    "Нет, остаться"
                ) { _, _ ->
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}