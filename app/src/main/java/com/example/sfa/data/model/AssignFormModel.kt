package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class AssignFormModel(@SerializedName("ModuleId") val ModuleId:Int,
                           @SerializedName("ModuleName") val ModuleName:String,
                           @SerializedName("AssignedPerson") val AssignedPerson:String,
                           @SerializedName("AssignedSite") val AssignedSite:String)
