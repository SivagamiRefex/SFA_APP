package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class CustomerDataModel(
    @SerializedName("Cust_Id")
    var CustId: String? = null,
    @SerializedName("Cust_Name")
    var CustName: String? = null,
    @SerializedName("Cust_Billing_Address")
    var CustBillingAddress : String? = null,
    @SerializedName("Loc_Latitude")
    var LocLatitude: String? = null,
    @SerializedName("Loc_Longitude")
    var LocLongitude: String? = null)
