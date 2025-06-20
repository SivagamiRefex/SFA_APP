package com.example.sfa.presentation.ui.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.VisitCusReportModel
import com.example.sfa.databinding.ItemCustomerVisitReportBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class VisitCustReportAdapter : RecyclerView.Adapter<VisitCustReportAdapter.ViewHolder>() {

    private var visitList = ArrayList<VisitCusReportModel>()

    fun setCustomerList(custList: List<VisitCusReportModel>) {
        this.visitList = ArrayList(custList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerVisitReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = visitList[position]
        with(holder.binding) {
            tvName.text = item.custNm
            tvDate.text = item.visitDate
            tvIntime.text = item.InTime

            if (!item.outTime.isNullOrEmpty()) {
                tvOuttime.text = item.outTime
                tvTottime.text = getInTime(item.InTime, item.outTime)
            } else {
                tvOuttime.text = "-"
                tvTottime.text = "-"
            }

            if(item.isActivityComplete>0){
               tvActivityStatus.text="Completed"
            }else{
                tvActivityStatus.text="Not Completed"

            }
        }
    }

    override fun getItemCount(): Int = visitList.size

    class ViewHolder(val binding: ItemCustomerVisitReportBinding) : RecyclerView.ViewHolder(binding.root)

    private fun getInTime(time1: String?, time2: String?): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return try {
            val date1 = format.parse(time1 ?: "") ?: return "-"
            val date2 = format.parse(time2 ?: "") ?: return "-"
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
