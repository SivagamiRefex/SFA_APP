package com.example.sfa.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.databinding.ActivityRouteBinding
import com.example.sfa.presentation.viewmodel.CustomerViewModel
import com.example.sfa.presentation.viewmodel.RouteViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddRouteActivity:AppCompatActivity() {
    private lateinit var binding: ActivityRouteBinding
    private val routeViewModel: RouteViewModel by viewModels()
    lateinit var dbController: DBController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    fun initView() {
        dbController = DBController(applicationContext)

        binding.layoutToolbar.tvTitle.text="Add Route"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        binding.btnSave.setOnClickListener {
            if (binding.etRoute.text.toString() == "") {
                Toast.makeText(applicationContext, "Enter Route Name", Toast.LENGTH_LONG).show()
            }else{
                saveRoute()
            }
        }


        routeViewModel.saveRouteState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {

                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, "Route Saved Successfully", Toast.LENGTH_SHORT).show()
                        /* val gson = Gson()
                         val jsonString = gson.toJson(result.data!!.data!!)
                         if (!dbController.updateProduct(StringConstants.CUSTOMER_DATA, jsonString.toString())) {
                             dbController.addProduct(StringConstants.CUSTOMER_DATA, jsonString.toString())
                         }*/
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

    fun saveRoute(){
        if (Constant.isNetworkAvailable(applicationContext)) {

            val jsonObject = JsonObject()
            jsonObject.addProperty("name", binding.etRoute.text.toString())
            jsonObject.addProperty("territory", binding.etTerritory.text.toString())
            jsonObject.addProperty("createdDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("createdBy", SecureStorage.getString(applicationContext, StringConstants.SP_ID))
            Log.e("token", "" + SecureStorage.getString(applicationContext, StringConstants.AUTH_TOKEN)!!)
            Log.e("route data", "" + jsonObject.toString())
            routeViewModel.saveRoute(SecureStorage.getString(applicationContext, StringConstants.AUTH_TOKEN)!!, jsonObject)

        }
    }
}