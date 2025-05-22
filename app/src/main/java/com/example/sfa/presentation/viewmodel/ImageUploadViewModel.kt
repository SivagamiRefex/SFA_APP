package com.example.sfa.presentation.viewmodel

import android.content.Context
import android.net.Uri
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
class ImageUploadViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uploadImgState = MutableLiveData<Resource<BaseRespWithoutData>>()
    val uploadImgState: LiveData<Resource<BaseRespWithoutData>> =_uploadImgState

    fun uploadImage(token:String,context: Context,uri: Uri,description:String){
        viewModelScope.launch {
            _uploadImgState.value = Resource.Loading()
            val result = repository.uploadImage(token,context,uri,description)
            _uploadImgState.value = result


        }
    }
}