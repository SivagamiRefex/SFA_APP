package com.example.sfa.presentation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.data.model.FormCreation
import com.example.sfa.databinding.FragmentHomeBinding
import com.example.sfa.presentation.ui.activity.AssignedTaskActivity
import com.example.sfa.presentation.ui.activity.CustomFormOneActivity
import com.example.sfa.presentation.ui.activity.CustomerVisitActivity
import com.example.sfa.presentation.ui.activity.FormCreationActivity
import com.example.sfa.presentation.ui.activity.FormResponsesActivity
import com.example.sfa.presentation.ui.activity.TaskCreationActivity
import com.example.sfa.presentation.viewmodel.MydayplanViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment()  {
    private var _homeBinding: FragmentHomeBinding? = null
    private val homeBinding get() = _homeBinding!!
    private val mydayplanViewModel: MydayplanViewModel by viewModels()
    lateinit var dbController: DBController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _homeBinding = FragmentHomeBinding.inflate(inflater,container,false)
        val view  = homeBinding.root
        initView(view)
        return view
    }
    fun initView(view:View){
        dbController = DBController(requireContext())
        homeBinding.tvSpname.setText(SecureStorage.getString(requireContext(),StringConstants.SP_NAME))

        if(SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE)==2){
          homeBinding.tvDesig.text="Admin"
            homeBinding.cvTaskCreation.visibility=View.VISIBLE
            homeBinding.llForm.visibility=View.VISIBLE
            homeBinding.llFormResponses.visibility=View.VISIBLE

        }else{
            homeBinding.tvDesig.text="Sales Person"
            homeBinding.cvTaskCreation.visibility=View.GONE
            homeBinding.llForm.visibility=View.GONE
            homeBinding.llFormResponses.visibility=View.GONE


        }
        homeBinding.cvTaskCreation.setOnClickListener{
            val intent = Intent(requireActivity(), TaskCreationActivity::class.java)
            startActivity(intent)
        }

        homeBinding.cvTaskAssigned.setOnClickListener{
            val intent = Intent(requireActivity(), AssignedTaskActivity::class.java)
            startActivity(intent)
        }

        homeBinding.cvCustomerVisit.setOnClickListener{
            checkMydayPlanDone()
        }

        homeBinding.cvFormCreation.setOnClickListener{
            val intent = Intent(requireActivity(), FormCreationActivity::class.java)
            startActivity(intent)
        }
        homeBinding.cvResponsesView.setOnClickListener{
            val intent = Intent(requireActivity(), FormResponsesActivity::class.java)
            startActivity(intent)
        }

        homeBinding.cvFormView.setOnClickListener{
            val intent = Intent(requireActivity(), CustomFormOneActivity::class.java)
            startActivity(intent)
        }


        mydayplanViewModel.getMydayplanState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        val gson = Gson()
                        val jsonString = gson.toJson(result.data.data!!)
                        Log.e("Login_data", "" + jsonString.toString())
                        if (!dbController.updateProduct(StringConstants.MYDAYPLAN_DATA, jsonString.toString())) {
                            dbController.addProduct(StringConstants.MYDAYPLAN_DATA, jsonString.toString())
                        }
                        val intent = Intent(requireActivity(), CustomerVisitActivity::class.java)
                        startActivity(intent)


                    } else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading indicator
                }

            }
        }

    }

    fun checkMydayPlanDone(){
        if(Constant.isNetworkAvailable(context)){
            mydayplanViewModel.getMydayplan(
                SecureStorage.getString(requireContext(),StringConstants.AUTH_TOKEN)!!,
                SecureStorage.getString(requireContext(),StringConstants.SP_ID)!!,
                TimesUtil.getCurrentTime(TimesUtil.FORMAT1))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _homeBinding = null
    }

}