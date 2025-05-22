package com.example.sfa.presentation.ui.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.ItemSelectionTextBinding

class SelectionAdapter(
    private val originalList: ArrayList<SelectionModel>,
    private val onItemClick: (SelectionModel) -> Unit
) : RecyclerView.Adapter<SelectionAdapter.ViewHolder>() {

    private var filteredList = originalList.toMutableList()

    inner class ViewHolder(private val binding: ItemSelectionTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: SelectionModel) {
            binding.tvName.text = model.name
            binding.root.setOnClickListener {
                onItemClick(model)
            }
        }
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.name.contains(query, true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectionTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }
}
