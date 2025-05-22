package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sfa.data.model.BaseResponse
import com.example.sfa.data.model.TaskModel
import com.example.sfa.data.model.VisitCusReportModel
import com.example.sfa.data.model.VisitCustLatLngRepModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class ReportVisitViewModel  @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _getMapListState = MutableLiveData<Resource<BaseResponse<ArrayList<VisitCustLatLngRepModel>>>>()
    val getMapListState: LiveData<Resource<BaseResponse<ArrayList<VisitCustLatLngRepModel>>>> = _getMapListState

    fun getMapVisitList(token: String,spId:String,fromdate: String){
        viewModelScope.launch {
            _getMapListState.value = Resource.Loading()
            val result = repository.getVisitCustMapList(token,spId,fromdate)
            _getMapListState.value = result


        }
    }

     suspend fun getVisitList(
        token: String,spId:String,fromdate: String,todate:String
    ): Resource<ResponseBody> {
        return repository.getVisitCustList(token, spId, fromdate, todate)
    }
}