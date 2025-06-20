package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class TaskModel(@SerializedName("Task_Id") val taskId:String,
                     @SerializedName("Task_Name")  val taskName:String,
                     @SerializedName("Assigned_To_Name")  val spName:String,
                     @SerializedName("Assigned_To_Id")  val spId:Int,
                     @SerializedName("Cust_Name")  val custNm:String,
                     @SerializedName("Cust_Id")  val custId:Int,
                     @SerializedName("Cust_Addr")  val custAddress:String,
                     @SerializedName("Cust_Lat")  val custLat:String,
                     @SerializedName("Cust_Long")  val custLong:String,
                     @SerializedName("End_Date")  val taskEndDt:String,
                     @SerializedName("task_status")  val taskstatus:Int,
                     @SerializedName("Route_Id")  val routeId:Int,
                     @SerializedName("Route_Name")  val routeName:String,
                     @SerializedName("Start_Date")  val taskSrtDate:String,
                     @SerializedName("Task_Detail")  val taskDetail:String,
                     @SerializedName("Completed_Date") var completeDt: String = "",
                     @SerializedName("check_in_time") var checkInTime: String = "",
                     @SerializedName("check_out_time") var checkOutTime: String = "",
                     @SerializedName("followup_date") var followupDate: String = "") {


}