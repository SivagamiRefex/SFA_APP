package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class CustomModuleDataModel( @SerializedName("ModuleId") var moduleId: Int? = null,
                                  @SerializedName("ModuleName") var moduleName : String? = null){

    @SerializedName("customerName")
    var customerName:String=""
    @SerializedName("entryDt")
    var entryDt:String?=""
    @SerializedName("entryId")
    var entryId:String?=""
    @SerializedName("spName")
    var spName:String?=""




}
