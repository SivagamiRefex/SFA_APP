package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.CheckInDataModel
import com.example.sfa.data.model.MydayPlanModel
import com.example.sfa.data.model.PiechartDataModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerVisitViewModel  @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _saveCheckInState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveCheckInState: LiveData<Resource<BaseRespWithoutData>> =_saveCheckInState

    private val _getTodayCheckIn=MutableLiveData<Resource<BaseResponse<CheckInDataModel>>>()
    val getTodayCheckIn: LiveData<Resource<BaseResponse<CheckInDataModel>>> = _getTodayCheckIn

    private val _saveCheckOutState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveCheckOutState: LiveData<Resource<BaseRespWithoutData>> =_saveCheckOutState

    private val _getPiechartCustData=MutableLiveData<Resource<BaseResponse<PiechartDataModel>>>()
    val getPiechartCustData: LiveData<Resource<BaseResponse<PiechartDataModel>>> = _getPiechartCustData


    fun saveCheckIn(token:String,data: JsonObject){
        viewModelScope.launch {
            _saveCheckInState.value = Resource.Loading()
            val result = repository.saveCheckIn(token,data)
            _saveCheckInState.value = result
        }
    }

    fun getTodayCheckIn(token:String,spId:String,date:String){
        viewModelScope.launch {
            _getTodayCheckIn.value = Resource.Loading()
            val result = repository.getTodayCheckIn(token,spId,date)
            _getTodayCheckIn.value = result
        }
    }

    fun saveCheckOut(token:String,data: JsonObject){
        viewModelScope.launch {
            _saveCheckOutState.value = Resource.Loading()
            val result = repository.saveCheckOut(token,data)
            _saveCheckOutState.value = result
        }
    }

    fun getPiechartCustData(token: String,spId:String,spType:Int,date: String,todate: String){
        viewModelScope.launch {
            _getPiechartCustData.value = Resource.Loading()
            val result = repository.getPiechartCustData(token,spId,spType,date,todate)
            _getPiechartCustData.value = result
        }
    }
}