package com.example.sfa.presentation.ui.listener

import com.example.sfa.data.model.SelectionModel

interface OnClickTypeListener {
    fun onClickTypeItem(selectionModel: SelectionModel?, position: Int,type:Int)
}