package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _saveRouteState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveRouteState: LiveData<Resource<BaseRespWithoutData>> =_saveRouteState

    fun saveRoute(token:String,data: JsonObject){
        viewModelScope.launch {
            _saveRouteState.value = Resource.Loading()
            val result = repository.saveRoute(token,data)
            _saveRouteState.value = result


        }
    }
}