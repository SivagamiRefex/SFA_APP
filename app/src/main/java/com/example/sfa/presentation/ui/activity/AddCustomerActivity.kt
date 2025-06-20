package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.data.model.SourceOfLeadListModel
import com.example.sfa.databinding.ActivityAddcustomerBinding
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.viewmodel.CustomerViewModel
import com.example.sfa.presentation.viewmodel.LoginViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.EmailValidator
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.PermissionUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.Locale
@AndroidEntryPoint
class AddCustomerActivity:AppCompatActivity() {

    private lateinit var binding: ActivityAddcustomerBinding
    private val custViewModel: CustomerViewModel by viewModels()
    lateinit var dbController: DBController
    private  var sourceList=ArrayList<SelectionModel>()
    var sourceOfLeadId:String="0"
    var sourceOfLeadName:String=""
    var customerLabel:String="Customer"
    var geoAddress:String=""
    private  var latitude:String=""
    private  var longitude:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddcustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView() {
        dbController=DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.tvCustomerLabelName.text="$customerLabel Name"
        binding.etNewCustomer.hint="Enter  $customerLabel Name"
        binding.layoutToolbar.tvTitle.text="Add $customerLabel"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        binding.ibRefresh.setOnClickListener {
            getCurrentLocation()
        }
        getSourceOfLeadList()
        getCurrentLocation()
        binding.cvSourceOfLead.setOnClickListener{
             SelectionBottomSheetFragment(title = "Select the Source", itemList = sourceList) {
                 selected ->
                        binding.tvSourceOfLead.text=selected.name
                        sourceOfLeadId=selected.id
                        sourceOfLeadName=selected.name
                 if(selected.id.equals("4")){
                     binding.cvOthers.visibility= View.VISIBLE

                 }else{
                     binding.cvOthers.visibility= View.GONE
                     sourceOfLeadName=""
                 }
                 Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT).show()
             }.show(supportFragmentManager, "MySelectionSheet")
        }

