package com.example.tea.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.tea.auth.LoginActivity
import com.example.tea.database.DatabaseHelper

class CancelEditProfileDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Отменить редактирование?")
                .setMessage("Вы уверены?")
                .setCancelable(true)
                .setPositiveButton("Да") { _, _ ->
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