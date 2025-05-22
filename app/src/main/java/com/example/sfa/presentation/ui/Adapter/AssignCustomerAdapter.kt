package com.example.sfa.presentation.ui.Adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.ItemAssignDataListBinding
import com.example.sfa.presentation.ui.listener.OnClickTypeListener
import kotlin.random.Random

class AssignCustomerAdapter  : RecyclerView.Adapter<AssignCustomerAdapter.ViewHolder>() {
    var onNewClickListener: OnClickTypeListener? = null
    private var siteList: ArrayList<SelectionModel> = arrayListOf()

    fun setSiteList(modlist: ArrayList<SelectionModel>) {
        this.siteList = modlist
        notifyDataSetChanged()
    }

    fun setOnClickListener(onListener: OnClickTypeListener?) {
        this.onNewClickListener = onListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssignDataListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = siteList[position]
        val randomColor = getRandomLightColor()

        holder.binding.tvName.text = item.name
        holder.binding.tvFirstLetter.text = item.name.first().toString()

        // Set random light background color
        val drawable = holder.binding.tvFirstLetter.background
        if (drawable is GradientDrawable) {
            drawable.setColor(randomColor)
        }

        // Set contrasting text color based on background
        val textColor = if (isColorDark(randomColor)) Color.WHITE else Color.BLACK
        holder.binding.tvFirstLetter.setTextColor(textColor)

        holder.binding.tvRemove.setOnClickListener {
            onNewClickListener?.onClickTypeItem(item, position, 1)
        }
    }

    override fun getItemCount(): Int = siteList.size

    class ViewHolder(val binding: ItemAssignDataListBinding) : RecyclerView.ViewHolder(binding.root)

    private fun getRandomLightColor(): Int {
        val red = 150 + Random.nextInt(106)
        val green = 150 + Random.nextInt(106)
        val blue = 150 + Random.nextInt(106)
        return Color.rgb(red, green, blue)
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
}