package com.example.sfa.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.LayoutBottomSheetDialogBinding
import com.example.sfa.presentation.ui.Adapter.SelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectionBottomSheetFragment(
    private val title: String,
    private val itemList: ArrayList<SelectionModel>,
    private val onItemSelected: (SelectionModel) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e("bottomdialog","start")

        _binding = LayoutBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("bottomdialog","entry")

        binding.tvSheetTitle.text = title

        adapter = SelectionAdapter(itemList) {
            onItemSelected(it)
            dismiss()
        }

        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        binding.etSearch.addTextChangedListener {
            adapter.filter(it.toString())
        }

        binding.ivClose.setOnClickListener{
            dismiss()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
