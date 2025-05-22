package com.example.sfa.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.FormResponsesListModel
import com.example.sfa.databinding.ActivityFormCreationBinding
import com.example.sfa.databinding.ActivityFormResponsesBinding
import com.example.sfa.presentation.ui.Adapter.CustomFormViewAdapter
import com.example.sfa.presentation.viewmodel.FormViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FormResponsesActivity:AppCompatActivity() {
    private lateinit var binding: ActivityFormResponsesBinding
    private lateinit var dbController: DBController
    private val formViewModel: FormViewModel by viewModels()
    private var moduleList = ArrayList<FormResponsesListModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormResponsesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.layoutToolbar.tvTitle.text = "Form Responses View"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dbController = DBController(applicationContext)
        getFormResponsesList()

        formViewModel.getFormResponsesListState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {

                        moduleList=result.data.data!!
                        if (moduleList.isNotEmpty()) {
                            callAdapter()
                        }
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading indicator
                }

            }
        }

    }
    fun getFormResponsesList(){
        if(Constant.isNetworkAvailable(applicationContext)){
            formViewModel.getFormResponsesList(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!,
                SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE))
        }else{
            Toast.makeText(applicationContext,"Please Check Your Network Connection",Toast.LENGTH_SHORT).show()
        }
    }

    fun callAdapter(){
        val courseAdapter = CustomFormViewAdapter(responseList = moduleList, this@FormResponsesActivity)
        binding.gvItem.adapter = courseAdapter
        binding.gvItem.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->


            val intent = Intent(applicationContext, FormResponseViewActivity::class.java)
            intent.putExtra("moduleId", moduleList[position].ModuleId)
            intent.putExtra("moduleName",moduleList[position].ModuleName)
            intent.putExtra("entryId",moduleList[position].EntryId)
            intent.putExtra("entryBy",moduleList[position].SalespersonName)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)

            Toast.makeText(
                applicationContext, moduleList[position].ModuleName + " selected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}