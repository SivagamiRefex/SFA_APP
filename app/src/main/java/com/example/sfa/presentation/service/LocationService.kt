package com.example.sfa.presentation.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.presentation.viewmodel.ImageUploadViewModel
import com.example.sfa.presentation.viewmodel.LocationTrackViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.LocationAddrUtils
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    @Inject
    lateinit var repository: AuthRepository
    @Inject
    @ApplicationContext
    lateinit var context: Context

    private var dbController: DBController? = null
    val locationList = ArrayList<HashMap<String, String>>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fetchLocation()
        return START_NOT_STICKY

    }

    private fun fetchLocation() {
        if (dbController == null)
            dbController = DBController(this)
        val locationProvider = LocationProvider(this)
        locationProvider.getCurrentLocation { location ->
            if (location != null) {
                Log.e("LocationService", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                Log.e("onreceive", "step2")

                location?.let {
                    val lat = location.latitude
                    val lng = location.longitude
                    val geocoder = this?.let { Geocoder(it, Locale.getDefault()) }
                    val list: List<Address> =
                        geocoder?.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )!!
                    val address =list[0].getAddressLine(0)


                    dbController?.addLocation(location, "0", address)
                    SecureStorage.setString(this,StringConstants.USER_LATITUDE,lat.toString())
                    SecureStorage.setString(this, StringConstants.USER_LONGITUDE,lng.toString())
                    SecureStorage.setString(this,StringConstants.USER_ACCURACY,location.accuracy.toString())
                    SecureStorage.setString(this,StringConstants.USER_BEARING,location.bearing.toString())
                    SecureStorage.setString(this,StringConstants.USER_SPEED,(location.speed > 0).toString())




                    Log.e("onreceive", "step3")

                    if (Constant.isNetworkAvailable(this)) {
                        updateLocationToServer(this)
                    }
                    Log.e("onreceive", "step9")

                }

            }
            stopSelf() // Important to stop the service after work is done
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking location")
            .setContentText("Getting location in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun updateLocationToServer(context: Context) {
        Log.e("onreceive", "step4")

        if (dbController == null)
            dbController = DBController(context)

        val locationList = dbController?.getAllLocationData(true) ?: return
        if (locationList.isEmpty()) return

        val jsonArray = JsonArray()
        val trackObj = JsonObject().apply {
            addProperty("spId", SecureStorage.getString(context, StringConstants.SP_ID))
            addProperty("spName", SecureStorage.getString(context, StringConstants.SP_NAME))
            addProperty("DvcID", Constant.getDeviceIdNew(context))
            addProperty("battery_percent", Constant.getBatteryPercentage(context))

            val locArray = JsonArray()
            for (location in locationList) {
                try {
                    val obj = JsonObject().apply {
                        addProperty("Latitude", location[DBController.Latitude])
                        addProperty("Longitude", location[DBController.Longitude])
                        addProperty("Time", location[DBController.CurrentTime])
                        addProperty("Accuracy", location[DBController.Accuracy])
                        addProperty("Speed", location[DBController.Speed])
                        addProperty("Bearing", location[DBController.Bearing])
                        addProperty(
                            "Address", location[DBController.Address]?.takeIf { it.isNotEmpty() }
                                ?: LocationAddrUtils.getCompleteAddressString(
                                    context,
                                    location[DBController.Latitude]?.toDouble() ?: 0.0,
                                    location[DBController.Longitude]?.toDouble() ?: 0.0
                                )
                        )
                    }
                    locArray.add(obj)

                } catch (e: Exception) {
                    Log.e("onreceive", "step4" + e.message.toString())

                    e.printStackTrace()
                }
            }

            add("TLocations", locArray)
        }

        val jsonObject2 = JsonObject().apply {
            add("TrackLoction", trackObj)
        }
        jsonArray.add(jsonObject2)


        // Launch coroutine for network call
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val result = repository.saveLiveLoction( SecureStorage.getString(context, StringConstants.AUTH_TOKEN)!!,
                    Constant.toRequestBody(jsonArray),
                    SecureStorage.getString(context, StringConstants.SP_ID)!!)
                if (result is Resource.Success && result.data?.status == true) {
                    // On success, update DB and UI as needed
                    for (location in locationList) {
                        try {
                            dbController?.updateLocation(location[DBController.ID].toString())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else if (result is Resource.Error) {
                    // handle error here if needed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}