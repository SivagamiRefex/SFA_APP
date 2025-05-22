package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class FormResponsesListModel(@SerializedName("ModuleId") val ModuleId:Int,
                                  @SerializedName("ModuleName") val ModuleName:String,
                                  @SerializedName("FGId") val FGId:Int,
                                  @SerializedName("EntryBy") val EntryBy:String,
                                  @SerializedName("EntryDate") val EntryDate:String,
                                  @SerializedName("EntryId") val EntryId:String,
                                  @SerializedName("CustomerId") val CustomerId:Int,
                                  @SerializedName("CustomerName") val CustomerName:String,
                                  @SerializedName("SalespersonName") val SalespersonName:String)
