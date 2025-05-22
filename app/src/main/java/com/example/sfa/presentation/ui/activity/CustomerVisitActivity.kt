package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.CheckInDataModel
import com.example.sfa.databinding.ActivityCustomerVisitBinding
import com.example.sfa.presentation.viewmodel.CustomerVisitViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.PermissionUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
@AndroidEntryPoint
class CustomerVisitActivity:AppCompatActivity() {
    private val visitViewmodel: CustomerVisitViewModel by viewModels()
    private lateinit var binding: ActivityCustomerVisitBinding
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    var geoAddress:String=""
    private  var latitude:String=""
    private  var longitude:String=""
    private var custId=""
    private var custName=""
    private  var myDayPlanId=0
    private  var taskId=0
    private var clocation: Location? = null
    private var flag:Int=0
    private var inLat:String = ""
    private var inLong:String = ""
    private var slNo=""
    private var planId=""
    private var newTaskId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    fun initView(){
        dbController=DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.layoutToolbar.tvTitle.text="$customerLabel Visit"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }

        getMydayplanData()
        getCurrentLocation()
        getTodayCheckInData()

        binding.llMap.setOnClickListener{
            val intent = Intent(applicationContext, GeoLocationActivity::class.java)
            intent.putExtra("customerName", custName)
            intent.putExtra("customerId",custId)
            intent.putExtra("customerAddr",geoAddress)
            intent.putExtra("cusLat",latitude)
            intent.putExtra("cusLong", longitude)
            intent.putExtra("mydayplanId", myDayPlanId)
            intent.putExtra("taskId", taskId)
            startActivity(intent)
        }

