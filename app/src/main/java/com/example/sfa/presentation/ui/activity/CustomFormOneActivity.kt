package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.CustomModuleDataModel
import com.example.sfa.databinding.ActivityCustomFormBinding
import com.example.sfa.presentation.ui.Adapter.CustomFormAdapter
import com.example.sfa.presentation.viewmodel.FormViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.PermissionUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomFormOneActivity:AppCompatActivity() {
    private lateinit var binding: ActivityCustomFormBinding
    private lateinit var dbController: DBController
    private var moduleList = ArrayList<CustomModuleDataModel>()
    private val formViewModel: FormViewModel by viewModels()

    private var customerId = "0"
    private var customerName = ""
    private var inLatitude = "0"
    private var inLongitude = "0"
    private var clocation: Location? = null
    private var screenType = 0
    private var screenFrom="visit"
    private var checkInId=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
            binding.layoutToolbar.tvTitle.text = "Form Entry"
            binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
            binding.layoutToolbar.menubtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            dbController = DBController(applicationContext)

        intent.extras?.let {
            customerId = it.getString("customerId", "0")
            customerName = it.getString("customerNm", "")
            inLatitude = it.getString("latitude", "0")
            inLongitude = it.getString("longitude", "0")
            screenType = it.getInt("type", 0)
            screenFrom=it.getString("from","visit")
            checkInId=it.getString("checkInId","")

        }


        val spType = SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE)
        if (((spType == 2 ||spType == 3) && screenType == 0 )||(Constant.getSetup("geofenc_need",0,dbController,this)==1)) {
            getAssignedCustomModuleList()
        } else {
            getCurrentLocation()
        }


        formViewModel.getFormListState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {

                        moduleList=result.data.data!!
                        if (moduleList.isNotEmpty()) {
                            callAdapter()
                        }
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    LoadingUtil.showLoading(this)
                    // Show loading indicator
                }

            }
        }
    }
    fun getCurrentLocation() {
       // Toast.makeText(applicationContext,"Permission Granted", Toast.LENGTH_SHORT).show()
        if (!PermissionUtil.isLocationPermissionGranted(this)) {
            PermissionUtil.requestLocationPermission(this)
        }else {
            val locationProvider = LocationProvider(this)
            locationProvider.getCurrentLocation { location ->
                if (location != null) {
                    clocation=location
                    val radius = 100.0
                    val meters: Double = Constant.meterDistanceBetweenPoints(
                        java.lang.Double.parseDouble(inLatitude),
                        java.lang.Double.parseDouble(inLongitude),
                        Constant.getLatitude(clocation),
                        Constant.getLongitude(clocation)
                    )
                    if (meters <= radius) {
                        getAssignedCustomModuleList()
                    } else {
                     //  getAssignedCustomModuleList()
                        Toast.makeText(applicationContext, "You are not in range.So can't view details ", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    println("Location unavailable or permission denied")
                }
            }
        }
    }

    fun getAssignedCustomModuleList(){
        if(Constant.isNetworkAvailable(applicationContext)){

            val jsonObject = JsonObject()
            jsonObject.addProperty("spId",  SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!)
            jsonObject.addProperty("spType", SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE))
            jsonObject.addProperty("custId", customerId)
            jsonObject.addProperty("type", screenType)
            formViewModel.getFormList(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,jsonObject)

        }else{
            Toast.makeText(applicationContext,"Please check your network connection",Toast.LENGTH_SHORT).show()
        }
    }

    private fun callAdapter() {
        val adapter = CustomFormAdapter(moduleList, this)
        binding.gvItem.adapter = adapter

        binding.gvItem.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val module = moduleList[position]
            val intent = Intent(applicationContext, CustomFormDetailActivity::class.java).apply {
                putExtra("moduleId", module.moduleId)
                putExtra("moduleName", module.moduleName)
                putExtra("title", module.moduleName)
                putExtra("custId", customerId)
                putExtra("screenType",screenType)
                Log.e("screenType",""+screenType)
                putExtra("screenFrom",screenFrom)
                putExtra("checkInId",checkInId)
            }
            startActivity(intent)

            Toast.makeText(applicationContext, "${module.moduleName} selected", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode ==PermissionUtil.LOCATION_PERMISSION_CODE){

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


}