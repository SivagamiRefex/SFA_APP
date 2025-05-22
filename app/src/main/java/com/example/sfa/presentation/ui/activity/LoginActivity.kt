package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.databinding.ActivityLoginBinding
import com.example.sfa.presentation.viewmodel.LoginViewModel
import com.example.sfa.presentation.viewmodel.SetupDataViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.EmailValidator
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.PermissionUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val setupDataViewModel: SetupDataViewModel by viewModels()
    var token=""
    var currentLat=""
    var currentLong=""
    lateinit var dbController: DBController
    var isFirstTime: Boolean = false
    var isFirstTimeSynAll: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbController = DBController(applicationContext)
        getToken()

        if (!PermissionUtil.areAllPermissionsGranted(this, PermissionUtil.requiredPermissions)) {
            PermissionUtil.requestPermissions(this, PermissionUtil.requiredPermissions, 100)
        } else {
            proceedToLogin()
        }
        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString()
            val pass = binding.etPassword.text.toString()
            if (user.trim().isEmpty()) {
                Toast.makeText(applicationContext, "Please enter the Email Id ", Toast.LENGTH_SHORT)
                    .show()
            } else if (!EmailValidator.isValid(user)) {
                Toast.makeText(
                    applicationContext,
                    "Please enter the valid Email Id ",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (pass.trim().isEmpty()) {
                Toast.makeText(applicationContext, "Please enter the Password ", Toast.LENGTH_SHORT)
                    .show()
            } else if (currentLat.equals("")) {
                getCurrentLocation()
                Toast.makeText(
                    applicationContext,
                    "Location not fetched. Check your location permission.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (Constant.isNetworkAvailable(this)) {
                    viewModel.login(
                        user,
                        pass,
                        TimesUtil.getCurrentTime(TimesUtil.FORMAT),
                        token,
                        currentLat,
                        currentLong
                    )
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please check your network connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        viewModel.loginState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {

                    if (result.data!!.status) {
                        SecureStorage.run {
                            setBoolean(applicationContext, StringConstants.IS_USER_LOGED_IN, true)
                            setString(applicationContext,StringConstants.AUTH_TOKEN,result.data.data!!.get(0).token)
                            setString(
                                applicationContext,
                                StringConstants.SP_NAME,
                                result.data.data!!.get(0).Sp_Name
                            )
                            setString(
                                applicationContext,
                                StringConstants.SP_ID,
                                result.data.data!!.get(0).Sp_Id
                            )
                            setString(
                                applicationContext,
                                StringConstants.SP_EMAIL,
                                result.data.data!!.get(0).Sp_Email
                            )
                            setInt(
                                applicationContext,
                                StringConstants.SP_TYPE,
                                result.data.data!!.get(0).SP_Type.toInt()
                            )
                        }

                        val gson = Gson()
                        val jsonString = gson.toJson(result.data.data!!)

                        Log.e("Login_data", "" + jsonString.toString())
                        if (!dbController.updateProduct(
                                StringConstants.LOGIN_DATA,
                                jsonString.toString()
                            )
                        ) {
                            dbController.addProduct(
                                StringConstants.LOGIN_DATA,
                                jsonString.toString()
                            )
                        }

                        if (dbController.getData().size <= 1) {
                            isFirstTimeSynAll = true
                            isFirstTime = true
                        }
                        Constant.isFirstTimeSynAll = isFirstTimeSynAll
                        TimesUtil.addLoginDate(applicationContext)

                        setupDataViewModel.getSetupData(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!)
                        Toast.makeText(this, "Login Successfully!", Toast.LENGTH_SHORT).show()

                        val intent1 = Intent(this@LoginActivity, MainActivity::class.java)
                        intent1.putExtra("isFirstTime", isFirstTime)
                        startActivity(intent1)
                        finish()
                    } else {
                        Toast.makeText(this, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    // Show loading indicator
                }
            }
        }


        setupDataViewModel.setupDataState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    val gson = Gson()
                    val jsonString = gson.toJson(result.data!!.data!!)
                    //Log.e("setup_data", "" + jsonString.toString())
                    if (!dbController.updateProduct(StringConstants.SETUP_DATA, jsonString.toString())) {
                        dbController.addProduct(StringConstants.SETUP_DATA, jsonString.toString())
                    }
                   // Log.e("setup data",dbController.getResponse(StringConstants.SETUP_DATA))

                }

                is Resource.Error -> {
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    // Show loading indicator
                }

            }
        }
    }

    fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Toast.makeText(applicationContext, "Token fetch failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        Log.e("FCM", "getToken failed", it)
                    }
                    return@addOnCompleteListener
                }
                token = task.result
                if (token != null) {
                    Log.e("FCM Token", token)

                } else {
                    Log.e("FCM Error", "Token is null, try again later")
                }
            }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                proceedToLogin()
            } else {
                Toast.makeText(this, "Please grant all permissions to continue", Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode ==PermissionUtil.LOCATION_PERMISSION_CODE){

            PermissionUtil.handlePermissionsResult(requestCode, grantResults) { granted ->
                if (granted) {
                    Toast.makeText(this, "Location Permission Granted!", Toast.LENGTH_SHORT).show()
                   // getLogin(etUserName.text.toString(),etPassword.text.toString(),TimeUtil.getCurrentTime(TimeUtil.FORMAT))
                    getCurrentLocation()
                } else {
                    val builder = AlertDialog.Builder(this)
                    val dialog = builder.create()

                    builder.setMessage("We need permission to use your location for the purpose of visit your customer.")
                        .setTitle("Device Location Required")
                        .setIcon(R.drawable.ic_location)
                        .setPositiveButton("OK") { _, _ ->
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                PermissionUtil.requestLocationPermission(this)
                            } else {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivityForResult(intent, PermissionUtil.REQUEST_CHECK_SETTINGS) }
                            dialog.cancel()

                        }
                        .setNegativeButton("Ask Me Later") { _, _ ->
                           // getLogin(etUserName.text.toString(),etPassword.text.toString(),TimeUtil.getCurrentTime(TimeUtil.FORMAT))
                            dialog.cancel()
                        }
                    dialog.show()
                    //Toast.makeText(this, "Location Permission Denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun proceedToLogin(){

        getCurrentLocation()
    }

    fun getCurrentLocation() {
        Toast.makeText(applicationContext,"Permission Granted",Toast.LENGTH_SHORT).show()
        if (!PermissionUtil.isLocationPermissionGranted(this)) {
            PermissionUtil.requestLocationPermission(this)
        }else {
            val locationProvider = LocationProvider(this)
            locationProvider.getCurrentLocation { location ->
                if (location != null) {
                    currentLat = location.latitude.toString()
                    currentLong = location.longitude.toString()
                    println("Lat: $currentLat, Lng: $currentLong")
                } else {
                    println("Location unavailable or permission denied")
                }
            }
        }
    }
}