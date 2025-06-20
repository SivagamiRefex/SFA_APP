package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class LocationTrackModel(@SerializedName("Loc_Lat") val locLat : String,
                              @SerializedName("Loc_Long") val locLong : String,
                              @SerializedName("Submitted_Dt") val submittedDt : String,
                              @SerializedName("Loc_Addr") val locAddr : String)
