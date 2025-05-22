package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(private val repository: AuthRepository,
@ApplicationContext private val context: Context, ) : ViewModel() {

    private val _syncState= MutableLiveData<Resource<ResponseBody>>()
    val syncState:LiveData<Resource<ResponseBody>> = _syncState
    fun syncDetails(token:String,data:JsonObject,axn:String,spId:String,spType:Int,date:String){
        viewModelScope.launch {
            _syncState.value=Resource.Loading()
            val result=repository.syncData(token,data,axn,spId,spType,date)
            _syncState.value=result
        }
    }
}