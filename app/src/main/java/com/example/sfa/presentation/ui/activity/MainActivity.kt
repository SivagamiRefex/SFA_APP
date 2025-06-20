package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sfa.R
import com.example.sfa.databinding.ActivityMainBinding
import com.example.sfa.presentation.ui.fragment.HomeFragment
import com.example.sfa.presentation.ui.fragment.MasterSyncFragment
import com.example.sfa.presentation.ui.fragment.MenuFragment
import com.example.sfa.presentation.ui.fragment.ReportFragment
import com.example.sfa.utils.AlarmUtils
import com.example.sfa.utils.Constant
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    var isFirstTime=false
    companion object {
        private const val REQUEST_FOREGROUND = 1001
        private const val REQUEST_BACKGROUND = 1002
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
         Log.e("token",""+SecureStorage.getToken(applicationContext))
        if (intent.hasExtra("isFirstTime"))
            isFirstTime = intent.getBooleanExtra("isFirstTime", false)


        if(Constant.isFirstTimeSynAll || isFirstTime) {
             replaceFragment(MasterSyncFragment())
        }else{
            replaceFragment(HomeFragment())
        }
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.reports -> replaceFragment(ReportFragment())
                R.id.menu -> replaceFragment(MenuFragment())
                R.id.sync->replaceFragment(MasterSyncFragment())
            }
            true
        }

        if(SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE)==1) {

            checkExactAlarmPermission()
            if (hasLocationPermission()) {
                safeSetRepeatingAlarm(this)
            } else {
                requestLocationPermissions()
            }
        }


    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment).commit()
    }
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun safeSetRepeatingAlarm(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                // Redirect once, not every time
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val prompted = prefs.getBoolean("exact_alarm_prompted", false)

                if (!prompted) {
                    prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:${context.packageName}")
                    AlertDialog.Builder(this)
                        .setTitle("Allow Exact Alarms")
                        .setMessage("This app requires permission to schedule exact alarms for background tasks like location tracking.")
                        .setPositiveButton("Allow") { _, _ ->
                            if (context is Activity) {
                                context.startActivity(intent)
                            }else {
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                        }
                        // .setNegativeButton("Cancel", null)
                        .show()


                } else {
                    Toast.makeText(context, "Enable exact alarm permission from settings to continue.", Toast.LENGTH_LONG).show()
                }

                return
            }
        }
        AlarmUtils.setRepeatingAlarm(context)
    }

    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (29+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FOREGROUND)
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND)
            } else {
                safeSetRepeatingAlarm(this)
                Toast.makeText(this, "All location permissions granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FOREGROUND)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_FOREGROUND -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Foreground permission granted", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Now request background
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            REQUEST_BACKGROUND
                        )
                    }

                } else {
                    Toast.makeText(this, "Foreground permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_BACKGROUND -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //AlarmUtils.setRepeatingAlarm(this)
                    safeSetRepeatingAlarm(this)

                    Toast.makeText(this, "Background permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Background permission denied", Toast.LENGTH_LONG).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        AlertDialog.Builder(this)
                            .setTitle("Allow Background Location")
                            .setMessage("To track your location in the background, please allow background location access in settings.\nPlease go to Permissions > Location > Allow all the time.")
                            .setPositiveButton("Go to Settings") { dialog, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                                dialog.dismiss()
                            }
                            /* .setNegativeButton("Cancel") { dialog, _ ->
                                 dialog.dismiss() // Explicitly dismiss on cancel
                             }*/
                            .setCancelable(false) // Allow tapping outside to dismiss
                            .show()

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE)==1) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("exact_alarm_prompted", false)
                    .apply()

                safeSetRepeatingAlarm(applicationContext)
            }
        }
    }
}