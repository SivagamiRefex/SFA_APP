package com.example.sfa.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.sfa.data.repository.AuthRepository
import com.example.sfa.utils.Resource
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.ResponseBody
import javax.inject.Inject


@HiltViewModel
class MasterSyncViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Now this returns Resource directly, not via LiveData
    suspend fun syncDetails(
        token: String,
        data: JsonObject,
        axn: String,
        spId: String,
        spType: Int,
        date: String
    ): Resource<ResponseBody> {
        return repository.syncData(token, data, axn, spId, spType, date)
    }
}