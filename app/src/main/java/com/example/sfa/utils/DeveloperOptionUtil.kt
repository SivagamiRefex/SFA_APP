package com.example.sfa.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object DeveloperOptionUtil {
    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) != 0
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    fun showDeveloperOptionsAlert(activity: Activity) {
        AlertDialog.Builder(activity).apply {
            setTitle("Developer Options Enabled")
            setMessage("Please disable Developer Options to continue using this app.")

            setCancelable(false)

            setPositiveButton("OK") { _, _ ->
                activity.finish()
            }

            create().show()
        }
    }


   /* fun showDeveloperOptionsAlert(activity: Activity) {
        AlertDialog.Builder(activity).apply {
            setTitle("Developer Options Enabled")
            setMessage("Please disable Developer Options to continue using this app.")

            setCancelable(false)

            setPositiveButton("Go to Settings") { _, _ ->
                safeOpenDeveloperOptions(activity)
            }

            setNegativeButton("Exit") { _, _ ->
                activity.finish()
            }


            create().show()
        }
    }

    private fun openDeveloperOptions(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("error",e.message.toString())

        }
    }

    private fun safeOpenDeveloperOptions(context: Context) {
        if (isDeveloperOptionsEnabled(context)) {
            openDeveloperOptions(context)
        } else {
            Toast.makeText(
                context,
                "Developer options are not enabled. Enable them manually from Settings > About phone.",
                Toast.LENGTH_LONG
            ).show()
            openGeneralSettings(context)
        }
    }

    fun openGeneralSettings(context: Context) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }*/
}