        binding.llCheckin.setOnClickListener{
            if(binding.tvSelectCustomer.text.equals("")){
                Toast.makeText(this,"Customer Not Found",Toast.LENGTH_SHORT).show()
            }else{

                if(Constant.getSetup("geofenc_need",0,dbController,this)==0) {

                    val radius = 200.0
                    val meters: Double = Constant.meterDistanceBetweenPoints(
                        java.lang.Double.parseDouble(latitude), java.lang.Double.parseDouble(longitude),
                        Constant.getLatitude(clocation),
                        Constant.getLongitude(clocation)
                    )
                    if (meters <= radius) {
                        showCheckInAlert()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "You are not in range.So can't Check-In",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    showCheckInAlert()
                }
            }
        }

        binding.llCheckout.setOnClickListener {
            if(flag==0){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Do Check-In First...")
                builder.setTitle("Alert!!!")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") {

                        dialog, which -> finish()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }else{
                showCheckOutAlert()
            }


        }

        binding.cvProductCatalog.setOnClickListener{
            val intent = Intent(applicationContext, ProductCatalogActivity::class.java)
            startActivity(intent)
        }

        binding.cvFormEntry.setOnClickListener{

                val intent = Intent(applicationContext, CustomFormOneActivity::class.java)
                intent.putExtra("customerNm",binding.tvSelectCustomer.text.toString())
                intent.putExtra("customerId",custId)
                intent.putExtra("latitude",inLat)
                intent.putExtra("longitude",inLong)
                intent.putExtra("type",1)
                startActivity(intent)
                finish()

        }

        visitViewmodel.saveCheckInState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, "Check-In Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
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

        visitViewmodel.getTodayCheckIn.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        val checkinModel: CheckInDataModel = result.data.data!!

                        flag=checkinModel.flg
                        planId=checkinModel.planId
                        newTaskId=checkinModel.taskId

                        if(flag==1) {
                            custId = checkinModel.custId
                            binding.tvSelectCustomer.text = checkinModel.custNm
                            slNo=checkinModel.slNo
                            binding.tvCheckinTime.text=checkinModel.InOutTime
                            binding.llCheckin.visibility = View.GONE
                            binding.llCheckinTime.visibility= View.VISIBLE
                            inLat=checkinModel.inLat
                            inLong=checkinModel.inLong
                            binding.cvHead.visibility=View.VISIBLE

                        }else{
                            binding.llCheckin.visibility = View.VISIBLE
                            binding.llCheckinTime.visibility= View.GONE
                            binding.cvHead.visibility=View.GONE
                        }
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                    binding.llCheckin.visibility = View.VISIBLE
                    binding.llCheckinTime.visibility= View.GONE
                    binding.cvHead.visibility=View.GONE

                }
                is Resource.Loading -> {
                    // Show loading indicator
                }

            }
        }

        visitViewmodel.saveCheckOutState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, "Check-Out Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
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


    }

    fun getMydayplanData(){
        var dataResponse = dbController.getResponse(StringConstants.MYDAYPLAN_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {

            try {
                val jsonObject = JSONObject(dataResponse)
                custId = jsonObject.getInt("Cust_Id").toString()
                custName = jsonObject.getString("Cust_Name")
                geoAddress = jsonObject.getString("Cust_Addr")
                latitude = jsonObject.getString("Cust_Lat")
                longitude = jsonObject.getString("Cust_Long")
                myDayPlanId = jsonObject.getInt("Mydayplan_Id")
                taskId=jsonObject.getInt("Task_Id")
                binding.tvSelectCustomer.text=custName
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(applicationContext, "Source of Lead List not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCurrentLocation() {
        Toast.makeText(applicationContext,"Permission Granted",Toast.LENGTH_SHORT).show()
        if (!PermissionUtil.isLocationPermissionGranted(this)) {
            PermissionUtil.requestLocationPermission(this)
        }else {
            val locationProvider = LocationProvider(this)
            locationProvider.getCurrentLocation { location ->
                if (location != null) {
                    clocation=location
                } else {
                    println("Location unavailable or permission denied")
                }
            }
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

    fun showCheckInAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to Check-In?")
        builder.setTitle("Check-In")
        builder.setCancelable(false)
        builder.setPositiveButton("Yes") {

                dialog, which ->

            saveCheckIn()
            finish()
        }
        builder.setNegativeButton("No") {

                dialog, which -> dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun saveCheckIn(){

        if(Constant.isNetworkAvailable(applicationContext)) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("name", custName)
            jsonObject.addProperty("id", custId)
            jsonObject.addProperty("Addr", geoAddress)
            jsonObject.addProperty("lat", latitude)
            jsonObject.addProperty("long", longitude)
            jsonObject.addProperty("mydayplanId", myDayPlanId)
            jsonObject.addProperty("taskId", taskId)
            jsonObject.addProperty("checkInDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("spId", SecureStorage.getString(applicationContext,StringConstants.SP_ID))
            visitViewmodel.saveCheckIn(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,jsonObject)

        }else{
            Toast.makeText(applicationContext,"Please check your network connection",Toast.LENGTH_SHORT).show()
        }


    }

    fun getTodayCheckInData(){
        if(Constant.isNetworkAvailable(applicationContext)){
            visitViewmodel.getTodayCheckIn(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!,
                TimesUtil.getCurrentTime(TimesUtil.FORMAT1))
        }
    }

    private fun showCheckOutAlert(){

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to Check-Out?")
        builder.setTitle("Check-Out")
        builder.setCancelable(false)
        builder.setPositiveButton("Yes") {

                                         dialog, which ->
                                                        saveCheckOut()
                                                        finish()
        }
        builder.setNegativeButton("No") {
                                        dialog, which -> dialog.cancel()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun saveCheckOut() {
        if (Constant.isNetworkAvailable(applicationContext)) {
            var addr=""
            var lat=""
            var long=""
            if (clocation != null){
                 val geocoder = applicationContext?.let { Geocoder(it, Locale.getDefault()) }
                 val list: List<Address> =
                  geocoder?.getFromLocation(
                      clocation!!.latitude,
                      clocation!!.longitude,
                    1
                  )!!
                addr=list[0].getAddressLine(0)
                lat=clocation!!.latitude.toString()
                long=clocation!!.longitude.toString()
             }

            val jsonObject = JsonObject()
            jsonObject.addProperty("name", binding.tvSelectCustomer.text.toString())
            jsonObject.addProperty("id", custId)
            jsonObject.addProperty("Addr", addr)
            jsonObject.addProperty("lat", lat)
            jsonObject.addProperty("long", long)
            jsonObject.addProperty("slNo", slNo)
            jsonObject.addProperty("planId", planId)
            jsonObject.addProperty("taskId", taskId)
            jsonObject.addProperty("checkOutDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("spId", SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!)
            visitViewmodel.saveCheckOut(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                jsonObject)
        }
    }

}