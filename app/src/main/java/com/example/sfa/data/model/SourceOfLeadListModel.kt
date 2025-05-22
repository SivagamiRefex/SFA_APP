package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class SourceOfLeadListModel(@SerializedName("id") val id : Int,
                                 @SerializedName("name") val name : String) {
}