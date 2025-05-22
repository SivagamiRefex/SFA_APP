package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class CustomerModel(@SerializedName("Cust_Id") val id: Int,
                         @SerializedName("Cust_Name") val name: String,
                         @SerializedName("Cust_Billing_Address") val billingAddress: String,
                         @SerializedName("Cust_City") val city: String,
                         @SerializedName("Cust_Phone") val phone: String,
                         @SerializedName("Cust_Email") val email: String,
                         @SerializedName("Cust_Pan_Id") val panId: String,
                         @SerializedName("Cust_Gst_No") val gstNo: String,
                         @SerializedName("Created_Date") val createdDate: String,
                         @SerializedName("Loc_Latitude") val latitude: String,
                         @SerializedName("Loc_Longitude") val longitude: String,
                         @SerializedName("Loc_Address") val locationAddress: String,
                         @SerializedName("Cust_Shipping_Address") val shippingAddress: String,
                         @SerializedName("Sp_Id") val spId: Int,
                         @SerializedName("Source_Id") val sourceId: Int,
                         @SerializedName("Source_Name") val sourceName: String,
                         @SerializedName("company_name") val companyName: String,
                         @SerializedName("cust_device_id") val deviceId: String?,
                         @SerializedName("Lastedit_Date") val lastEditDate: String?,
                         @SerializedName("Cust_Password") val password: String)
