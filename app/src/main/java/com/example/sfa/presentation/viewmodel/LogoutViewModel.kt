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
class LogoutViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _logoutState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val logoutState: LiveData<Resource<BaseRespWithoutData>> =_logoutState

    fun getLogout(token:String,spId:String,date:String,spType:Int){
        viewModelScope.launch {
            _logoutState.value = Resource.Loading()
            val result = repository.logout(token,spId,date,spType)
            _logoutState.value = result


        }
    }
}