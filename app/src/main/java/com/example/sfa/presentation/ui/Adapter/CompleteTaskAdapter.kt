package com.example.sfa.presentation.ui.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.R
import com.example.sfa.data.model.TaskModel
import com.example.sfa.databinding.ItemCompleteTaskBinding
import com.example.sfa.databinding.ItemPendingTaskBinding
import com.example.sfa.presentation.ui.listener.OnTaskClickListener
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants

class CompleteTaskAdapter(private var context: Context, private var taskList: ArrayList<TaskModel>
) : RecyclerView.Adapter<CompleteTaskAdapter.TaskViewHolder>() {
    var onTaskClickListener: OnTaskClickListener? = null

    inner class TaskViewHolder(val binding: ItemCompleteTaskBinding) : RecyclerView.ViewHolder(binding.root)

    fun setList( taskkList: ArrayList<TaskModel>){
        taskList= taskkList
        notifyDataSetChanged()
    }
    fun setOnClickListener(onlistener: OnTaskClickListener?) {
        this.onTaskClickListener = onlistener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemCompleteTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        if(SecureStorage.getInt(context, StringConstants.SP_TYPE)==2){
            holder.binding.tvAssignedTo.visibility= View.VISIBLE
        }else{
            holder.binding.tvAssignedTo.visibility= View.GONE
        }
        holder.binding.btnMydayplan.visibility= View.VISIBLE

        holder.binding.ivPreview.setOnClickListener{
            onTaskClickListener?.onClickItem(task,position)
        }
        with(holder.binding) {
            tvTaskName.text = task.taskName
            tvAssignedTo.text = task.spName
            tvCustomerName.text=task.custNm
            tvCompleteDate.text=task.completeDt
        }
    }

}