package com.example.sfa.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import java.util.Locale

object LocationAddrUtils {

    @JvmStatic
    fun getCompleteAddressString(context: Context, latitude: Double, longitude: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder()
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return strAdd
    }

    @JvmStatic
    fun getAddress(context: Context, latitude: Double, longitude: Double): String {
        val result = StringBuilder()
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            try {
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val stringArray = address.getAddressLine(0).split(",")
                    for (s in stringArray) {
                        result.append(s)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result.toString()
    }
}