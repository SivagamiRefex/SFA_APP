package com.example.sfa.data.api

import com.example.sfa.data.model.AssignFormModel
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.CheckInDataModel
import com.example.sfa.data.model.CustomModuleDataModel
import com.example.sfa.data.model.CustomerModel
import com.example.sfa.data.model.FileModel
import com.example.sfa.data.model.FormResponsesListModel
import com.example.sfa.data.model.LocationTrackModel
import com.example.sfa.data.model.LoginRequest
import com.example.sfa.data.model.LoginResponse
import com.example.sfa.data.model.MydayPlanModel
import com.example.sfa.data.model.PiechartDataModel
import com.example.sfa.data.model.RepeatCustVisitModel
import com.example.sfa.data.model.SetupDataResponse
import com.example.sfa.data.model.TaskModel
import com.example.sfa.data.model.VisitCusReportModel
import com.example.sfa.data.model.VisitCustLatLngRepModel
import com.example.sfa.presentation.viewmodel.LocationTrackViewModel
import com.google.firebase.inappmessaging.internal.ApiClient
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    suspend fun login(@Query("Email") email: String,
                      @Query("Password") password: String,
                      @Query("Login_Date") loginDate: String,
                      @Query("deviceRegId") deviceRegId: String,
                      @Query("lat") lat: String? = null,
                      @Query("long") long: String? = null): Response<BaseResponse<ArrayList<LoginResponse>>>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String,
                       @Query("spId") spId: String,
                       @Query("date") date: String,
                       @Query("spType") type:Int):Response<BaseRespWithoutData>

    @GET("setup")
    suspend fun getSetupData(@Header("Authorization") token: String): Response<BaseResponse<ArrayList<SetupDataResponse>>>

    @POST("syncdata")
    @FormUrlEncoded
    suspend fun getSyncDetails(@Header("Authorization") token: String,@Field("data") data: JsonObject?, @Query("axn") ax: String?, @Query("Sp_Id") spId: String?, @Query("Sp_Type") spType: Int?, @Query("Date")date:String
    ): Response<ResponseBody>

    @POST("save/customer")
    @FormUrlEncoded
    fun saveCustomerOld(@Header("Authorization") token: String,@Field("data") data:JsonObject): Response<BaseResponse<ArrayList<CustomerModel>>>

    @POST("savecustomer")
    @FormUrlEncoded
    suspend fun saveCustomer(@Header("Authorization") token: String, @Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("getcustomer")
    suspend fun getCustomer(@Header("Authorization") token: String, @Body data:JsonObject):Response<ResponseBody>

    @POST("savetask")
    @FormUrlEncoded
    suspend fun saveTask(@Header("Authorization") token: String, @Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("saveroute")
    @FormUrlEncoded
    suspend fun saveRoute(@Header("Authorization") token: String, @Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("getpendingtask")
    suspend fun getpendingTask(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String, @Query("spType") type: Int): Response<BaseResponse<ArrayList<TaskModel>>>

    @POST("savemydayplan")
    @FormUrlEncoded
    suspend fun saveMydayPlan(@Header("Authorization") token: String,@Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("cancelmydayplan")
    @FormUrlEncoded
    suspend fun cancelMydayPlan(@Header("Authorization") token: String,@Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("getmydayplan")
    suspend fun getMydayPlan(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String) :Response<BaseResponse<MydayPlanModel>>

    @POST("savecheckin")
    @FormUrlEncoded
    suspend fun saveCheckIn(@Header("Authorization") token: String,@Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("gettodaycheckin")
    suspend fun getTodayCheckIn(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String) :Response<BaseResponse<CheckInDataModel>>

    @POST("savecheckout")
    @FormUrlEncoded
    suspend fun saveCheckOut(@Header("Authorization") token: String,@Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("getcompletetask")
    suspend fun getCompleteTask(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String, @Query("spType") type: Int): Response<BaseResponse<ArrayList<TaskModel>>>

    @POST("getfilelist")
    suspend fun getFileList(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String, @Query("spType") type: Int): Response<BaseResponse<ArrayList<FileModel>>>

    @POST("getformlist")
    @FormUrlEncoded
    suspend fun getFormList(@Header("Authorization") token: String,@Field("data") data:JsonObject):Response<BaseResponse<ArrayList<CustomModuleDataModel>>>
    @POST("getvisitcustlist")
    suspend fun getVisitCustomerList(@Header("Authorization") token: String,
                             @Query("Sp_Id") spId:String,
                             @Query("FromDate") fromdate:String,
                             @Query("ToDate") todate:String):Response<BaseResponse<ArrayList<VisitCusReportModel>>>
    @POST("getvisitcustmaplist")
    suspend fun getVisitCustLatLngList(@Header("Authorization") token: String,
                               @Query("Sp_Id") spId:String,
                               @Query("Date") fromdate:String):Response<BaseResponse<ArrayList<VisitCustLatLngRepModel>>>

    @POST("getcustomfielddata")
    suspend fun getCustomFieldData(@Header("Authorization") token: String,
                                     @Query("Sp_Id") spId:String,
                                     @Query("Module_Id") moduleId:Int):Response<ResponseBody>

    @Multipart
    @POST("uploadimage") // Change to your endpoint
    suspend fun uploadImage(@Header("Authorization") token: String, @Part image: MultipartBody.Part, @Part("description") description: RequestBody
    ): Response<BaseRespWithoutData>



    @Multipart
    @POST("savecutomform")
    suspend fun saveCustomForm(
        @Header("Authorization") token: String,
        @Part("data") data: RequestBody?,
        @Query("Sp_Id") spId: String?
    ): Response<BaseRespWithoutData>

    @POST("savecreationform")
    @FormUrlEncoded
    suspend fun saveFormCreation(@Header("Authorization") token: String,@Field("data") data:JsonObject): Response<BaseRespWithoutData>

    @POST("getformresponseslist")
    suspend fun getFormResponsesList(@Header("Authorization") token: String,
                        @Query("Sp_Id") spId:String,
                        @Query("Sp_Type") spType:Int):Response<BaseResponse<ArrayList<FormResponsesListModel>>>


    @POST("getformresponsedetails")
    suspend fun getFormResponseDetails(@Header("Authorization") token: String,
                             @Query("ModuleId") id:Int,
                             @Query("EntryId") entryId:String,
                             @Query("SpId") spId:String):Response<ResponseBody>


    @POST("getformwithassignee")
    suspend  fun getFormWithAssignee(@Header("Authorization") token: String,
                        @Query("Sp_Id") spId:String,
                        @Query("Sp_Type") spType:Int):Response<BaseResponse<ArrayList<AssignFormModel>>>


    @Multipart
    @POST("saveformassignee")
    suspend  fun saveFormAssignee(@Header("Authorization") token: String,
                                      @Part("data") data: RequestBody?,
                                      @Query("Sp_Id") spId: String?)
    : Response<BaseRespWithoutData>


    @POST("gettodayfollowuptask")
    suspend fun getTodayFollowupTask(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String, @Query("spType") type: Int): Response<BaseResponse<ArrayList<TaskModel>>>

    @POST("getupcomingfollowuptask")
    suspend fun getUpcomingFollowupTask(@Header("Authorization") token: String, @Query("spId") id: String, @Query("date") date: String, @Query("spType") type: Int): Response<BaseResponse<ArrayList<TaskModel>>>


    @POST("getpiechartcustdata")
    suspend fun getPiechartCustData(@Header("Authorization") token: String, @Query("spId") id: String, @Query("spType") type: Int, @Query("date") date: String, @Query("todate") todate: String): Response<BaseResponse<PiechartDataModel>>

    @POST("getrepeatvisitcustlist")
    suspend fun getRepeatCustVisitList(@Header("Authorization") token: String,
                                       @Query("spId") spId:String,
                                       @Query("date") fromdate:String,
                                       @Query("month") month:Int,
                                       @Query("year") year:Int):Response<BaseResponse<ArrayList<RepeatCustVisitModel>>>


    @Multipart
    @POST("updatelivelocation")
    suspend fun updateLocation(@Header("Authorization") token: String,@Part("data") data: RequestBody?, @Query("spId") sfCode: String,
    ):  Response<BaseRespWithoutData>

    @POST("getloctracklist")
    suspend fun getLocationTrackList(@Header("Authorization") token: String,
                             @Query("Sp_Id") spId:String,
                             @Query("Date") fromdate:String): Response<BaseResponse<ArrayList<LocationTrackModel>>>
}