package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseRespWithoutData
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.CustomerModel
import com.example.sfa.data.model.SetupDataResponse
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _saveCustState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val saveCustState: LiveData<Resource<BaseRespWithoutData>> =_saveCustState

    fun saveCustomer(token:String,data:JsonObject){
        viewModelScope.launch {
            _saveCustState.value = Resource.Loading()
            val result = repository.saveCustomer(token,data)
            _saveCustState.value = result


        }
    }

    /*fun saveCustomer(token: String, data: JsonObject) {
        viewModelScope.launch {
            _saveCustState.value = Resource.Loading()
            val response = repository.saveCustomer(token, data)
            if (response.isSuccessful) {
                _saveCustState.value = Resource.Success(response.body())
            } else {
                _saveCustState.value = Resource.Error("APII Error: ${response.message()}")
            }
        }
    }

    fun saveCustomer(token: String, data: JsonObject) {
        viewModelScope.launch {
            _saveCustState.value = Resource.Loading()
            try {
                val response = repository.saveCustomer(token, data)

                if (response.status) {
                    _saveCustState.value = Resource.Success(response)
                } else {
                    _saveCustState.value = Resource.Error("API Error: ${response.message}")
                }
            } catch (e: Exception) {
                _saveCustState.value = Resource.Error("Network Error: ${e.localizedMessage}")
            }
        }
    }*/

}