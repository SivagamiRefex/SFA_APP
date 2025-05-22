package com.example.sfa.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.LoginResponse
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<BaseResponse<ArrayList<LoginResponse>>>>()
    val loginState: LiveData<Resource<BaseResponse<ArrayList<LoginResponse>>>> = _loginState
     fun login(email: String, password: String, loginDate: String, deviceRegId: String, lat: String?, long: String?){
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.login(email, password,loginDate,deviceRegId,lat,long)
            _loginState.value = result

            /*if (result is Resource.Success<*> && result.data != null) {
                Log.e("login token",result.data.data!![0].token
                )
                SecureStorage.saveToken(context, result.data.data!![0].token
                )

            }*/
        }
    }
}