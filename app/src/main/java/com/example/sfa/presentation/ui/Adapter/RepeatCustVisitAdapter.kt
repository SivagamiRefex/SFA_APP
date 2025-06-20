package com.example.sfa.presentation.ui.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.RepeatCustVisitModel
import com.example.sfa.databinding.ItemRepeatCustVisitReportBinding


class RepeatCustVisitAdapter  : RecyclerView.Adapter<RepeatCustVisitAdapter.ViewHolder>() {

    private var visitList = ArrayList<RepeatCustVisitModel>()

    fun setVisitList(repeatCustList: List<RepeatCustVisitModel>) {
        this.visitList = ArrayList(repeatCustList)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRepeatCustVisitReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = visitList[position]
        with(holder.binding) {
            tvName.text=item.custName
            tvAddress.text=item.custAddr
            tvVisitCount.text=item.visitCnt.toString()

        }
    }
    override fun getItemCount(): Int = visitList.size

    class ViewHolder(val binding: ItemRepeatCustVisitReportBinding) : RecyclerView.ViewHolder(binding.root)
}