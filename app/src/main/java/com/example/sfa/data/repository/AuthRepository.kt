package com.example.sfa.data.repository

import android.content.Context
import android.net.Uri
import com.example.sampleapp.utils.FileUtils
import com.example.sfa.data.api.ApiService
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.CustomerModel
import com.example.sfa.data.model.LoginRequest
import com.example.sfa.utils.safeApiCall
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun login(username: String, password: String, loginDate: String, deviceRegId: String, lat: String?, long: String?) = safeApiCall {
        api.login(username, password,loginDate,deviceRegId,lat,long)

    }
    suspend fun logout(token:String,spId: String,date : String, spType: Int) = safeApiCall {
        api.logout("Bearer $token",spId, date,spType)

    }

    suspend fun setupData(token:String)= safeApiCall {
        api.getSetupData("Bearer $token")
    }

    suspend fun syncData(token:String,data:JsonObject,axn:String,spId:String,spType:Int,date:String)= safeApiCall {
            api.getSyncDetails("Bearer $token",data,axn,spId,spType,date)!!
    }

    suspend fun  saveCustomerOld(token:String,data:JsonObject)= safeApiCall {
        api.saveCustomerOld("Bearer $token",data)
    }

    suspend fun  saveCustomer(token:String,data:JsonObject)= safeApiCall {
        api.saveCustomer("Bearer $token",data)
    }

    suspend fun  getCustomer(token:String,data:JsonObject)= safeApiCall {
        api.getCustomer("Bearer $token",data)
    }
    suspend fun  saveTask(token:String,data:JsonObject)= safeApiCall {
        api.saveTask("Bearer $token",data)
    }

    suspend fun  saveRoute(token:String,data:JsonObject)= safeApiCall {
        api.saveRoute("Bearer $token",data)
    }
    suspend fun getPendingTask(token:String,spId: String,date: String,spType: Int)= safeApiCall {
        api.getpendingTask("Bearer $token",spId,date,spType)
    }
    suspend fun saveMydayplan(token:String,data:JsonObject)= safeApiCall {
        api.saveMydayPlan("Bearer $token",data)
    }

    suspend fun cancelMydayplan(token:String,data:JsonObject)= safeApiCall {
        api.cancelMydayPlan("Bearer $token",data)
    }

    suspend fun getMydayplan(token:String,spId: String,date: String)= safeApiCall {
        api.getMydayPlan("Bearer $token",spId,date)
    }

    suspend fun saveCheckIn(token:String,data:JsonObject)= safeApiCall {
        api.saveCheckIn("Bearer $token",data)
    }

    suspend fun getTodayCheckIn(token:String,spId: String,date: String)= safeApiCall {
        api.getTodayCheckIn("Bearer $token",spId,date)
    }

    suspend fun saveCheckOut(token:String,data:JsonObject)= safeApiCall {
        api.saveCheckOut("Bearer $token",data)
    }
    suspend fun getCompleteTask(token:String,spId: String,date: String,spType: Int)= safeApiCall {
        api.getCompleteTask("Bearer $token",spId,date,spType)
    }

    suspend fun getFileList(token:String,spId: String,date: String,spType: Int)= safeApiCall {
        api.getFileList("Bearer $token",spId,date,spType)
    }

    suspend fun getFormList(token:String,data:JsonObject)= safeApiCall {
        api.getFormList("Bearer $token",data)
    }
    suspend fun getVisitCustList(token:String,spId: String,fromdate: String,todate: String)= safeApiCall {
        api.getVisitCustomerList("Bearer $token",spId,fromdate,todate)
    }
    suspend fun getVisitCustMapList(token:String,spId: String,date: String)= safeApiCall {
        api.getVisitCustLatLngList("Bearer $token",spId,date)
    }

    suspend fun getCustomFieldData(token:String,spId: String,moduleId:Int)= safeApiCall {
        api.getCustomFieldData("Bearer $token",spId,moduleId)
    }

    suspend fun uploadImage(token:String, context: Context, imageUri: Uri, description: String)= safeApiCall {
        val file = File(FileUtils.getPath(context, imageUri)) // Convert URI to File
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        val descriptionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
         api.uploadImage("Bearer $token",body,descriptionBody)

    }
    suspend fun saveCustomForm(token:String,data:RequestBody,spId: String)= safeApiCall {
        api.saveCustomForm("Bearer $token",data,spId)
    }

    suspend fun saveFormCreation(token:String,data:JsonObject)= safeApiCall {
        api.saveFormCreation("Bearer $token",data)
    }
    suspend fun getFormResponsesList(token:String,spId: String,spType: Int)= safeApiCall {
        api.getFormResponsesList("Bearer $token",spId,spType)
    }

    suspend fun getFormResponseDetails(token:String,moduleId:Int,entryId: String,spId: String)= safeApiCall {
        api.getFormResponseDetails("Bearer $token",moduleId,entryId,spId)
    }

    suspend fun getFormWithAssignee(token:String,spId: String,spType : Int)= safeApiCall {
        api.getFormWithAssignee("Bearer $token",spId,spType)
    }

    suspend fun saveFormAssignee(token:String,data:RequestBody,spId: String)= safeApiCall {
        api.saveFormAssignee("Bearer $token",data,spId)
    }
    suspend fun getTodayFollowupTask(token:String,spId: String,date: String,spType: Int)= safeApiCall {
        api.getTodayFollowupTask("Bearer $token",spId,date,spType)
    }
    suspend fun getUpcomingFollowupTask(token:String,spId: String,date: String,spType: Int)= safeApiCall {
        api.getUpcomingFollowupTask("Bearer $token",spId,date,spType)
    }
    suspend fun getPiechartCustData(token:String,spId: String,spType: Int,date: String,todate: String)= safeApiCall {
        api.getPiechartCustData("Bearer $token",spId,spType,date,todate)
    }

    suspend fun getRepeatVisitCustList(token:String,spId: String,date: String,month: Int,year: Int)= safeApiCall {
        api.getRepeatCustVisitList("Bearer $token",spId,date,month,year)
    }

    suspend fun saveLiveLoction(token:String,data:RequestBody,spId: String)= safeApiCall{
        api.updateLocation("Bearer $token",data,spId)!!
    }

    suspend fun getLocTrackList(token:String,spId: String,date:String)= safeApiCall{
        api.getLocationTrackList("Bearer $token",spId,date)!!
    }

}