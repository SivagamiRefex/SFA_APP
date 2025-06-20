package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R

import com.example.sfa.databinding.ActivityGeoLocationBinding
import com.example.sfa.presentation.viewmodel.CustomerVisitViewModel
import com.example.sfa.presentation.viewmodel.RouteViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.PermissionUtil
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread
@AndroidEntryPoint
class GeoLocationActivity:AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityGeoLocationBinding
    private val visitViewmodel: CustomerVisitViewModel by viewModels()
    private lateinit var customerName:String
    private lateinit var customerId:String
    private lateinit var customerAddr:String
    private lateinit var latitude:String
    private lateinit var longutidue:String
    private var clocation: Location? = null
    private var apiKey:String = ""
    private var destinationLatLng: LatLng? = null
    private var currentLocation: LatLng? = null
    private var currentPolyline: Polyline? = null
    private var mMap: GoogleMap? = null
    private lateinit var startMarker: Marker
    lateinit var dbController: DBController
    var customerLabel=""
    private  var mydayplanId:Int=0
    private var taskId:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeoLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView(){
        dbController= DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.layoutToolbar.tvTitle.text="Map View"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        if (intent.hasExtra("customerName"))
            customerName= intent.getStringExtra("customerName").toString()
        if (intent.hasExtra("customerId"))
            customerId= intent.getStringExtra("customerId").toString()
        if (intent.hasExtra("customerAddr"))
            customerAddr= intent.getStringExtra("customerAddr").toString()
        if (intent.hasExtra("cusLat"))
            latitude= intent.getStringExtra("cusLat").toString()
        if (intent.hasExtra("cusLong"))
            longutidue= intent.getStringExtra("cusLong").toString()
        if (intent.hasExtra("mydayplanId"))
            mydayplanId= intent.getIntExtra("mydayplanId",0)
        if (intent.hasExtra("taskId"))
            taskId= intent.getIntExtra("taskId",0)
        destinationLatLng = LatLng(latitude.toDouble(), longutidue.toDouble())
        val ai: ApplicationInfo = applicationContext.packageManager.getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        apiKey = value.toString()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        getContinuousCurrentLocation()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        visitViewmodel.saveCheckInState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, "Check-In Successfully", Toast.LENGTH_SHORT).show()
                        finish()
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

    override fun onMapReady(pMap: GoogleMap) {
        mMap = pMap
    }

    fun getContinuousCurrentLocation() {
       // Toast.makeText(applicationContext,"Permission Granted", Toast.LENGTH_SHORT).show()
        if (!PermissionUtil.isLocationPermissionGranted(this)) {
            PermissionUtil.requestLocationPermission(this)
        }else {
            val locationProvider = LocationProvider(this)
            locationProvider.getContinuousCurrentLocation { location ->
                if (location != null) {

                    clocation=location
                    val currentLatLng = LatLng(location!!.latitude, location!!.longitude)
                    //val currentLatLng =LatLng(13.0660512, 80.2402281)
                    currentLocation = currentLatLng
                    if (currentLatLng != null) {
                        Log.e("test4","location callback2")
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 14f))
                        addShopMarkers()
                        if (::startMarker.isInitialized) {
                            startMarker!!.remove()
                        }else {
                            startMarker = mMap!!.addMarker(
                                MarkerOptions()
                                    .position(currentLocation!!)
                                    .title("You are here")
                                    .icon(resizeIcon(R.drawable.ic_directions_bike, 64, 64))
                            )!!
                            mMap!!.uiSettings.isZoomControlsEnabled = true

                        }

                        updateRoutePath(currentLocation!!, destinationLatLng!!)
                    }
                } else {
                    println("Location unavailable or permission denied")
                }
            }
        }
    }
   /* private fun resizeIcon(drawableId: Int, width: Int, height: Int): BitmapDescriptor {
        val imageBitmap = BitmapFactory.decodeResource(resources, drawableId)
        val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }*/

    private fun resizeIcon(@DrawableRes drawableId: Int, width: Int, height: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, drawableId)
            ?: throw IllegalArgumentException("Drawable not found")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addShopMarkers() {
        if (latitude.isNotEmpty() && longutidue.isNotEmpty()) {
            val originalLatLng = LatLng(latitude.toDouble(), longutidue.toDouble())
            val customMarker: Bitmap = createCustomMarker(this, R.drawable.location_pin_01, customerName)
            val marker = mMap!!.addMarker(
                MarkerOptions()
                    .position(originalLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
            )
            marker!!.tag = customerName
            mMap!!.setOnMarkerClickListener { marker ->

                if(marker.tag==customerName) {
                    val radius: Double = 200.0
                    val meters: Double = Constant.meterDistanceBetweenPoints(
                        java.lang.Double.parseDouble(latitude),
                        java.lang.Double.parseDouble(longutidue),
                        Constant.getLatitude(clocation),
                        Constant.getLongitude(clocation)
                    )
                    if (meters <= radius) {
                        showCustomDialog()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "You are not in range.So can't check-in",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return@setOnMarkerClickListener false
            }
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
    private fun updateRoutePath(origin: LatLng, destination: LatLng) {
        Log.e("test5","location direction")
        fetchDirections(origin, destination) { path ->
            runOnUiThread {
                if (currentPolyline == null) {
                    // Draw the initial route
                    Log.e("test6","location direction1")

                    currentPolyline = mMap!!.addPolyline(
                        PolylineOptions()
                            .addAll(path)
                            .width(10f)
                            .color(0xFF0000FF.toInt())
                    )
                } else {
                    Log.e("test7","location direction2")



                    // Update the existing route
                    currentPolyline?.points = path
                    val startPoint = path.first()

                    if (::startMarker.isInitialized) {
                        startMarker!!.remove()
                    }
                    // Add the new start marker
                    startMarker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(currentLocation!!)
                            .title("You are here")
                            .icon(resizeIcon(R.drawable.ic_directions_bike, 64, 64))
                    )!!
                    mMap!!.uiSettings.isZoomControlsEnabled = true


                }

            }
        }
    }
    private fun fetchDirections(origin: LatLng, destination: LatLng, callback: (List<LatLng>) -> Unit) {
        Log.e("test8","location direction3")

        val apiKey =getString(R.string.api_key)// "AIzaSyBn9eGybmgpvAp7MXbG1b1i1ODBo0YRruM"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"

        thread {
            try {
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)
                val routes = jsonObject.getJSONArray("routes")
                Log.e("test9","location direction4")

                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                    val path = decodePolys(points)
                    Log.e("test10","location direction5")
                    callback(path)
                    Log.e("test11","path"+path)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun decodePolys(encoded: String): List<LatLng> {
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
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if (result and 1 != 0) -(result shr 1) else (result shr 1)
            lat += dLat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            }
            while (b >= 0x20)
            val dLng = if (result and 1 != 0) -(result shr 1) else (result shr 1)
            lng += dLng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }
    private fun showCustomDialog(){
        val dialogBuilder = AlertDialog.Builder(this, R.style.DialogStyle)
        val view = layoutInflater.inflate(R.layout.custom_dialog_checkin, null)
        dialogBuilder.setView(view)
        val dialog = dialogBuilder.create()

        val title = view.findViewById<TextView>(R.id.tv_custom_title)
        val message = view.findViewById<TextView>(R.id.tv_message)
        val btn_yes = view.findViewById<Button>(R.id.btn_yes)
        val btn_no = view.findViewById<Button>(R.id.btn_no)

        title.text = "Check-In"
        message.text = "Do you Want to Check-In?"
        btn_yes.setOnClickListener { V: View? ->
            saveCheckIn()
            finish()
            dialog.dismiss()
        }

        btn_no.setOnClickListener { V: View? ->

            dialog.dismiss()
        }


        dialog.setCancelable(true)
        dialog.show()
    }

    private fun saveCheckIn(){

        if(Constant.isNetworkAvailable(applicationContext)) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("name", customerName)
            jsonObject.addProperty("id", customerId)
            jsonObject.addProperty("Addr", customerAddr)
            jsonObject.addProperty("lat", latitude)
            jsonObject.addProperty("long", longutidue)
            jsonObject.addProperty("mydayplanId", mydayplanId)
            jsonObject.addProperty("taskId", taskId)
            jsonObject.addProperty("checkInDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("spId", SecureStorage.getString(applicationContext,StringConstants.SP_ID))
            visitViewmodel.saveCheckIn(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,jsonObject)

        }else{
            Toast.makeText(applicationContext,"Please check your network connection",Toast.LENGTH_SHORT).show()
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode ==PermissionUtil.LOCATION_PERMISSION_CODE){

            PermissionUtil.handlePermissionsResult(requestCode, grantResults) { granted ->
                if (granted) {
                    Toast.makeText(this, "Location Permission Granted!", Toast.LENGTH_SHORT).show()
                    // getLogin(etUserName.text.toString(),etPassword.text.toString(),TimeUtil.getCurrentTime(TimeUtil.FORMAT))
                    getContinuousCurrentLocation()
                } else {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(this)
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