package com.example.sfa.utils

import android.util.Log
import com.example.sfa.data.model.BaseResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response
import java.io.IOException

sealed  class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String) : Resource<T>(null, message)
    class Loading<T> : Resource<T>()

}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            Resource.Success(response.body())
        }else if(response.code()==400){
            val errorBody = response.errorBody()?.string()
            val message = try {
                val json = Gson().fromJson(errorBody, JsonObject::class.java)
                json["message"]?.asString ?: "API Error"
            } catch (e: Exception) {
                "API Error: ${response.message()}"
            }
            Resource.Error(message)
        } else{
            Resource.Error("API Error: ${response.message()}")
        }
    } catch (e: IOException) {
        Resource.Error("Network Error: ${e.message}")
    } catch (e: Exception) {
        Log.e("safeapicall error:","Error: ${e.message}")
        Resource.Error("Unexpected Error: ${e.message}")

    }
}