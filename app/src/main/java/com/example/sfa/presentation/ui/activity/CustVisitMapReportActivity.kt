package com.example.sfa.presentation.ui.activity

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.SalesPersonModel
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.data.model.VisitCustLatLngRepModel
import com.example.sfa.databinding.ActivityCustVisitMapReportBinding
import com.example.sfa.databinding.ActivityCustVisitReportBinding
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.viewmodel.ReportVisitViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.Calendar
@AndroidEntryPoint
class CustVisitMapReportActivity:AppCompatActivity(), OnMapReadyCallback {
    private val visitViewmodel: ReportVisitViewModel by viewModels()
    private lateinit var binding: ActivityCustVisitMapReportBinding
    lateinit var  currentDt:String
    private lateinit var map: GoogleMap
    private val visitedPlaces =ArrayList<LatLng>()
    private val visistedCustList =ArrayList<VisitCustLatLngRepModel>()
    private  var commonList=ArrayList<SelectionModel>()
    private var currentPolyline: Polyline? = null
    private var spId:String = ""
    private var spName:String = ""
    private lateinit var  selectDate:String
    private lateinit var movingMarker: Marker
    private lateinit var custMarker: Marker
    private val handler = Handler(Looper.getMainLooper())
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    private var salespersonList= ArrayList<SalesPersonModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustVisitMapReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView(){
        dbController=DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.layoutToolbar.tvTitle.text="$customerLabel Visit with Mapview"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        currentDt= TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        selectDate= TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        // currentDt= "2025-01-07"
        binding.tvDate.setText(currentDt)

        spId=SecureStorage.getString(applicationContext,StringConstants.SP_ID).toString()
        spName=SecureStorage.getString(applicationContext,StringConstants.SP_NAME).toString()
        binding.tvSelectSp.text=spName
        if(SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==2){
            binding.llSp.visibility  = View.VISIBLE
        }else{
            binding.llSp.visibility  = View.GONE
        }
        getSalesPersonList()
        getVisitData()

        binding.cvSalesperson.setOnClickListener {

            SelectionBottomSheetFragment(
                title = "Select a Person",
                itemList = commonList
            ) { selected ->
                binding.tvSelectSp.text = selected.name
                spId = selected.id
                spName= selected.name
                getVisitData()

                Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT)
                    .show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }

        binding.tvDate.setOnClickListener(View.OnClickListener {
            val day: Int
            val month: Int
            val year: Int
            if (binding.tvDate.getText().toString() != "") {
                val dateArray: Array<String> = binding.tvDate.getText().toString().split("-".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                year = dateArray[0].toInt()
                month = dateArray[1].toInt() - 1
                day = dateArray[2].toInt()
            } else {
                val c = Calendar.getInstance()

                day = c[Calendar.MONTH]
                month = c[Calendar.MONTH]
                year = c[Calendar.YEAR]
            }
            val dialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    val _year = year.toString()
                    val _month = if ((month + 1) < 10) "0" + (month + 1) else (month + 1).toString()
                    val _date = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                    val _pickedDate = "$year-$_month-$_date"
                    Log.e("PickedDate: ", "Date: $_pickedDate") //2019-02-12
                    currentDt = _pickedDate // _date +"/"+_month+"/"+_year;

                    binding.tvDate.setText(currentDt)

                    /* if (::custMarker.isInitialized) { custMarker!!.remove() }
                     visitedPlaces.clear()
                     map.clear()
                     currentPolyline?.remove()*/
                    resetMapData()
                    if(Constant.isNetworkAvailable(applicationContext)) {
                        getVisitData()
                    }else{
                        Toast.makeText(applicationContext,"Please Check your Network Connection",
                            Toast.LENGTH_SHORT).show()
                    }

                }, year, month, day
            )
            dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
            dialog.show()
        })

