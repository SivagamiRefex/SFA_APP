package com.example.sfa.presentation.ui.listener

import com.example.sfa.data.model.TaskModel

interface OnTaskClickListener {
    fun onClickItem(selectionModel: TaskModel?, position: Int)

}