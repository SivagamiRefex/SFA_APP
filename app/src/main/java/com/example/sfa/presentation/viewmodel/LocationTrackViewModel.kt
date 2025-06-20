package com.example.sfa.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.LocationTrackModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class LocationTrackViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel()  {


    private val _updateLocationState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val updateLocationState: LiveData<Resource<BaseRespWithoutData>> =_updateLocationState

    private val _getLocTrackState = MutableLiveData<Resource<BaseResponse<ArrayList<LocationTrackModel>>>>()
    val getLocTrackState: LiveData<Resource<BaseResponse<ArrayList<LocationTrackModel>>>> =_getLocTrackState

    fun updateLocation(token:String, data:RequestBody,  spId:String){
        viewModelScope.launch {
            _updateLocationState.value = Resource.Loading()
            val result = repository.saveLiveLoction(token,data,spId)
            _updateLocationState.value = result

        }
    }

    fun getLocTrackList(token:String,spId:String,date:String){
        viewModelScope.launch {
            _getLocTrackState.value = Resource.Loading()
            val result = repository.getLocTrackList(token,spId,date)
            _getLocTrackState.value = result
        }
    }
}