        visitViewmodel.getMapListState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        visistedCustList.clear()
                        if(result.data.data!!.size>0){
                            resetMapData()
                            visistedCustList.addAll(result.data.data!!)
                            for(list in result.data.data!!){
                                var lat:Double= 0.0
                                var long:Double= 0.0
                                if(!list.InLat.equals(""))
                                    lat=list.InLat.toDouble()
                                if(!list.InLong.equals(""))
                                    long=list.InLong.toDouble()

                                val currentLatLng = LatLng(lat,long)
                                visitedPlaces.add(currentLatLng)


                            }
                            Log.e("data","sz2"+visitedPlaces.size)
                            if(visitedPlaces.size>0) {

                                visitedPlaces.forEachIndexed { index, location ->
                                    var name:String="Place ${index + 1}"
                                    for(list in visistedCustList){
                                        if(list.InLat.equals(location.latitude.toString())&&list.InLong.equals(location.longitude.toString())){
                                            name =list.custNm
                                        }
                                    }
                                    val customMarker: Bitmap = createCustomMarker(this, R.drawable.location_pin_01, name)//"Place ${index + 1}"
                                    custMarker= map!!.addMarker(
                                        MarkerOptions()
                                            .position(location)
                                            .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
                                    )!!
                                }
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(visitedPlaces.first(), 14f))
                                fetchAndAnimateRoute()
                            }
                            binding.llTotalDistance.visibility=View.VISIBLE


                        }

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
    private fun resetMapData() {
        if (::custMarker.isInitialized) {
            custMarker.remove()
        }
        if (::movingMarker.isInitialized) {
            movingMarker.remove()
        }

        visitedPlaces.clear()
        map.clear()

        currentPolyline?.remove()
        currentPolyline = null

        handler.removeCallbacksAndMessages(null)
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
                    if(!jsonObject.getString("Sp_Id").equals(SecureStorage.getString(applicationContext,StringConstants.SP_ID))) {
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

    private fun getVisitData(){
        if(Constant.isNetworkAvailable(applicationContext)){
             visitViewmodel.getMapVisitList(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,spId,currentDt)
        }else{
            Toast.makeText(applicationContext,"Please Check Your Network Connection",Toast.LENGTH_SHORT).show()
        }
    }

    private fun createCustomMarker(context: Context, @DrawableRes resource: Int, text: String): Bitmap {
        val marker: View = (context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.item_marker, null)
        val markerIcon = marker.findViewById<ImageView>(R.id.marker_icon)
        markerIcon.setImageResource(resource)
        val markerText = marker.findViewById<TextView>(R.id.marker_text)
        markerText.text = text
        marker.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        marker.layout(0, 0, marker.measuredWidth, marker.measuredHeight)
        val bitmap = Bitmap.createBitmap(marker.measuredWidth, marker.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        marker.draw(canvas)
        return bitmap
    }

    private fun fetchAndAnimateRoute() {
        if (::movingMarker.isInitialized) {
            movingMarker.remove()
        }
        if (::custMarker.isInitialized) {
            custMarker.remove()
        }
        currentPolyline?.remove()
        currentPolyline = null

        Log.e("visitedPlaces",""+visitedPlaces.size)
        var distance :Double=0.0

        for (i in 0 until visitedPlaces.size - 1) {
            val origin = visitedPlaces[i]
            val destination = visitedPlaces[i + 1]

            val originLatLong = LatLng(origin.latitude,origin.longitude)
            val destinationLatLong = LatLng(destination.latitude,destination.longitude)
            distance+= SphericalUtil.computeDistanceBetween(originLatLong, destinationLatLong);



            if(distance>0){
                binding.tvDistance.text=String.format("%.2f", (distance/1000))+" km"
            }

            val apiKey =getString(R.string.api_key)// "AIzaSyBn9eGybmgpvAp7MXbG1b1i1ODBo0YRruM"
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&key=$apiKey"


            Thread {
                try {
                    val response = URL(url).readText()
                    val jsonObject = JSONObject(response)
                    val routes = jsonObject.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val points =
                            routes.getJSONObject(0).getJSONObject("overview_polyline")
                                .getString("points")
                        val path = decodePolyline(points)

                        // Run on the UI thread to update the map
                        runOnUiThread {

                            if (::movingMarker.isInitialized) {
                                movingMarker.remove()
                            }
                            if (::custMarker.isInitialized) {
                                custMarker.remove()
                            }
                            animateMarkerAlongRoute(path)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun animateMarkerAlongRoute(path: List<LatLng>) {
        val delay: Long = 50
        var step = 0
        handler.removeCallbacksAndMessages(null)
        if(selectDate.equals(currentDt)) {
            val polylineOptions = PolylineOptions().addAll(path).width(10f)
                .color(resources.getColor(R.color.purple_500, theme))
            currentPolyline = map.addPolyline(polylineOptions)
            if (::movingMarker.isInitialized) {
                movingMarker.remove()
            }
            movingMarker = map.addMarker(
                MarkerOptions()
                    .position(path.last())
                    .title("") // Start with 1
            )!!

            // Animate the marker along the path
            handler.post(object : Runnable {
                override fun run() {
                    if (step < path.size) {
                        movingMarker.position = path[step]
                        step++
                        handler.postDelayed(this, delay)
                    } else {

                        // movingMarker.position = path.last()
                        movingMarker.title = "Lastly visited place : "+visistedCustList.get(visistedCustList.size-1).custNm
                        movingMarker.showInfoWindow()
                    }
                }
            })
        }
        //map.addPolyline(polylineOptions)
        selectDate=currentDt



    }

    // Decode polyline string into a list of LatLng
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }

        return poly
    }



}