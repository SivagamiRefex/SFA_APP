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
import com.example.sfa.databinding.ItemPendingTaskBinding
import com.example.sfa.presentation.ui.listener.OnTaskClickListener
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.google.android.material.card.MaterialCardView

class PendingTaskAdapter (private var context: Context, private var taskList: ArrayList<TaskModel>
) : RecyclerView.Adapter<PendingTaskAdapter.TaskViewHolder>() {
    var onTaskClickListener: OnTaskClickListener? = null

    inner class TaskViewHolder(val binding: ItemPendingTaskBinding) : RecyclerView.ViewHolder(binding.root)

    fun setList( taskkList: ArrayList<TaskModel>){
         taskList= taskkList
         notifyDataSetChanged()
    }
    fun setOnClickListener(onlistener: OnTaskClickListener?) {
        this.onTaskClickListener = onlistener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemPendingTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        if(SecureStorage.getInt(context,StringConstants.SP_TYPE)==2){
            holder.binding.tvAssignedTo.visibility=View.VISIBLE
            holder.binding.btnMydayplan.visibility=View.GONE
        }else if(SecureStorage.getInt(context,StringConstants.SP_TYPE)==3){
           if( (SecureStorage.getString(context,StringConstants.SP_ID)!!).equals(task.spId)) {
               holder.binding.tvAssignedTo.visibility = View.VISIBLE
               holder.binding.btnMydayplan.visibility = View.VISIBLE

               if (task.taskstatus == 1) {
                   holder.binding.tvStatus.visibility = View.GONE
                   holder.binding.btnMydayplan.text = "Cancel"
                   (holder.binding.root as CardView).setCardBackgroundColor(
                       ContextCompat.getColor(
                           context,
                           R.color.light_report_header
                       )
                   )
                   holder.binding.btnMydayplan.visibility = View.VISIBLE
               } else if (task.taskstatus == 2) {
                   holder.binding.tvStatus.visibility = View.VISIBLE
                   (holder.binding.root as CardView).setCardBackgroundColor(
                       ContextCompat.getColor(
                           context,
                           R.color.light_yellow
                       )
                   )
                   holder.binding.btnMydayplan.visibility = View.GONE
               } else {
                   holder.binding.btnMydayplan.text = "Mydayplan"
                   (holder.binding.root as CardView).setCardBackgroundColor(
                       ContextCompat.getColor(
                           context,
                           R.color.white
                       )
                   )
                   holder.binding.tvStatus.visibility = View.GONE
                   holder.binding.btnMydayplan.visibility = View.VISIBLE
               }
           }else{
               holder.binding.tvAssignedTo.visibility=View.VISIBLE
               holder.binding.btnMydayplan.visibility=View.GONE
           }

        }else {
            holder.binding.tvAssignedTo.visibility=View.GONE
            if(task.taskstatus==1) {
                holder.binding.tvStatus.visibility = View.GONE
                holder.binding.btnMydayplan.text="Cancel"
                (holder.binding.root as CardView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_report_header))
                holder.binding.btnMydayplan.visibility=View.VISIBLE
            }else if(task.taskstatus==2){
                holder.binding.tvStatus.visibility = View.VISIBLE
                (holder.binding.root as CardView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_yellow))
                holder.binding.btnMydayplan.visibility=View.GONE
            }else{
                holder.binding.btnMydayplan.text="Mydayplan"
                (holder.binding.root as CardView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                holder.binding.tvStatus.visibility = View.GONE
                holder.binding.btnMydayplan.visibility=View.VISIBLE
            }

        }
        holder.binding.btnMydayplan.setOnClickListener{
            onTaskClickListener?.onClickItem(task,position)
        }
        with(holder.binding) {
            tvTaskName.text = task.taskName
            tvAssignedTo.text = task.spName
            tvCustomerName.text=task.custNm
            tvEndDate.text=task.taskEndDt
        }
    }

}