        binding.etAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if(s.toString().equals("")){
                    latitude=""
                    longitude=""
                }

            }

        })

        binding.btnSave.setOnClickListener{
            if(binding.etNewCustomer.text.toString() == ""){
                Toast.makeText(applicationContext, "Enter $customerLabel Name", Toast.LENGTH_LONG).show()
            }else if(binding.etAddress.text.toString() == ""){
                Toast.makeText(applicationContext, "Enter $customerLabel Address", Toast.LENGTH_LONG).show()
            }else if(binding.etCity.text.toString() == ""){
                Toast.makeText(applicationContext, "Enter $customerLabel City", Toast.LENGTH_LONG).show()
            }else if(binding.etPhone.text.toString() == ""){
                Toast.makeText(applicationContext, "Enter $customerLabel Phone", Toast.LENGTH_LONG).show()
            }else if(!isValidPhoneNumber(binding.etPhone.text.toString())){
                Toast.makeText(applicationContext, "Enter Valid Phone Number", Toast.LENGTH_LONG).show()
            }
            // else if(etEmail.text.toString() == ""){
            //    Toast.makeText(context, "Enter $customerLabel Email", Toast.LENGTH_LONG).show()
            // }
             else if(!binding.etEmail.text.toString().equals("")&&!EmailValidator.isValid(binding.etEmail.text.toString())) {
                Toast.makeText(applicationContext, "Enter Valid $customerLabel Email", Toast.LENGTH_LONG).show()
             }
            //else if(etOrganization.text.toString() == ""){
            //    Toast.makeText(context, "Enter Organization Name", Toast.LENGTH_LONG).show()
            //}else if(sourceOfLeadId == ""||(sourceOfLeadId=="4" && tvotherSourceOfLead.text.toString() == "")){
            //    Toast.makeText(context, "Select (or) Enter Source of $customerLabel", Toast.LENGTH_LONG).show()
            // }else if(etPanNo.text.toString() == ""){
            // Toast.makeText(context, "Enter $customerLabel Pan Number", Toast.LENGTH_LONG).show()
            //  }else if(etGstNo.text.toString() == ""){
            //  Toast.makeText(context, "Enter $customerLabel Gst No", Toast.LENGTH_LONG).show()
            // }
            else{
                saveCustomer(binding.etNewCustomer.text.toString(),binding.etAddress.text.toString(),binding.etCity.text.toString(),binding.etEmail.text.toString(),binding.etPhone.text.toString(),binding.etPannumber.text.toString(),binding.etGstno.text.toString(),binding.etOrganization.text.toString())
            }
        }

        custViewModel.saveCustState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, "$customerLabel Saved Successfully", Toast.LENGTH_SHORT).show()
                       /* val gson = Gson()
                        val jsonString = gson.toJson(result.data!!.data!!)
                        if (!dbController.updateProduct(StringConstants.CUSTOMER_DATA, jsonString.toString())) {
                            dbController.addProduct(StringConstants.CUSTOMER_DATA, jsonString.toString())
                        }*/
                        lifecycleScope.launch {
                            getCustomer()
                        }
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
                }

            }
        }


    }
    fun getCurrentLocation() {
       // Toast.makeText(applicationContext,"Permission Granted",Toast.LENGTH_SHORT).show()
        if (!PermissionUtil.isLocationPermissionGranted(this)) {
            PermissionUtil.requestLocationPermission(this)
        }else {
            val locationProvider = LocationProvider(this)
            locationProvider.getCurrentLocation { location ->
                if (location != null) {
                    val geocoder = applicationContext?.let { Geocoder(it, Locale.getDefault()) }
                    val list: List<Address> =
                        geocoder?.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )!!
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    geoAddress=list[0].getAddressLine(0)
                    binding.etAddress.setText(geoAddress)
                    println("Lat: $latitude, Lng: $longitude, Addr: $geoAddress")
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
    fun getSourceOfLeadList() {
        var dataResponse = dbController.getResponse(StringConstants.SOURCEOFLEAD_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {
            sourceList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val selectionModel: SelectionModel = SelectionModel(
                        jsonObject.getString("id"),
                        jsonObject.getString("name")
                    )
                    sourceList.add(selectionModel)
                }
                Log.e("commonlist", "sourceOfLeadList: $sourceList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Source of Lead List not available",
                Toast.LENGTH_SHORT
            ).show()
        }


    }
    private fun saveCustomer(name:String, addr:String, city:String, email:String, phone:String, panNo:String, gstNo:String,organizationName:String) {
        if (Constant.isNetworkAvailable(applicationContext)) {

            val jsonObject = JsonObject()
            jsonObject.addProperty("name", name)
            jsonObject.addProperty("billingAddr", addr)
            jsonObject.addProperty("city", city)
            jsonObject.addProperty("eMail", email)
            jsonObject.addProperty("phone", phone)
            jsonObject.addProperty("organizationNm", organizationName)
            jsonObject.addProperty("panNo", panNo)
            jsonObject.addProperty("gstNo", gstNo)
            jsonObject.addProperty("lat", latitude)
            jsonObject.addProperty("long", longitude)
            jsonObject.addProperty("geoAddr", geoAddress)
            jsonObject.addProperty("createdDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("sourceOfLeadId", sourceOfLeadId)
            jsonObject.addProperty(
                "sourceOfLeadName",
                if (sourceOfLeadId == "4") binding.tvOtherSourceOfLead.text.toString() else binding.tvSourceOfLead.text.toString()
            )
            jsonObject.addProperty(
                "spId", SecureStorage.getString(applicationContext, StringConstants.SP_ID)
            )

            Log.e(
                "token",
                "" + SecureStorage.getString(applicationContext, StringConstants.AUTH_TOKEN)!!
            )
            Log.e("customer data", "" + jsonObject.toString())
            custViewModel.saveCustomer(
                SecureStorage.getString(
                    applicationContext,
                    StringConstants.AUTH_TOKEN
                )!!, jsonObject
            )

        }
    }

     suspend  fun getCustomer(){

        val jsonObject = JsonObject()
        jsonObject.addProperty("spName", SecureStorage.getString(applicationContext, StringConstants.SP_NAME))
        jsonObject.addProperty("spType",SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE) )
        jsonObject.addProperty("spId", SecureStorage.getString(applicationContext, StringConstants.SP_ID))

         Log.e("customer data", "" + jsonObject.toString())
        val result = custViewModel.getCustomer(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,jsonObject)
        when (result) {
            is Resource.Success -> {
                LoadingUtil.hideLoading()

                try {
                    var json = JSONTokener(result.data!!.string()).nextValue()
                    var jsonArray = JSONArray()
                    if (json is JSONObject) {
                        if (json.getBoolean("status")) {
                            if (json is JSONObject) {
                                jsonArray = json.getJSONArray("data")
                                //withContext(Dispatchers.Main) {

                                    if (!dbController.updateProduct(
                                            StringConstants.CUSTOMER_DATA,
                                            jsonArray.toString()
                                        )
                                    ) {
                                        dbController.addProduct(
                                            StringConstants.CUSTOMER_DATA,
                                            jsonArray.toString()
                                        )
                                    }
                                finish()

                                // }

                            }
                        }
                    }
                }catch (e:Exception){
                    Toast.makeText(applicationContext,"Error: "+e.message,Toast.LENGTH_SHORT).show()
                }
            }

            is Resource.Error -> {
                LoadingUtil.hideLoading()

            }
            is Resource.Loading -> {
                LoadingUtil.showLoading(applicationContext)
            }

            else -> {}
        }
     }
    private fun isValidPhoneNumber(mobileNum: String): Boolean{
            if(mobileNum.equals("0000000000")||mobileNum.get(0).toString().equals("0"))
            {
                return false
            }else if(mobileNum.length == 10 && android.util.Patterns.PHONE.matcher(mobileNum).matches() ) {
                return true
            }else {
                return false
            }
    }







}