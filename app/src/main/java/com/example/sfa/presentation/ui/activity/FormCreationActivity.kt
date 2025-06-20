package com.example.sfa.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.FormCreation
import com.example.sfa.data.model.Section
import com.example.sfa.databinding.ActivityCustomFormBinding
import com.example.sfa.databinding.ActivityFormCreationBinding
import com.example.sfa.presentation.ui.Adapter.SectionFormAdapter
import com.example.sfa.presentation.viewmodel.FormViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FormCreationActivity:AppCompatActivity() {
    private lateinit var binding: ActivityFormCreationBinding
    private lateinit var dbController: DBController
    private val formViewModel: FormViewModel by viewModels()
    private lateinit var adapter: SectionFormAdapter
    private val form = FormCreation()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.layoutToolbar.tvTitle.text = "Form Creation"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dbController = DBController(applicationContext)
        adapter = SectionFormAdapter(form.sections) { sectionIndex ->
            if (sectionIndex in form.sections.indices) {  // 🔹 Ensure index exists before removing
                form.sections.removeAt(sectionIndex)
                adapter.notifyItemRemoved(sectionIndex)
                adapter.notifyItemRangeChanged(sectionIndex, form.sections.size)
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.addSectionButton.setOnClickListener {
            form.sections.add(Section(sectionName = ""))
            adapter.notifyItemInserted(form.sections.size - 1)
        }


        binding.submitButton.setOnClickListener {

            if (binding.formNameInput.text.toString().equals("")) {
                Toast.makeText(applicationContext, "Enter the Form Name", Toast.LENGTH_SHORT).show()
            }else if( form.sections.size==0){
                Toast.makeText(applicationContext, "Atleast Add One Section", Toast.LENGTH_SHORT).show()
            }else if(isCheckSectionName()) {
                Toast.makeText(applicationContext, "Enter The Section Name", Toast.LENGTH_SHORT).show()
            }else if(getQuestionCount()) {
                Toast.makeText(applicationContext, "Atleast Add One Question", Toast.LENGTH_SHORT).show()
            }else if(isCheckQuestionName()) {
                Toast.makeText(applicationContext, "Enter The Question", Toast.LENGTH_SHORT).show()
            }else{
                form.formName = binding.formNameInput.text.toString()
                // Toast.makeText(this, formData.joinToString("\n"), Toast.LENGTH_LONG).show()
                // Log.e("Saved Data", formData.joinToString("\n"))
                sendDataToServer()
            }
        }

        formViewModel.saveCreationformState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()

                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                        finish()

                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()

                    Toast.makeText(applicationContext, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    LoadingUtil.showLoading(this)

                    // Show loading indicator
                }

            }
        }


    }

    fun getQuestionCount():Boolean{
        var cnt =0;
        if(form.sections.size>0){
            for (section in form.sections) {
                if(section.questions.size>0){
                    cnt++
                }
            }
        }
        if(cnt>0){
            return false
        }else{
            return true
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun isCheckSectionName():Boolean{
        var cnt =0;
        if(form.sections.size>0){
            for (section in form.sections) {
                if(section.sectionName.equals("")){
                    cnt++
                }
            }
        }
        if(cnt>0){
            return true
        }else{
            return false
        }
    }
    fun isCheckQuestionName():Boolean{
        var cnt =0;
        if(form.sections.size>0){
            for (section in form.sections) {
                if(section.questions.size>0){
                    for (question in section.questions){
                        if(question.label.equals("")){
                            cnt++
                        }
                    }
                }
            }
        }

        if(cnt>0){
            return true
        }else{
            return false
        }
    }

    fun sendDataToServer()
    {
        if(Constant.isNetworkAvailable(applicationContext)) {
            //val jsonObject = JSONObject()
            val jsonObject = JsonObject()
            jsonObject.addProperty("formName", form.formName)
            jsonObject.addProperty("createdDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("spId", SecureStorage.getString(applicationContext, StringConstants.SP_ID))


            val sectionsArray = JsonArray()
            for (section in form.sections) {
                val sectionObject = JsonObject()
                sectionObject.addProperty("sectionName", section.sectionName)

                val questionsArray = JsonArray()
                for (question in section.questions) {
                    val questionObject = JsonObject()
                    var timeStamp = TimesUtil.getTimeStamp(
                        TimesUtil.getCurrentTime(TimesUtil.FORMAT),
                        TimesUtil.FORMAT
                    )
                    questionObject.addProperty("Fld_Id", "admin_$timeStamp")
                    questionObject.addProperty("label", question.label)
                    questionObject.addProperty("hint", question.hint)
                    questionObject.addProperty("value", question.value)
                    questionObject.addProperty("Field_Name", question.label)
                    questionObject.addProperty("Field_Col", "Fld" + "admin_$timeStamp")
                    questionObject.addProperty("Fldtyp", "TA")
                    questionObject.addProperty("Fld_Src_Name", "others")
                    questionObject.addProperty("Fld_Src_Field", "")
                    questionObject.addProperty("Fld_Length", 100)
                    questionObject.addProperty("Fld_Mandatory", 0)
                    questionObject.addProperty("Active_flag", 0)
                    questionObject.addProperty("Fldtype", "TEXT")
                    questionObject.addProperty("Control_id", 1)
                    questionObject.addProperty("cusxml", "")
                    questionsArray.add(questionObject)
                }

                sectionObject.add("questions", questionsArray)
                sectionsArray.add(sectionObject)
            }

            jsonObject.add("sections", sectionsArray)
            //  Log.e("Saved Data", jsonObject.toString())
            Log.e("jsonData", jsonObject.toString())
            formViewModel.saveCreationForm(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,jsonObject)

        }else{
            Toast.makeText(applicationContext,"Please check your network connection",Toast.LENGTH_SHORT).show()
        }
    }
}