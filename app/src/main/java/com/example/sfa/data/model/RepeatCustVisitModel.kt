package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class RepeatCustVisitModel(@SerializedName("Cust_Id") var custId:Int,
                                @SerializedName("Cust_Name") var custName:String,
                                @SerializedName("Cust_Address") var custAddr:String,
                                 @SerializedName("visit_count") var visitCnt:Int)
