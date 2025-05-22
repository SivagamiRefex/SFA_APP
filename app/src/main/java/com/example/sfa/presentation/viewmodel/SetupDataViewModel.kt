package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.LoginResponse
import com.example.sfa.data.model.SetupDataResponse
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel

class SetupDataViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _setupDataState = MutableLiveData<Resource<BaseResponse<ArrayList<SetupDataResponse>>>>()
    val setupDataState: LiveData<Resource<BaseResponse<ArrayList<SetupDataResponse>>>> = _setupDataState

    fun getSetupData(token:String){
        viewModelScope.launch {
            _setupDataState.value = Resource.Loading()
            val result = repository.setupData(token)
            _setupDataState.value = result


        }
    }

}