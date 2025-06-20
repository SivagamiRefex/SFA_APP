package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.TaskModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _saveTaskState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveTaskState: LiveData<Resource<BaseRespWithoutData>> = _saveTaskState

    private val _getptState = MutableLiveData<Resource<BaseResponse<ArrayList<TaskModel>>>>()
    val getpdState: LiveData<Resource<BaseResponse<ArrayList<TaskModel>>>> = _getptState

    private val _getctState = MutableLiveData<Resource<BaseResponse<ArrayList<TaskModel>>>>()
    val getctState: LiveData<Resource<BaseResponse<ArrayList<TaskModel>>>> = _getctState

    private val _gettftState = MutableLiveData<Resource<BaseResponse<ArrayList<TaskModel>>>>()
    val gettftState: LiveData<Resource<BaseResponse<ArrayList<TaskModel>>>> = _gettftState

    private val _getuftState = MutableLiveData<Resource<BaseResponse<ArrayList<TaskModel>>>>()
    val getuftState: LiveData<Resource<BaseResponse<ArrayList<TaskModel>>>> = _getuftState


    fun saveTask(token: String, data: JsonObject) {
        viewModelScope.launch {
            _saveTaskState.value = Resource.Loading()
            val result = repository.saveTask(token, data)
            _saveTaskState.value = result


        }
    }

    fun getPendingTask(token: String,spId:String,date: String,spType:Int){
        viewModelScope.launch {
            _getptState.value = Resource.Loading()
            val result = repository.getPendingTask(token,spId,date,spType)
            _getptState.value = result


        }
    }

    fun getCompletTask(token: String,spId:String,date: String,spType:Int){
        viewModelScope.launch {
            _getctState.value = Resource.Loading()
            val result = repository.getCompleteTask(token,spId,date,spType)
            _getctState.value = result


        }
    }

    fun getTodayFollowupTask(token: String,spId:String,date: String,spType:Int){
        viewModelScope.launch {
            _gettftState.value = Resource.Loading()
            val result = repository.getTodayFollowupTask(token,spId,date,spType)
            _gettftState.value = result


        }
    }

    fun getUpcomingFollowupTask(token: String,spId:String,date: String,spType:Int){
        viewModelScope.launch {
            _getuftState.value = Resource.Loading()
            val result = repository.getUpcomingFollowupTask(token,spId,date,spType)
            _getuftState.value = result


        }
    }
}
