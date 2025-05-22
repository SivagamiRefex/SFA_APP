package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class SalesPersonModel(@SerializedName("Sp_Id") val spId : String,
                            @SerializedName("Sp_Name") val spName : String){
 var spType:String?=null
    constructor( spId: String):this(spId,""){

    }
}
