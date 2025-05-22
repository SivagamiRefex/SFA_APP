package com.example.sfa.presentation.ui.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.sfa.data.model.FormResponsesListModel
import com.example.sfa.databinding.ItemCustomformViewBinding

class CustomFormViewAdapter (
    private val responseList: ArrayList<FormResponsesListModel>,
    private val context: Context
) : BaseAdapter() {

    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int = responseList.size

    override fun getItem(position: Int): Any = responseList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemCustomformViewBinding

        val view: View

        if (convertView == null) {
            if (layoutInflater == null) {
                layoutInflater = LayoutInflater.from(context)
            }

            binding = ItemCustomformViewBinding.inflate(layoutInflater!!, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = convertView.tag as ItemCustomformViewBinding
        }

        val item = responseList[position]

        binding.tvModuleName.text = item.ModuleName

        binding.tvCustomerName.text =
            if (!item.CustomerName.isNullOrEmpty() && item.CustomerName != "null")
                item.CustomerName
            else ""

        binding.tvEntryDate.text = item.EntryDate

        // If you want to load image into binding.ivPreview, add your image logic here
        // Example: Glide.with(context).load(item.imageUrl).into(binding.ivPreview)

        return view
    }
}