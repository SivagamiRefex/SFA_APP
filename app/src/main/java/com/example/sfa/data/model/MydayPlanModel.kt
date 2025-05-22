package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class MydayPlanModel(@SerializedName("Mydayplan_Id") val mydayplanId:Int,
                          @SerializedName("Task_Id") val taskId:Int,
                          @SerializedName("Route_Id") val routeId:Int,
                          @SerializedName("Cust_Id") val custId:Int,
                          @SerializedName("Cust_Name") val custName:String,
                          @SerializedName("Cust_Addr") val custAddress:String,
                          @SerializedName("Cust_Lat") val custLat:String,
                          @SerializedName("Cust_Long") val custLong:String)

