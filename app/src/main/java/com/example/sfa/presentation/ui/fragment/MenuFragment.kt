package com.example.sfa.presentation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.databinding.FragmentMenuBinding
import com.example.sfa.presentation.ui.activity.AddCustomerActivity
import com.example.sfa.presentation.ui.activity.AddRouteActivity
import com.example.sfa.presentation.ui.activity.AssignFormActivity
import com.example.sfa.presentation.ui.activity.LoginActivity
import com.example.sfa.presentation.viewmodel.LogoutViewModel
import com.example.sfa.presentation.viewmodel.TaskViewModel
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment: Fragment() {

    private var _menuBinding: FragmentMenuBinding? = null
    private val menuBinding get() = _menuBinding!!
    private val logoutViewModel: LogoutViewModel by viewModels()
    lateinit var dbController: DBController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _menuBinding = FragmentMenuBinding.inflate(inflater, container, false)
        val view = menuBinding.root

        initView(view);
        return view
    }

    fun initView(view:View){
        dbController=DBController(requireContext())
        menuBinding.cvAddCustomer.setOnClickListener(){
             val intent = Intent(context, AddCustomerActivity::class.java)
             startActivity(intent)
        }
        menuBinding.cvRoute.setOnClickListener(){
            val intent = Intent(context, AddRouteActivity::class.java)
            startActivity(intent)
        }
        menuBinding.cvLogout.setOnClickListener(){
            val builder = requireContext()?.let { it1 -> AlertDialog.Builder(it1) }
            builder?.setMessage("Are you sure want to Log Out?")
            builder?.setTitle("Confirm")
            builder?.setCancelable(false)
            builder?.setPositiveButton("Logout") {

                    dialog, which ->
                getLogout()
            }
            builder?.setNegativeButton("Cancel") {

                    dialog, which ->
                dialog.cancel()
            }
            val alertDialog = builder?.create()
            alertDialog?.show()
        }
        menuBinding.cvAssignForm.setOnClickListener(){
            val intent = Intent(context, AssignFormActivity::class.java)
            startActivity(intent)
        }

        logoutViewModel.logoutState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {

                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), "User Logout Successfully", Toast.LENGTH_SHORT).show()
                        SecureStorage.clearAll(requireContext())
                        dbController.clearDatabase(DBController.TABLE_NAME);
                        requireActivity().finish()
                        val intent = Intent(context, LoginActivity::class.java)
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

    fun getLogout(){
        logoutViewModel.getLogout(SecureStorage.getString(requireContext(),StringConstants.AUTH_TOKEN)!!,
            SecureStorage.getString(requireContext(),StringConstants.SP_ID)!!,
            TimesUtil.getCurrentTime(TimesUtil.FORMAT),
            SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE))

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _menuBinding = null
    }



}