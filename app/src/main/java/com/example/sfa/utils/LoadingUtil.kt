package com.example.sfa.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.example.sfa.databinding.LayoutProgressDialogBinding

object LoadingUtil {

    private var progressDialog: AlertDialog? = null

    fun showLoading(context: Context, message: String = "Loading...") {
        if (progressDialog?.isShowing == true) return

        val binding = LayoutProgressDialogBinding.inflate(LayoutInflater.from(context))
        binding.tvProgress.text = message

        val builder = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)

        progressDialog = builder.create()
        progressDialog?.show()
    }

    fun hideLoading() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}