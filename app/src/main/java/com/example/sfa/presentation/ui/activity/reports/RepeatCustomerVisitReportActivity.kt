package com.example.sfa.presentation.ui.activity.reports

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.PiechartDataModel
import com.example.sfa.data.model.RepeatCustVisitModel
import com.example.sfa.data.model.SalesPersonModel
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.data.model.VisitCusReportModel
import com.example.sfa.databinding.ActivityCustVisitMapReportBinding
import com.example.sfa.databinding.ActivityCustVisitReportBinding
import com.example.sfa.databinding.ActivityRepeatCustomerVisitReportBinding
import com.example.sfa.presentation.ui.Adapter.RepeatCustVisitAdapter
import com.example.sfa.presentation.ui.Adapter.VisitCustReportAdapter
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.viewmodel.ReportVisitViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar

@AndroidEntryPoint
class RepeatCustomerVisitReportActivity:AppCompatActivity() {
    private val reportViewmodel: ReportVisitViewModel by viewModels()
    private lateinit var binding: ActivityRepeatCustomerVisitReportBinding
    lateinit var  currentDt:String
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    private var spId: String = ""
    private var spName: String = ""
    private var salespersonList = ArrayList<SalesPersonModel>()
    private var visitList= ArrayList<RepeatCustVisitModel>()
    lateinit var adapter: RepeatCustVisitAdapter
    private  var spSelectionList= ArrayList<SelectionModel>()
    var selectedMonth=0
    var selectedYear=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepeatCustomerVisitReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView() {
        dbController=DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.layoutToolbar.tvTitle.text="Repeat $customerLabel Visit Report"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        prepareRecyclerView()
        binding.llSp.visibility = View.VISIBLE
        spId = SecureStorage.getString(applicationContext, StringConstants.SP_ID).toString()
        spName = SecureStorage.getString(applicationContext, StringConstants.SP_NAME).toString()
        binding.tvSelectSp.text =spName

        val calendar = Calendar.getInstance()
         selectedMonth = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
         selectedYear = calendar.get(Calendar.YEAR)
        binding.tvMonthYearFilter.text = "$selectedMonth/$selectedYear"
        getRepeatCustVisitData()

        if (SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE)==2|| SecureStorage.getInt(applicationContext,
                StringConstants.SP_TYPE)==3) {
            binding.llSp.visibility = View.VISIBLE
            //getSalesPersonList()
        } else {
            binding.llSp.visibility = View.GONE

        }
        getSalesPersonList()
        binding.cvSalesperson.setOnClickListener {

            SelectionBottomSheetFragment(
                title = "Select a Person",
                itemList = spSelectionList
            ) { selected ->
                binding.tvSelectSp.text = selected.name
                spId = selected.id
                spName= selected.name
                getRepeatCustVisitData()
                Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT)
                    .show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }
        binding.tvMonthYearFilter.setOnClickListener {
            showCustomMonthYearPicker { month, year ->
                if(year==0){
                    binding.tvMonthYearFilter.text = "Month/Year"
                    selectedYear=0
                    selectedMonth=0
                }else{
                    binding.tvMonthYearFilter.text = "$month/$year"
                    selectedMonth=month.toInt()
                    selectedYear=year
                    getRepeatCustVisitData()
                }


            }
        }

        reportViewmodel.getRepeatCustVisitState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        //Toast.makeText(requireContext(), "Today Followup Task List Updated", Toast.LENGTH_SHORT).show()
                        visitList=result.data.data!!
                        adapter.setVisitList(visitList)
                        binding.tvNoData.visibility=View.GONE


                    } else {
                        visitList.clear()
                        adapter.setVisitList(visitList)
                        binding.tvNoData.visibility=View.VISIBLE
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    visitList.clear()
                    adapter.setVisitList(visitList)
                    binding.tvNoData.visibility=View.VISIBLE
                    Toast.makeText(applicationContext, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    LoadingUtil.showLoading(this)
                }

            }
        }

    }
    fun prepareRecyclerView(){
        adapter = RepeatCustVisitAdapter()
        binding.rvReportData.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvReportData.adapter = adapter
    }

    private fun getSalesPersonList() {


        var dataResponse = dbController.getResponse(StringConstants.SALESPERSON_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {

            // Log.e("data there","available")
            salespersonList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)
                for (i in 0 until jsonArray.length()) {

                    val jsonObject = jsonArray.getJSONObject(i)
                    //&& !jsonObject.getString("SP_Type").equals("3")
                    // if(!jsonObject.getString("Sp_Id").equals(SecureStorage.getString(applicationContext,StringConstants.SP_ID))) {
                    if((SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==2 &&
                                !jsonObject.getString("SP_Type").equals("2")) ||
                        (SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==3&&
                                jsonObject.getString("Sp_Reporting_To_Id").equals
                                    (SecureStorage.getString(applicationContext,StringConstants.SP_ID)))){
                        val selectionModel = SalesPersonModel(
                            jsonObject.getString("Sp_Id"),
                            jsonObject.getString("Sp_Name")

                        )
                        salespersonList.add(selectionModel)
                    }
                }
                getCommonListModel(salespersonList)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }
    fun getCommonListModel(customerList: ArrayList<SalesPersonModel>) {
        spSelectionList = ArrayList<SelectionModel>()
        var datamodel = SelectionModel(spId, spName)
        spSelectionList.add(datamodel)
        if (customerList.isNotEmpty()) {
            for (list in customerList) {
                var datamodel = SelectionModel(
                    list.spId, list.spName,
                )
                spSelectionList.add(datamodel)
            }
        }

    }
    fun showCustomMonthYearPicker(onDateSelected: (String, Int) -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_month_year_picker)

        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
        val btnAll = dialog.findViewById<Button>(R.id.btn_all)
        val monthPicker = dialog.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = dialog.findViewById<NumberPicker>(R.id.yearPicker)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Month Picker
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = currentMonth + 1
        monthPicker.displayedValues = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        // Year Picker
        yearPicker.minValue = currentYear - 20
        yearPicker.maxValue = currentYear + 30
        yearPicker.value = currentYear

        btnOk.setOnClickListener {
            val selectedMonth = String.format("%02d", monthPicker.value)
            val selectedYear = yearPicker.value
            onDateSelected(selectedMonth, selectedYear)
            dialog.dismiss()
        }

        btnAll.setOnClickListener{
            onDateSelected("mm",0)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun getRepeatCustVisitData(){
        if(Constant.isNetworkAvailable(applicationContext)){
            reportViewmodel.getRepeatCustVisit(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                spId,TimesUtil.getCurrentTime(TimesUtil.FORMAT1),selectedMonth,selectedYear)
        }else{
            Toast.makeText(applicationContext,"Please Check Your Network Connection",Toast.LENGTH_SHORT).show()
        }
    }
}

