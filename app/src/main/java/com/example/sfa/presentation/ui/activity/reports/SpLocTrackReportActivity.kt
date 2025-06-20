package com.example.sfa.presentation.ui.activity.reports

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.LocationTrackModel
import com.example.sfa.data.model.SalesPersonModel
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.ActivityCustVisitMapReportBinding
import com.example.sfa.databinding.ActivityRepeatCustomerVisitReportBinding
import com.example.sfa.databinding.ActivitySpLocTrackReportBinding
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.viewmodel.LocationTrackViewModel
import com.example.sfa.presentation.viewmodel.ReportVisitViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONException
import java.util.Locale

@AndroidEntryPoint
class SpLocTrackReportActivity:AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivitySpLocTrackReportBinding
    private val locationTrackViewModel: LocationTrackViewModel by viewModels()
    lateinit var  currentDt:String
    private lateinit var map: GoogleMap
    private  var commonList=ArrayList<SelectionModel>()
    private var spId:String = ""
    private var spName:String = ""
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    private var salespersonList= ArrayList<SalesPersonModel>()
    private lateinit var startMarker: Marker
    private  var tracklist=ArrayList<LocationTrackModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpLocTrackReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView() {
        dbController = DBController(applicationContext)
        customerLabel =
            Constant.getSetup("customer_label", "Customer", dbController, applicationContext)!!
        binding.layoutToolbar.tvTitle.text = "Salesperson Location Track View"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        currentDt= TimesUtil.getCurrentTime(TimesUtil.FORMAT)
        binding.tvCurrentDate.setText(currentDt)


        spId=SecureStorage.getString(applicationContext,StringConstants.SP_ID).toString()
        spName=SecureStorage.getString(applicationContext,StringConstants.SP_NAME).toString()
        if(SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE)==2|| SecureStorage.getInt(applicationContext,
                StringConstants.SP_TYPE)==3){
            getSalesPersonList()
            binding.cvSalesperson.isEnabled=true


        }else{

            binding.tvSelectSp.text=spName
            getLocationData()
            binding.cvSalesperson.isEnabled=false
        }

        binding.cvSalesperson.setOnClickListener {
            SelectionBottomSheetFragment(
                title = "Select a Person",
                itemList = commonList
            ) { selected ->
                binding.tvSelectSp.text = selected.name
                spId = selected.id
                spName= selected.name
                getLocationData()

                //Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT).show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }


        locationTrackViewModel.getLocTrackState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        tracklist=result.data.data!!
                        if(tracklist.size>0) {
                            val currentLatLng = LatLng(
                                tracklist.get(0).locLat.toDouble(),
                                tracklist.get(0).locLong.toDouble()
                            )
                            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 14f))

                            try {

                                if (::startMarker.isInitialized) {
                                    startMarker!!.remove()
                                }
                                startMarker = map!!.addMarker(
                                    MarkerOptions()
                                        .position(currentLatLng!!)
                                        .title(spName)
                                        .icon(resizeIcon(R.drawable.icon_man, 64, 64))
                                )!!
                                map!!.uiSettings.isZoomControlsEnabled = true

                                binding.tvTrackTime.text = tracklist.get(0).submittedDt
                                val geocoder =
                                    applicationContext?.let { Geocoder(it, Locale.getDefault()) }
                                val list: List<Address> =
                                    geocoder?.getFromLocation(
                                        tracklist.get(0).locLat.toDouble(),
                                        tracklist.get(0).locLong.toDouble(),
                                        1
                                    )!!

                                binding.tvTrackAddress.text = list[0].getAddressLine(0)


                            } catch (e: Exception) {
                                Log.e("exception", e.message.toString())
                            }
                        }else{
                            Toast.makeText(applicationContext,"Data Not Available",Toast.LENGTH_SHORT).show()
                            binding.tvTrackTime.text = ""
                            binding.tvTrackAddress.text = ""
                            if (::startMarker.isInitialized) {
                                startMarker!!.remove()
                            }
                        }

                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                        binding.tvTrackTime.text = ""
                        binding.tvTrackAddress.text = ""
                        if (::startMarker.isInitialized) {
                            startMarker!!.remove()
                        }
                    }
                }
                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                    binding.tvTrackTime.text = ""
                    binding.tvTrackAddress.text = ""
                    if (::startMarker.isInitialized) {
                        startMarker!!.remove()
                    }
                }
                is Resource.Loading -> {
                    LoadingUtil.showLoading(this)
                    // Show loading indicator
                }

            }
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }
    private fun getSalesPersonList() {


        var dataResponse = dbController.getResponse(StringConstants.SALESPERSON_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {

            // Log.e("data there","available")
            salespersonList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    //&& !jsonObject.getString("SP_Type").equals("3")
                    //if(!jsonObject.getString("Sp_Id").equals (SecureStorage.getString(applicationContext,StringConstants.SP_ID))) {
                    if((SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==2 &&
                                !jsonObject.getString("SP_Type").equals("2")) ||
                        (SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==3&&
                                jsonObject.getString("Sp_Reporting_To_Id").equals
                                    (SecureStorage.getString(applicationContext,StringConstants.SP_ID)))){
                        val selectionModel = SalesPersonModel(
                            jsonObject.getString("Sp_Id"),
                            jsonObject.getString("Sp_Name")
                        )
                        salespersonList.add(selectionModel)
                    }
                }
                getCommonListModel(salespersonList)
                Log.e("commonlist", "sourceOfLeadList: $salespersonList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }
    fun getCommonListModel(customerList: ArrayList<SalesPersonModel>) {
        commonList = ArrayList<SelectionModel>()
        var datamodel = SelectionModel(spId,spName)
        commonList.add(datamodel)
        if (customerList.isNotEmpty()) {
            for (list in customerList) {
                var datamodel = SelectionModel(
                    list.spId, list.spName,
                )
                commonList.add(datamodel)
            }
        }
    }

    fun getLocationData() {
        locationTrackViewModel.getLocTrackList(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
            spId,currentDt)
    }

    private fun resizeIcon(drawableId: Int, width: Int, height: Int): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(resources, drawableId)
        val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

}