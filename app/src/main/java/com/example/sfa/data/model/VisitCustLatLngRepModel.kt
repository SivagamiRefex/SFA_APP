package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

class VisitCustLatLngRepModel(@SerializedName("Cust_Id") val custId : String,
                              @SerializedName("Cust_Name") val custNm : String,
                              @SerializedName("LatLng") val latLng : String,
                              @SerializedName("In_latitude") val InLat : String,
                              @SerializedName("In_Longitude") val InLong : String)