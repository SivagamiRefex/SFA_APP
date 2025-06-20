package com.example.sfa.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sfa.data.model.CustomerDataModel
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.AssignFormModel
import com.example.sfa.data.model.SalesPersonModel
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.ActivityAssignFormBinding
import com.example.sfa.presentation.ui.Adapter.AssignCustomerAdapter
import com.example.sfa.presentation.ui.Adapter.AssignSalesmanAdapter
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.ui.listener.OnClickTypeListener
import com.example.sfa.presentation.viewmodel.FormViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONException

@AndroidEntryPoint
class AssignFormActivity:AppCompatActivity() {
    private lateinit var binding: ActivityAssignFormBinding
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    private var moduleList = ArrayList<SelectionModel>()
    lateinit var assignSalesmanAdapter: AssignSalesmanAdapter
    lateinit var assignSiteAdapter: AssignCustomerAdapter
    private var salespersonList = ArrayList<SalesPersonModel>()
    private var filteredSalespersonList = ArrayList<SelectionModel>()
    private  var unAssignedPersonList= ArrayList<SelectionModel>()
    private  var commonSalespersonList= ArrayList<SelectionModel>()
    private  var commonCustomerList= ArrayList<SelectionModel>()
    private var customerList= ArrayList<CustomerDataModel>()
    private var filteredCustomerList = ArrayList<SelectionModel>()
    private  var unAssignedCustomerList= ArrayList<SelectionModel>()
    private var assignModuleList = ArrayList<AssignFormModel>()

    var formName=""
    var formId=0
    private var spId: String = ""
    private var spName: String = ""
    private val formViewModel: FormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView() {
        dbController = DBController(applicationContext)
        customerLabel =
            Constant.getSetup("customer_label", "Customer", dbController, applicationContext)!!
        binding.layoutToolbar.tvTitle.text = "Assign Form"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }

        spId=SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!;
        spName=SecureStorage.getString(applicationContext,StringConstants.SP_NAME).toString()
        prepareSalesmanRecyclerView()
        prepareCustomerRecyclerView()
        getModuleList()
        getSalespersonList()
        getCustomerList()

        binding.cvForm.setOnClickListener{
            SelectionBottomSheetFragment(
                title = "Select a Form",
                itemList = moduleList
            ) { selected ->
                binding.tvForm.text=selected.name;
                formId=selected.id!!.toInt()
                formName= selected.name.toString()
                getFilteredPersonList()
                getUnAssignedPersonList()
                getFilteredSiteList()
                getUnAssignedSiteList()
                Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT)
                    .show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }

        binding.tvAssignPerson.setOnClickListener{
            if(formId==0){
                Toast.makeText(applicationContext,"Please select the Form Name",Toast.LENGTH_SHORT).show()
            }else {
                if(unAssignedPersonList.size>0){
                    showPersonSelectionDialog(formId)
                }else{
                    Toast.makeText(applicationContext,"All SalesPersons are already assigned",Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvAssignSite.setOnClickListener{
            if(formId==0){
                Toast.makeText(applicationContext,"Please select the Form Name",Toast.LENGTH_SHORT).show()
            }else {
                if(unAssignedCustomerList.size>0){
                    showSiteSelectionDialog(formId)
                }else{
                    Toast.makeText(applicationContext,"All Sites are already assigned",Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnSave.setOnClickListener{
            saveAssignCustomform()
        }


        formViewModel.getFormWithAssigneeState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {

                        assignModuleList=result.data.data!!
                        moduleList.clear()
                        for (i in 0 until assignModuleList.size) {
                               var dataModel=assignModuleList[i]
                            val selectionModel = SelectionModel(dataModel.ModuleId.toString(),dataModel.ModuleName)
                            selectionModel.selectedPerson=dataModel.AssignedPerson
                            selectionModel.selectedSite=dataModel.AssignedSite
                            selectionModel.selectedPersons =dataModel.AssignedPerson
                                .split(",")
                                .map { it.trim() }
                            selectionModel.selectedSitesList = dataModel.AssignedSite
                                .split(",")
                                .map { it.trim() }
                            moduleList.add(selectionModel)
                            Log.e("modulelist",moduleList.get(i).selectedPersons.toString())
                        }
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }
                is Resource.Error -> {
                    LoadingUtil.hideLoading()

                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading indicator
                    LoadingUtil.showLoading(this)

                }

            }
        }

        formViewModel.saveFormAssigneeState.observe(this) { result ->
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

                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading indicator
                    LoadingUtil.showLoading(this)
                }

            }
        }

    }

    private fun prepareSalesmanRecyclerView()  {
        assignSalesmanAdapter= AssignSalesmanAdapter()
        binding.rvListSalesperson.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL,false)
        binding.rvListSalesperson.adapter= assignSalesmanAdapter
        assignSalesmanAdapter.setOnClickListener(object :
            OnClickTypeListener {
            override fun onClickTypeItem(selectionModel: SelectionModel?, position: Int, type: Int) {
                if(type==1){
                    Log.e("datadialog", selectionModel?.name.toString())

                    if (selectionModel != null) {

                        for(list in moduleList){
                            if(formId==list.id.toInt()){
                                // list.selectedPerson=list.selectedPerson?.replace(",${selectionModel.Id}","")
                                val firstItem = list.selectedPerson?.split(",")!!.first()
                                if(firstItem.equals(selectionModel.id)) {
                                    list.selectedPerson =
                                        list.selectedPerson?.replace("${selectionModel.id}", "")
                                }else{
                                    list.selectedPerson =
                                        list.selectedPerson?.replace(",${selectionModel.id}", "")
                                }
                                list.selectedPersons = list.selectedPersons.toMutableList().apply {
                                    remove(selectionModel.id)
                                }

                            }
                        }
                        getFilteredPersonList()
                        getUnAssignedPersonList()

                    }


                }
            }
        })

    }
    private fun prepareCustomerRecyclerView()  {
        assignSiteAdapter= AssignCustomerAdapter()
        binding.rvListSite.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL,false)
        binding.rvListSite.adapter= assignSiteAdapter
        assignSiteAdapter.setOnClickListener(object :
            OnClickTypeListener {
            override fun onClickTypeItem(selectionModel: SelectionModel?, position: Int, type: Int) {
                if(type==1){
                    Log.e("datadialog", selectionModel?.name.toString())
                    if (selectionModel != null) {
                        for(list in moduleList){
                            if(formId==list.id.toInt()){
                                val firstItem = list.selectedSite?.split(",")!!.first()
                                if(firstItem.equals(selectionModel.id)) {
                                    list.selectedSite =
                                        list.selectedSite?.replace("${selectionModel.id}", "")
                                }else{
                                    list.selectedSite =
                                        list.selectedSite?.replace(",${selectionModel.id}", "")
                                }
                                list.selectedSitesList = list.selectedSitesList.toMutableList().apply {
                                    remove(selectionModel.id)
                                }

                            }
                        }
                        getFilteredSiteList()
                        getUnAssignedSiteList()


                    }
                }
            }
        })

    }

    fun getModuleList(){
        if(Constant.isNetworkAvailable(applicationContext)){
            formViewModel.getFormWithAssignee(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!,
                SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE))
        }else{
            Toast.makeText(applicationContext,"Please Check Your Network Connection", Toast.LENGTH_SHORT).show()
        }
    }
    fun getSalespersonList(){
        var dataResponse = dbController.getResponse(StringConstants.SALESPERSON_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {
            commonSalespersonList.clear()
            salespersonList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
//!jsonObject.getString("SP_Type").equals("3")
                   // if(!jsonObject.getString("Sp_Id").equals(SecureStorage.getString(applicationContext,StringConstants.SP_ID))){
                    if((SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==2 &&
                                !jsonObject.getString("SP_Type").equals("2")) ||
                        (SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==3&&
                                (jsonObject.getString("Sp_Reporting_To_Id").equals(
                                    SecureStorage.getString(applicationContext,StringConstants.SP_ID))||
                                        SecureStorage.getString(applicationContext,StringConstants.SP_ID).
                                        equals(jsonObject.getString("Sp_Id"))))){
                        val selectionModel: SelectionModel = SelectionModel(
                            jsonObject.getString("Sp_Id"),
                            jsonObject.getString("Sp_Name"))
                        commonSalespersonList.add(selectionModel)

                        val selectionModel1= SalesPersonModel(
                            jsonObject.getString("Sp_Id"),
                            jsonObject.getString("Sp_Name")
                        )
                        salespersonList.add(selectionModel1)

                    }
                }
                Log.e("commonlist", "SalespersonList: $commonSalespersonList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Salesperson not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getCustomerList(){
        var dataResponse = dbController.getResponse(StringConstants.CUSTOMER_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {
            commonCustomerList.clear()
            customerList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val selectionModel: SelectionModel = SelectionModel(
                        jsonObject.getString("Cust_Id"),
                        jsonObject.getString("Cust_Name")
                    )
                    selectionModel.address=jsonObject.getString("Cust_Billing_Address")
                    selectionModel.lat=jsonObject.getString("Loc_Latitude").toDouble()
                    selectionModel.long=jsonObject.getString("Loc_Longitude").toDouble()
                    commonCustomerList.add(selectionModel)

                    val selectionModel1 = CustomerDataModel(
                        jsonObject.getString("Cust_Id"),
                        jsonObject.getString("Cust_Name"),
                        jsonObject.getString("Cust_Billing_Address"),
                        jsonObject.getString("Loc_Latitude"),
                        jsonObject.getString("Loc_Longitude")
                    )
                    customerList.add(selectionModel1)

                }
                Log.e("commonlist", "customerlist: $customerList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(applicationContext, "Customer List not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFilteredPersonList(){

        filteredSalespersonList=ArrayList<SelectionModel>()
        filteredSalespersonList.clear()
        if(moduleList.isNotEmpty()) {
            for (list in moduleList) {
                if (list.id.equals(formId.toString())) {

                    for (salesManList in salespersonList) {
                        var persondData=","+list.selectedPerson!!+","
                        if (persondData.contains("," + salesManList.spId + ",")) {
                            filteredSalespersonList.add(
                                SelectionModel(
                                    salesManList.spId,
                                    salesManList.spName,

                                )
                            )
                        }

                    }

                }
            }

            //  if(commonList.size>0){
            assignSalesmanAdapter.setPersonList(filteredSalespersonList)
            // }
        }

    }

    fun getFilteredSiteList(){

        filteredCustomerList=ArrayList<SelectionModel>()
        filteredCustomerList.clear()
        if(moduleList.isNotEmpty()) {
            for (list in moduleList) {
                if (list.id.equals(formId.toString())) {

                    for (siteList in customerList) {
                        var persondData=","+list.selectedSite!!+","
                        if (persondData.contains("," + siteList.CustId.toString() + ",")) {
                            filteredCustomerList.add(
                                SelectionModel(
                                    siteList.CustId.toString(),
                                    siteList.CustName.toString(),

                                )
                            )
                        }

                    }

                }
            }

            //  if(commonList.size>0){
            assignSiteAdapter.setSiteList(filteredCustomerList)
            // }
        }

    }

    fun getUnAssignedPersonList(){
        unAssignedPersonList.clear()
        if(moduleList.isNotEmpty()) {
            for (list in moduleList) {
                if (list.id.equals(formId.toString())) {

                    for (salesManList in salespersonList) {
                        var persondData=","+list.selectedPerson!!+","
                        if (!persondData.contains("," + salesManList.spId + ",")) {
                            unAssignedPersonList.add(
                                SelectionModel(
                                    salesManList.spId,
                                    salesManList.spName,

                                )
                            )
                        }

                    }

                }
            }


        }
    }

    fun getUnAssignedSiteList(){
        unAssignedCustomerList.clear()
        if(moduleList.isNotEmpty()) {
            for (list in moduleList) {
                if (list.id.equals(formId.toString())) {

                    for (cusList in customerList) {
                        var persondData=","+list.selectedSite!!+","
                        if (!persondData.contains("," + cusList.CustId.toString() + ",")) {
                            unAssignedCustomerList.add(
                                SelectionModel(
                                    cusList.CustId.toString(),
                                    cusList.CustName.toString(),

                                )
                            )
                        }

                    }

                }
            }


        }
    }

    fun showPersonSelectionDialog(productId: Int) {
        val personsList = unAssignedPersonList

        // Find the selected persons for this product
        val productOption = moduleList.find { it.id == productId.toString() }
        val selectedPersonIds = productOption?.selectedPersons ?: emptyList()

        // Create a boolean array to mark already selected persons
        val selectedItems = BooleanArray(personsList.size) { index ->
            personsList[index].id in selectedPersonIds
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select the Salesperson")


        builder.setMultiChoiceItems(
            personsList.map { it.name }.toTypedArray(),
            selectedItems
        ) { _, which, isChecked ->
            productOption?.selectedPersons = productOption?.selectedPersons?.toMutableList()?.apply {
                if (isChecked) add(personsList[which].id) else remove(personsList[which].id)
            } ?: emptyList()

            val currentSelection = productOption?.selectedPerson ?: ""
            productOption?.selectedPerson = if (isChecked) {
                if (currentSelection.isEmpty()) {
                    personsList[which].id.toString()
                } else {
                    "$currentSelection,${personsList[which].id}"
                }
            } else {
                currentSelection.replace(",${personsList[which].id}", "").replace("${personsList[which].id},", "").replace(personsList[which].id.toString(), "")
            }

            Log.e("selected data:", productOption?.selectedPerson.toString())
        }

        builder.setPositiveButton("OK") { _, _ ->
            Log.d("Selected Person IDs", productOption?.selectedPersons?.joinToString(",") ?: "None")
            // assignCustomFormAdapter.setModuleList(moduleList)
            getFilteredPersonList()
            getUnAssignedPersonList()
        }

        builder.setNegativeButton("Cancel", null)

        builder.show()
    }


    fun showSiteSelectionDialog(productId: Int){
        val sitesList = unAssignedCustomerList

        // Find the selected persons for this product
        val productOption = moduleList.find { it.id == productId.toString() }
        val selectedSiteIds = productOption?.selectedSitesList ?: emptyList()

        // Create a boolean array to mark already selected persons
        val selectedItems = BooleanArray(sitesList.size) { index ->
            sitesList[index].id in selectedSiteIds
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select the $customerLabel")


        builder.setMultiChoiceItems(
            sitesList.map { it.name }.toTypedArray(),
            selectedItems
        ) { _, which, isChecked ->
            productOption?.selectedSitesList = productOption?.selectedSitesList?.toMutableList()?.apply {
                if (isChecked) add(sitesList[which].id) else remove(sitesList[which].id)
            } ?: emptyList()

            val currentSelection = productOption?.selectedSite ?: ""
            productOption?.selectedSite = if (isChecked) {
                if (currentSelection.isEmpty()) {
                    sitesList[which].id.toString()
                } else {
                    "$currentSelection,${sitesList[which].id}"
                }
            } else {
                currentSelection.replace(",${sitesList[which].id}", "").replace("${sitesList[which].id},", "").replace(sitesList[which].id.toString(), "")
            }

            Log.e("selected data:", productOption?.selectedSite.toString())
        }

        builder.setPositiveButton("OK") { _, _ ->
            Log.d("Selected Person IDs", productOption?.selectedSitesList?.joinToString(",") ?: "None")
            // assignCustomFormAdapter.setModuleList(moduleList)
            getFilteredSiteList()
            getUnAssignedSiteList()
        }

        builder.setNegativeButton("Cancel", null)

        builder.show()
    }

    fun saveAssignCustomform(){
        if(Constant.isNetworkAvailable(applicationContext)) {
            val jsonArray = JsonArray()
            val activityReportAppObject = JsonObject()
            activityReportAppObject.addProperty(
                "spId",
                SecureStorage.getString(applicationContext, StringConstants.SP_ID)
            )
            val jsonObject1 = JsonObject()
            jsonObject1.add("common_data", activityReportAppObject)
            jsonArray.add(jsonObject1)
            Log.e("common_data: ", " $activityReportAppObject")
            val activitySampleReportArray = JsonArray()
            try {
                var i = 0
                while (moduleList.size > i) {
                    if (moduleList[i].id.toInt() == formId) {
                        val jResult = JsonObject() // main object
                        val moduleId: Int = moduleList[i].id.toInt()
                        val moduleName: String = moduleList[i].name
                        val selectedIdList =
                            moduleList[i].selectedPersons.filter { it.isNotBlank() }
                        val selectedSiteList =
                            moduleList[i].selectedSitesList.filter { it.isNotBlank() }

                        val selectedId = if (selectedIdList.isNotEmpty()) {
                            selectedIdList.joinToString(
                                ",",
                                "[",
                                "]"
                            ) { it.toString() }
                        } else {
                            "[]"
                        }
                        val selectedSiteId = if (selectedSiteList.isNotEmpty()) {
                            selectedSiteList.joinToString(
                                ",",
                                "[",
                                "]"
                            ) { it.toString() }
                        } else {
                            "[]"
                        }


                        jResult.addProperty("moduleId", moduleId)
                        jResult.addProperty("moduleName", moduleName)
                        jResult.addProperty("selectedId", formatSelectedId(selectedId))
                        jResult.addProperty("selectedSiteId", formatSelectedId(selectedSiteId))
                        activitySampleReportArray.add(jResult)
                    }
                    i++
                }
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
            val jsonObject3 = JsonObject()
            jsonObject3.add("assigned_detail", activitySampleReportArray)
            jsonArray.add(jsonObject3)
            Log.e("jsonarray", jsonArray.toString())

            formViewModel.saveFormAssignee(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                Constant.toRequestBody(jsonArray),SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!)

        }else{
            Toast.makeText(applicationContext,"Please Check Your Network Connection",Toast.LENGTH_SHORT).show()
        }


    }
    fun formatSelectedId(selectedId: String): String {
        return try {
            val jsonArray = JSONArray(selectedId) // Convert string to JSON array
            (0 until jsonArray.length()).joinToString(",") { jsonArray.getInt(it).toString() }
        } catch (e: Exception) {
            "" // Handle cases where the format is incorrect
        }
    }

}