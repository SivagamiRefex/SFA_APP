package com.example.sfa.presentation.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.data.model.TaskModel
import com.example.sfa.databinding.DialogTaskDetailBinding
import com.example.sfa.databinding.FragmentHomeBinding
import com.example.sfa.databinding.FragmentPendingTaskBinding
import com.example.sfa.presentation.ui.Adapter.PendingTaskAdapter
import com.example.sfa.presentation.ui.listener.OnTaskClickListener
import com.example.sfa.presentation.viewmodel.MydayplanViewModel
import com.example.sfa.presentation.viewmodel.TaskViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class PendingTaskFragment: Fragment() {
    private var _pendingTaskBinding: FragmentPendingTaskBinding? = null
    private val binding get() = _pendingTaskBinding!!
    private val taskViewModel: TaskViewModel by viewModels()
    private val mydayplanViewModel: MydayplanViewModel by viewModels()
    lateinit var dbController: DBController
    private var taskList= ArrayList<TaskModel>()
    private lateinit var pendingTaskAdapter : PendingTaskAdapter
    var currentPlanId=""
    var currentPlanName=""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _pendingTaskBinding = FragmentPendingTaskBinding.inflate(inflater,container,false)
        val view  = binding.root

        initView(view)
        return view
    }
    fun initView(view:View){
        dbController = DBController(requireContext())
        taskViewModel.getPendingTask(
            SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!,
            SecureStorage.getString(requireContext(),StringConstants.SP_ID)!!,
            TimesUtil.getCurrentTime(TimesUtil.FORMAT1),
            SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE)!!
        )

        taskViewModel.getpdState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), "Open Task List Updated", Toast.LENGTH_SHORT).show()
                        taskList= result.data.data!!
                        pendingTaskAdapter.setList(taskList)
                       // checkSwitchPlanNd(taskList)

                    } else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    LoadingUtil.showLoading(requireContext())
                    // Show loading indicator
                }

            }
        }
        mydayplanViewModel.saveMydayplanState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), "Mydayplan Saved Successfully", Toast.LENGTH_SHORT).show()
                        taskViewModel.getPendingTask(
                            SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!,
                            SecureStorage.getString(requireContext(),StringConstants.SP_ID)!!,
                            TimesUtil.getCurrentTime(TimesUtil.FORMAT1),
                            SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE)!!
                        )
                    } else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }


                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    LoadingUtil.showLoading(requireContext())
                }

            }
        }
        mydayplanViewModel.cancelMydayplanState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                        taskViewModel.getPendingTask(
                            SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!,
                            SecureStorage.getString(requireContext(),StringConstants.SP_ID)!!,
                            TimesUtil.getCurrentTime(TimesUtil.FORMAT1),
                            SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE)!!
                        )
                    } else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    LoadingUtil.showLoading(requireContext())
                    // Show loading indicator
                }

            }
        }
        prepareRecyclerView()

    }

    private fun prepareRecyclerView() {
        pendingTaskAdapter = PendingTaskAdapter(requireContext(),taskList)
        binding.rvPendingTask.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        binding.rvPendingTask.adapter= pendingTaskAdapter
        pendingTaskAdapter.setOnClickListener(object :
            OnTaskClickListener {
                   override fun onClickItem(selectionModel: TaskModel?, position: Int) {
                       showTaskDetailDialog(requireContext(),selectionModel!!)

                   }
            }
        )

    }
    fun showTaskDetailDialog(context: Context,selectionModel: TaskModel) {
        val binding = DialogTaskDetailBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        binding.tvTaskName.text = selectionModel.taskName
        binding.tvEndDate.text =selectionModel.taskEndDt
        binding.tvStatus.text =  if(!TimesUtil.isFirstDateBeforeSecond(TimesUtil.getCurrentTime(
                TimesUtil.FORMAT1),selectionModel.taskEndDt) &&
            selectionModel.taskstatus==0 &&
            !TimesUtil.getCurrentTime(TimesUtil.FORMAT1).
            equals(selectionModel.taskEndDt)) "Overdue"
        else if(selectionModel.taskstatus==0) "Open"
        else if(selectionModel.taskstatus==1) "Working"
        else "Completed"
        binding.tvRoute.text = selectionModel.routeName
        binding.tvCustomer.text = selectionModel.custNm
        binding.tvCustomerAddr.text=selectionModel.custAddress

        binding.tvTaskDetails.text = selectionModel.taskDetail

        if(selectionModel.taskstatus==1){
            binding.btnSubmit.text="Cancel"
        }else{
            binding.btnSubmit.text="Submit"
        }

        binding.btnSubmit.setOnClickListener {

            if(selectionModel.taskstatus==1) {
                cancelMydayplan(selectionModel)
            }else{
                saveMydayplan(selectionModel)

            }
            dialog.cancel()
        }

        dialog.show()
    }

    fun cancelMydayplan(taskmodel: TaskModel){
        if(Constant.isNetworkAvailable(context)){
            val jsonObject = JsonObject()
            jsonObject.addProperty("taskId", taskmodel.taskId)
            jsonObject.addProperty("cancelledDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("spId", SecureStorage.getString(requireContext(), StringConstants.SP_ID))
            Log.e("token", "" + SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!)
            Log.e("masterSync data", "" + jsonObject.toString())
            mydayplanViewModel.cancelMydayplan(SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!, jsonObject)
        }
    }

    fun saveMydayplan(taskmodel:TaskModel){
        if(Constant.isNetworkAvailable(context)){
            val jsonObject = JsonObject()
            jsonObject.addProperty("taskId", taskmodel.taskId)
            jsonObject.addProperty("taskName", taskmodel.taskName)
            jsonObject.addProperty("routeId", taskmodel.routeId)
            jsonObject.addProperty("routeName", taskmodel.routeName)
            jsonObject.addProperty("custId", taskmodel.custId)
            jsonObject.addProperty("custName", taskmodel.custNm)
            jsonObject.addProperty("custAddr", taskmodel.custAddress)
            jsonObject.addProperty("custLat", taskmodel.custLat)
            jsonObject.addProperty("custLong", taskmodel.custLong)
            jsonObject.addProperty("endDt", taskmodel.taskEndDt)
            jsonObject.addProperty("taskDetail", taskmodel.taskDetail)
            jsonObject.addProperty("submittedDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty(
                "spId", SecureStorage.getString(requireContext(), StringConstants.SP_ID)
            )

            Log.e("token", "" + SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!)
            Log.e("masterSync data", "" + jsonObject.toString())
            mydayplanViewModel.saveMydayplan(SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!, jsonObject)
        }

    }

    fun checkSwitchPlanNd(taskList:ArrayList<TaskModel>){
        var cnt=0
        if(taskList.isNotEmpty()){
            for (list in taskList){
                if(list.taskstatus==1){

                    cnt++
                    currentPlanId=list.taskId
                    currentPlanName=list.taskName
                }
            }
        }
        if(cnt>0){
            binding.btnSwitch.visibility=View.VISIBLE
        }else{
            binding.btnSwitch.visibility=View.GONE
        }

    }

}