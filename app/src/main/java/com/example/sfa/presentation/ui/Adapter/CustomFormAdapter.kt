package com.example.sfa.presentation.ui.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.sfa.data.model.CustomModuleDataModel
import com.example.sfa.databinding.ItemCustomFormBinding

class CustomFormAdapter (
    private val moduleList: ArrayList<CustomModuleDataModel>,
    private val context: Context
) : BaseAdapter() {

    override fun getCount(): Int = moduleList.size

    override fun getItem(position: Int): Any = moduleList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemCustomFormBinding

        val view: View = if (convertView == null) {
            binding = ItemCustomFormBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.root.tag = binding
            binding.root
        } else {
            binding = convertView.tag as ItemCustomFormBinding
            convertView
        }

        val module = moduleList[position]
        binding.tvModuleName.text = module.moduleName
        return view
    }
}