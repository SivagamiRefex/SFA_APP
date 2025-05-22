package com.example.sfa.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.example.sampleapp.sqlite.DBController
import com.google.gson.Gson
import com.google.gson.JsonArray
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


object Constant {


    var count: Int = 0
    var DEBUG_MODE: Boolean = true
    var isFirstTimeSynAll: Boolean = false




   fun isNetworkAvailable(context: Context?): Boolean {
        var isConnected = false
        if (context != null) {
            val connectivityManager =
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            if (connectivityManager.activeNetworkInfo != null) {
                isConnected =
                    connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
                        .isConnected
            }
        }
        return isConnected
    }


    fun toRequestBody(value: String?): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value!!)
    }

    fun toRequestBody(value: JsonArray): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value.toString())
    }
    fun getLatitude(location: Location?): Double {
        if (location != null) {
            return location.latitude
        }
        return 0.0
    }

    fun getLongitude(location: Location?): Double {
        if (location != null) {
            return location.longitude
        }
        return 0.0
    }


    fun meterDistanceBetweenPoints(
        lat_a: Double,
        lng_a: Double,
        lat_b: Double,
        lng_b: Double
    ): Double {
        val pk = (180f / Math.PI).toFloat()

        val a1 = lat_a / pk
        val a2 = lng_a / pk
        val b1 = lat_b / pk
        val b2 = lng_b / pk

        val t1 = cos(a1) * cos(a2) * cos(b1) * cos(b2)
        val t2 = cos(a1) * sin(a2) * cos(b1) * sin(b2)
        val t3 = sin(a1) * sin(b1)
        val tt = acos(t1 + t2 + t3)

        return 6366000 * tt
    }



    fun isValidPhone(phone: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(phone)) {
            false
        } else {
            Patterns.PHONE.matcher(phone).matches()
        }
    }

    fun getSetup(key: String?, defaultValue: Int, dbController: DBController?,context: Context?): Int {
        var dbController: DBController? = dbController
        if (dbController == null) dbController = context?.let { DBController(it) }

        var value = defaultValue
        var valueStr = ""
        try {
            valueStr = Constant.getResponseFromArrayValue(
                dbController?.getResponse(StringConstants.SETUP_DATA), key
            )
            if (valueStr != "" && valueStr != "null") value = valueStr.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return value
    }


    fun getSetup(key: String?, defaultValue: String?, dbController: DBController?,context: Context?): String? {
        var dbController: DBController? = dbController
        if (dbController == null) dbController = context?.let { DBController(it) }

        var value = defaultValue
        var valueStr = ""
        try {
            valueStr = Constant.getResponseFromArrayValue(
                dbController?.getResponse(StringConstants.SETUP_DATA), key
            )
            if (valueStr != "" && valueStr != "null") value = valueStr
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return value
    }

    fun getResponseFromArrayValue(response: String?, key: String?): String {
        var value = ""
        if (response == null || response == "" || key == null || key == "" || key == "null") return value
        var jsonArray:JSONArray=JSONArray()

        if(isJsonArray(response)) {
            jsonArray=JSONArray(response)
        }else{
            val gson = Gson()
            val json = gson.toJson(response)
            jsonArray=JSONArray(json)
        }
        try {
           // val jsonArray = JSONArray(jsonList)
            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                if (jsonObject.has(key)) value = jsonObject.getString(key)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        return value
    }

    fun getResponseFromObjectValue(response: String?, key: String?): String {
        Log.v(
            "data",
            "getResponseFromObjectValue: response $response"
        )

        var value = ""
        if (response == null || response == "" || key == null || key == "") return value

        try {
            val jsonObject = JSONObject(response)
            if (jsonObject.has(key)) value = jsonObject.getString(key)
        } catch (e: JSONException) {
            e.printStackTrace()
            return value
        }


        return value
    }


    fun isJsonArray(response: String): Boolean {
        return try {
            JSONArray(response)
            true
        } catch (e: JSONException) {
            false
        }
    }


    fun getMonth(dateString:String):Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        val month = calendar.get(Calendar.MONTH) + 1
        return month
    }

    fun getYear(dateString:String):Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        return year
    }


}