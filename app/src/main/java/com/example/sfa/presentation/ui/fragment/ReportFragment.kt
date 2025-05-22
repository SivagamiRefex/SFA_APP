package com.example.sfa.presentation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.databinding.FragmentMenuBinding
import com.example.sfa.databinding.FragmentReportBinding
import com.example.sfa.presentation.ui.activity.CustVisitMapReportActivity
import com.example.sfa.presentation.ui.activity.CustomerVisitReportActivity
import com.example.sfa.utils.Constant

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    lateinit var dbController: DBController
    var customerLabel: String = "Customer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val tx = requireActivity().supportFragmentManager.beginTransaction()
                tx.replace(R.id.container, HomeFragment())
                tx.addToBackStack(null)
                tx.commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {




        dbController = DBController(requireActivity())
        customerLabel = Constant.getSetup("customer_label", "Customer", dbController, context) ?: "Customer"

        binding.tvCustVisit.text = "$customerLabel Visit Details"
        binding.tvCustVisitMap.text = "$customerLabel Visit with Map View"

        binding.tvCustVisit.setOnClickListener {
            startActivity(Intent(context, CustomerVisitReportActivity::class.java))
        }

        binding.tvCustVisitMap.setOnClickListener {
            startActivity(Intent(context, CustVisitMapReportActivity::class.java))
        }

       /* binding.tvCustomFormDataView.setOnClickListener {
            startActivity(Intent(context, CustomFormViewReportActivity::class.java))
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
