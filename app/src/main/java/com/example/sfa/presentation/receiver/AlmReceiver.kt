package com.example.sfa.presentation.receiver



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.presentation.service.LocationService
import com.example.sfa.utils.AlarmUtils
import dagger.hilt.android.AndroidEntryPoint

class AlmReceiver : BroadcastReceiver() {

    private var dbController: DBController? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (dbController == null)
            dbController = DBController(context)
        Log.e("onreceive", "almreceiver")
        AlarmUtils.setRepeatingAlarm(context)
        val serviceIntent = Intent(context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e("onreceive", "almreceiver1")

            context.startForegroundService(serviceIntent)
        } else {
            Log.e("onreceive", "almreceiver2")

            context.startService(serviceIntent)
        }

    }
    companion object {
        const val TAG = "AlmReceiver"
    }
}
