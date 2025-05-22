package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class VisitCusReportModel(
    @SerializedName("custNm") val custNm : String,
    @SerializedName("visitDate") val visitDate : String,
    @SerializedName("InTime") val InTime : String,
    @SerializedName("outTime") val outTime : String,
    @SerializedName("InLat") val InLat : String,
    @SerializedName("InLong") val InLong : String
)
