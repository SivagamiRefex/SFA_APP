package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.MydayPlanModel
import com.example.sfa.data.model.SetupDataResponse
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MydayplanViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _saveMydayplanState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveMydayplanState: LiveData<Resource<BaseRespWithoutData>> =_saveMydayplanState

    private val _cancelMydayplanState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val cancelMydayplanState: LiveData<Resource<BaseRespWithoutData>> =_cancelMydayplanState

    private val _getMydayplanState=MutableLiveData<Resource<BaseResponse<MydayPlanModel>>>()
    val getMydayplanState: LiveData<Resource<BaseResponse<MydayPlanModel>>> = _getMydayplanState

    fun saveMydayplan(token:String,data:JsonObject){
        viewModelScope.launch {
            _saveMydayplanState.value = Resource.Loading()
            val result = repository.saveMydayplan(token,data)
            _saveMydayplanState.value = result
        }
    }

    fun cancelMydayplan(token: String,data:JsonObject){
        viewModelScope.launch {
            _cancelMydayplanState.value = Resource.Loading()
            val result = repository.cancelMydayplan(token,data)
            _cancelMydayplanState.value = result
        }

    }

    fun getMydayplan(token: String,spId:String,date:String){
        viewModelScope.launch {
            _getMydayplanState.value = Resource.Loading()
            val result = repository.getMydayplan(token,spId,date)


            _getMydayplanState.value = result


        }

    }
}