package com.example.sfa.presentation.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.data.model.TaskModel
import com.example.sfa.databinding.DialogCompleteTaskDetailBinding
import com.example.sfa.databinding.FragmentCompletedTaskBinding
import com.example.sfa.presentation.ui.Adapter.CompleteTaskAdapter
import com.example.sfa.presentation.ui.listener.OnTaskClickListener
import com.example.sfa.presentation.viewmodel.TaskViewModel
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class CompletedTaskFragment: Fragment() {
    private var _completedTaskBinding: FragmentCompletedTaskBinding? = null
    private val binding get() = _completedTaskBinding!!
    private val taskViewModel: TaskViewModel by viewModels()
    lateinit var dbController: DBController
    private var taskList= ArrayList<TaskModel>()
    private lateinit var completeTaskAdapter : CompleteTaskAdapter
    var currentPlanId=""
    var currentPlanName=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _completedTaskBinding = FragmentCompletedTaskBinding.inflate(inflater,container,false)
        val view  = binding.root
        initView(view)
        return view
    }
    fun initView(view:View){
        dbController = DBController(requireContext())
        taskViewModel.getCompletTask(
            SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!,
            SecureStorage.getString(requireContext(), StringConstants.SP_ID)!!,
            TimesUtil.getCurrentTime(TimesUtil.FORMAT1),
            SecureStorage.getInt(requireContext(), StringConstants.SP_TYPE)!!
        )
        taskViewModel.getctState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), "Completed Task List Updated", Toast.LENGTH_SHORT).show()
                        taskList= result.data.data!!
                        completeTaskAdapter.setList(taskList)
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
        prepareRecyclerView()


    }
    private fun prepareRecyclerView() {
        completeTaskAdapter = CompleteTaskAdapter(requireContext(),taskList)
        binding.rvPendingTask.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        binding.rvPendingTask.adapter= completeTaskAdapter
        completeTaskAdapter.setOnClickListener(object :
            OnTaskClickListener {
            override fun onClickItem(selectionModel: TaskModel?, position: Int) {
                showTaskDetailDialog(requireContext(),selectionModel!!)

            }
        }
        )

    }

    fun showTaskDetailDialog(context: Context, selectionModel: TaskModel) {
        val binding = DialogCompleteTaskDetailBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        binding.tvTaskName.text = selectionModel.taskName
        binding.tvEndDate.text =selectionModel.taskEndDt
       // binding.tvStatus.text = if(selectionModel.taskstatus==0) "Pending" else if(selectionModel.taskstatus==1) "In Progress" else "Completed"
        binding.tvRoute.text = selectionModel.routeName
        binding.tvCustomer.text = selectionModel.custNm
        binding.tvCustomerAddr.text=selectionModel.custAddress
        binding.tvTaskDetails.text = selectionModel.taskDetail
        binding.tvCompleteDate.text=selectionModel.completeDt
        binding.tvCheckInTime.text=selectionModel.checkInTime
        binding.tvCheckOutTime.text=selectionModel.checkOutTime
        binding.tvTotalDuration.text=getTotalInTime(selectionModel.checkInTime,selectionModel.checkOutTime)
        dialog.show()
    }

    private fun getTotalInTime(InTime: String?, outTime: String?): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
        return try {
            val date1 = format.parse(InTime ?: "") ?: return "-"
            val date2 = format.parse(outTime ?: "") ?: return "-"
            val diff = date2.time - date1.time

            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: ParseException) {
            e.printStackTrace()
            "-"
        }
    }
}