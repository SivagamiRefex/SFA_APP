package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.AssignFormModel
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.CustomModuleDataModel
import com.example.sfa.data.model.FormResponsesListModel
import com.example.sfa.data.model.MydayPlanModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _getFormState= MutableLiveData<Resource<BaseResponse<ArrayList<CustomModuleDataModel>>>>()
    val getFormListState: LiveData<Resource<BaseResponse<ArrayList<CustomModuleDataModel>>>> = _getFormState

    private val _saveCustomformState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveCustomformState: LiveData<Resource<BaseRespWithoutData>> =_saveCustomformState

    private val _saveCrationformState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveCreationformState: LiveData<Resource<BaseRespWithoutData>> =_saveCrationformState

    private val _getFormResponsesListState= MutableLiveData<Resource<BaseResponse<ArrayList<FormResponsesListModel>>>>()
    val getFormResponsesListState: LiveData<Resource<BaseResponse<ArrayList<FormResponsesListModel>>>> = _getFormResponsesListState

    private val _getFormWithAssigneeState= MutableLiveData<Resource<BaseResponse<ArrayList<AssignFormModel>>>>()
    val getFormWithAssigneeState: LiveData<Resource<BaseResponse<ArrayList<AssignFormModel>>>> = _getFormWithAssigneeState


    private val _saveFormAssigneeState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveFormAssigneeState: LiveData<Resource<BaseRespWithoutData>> =_saveFormAssigneeState



    fun getFormList(token: String,data:JsonObject){
        viewModelScope.launch {
            _getFormState.value = Resource.Loading()
            val result = repository.getFormList(token,data)
            _getFormState.value = result
        }

    }

    suspend fun getCustomFieldData(
        token: String,spId:String,moduleId:Int
    ): Resource<ResponseBody> {
        return repository.getCustomFieldData(token, spId,moduleId)
    }

    fun saveCustomForm(token:String,data:RequestBody,spId:String){
        viewModelScope.launch {
            _saveCustomformState.value = Resource.Loading()
            val result = repository.saveCustomForm(token,data,spId)
            _saveCustomformState.value = result
        }

    }
    fun saveCreationForm(token:String,data:JsonObject){
        viewModelScope.launch {
            _saveCrationformState.value = Resource.Loading()
            val result = repository.saveFormCreation(token,data)
            _saveCrationformState.value = result
        }

    }

    fun getFormResponsesList(token: String,spId:String,spType:Int){
        viewModelScope.launch {
            _getFormResponsesListState.value = Resource.Loading()
            val result = repository.getFormResponsesList(token,spId,spType)
            _getFormResponsesListState.value = result
        }

    }

    suspend fun getFormResponseDetails(
        token: String,moduleId:Int,entryId:String,spId:String
    ): Resource<ResponseBody> {
        return repository.getFormResponseDetails(token,moduleId, entryId, spId)
    }



    fun getFormWithAssignee(token: String,spId:String,spType:Int){
        viewModelScope.launch {
            _getFormWithAssigneeState.value = Resource.Loading()
            val result = repository.getFormWithAssignee(token,spId,spType)
            _getFormWithAssigneeState.value = result
        }

    }

    fun saveFormAssignee(token:String,data:RequestBody,spId:String){
        viewModelScope.launch {
            _saveFormAssigneeState.value = Resource.Loading()
            val result = repository.saveFormAssignee(token,data,spId)
            _saveFormAssigneeState.value = result
        }

    }
}