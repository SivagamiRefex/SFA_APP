package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.FileModel
import com.example.sfa.data.model.TaskModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductCatalogViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _getFileState = MutableLiveData<Resource<BaseResponse<ArrayList<FileModel>>>>()
    val getFileState: LiveData<Resource<BaseResponse<ArrayList<FileModel>>>> = _getFileState

    fun getFileList(token: String,spId:String,date: String,spType:Int){
        viewModelScope.launch {
            _getFileState.value = Resource.Loading()
            val result = repository.getFileList(token,spId,date,spType)
            _getFileState.value = result


        }
    }
}