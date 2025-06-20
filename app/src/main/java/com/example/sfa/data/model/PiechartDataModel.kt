package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class PiechartDataModel( @SerializedName("Total_Cust") val totalCust:Int,
                              @SerializedName("Assigned_Cust") val assignedCust:Int,
                              @SerializedName("Visited_Cust") val visitedCust:Int) {
}