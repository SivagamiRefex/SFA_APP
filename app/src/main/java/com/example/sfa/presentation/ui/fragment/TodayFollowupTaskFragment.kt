package com.example.sfa.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.data.model.TaskModel
import com.example.sfa.databinding.FragmentCompletedTaskBinding
import com.example.sfa.databinding.FragmentTodayFollowupTaskBinding
import com.example.sfa.presentation.ui.Adapter.CompleteTaskAdapter
import com.example.sfa.presentation.ui.Adapter.FollowupTaskAdapter
import com.example.sfa.presentation.ui.listener.OnTaskClickListener
import com.example.sfa.presentation.viewmodel.TaskViewModel
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TodayFollowupTaskFragment:Fragment() {
    private var _todayFollowupTaskBinding: FragmentTodayFollowupTaskBinding? = null
    private val binding get() = _todayFollowupTaskBinding!!
    private val taskViewModel: TaskViewModel by viewModels()
    lateinit var dbController: DBController
    private var taskList= ArrayList<TaskModel>()
    private lateinit var followupTaskAdapter : FollowupTaskAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _todayFollowupTaskBinding = FragmentTodayFollowupTaskBinding.inflate(inflater,container,false)
        val view  = binding.root
        initView(view)
        return view
    }
    fun initView(view:View){
        dbController = DBController(requireContext())
        taskViewModel.getTodayFollowupTask(
            SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!,
            SecureStorage.getString(requireContext(), StringConstants.SP_ID)!!,
            TimesUtil.getCurrentTime(TimesUtil.FORMAT1),
            SecureStorage.getInt(requireContext(), StringConstants.SP_TYPE)!!
        )
        taskViewModel.gettftState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), "Today Followup Task List Updated", Toast.LENGTH_SHORT).show()
                        taskList= result.data.data!!
                        followupTaskAdapter.setList(taskList)
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
        followupTaskAdapter = FollowupTaskAdapter(requireContext(),taskList)
        binding.rvTodayFollowupTask.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        binding.rvTodayFollowupTask.adapter= followupTaskAdapter
        followupTaskAdapter.setOnClickListener(object :
            OnTaskClickListener {
            override fun onClickItem(selectionModel: TaskModel?, position: Int) {
                //showTaskDetailDialog(requireContext(),selectionModel!!)
                Log.e("selectedItem",selectionModel.toString())

            }
        }
        )

    }


}