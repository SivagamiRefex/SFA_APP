package com.example.sfa.presentation.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.SyncModel
import com.example.sfa.databinding.FragmentMastersyncBinding
import com.example.sfa.presentation.ui.activity.MainActivity
import com.example.sfa.presentation.viewmodel.MasterSyncViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import pl.droidsonroids.gif.GifImageView

@AndroidEntryPoint
class MasterSyncFragment:Fragment() {
    private var _syncBinding: FragmentMastersyncBinding? = null
    private val syncBinding get() = _syncBinding!!
    lateinit var dbController: DBController;
    var customerLabel:String="Customer"
    var totalSyncSize:Int=0
    var activity: Activity? = null
    private val gifImageViewArrayList = ArrayList<GifImageView>()
    private val failImageViewArrayList = ArrayList<TextView>()
    private val viewModel: MasterSyncViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _syncBinding = FragmentMastersyncBinding.inflate(inflater, container, false)
        val view = syncBinding.root

        initView(view);
        return view
    }
    fun initView(view:View){

        activity=requireActivity()

        totalSyncSize = (activity as MainActivity).masterSyncList.size
        dbController=DBController(requireActivity())
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,context)!!
        syncBinding.tvCustomer.text=customerLabel
        syncBinding.layoutToolbar.tvTitle.text="Master Sync"
        syncBinding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        syncBinding.layoutToolbar.menubtn.visibility=View.GONE



        gifImageViewArrayList.clear()
        gifImageViewArrayList.add(syncBinding.givCustomer)
        gifImageViewArrayList.add(syncBinding.givRoute)
        gifImageViewArrayList.add(syncBinding.givSalesperson)
        gifImageViewArrayList.add(syncBinding.givSource)
        gifImageViewArrayList.add(syncBinding.givFormList)
        gifImageViewArrayList.add(syncBinding.givFormData)


        failImageViewArrayList.clear()
        failImageViewArrayList.add(syncBinding.failCustomer)
        failImageViewArrayList.add(syncBinding.failRoute)
        failImageViewArrayList.add(syncBinding.failSalesperson)
        failImageViewArrayList.add( syncBinding.failSource)
        failImageViewArrayList.add(syncBinding.failFormList)
        failImageViewArrayList.add(syncBinding.failFormData)

        for (i in gifImageViewArrayList.indices) {
            val syncModel: SyncModel = (activity as MainActivity).masterSyncList.get(i)
            syncModel.gifImageView = gifImageViewArrayList[i]
            syncModel.textView=failImageViewArrayList[i]
            (activity as MainActivity).masterSyncList.set(i, syncModel)

        }

        syncBinding.cvSyncAllData.setOnClickListener(){
            if (!Constant.isNetworkAvailable(context)) {
                Toast.makeText(
                    context,
                    "Please check the internet connectivity",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                syncAll()
            }
        }

        syncBinding.tvCustomer.setOnClickListener{
            lifecycleScope.launch {

                getMasterSycDetails((activity as MainActivity).masterSyncList.get(0), 0)
            }
        }
        syncBinding.tvRoute.setOnClickListener{
            lifecycleScope.launch {

                getMasterSycDetails((activity as MainActivity).masterSyncList.get(1), 1)
            }
        }
        syncBinding.tvSalesperson.setOnClickListener{
            lifecycleScope.launch {

                getMasterSycDetails((activity as MainActivity).masterSyncList.get(2), 2)
            }
        }
        syncBinding.tvSource.setOnClickListener{
            lifecycleScope.launch {

                getMasterSycDetails((activity as MainActivity).masterSyncList.get(3), 3)
            }
        }

        syncBinding.tvFormList.setOnClickListener{
            lifecycleScope.launch {

                getMasterSycDetails((activity as MainActivity).masterSyncList.get(4), 4)
            }
        }

        syncBinding.tvFormData.setOnClickListener{
            lifecycleScope.launch {

                getMasterSycDetails((activity as MainActivity).masterSyncList.get(5), 5)
            }

        }
       if (Constant.isFirstTimeSynAll) {
            startMasterSync()
        }



    }


    private fun syncAll() {
        lifecycleScope.launch {
            for (i in 0 until totalSyncSize) {
                getMasterSycDetails((activity as MainActivity).masterSyncList.get(i), i)
            }
        }
    }



    fun startMasterSync() {
        // Optional: show a progress dialog or loading spinner here
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0 until totalSyncSize) {
                getMasterSycDetails((activity as MainActivity).masterSyncList[i], i)
            }

            withContext(Dispatchers.Main) {
                // Optional: dismiss progress dialog here
                Constant.isFirstTimeSynAll = false
               SecureStorage.setBoolean(requireContext(),StringConstants.IS_FIRST_TIME_SYNC_ALL,false)
                Log.e("data error sync", "onPostExecute: message => ")
            }
        }
    }

    suspend fun getMasterSycDetails(syncModel:SyncModel, position:Int){
        try {
            requireActivity().runOnUiThread(Runnable {
                if (syncModel.gifImageView != null)
                    syncModel.gifImageView!!.setVisibility(View.VISIBLE)
                if (syncModel.gifImageView != null) {
                    syncModel.gifImageView!!.setVisibility(View.GONE)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val temp: String = SecureStorage.getString(requireContext(), StringConstants.SP_ID)!!
        val spType: Int = SecureStorage.getInt(requireContext(), StringConstants.SP_TYPE)
        if (Constant.isNetworkAvailable(context)) {
            val result = viewModel.syncDetails(SecureStorage.getString(requireContext(),StringConstants.AUTH_TOKEN)!!,syncModel.inputValue,
                syncModel.axn, temp, spType, TimesUtil.getCurrentTime(TimesUtil.FORMAT1))
            when (result) {
                    is Resource.Success -> {
                        LoadingUtil.hideLoading()
                        if(isAdded) {
                            try {
                                var json = JSONTokener(result.data!!.string()).nextValue()
                                var jsonArray = JSONArray()

                                if (json is JSONArray) {
                                    jsonArray = JSONArray(result.data)
                                    syncModel.count = jsonArray.length()
                                } else if (json is JSONObject) {

                                    if (json.getBoolean("status")) {

                                        syncModel.isDataSynced = true

                                        if (json is JSONObject) {
                                            jsonArray = json.getJSONArray("data")
                                            syncModel.count = jsonArray.length()

                                        }
                                        // val gson = Gson()
                                        // val jsonString = gson.toJson(result.data!!)
                                        requireActivity().runOnUiThread(Runnable {
                                            if (syncModel.gifImageView != null) syncModel.gifImageView!!
                                                .setVisibility(View.INVISIBLE)
                                            if (syncModel.textView != null) {
                                                syncModel.textView!!.setVisibility(View.VISIBLE)
                                                syncModel.textView!!
                                                    .setCompoundDrawables(null, null, null, null)
                                                syncModel.textView!!
                                                    .setText(java.lang.String.valueOf(syncModel.count))
                                            }
                                        })

                                        try {
                                            if (dbController == null) dbController =
                                                DBController(requireContext())
                                            var tableName: String? = ""
                                            tableName = syncModel.tableName

                                            if (!syncModel.isLoadFromPreference) {
                                                if (!dbController.updateProduct(
                                                        tableName,
                                                        jsonArray.toString()
                                                    )
                                                ) {
                                                    dbController.addProduct(
                                                        tableName,
                                                        jsonArray.toString()
                                                    )
                                                }
                                            } else {
                                                SecureStorage.setString(
                                                    requireContext(),
                                                    tableName,
                                                    jsonArray.toString()
                                                )
                                            }
                                            var passCount = 0
                                            for (j in 0 until (activity as MainActivity).masterSyncList.size) {
                                                if ((activity as MainActivity).masterSyncList.get(j).isDataSynced) {
                                                    passCount++
                                                }
                                            }
                                            if (passCount == (activity as MainActivity).masterSyncList.size) {
                                                 val intent1 = Intent(requireContext(), MainActivity::class.java)
                                                startActivity(intent1)
                                              /*  val bpfragment = HomeFragment()
                                                (activity as MainActivity)?.supportFragmentManager?.beginTransaction()
                                                    ?.replace(R.id.container, bpfragment)?.commit()
                                                val tx3 = (activity as MainActivity)!!.supportFragmentManager.beginTransaction()
                                                tx3.replace(R.id.container, HomeFragment())
                                                tx3.addToBackStack(null)
                                                tx3.commit()*/
                                            }
                                        } catch (ex: java.lang.Exception) {
                                            ex.printStackTrace()

                                            requireActivity().runOnUiThread(Runnable {

                                                if (syncModel.gifImageView != null) {
                                                    syncModel.gifImageView!!.setVisibility(View.GONE)
                                                }
                                                if (syncModel.textView != null) {
                                                    syncModel.textView!!.setVisibility(View.VISIBLE)
                                                }
                                            })
                                        }

                                    } else {
                                        syncModel.count = 0
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("result success error", e.message.toString())
                            }
                        }

                    }

                    is Resource.Error -> {
                       // Log.e("SYNC", "$label sync failed: ${result.message}")
                       // Toast.makeText(requireContext(), "$label failed: ${result.message}", Toast.LENGTH_SHORT).show()

                        if(isAdded) {
                            Toast.makeText(
                                requireContext(),
                                result.message ?: "Error",
                                Toast.LENGTH_SHORT
                            ).show()
                            requireActivity().runOnUiThread(Runnable {
                                syncModel.isDataSynced = false
                                if (syncModel.gifImageView != null) {
                                    syncModel.gifImageView!!.setVisibility(View.GONE)
                                }
                                if (syncModel.textView != null) {
                                    syncModel.textView!!.setVisibility(View.VISIBLE)
                                }
                            })
                        }

                        LoadingUtil.hideLoading()

                    }
                is Resource.Loading -> {
                    LoadingUtil.showLoading(requireContext())
                }

                    else -> {}
            }
        } else {
            Toast.makeText(
                context,
                "Please check your network connection",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

   /* fun getSyncDetails(syncModel:SyncModel,position:Int) {


        try {
            requireActivity().runOnUiThread(Runnable {
                if (syncModel.gifImageView != null)
                    syncModel.gifImageView!!.setVisibility(View.VISIBLE)
                if (syncModel.gifImageView != null) {
                    syncModel.gifImageView!!.setVisibility(View.GONE)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val temp: String = SecureStorage.getString(requireContext(), StringConstants.SP_ID)!!
        val spType: Int = SecureStorage.getInt(requireContext(), StringConstants.SP_TYPE)

        if (Constant.isNetworkAvailable(context)) {
            viewModel.syncDetails(SecureStorage.getString(requireContext(),StringConstants.AUTH_TOKEN)!!,
                syncModel.inputValue,
                syncModel.axn, temp, spType, TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
            )
        } else {
            Toast.makeText(
                context,
                "Please check your network connection",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.syncState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {

                    try {
                        var json = JSONTokener(result.data!!.string()).nextValue()
                        var jsonArray = JSONArray()

                        if (json is JSONArray) {
                            jsonArray = JSONArray(result.data)
                            syncModel.count = jsonArray.length()
                        } else if (json is JSONObject) {

                            if (json.getBoolean("status")) {

                                syncModel.isDataSynced = true

                                if (json is JSONObject) {
                                    jsonArray = json.getJSONArray("data")
                                    syncModel.count = jsonArray.length()

                                }
                                // val gson = Gson()
                                // val jsonString = gson.toJson(result.data!!)
                                requireActivity().runOnUiThread(Runnable {
                                    if (syncModel.gifImageView != null) syncModel.gifImageView!!
                                        .setVisibility(View.INVISIBLE)
                                    if (syncModel.textView != null) {
                                        syncModel.textView!!.setVisibility(View.VISIBLE)
                                        syncModel.textView!!
                                            .setCompoundDrawables(null, null, null, null)
                                        syncModel.textView!!
                                            .setText(java.lang.String.valueOf(syncModel.count))
                                    }
                                })

                                try {
                                    if (dbController == null) dbController =
                                        DBController(requireContext())
                                    var tableName: String? = ""
                                    tableName = syncModel.tableName

                                    if (!syncModel.isLoadFromPreference) {
                                        if (!dbController.updateProduct(
                                                tableName,
                                                jsonArray.toString()
                                            )
                                        ) {
                                            dbController.addProduct(tableName, jsonArray.toString())
                                        }
                                    } else {
                                        SecureStorage.setString(
                                            requireContext(),
                                            tableName,
                                            jsonArray.toString()
                                        )
                                    }
                                    var passCount = 0
                                    for (j in 0 until (activity as MainActivity).masterSyncList.size) {
                                        if ((activity as MainActivity).masterSyncList.get(j).isDataSynced) {
                                            passCount++
                                        }
                                    }
                                    if (passCount == (activity as MainActivity).masterSyncList.size) {
                                        // val intent1 = Intent(requireContext(), MainActivity::class.java)
                                        //startActivity(intent1)
                                        val bpfragment = HomeFragment()
                                        (activity as MainActivity)?.supportFragmentManager?.beginTransaction()
                                            ?.replace(R.id.container, bpfragment)?.commit()

                                    }
                                } catch (ex: java.lang.Exception) {
                                    ex.printStackTrace()

                                    requireActivity().runOnUiThread(Runnable {

                                        if (syncModel.gifImageView != null) {
                                            syncModel.gifImageView!!.setVisibility(View.GONE)
                                        }
                                        if (syncModel.textView != null) {
                                            syncModel.textView!!.setVisibility(View.VISIBLE)
                                        }
                                    })
                                }

                            } else {
                                syncModel.count = 0
                            }
                        }
                    }catch (e:Exception){
                        Log.e("result success error",e.message.toString())
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                    requireActivity().runOnUiThread(Runnable {
                        syncModel.isDataSynced=false
                        if (syncModel.gifImageView != null) {
                            syncModel.gifImageView!!.setVisibility(View.GONE)
                        }
                        if (syncModel.textView != null) {
                            syncModel.textView!!.setVisibility(View.VISIBLE)
                        }
                    })
                }

                is Resource.Loading -> {
                    // Show loading indicator
                }

            }
        }
    }*/


}