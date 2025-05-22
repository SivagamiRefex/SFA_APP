package com.example.sfa.data.model

import org.json.JSONArray
import org.json.JSONObject

data class BaseResponse<T>(
val status: Boolean,
val message: String,
val data: T?//,
//val  dataArray:JSONArray
)
