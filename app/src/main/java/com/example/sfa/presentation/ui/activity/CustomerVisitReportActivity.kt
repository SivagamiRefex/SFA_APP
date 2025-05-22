package com.example.sfa.presentation.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.SalesPersonModel
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.data.model.TaskModel
import com.example.sfa.data.model.VisitCusReportModel
import com.example.sfa.databinding.ActivityCustVisitReportBinding
import com.example.sfa.presentation.ui.Adapter.VisitCustReportAdapter
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.viewmodel.CustomerVisitViewModel
import com.example.sfa.presentation.viewmodel.ReportVisitViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.Calendar


@AndroidEntryPoint
class CustomerVisitReportActivity:AppCompatActivity() {
    private val visitViewmodel: ReportVisitViewModel by viewModels()
    private lateinit var binding: ActivityCustVisitReportBinding
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    lateinit var currentFromDt: String
    lateinit var currentToDt: String
    lateinit var adapter: VisitCustReportAdapter
    private  var commonList= ArrayList<SelectionModel>()
    private var spId: String = ""
    private var spName: String = ""
    private var salespersonList = ArrayList<SalesPersonModel>()
    private var visitList= ArrayList<VisitCusReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustVisitReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView(){
        dbController=DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.layoutToolbar.tvTitle.text="$customerLabel Visit"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        binding.tvCustomerLabel.text = "$customerLabel Name"
        currentFromDt = TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        currentToDt = TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        binding.tvFromDate.setText(currentFromDt)
        binding.tvToDate.setText(currentToDt)
        prepareRecyclerView()
        binding.llSp.visibility = View.VISIBLE
        spId = SecureStorage.getString(applicationContext, StringConstants.SP_ID).toString()
        spName = SecureStorage.getString(applicationContext,StringConstants.SP_NAME).toString()
        binding.tvSelectSp.text =spName
        if (SecureStorage.getInt(applicationContext,StringConstants.SP_TYPE)==2) {
            binding.llSp.visibility = View.VISIBLE
            //getSalesPersonList()
        } else {
            binding.llSp.visibility = View.GONE

        }
        getSalesPersonList()
        lifecycleScope.launch(Dispatchers.IO) {

            getCustomerVisitList()
        }
        binding.cvSalesperson.setOnClickListener {

            SelectionBottomSheetFragment(
                title = "Select a Person",
                itemList = commonList
            ) { selected ->
                binding.tvSelectSp.text = selected.name
                spId = selected.id
                spName= selected.name
                lifecycleScope.launch(Dispatchers.IO) {

                    getCustomerVisitList()
                }
                Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT)
                    .show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }

        binding.tvFromDate.setOnClickListener(View.OnClickListener {
            val day: Int
            val month: Int
            val year: Int
            if (binding.tvFromDate.getText().toString() != "") {
                val dateArray: Array<String> = binding.tvFromDate.getText().toString().split("-".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                year = dateArray[0].toInt()
                month = dateArray[1].toInt() - 1
                day = dateArray[2].toInt()
            } else {
                val c = Calendar.getInstance()

                day = c[Calendar.MONTH]
                month = c[Calendar.MONTH]
                year = c[Calendar.YEAR]
            }
            val dialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    val _year = year.toString()
                    val _month = if ((month + 1) < 10) "0" + (month + 1) else (month + 1).toString()
                    val _date = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                    val _pickedDate = "$year-$_month-$_date"
                    Log.e("PickedDate: ", "Date: $_pickedDate") //2019-02-12
                    currentFromDt = _pickedDate // _date +"/"+_month+"/"+_year;

                    binding.tvFromDate.setText(currentFromDt)
                    lifecycleScope.launch(Dispatchers.IO) {

                        getCustomerVisitList()
                    }

                }, year, month, day
            )
            dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
            dialog.show()
        })
        binding.tvToDate.setOnClickListener(View.OnClickListener {
            val day: Int
            val month: Int
            val year: Int
            if (binding.tvToDate.getText().toString() != "") {
                val dateArray: Array<String> =
                    binding.tvToDate.getText().toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                year = dateArray[0].toInt()
                month = dateArray[1].toInt() - 1
                day = dateArray[2].toInt()
            } else {
                val c = Calendar.getInstance()

                day = c[Calendar.MONTH]
                month = c[Calendar.MONTH]
                year = c[Calendar.YEAR]
            }
            val dialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    val _year = year.toString()
                    val _month = if ((month + 1) < 10) "0" + (month + 1) else (month + 1).toString()
                    val _date = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                    val _pickedDate = "$year-$_month-$_date"
                    Log.e("PickedDate: ", "Date: $_pickedDate") //2019-02-12
                    currentToDt = _pickedDate //_date +"/"+_month+"/"+_year;
                    binding.tvToDate.setText(currentToDt)
                    lifecycleScope.launch(Dispatchers.IO) {

                        getCustomerVisitList()
                    }

                }, year, month, day
            )
            dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
            dialog.show()
        })

       /* visitViewmodel.getListState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        binding.rvReportData.visibility = View.VISIBLE
                        binding.tvNoData.visibility = View.GONE
                        binding.llTotalDistance.visibility = View.VISIBLE
                        visitList=result.data!!.data!!
                        adapter.setCustomerList(visitList)
                        var distance: Double = 0.0
                        for (i in 0 until visitList.size - 1) {
                            val origin = visitList[i]
                            val destination = visitList[i + 1]
                            val originLatLong = LatLng(origin.InLat.toDouble(), origin.InLong.toDouble())
                            val destinationLatLong =
                                LatLng(destination.InLat.toDouble(), destination.InLong.toDouble())
                            distance += SphericalUtil.computeDistanceBetween(
                                originLatLong,
                                destinationLatLong
                            );

                        }

                        if (distance > 0) {
                            binding.tvDistance.text = String.format("%.2f", (distance / 1000)) + " km"
                        }

                    } else {
                        visitList.clear()
                        binding.rvReportData.visibility = View.GONE
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.llTotalDistance.visibility = View.GONE
                        Toast.makeText(this, "No Data Available", Toast.LENGTH_SHORT).show()
                        adapter.setCustomerList(visitList)
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
        }*/

    }

    private fun prepareRecyclerView() {
        adapter = VisitCustReportAdapter()
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
                    if(!jsonObject.getString("Sp_Id").equals(SecureStorage.getString(applicationContext,StringConstants.SP_ID))) {
                        val selectionModel = SalesPersonModel(
                            jsonObject.getString("Sp_Id"),
                            jsonObject.getString("Sp_Name")

                        )
                        salespersonList.add(selectionModel)
                    }
                }
                getCommonListModel(salespersonList)
                Log.e("commonlist", "sourceOfLeadList: $salespersonList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }
    fun getCommonListModel(customerList: ArrayList<SalesPersonModel>) {
        commonList = ArrayList<SelectionModel>()
        var datamodel = SelectionModel(spId,spName)
        commonList.add(datamodel)
        if (customerList.isNotEmpty()) {
            for (list in customerList) {
                var datamodel = SelectionModel(
                    list.spId, list.spName,
                    )
                commonList.add(datamodel)
            }
        }
    }
    suspend  fun getCustomerVisitList(){
        if(Constant.isNetworkAvailable(applicationContext)){
            val result = visitViewmodel.getVisitList(SecureStorage.getString(applicationContext,com.example.sfa.utils.StringConstants.AUTH_TOKEN)!!,
           spId, currentFromDt,currentToDt)
            when (result) {
                is Resource.Success -> {
                    var json = JSONTokener(result.data!!.string()).nextValue()
                    var jsonArray = JSONArray()

                    if (json is JSONArray) {
                        jsonArray = JSONArray(result.data)

                    } else if (json is JSONObject) {

                        if (json.getBoolean("status")) {
                            binding.rvReportData.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            binding.llTotalDistance.visibility = View.VISIBLE

                            if (json is JSONObject) {
                                jsonArray = json.getJSONArray("data")
                                visitList.clear()
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val selectionModel = VisitCusReportModel(
                                        jsonObject.getString("custNm"),
                                        jsonObject.getString("visitDate"),
                                        jsonObject.getString("InTime"),
                                        jsonObject.getString("outTime"),
                                        jsonObject.getString("InLat"),
                                        jsonObject.getString("InLong")
                                    )
                                    visitList.add(selectionModel)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                adapter.setCustomerList(visitList)
                            }
                            var distance: Double = 0.0
                            for (i in 0 until visitList.size - 1) {
                                val origin = visitList[i]
                                val destination = visitList[i + 1]
                                val originLatLong =
                                    LatLng(origin.InLat.toDouble(), origin.InLong.toDouble())
                                val destinationLatLong =
                                    LatLng(
                                        destination.InLat.toDouble(),
                                        destination.InLong.toDouble()
                                    )
                                distance += SphericalUtil.computeDistanceBetween(
                                    originLatLong,
                                    destinationLatLong
                                );

                            }

                            if (distance > 0) {
                                binding.tvDistance.text =
                                    String.format("%.2f", (distance / 1000)) + " km"
                            }

                        } else {
                        visitList.clear()
                        binding.rvReportData.visibility = View.GONE
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.llTotalDistance.visibility = View.GONE
                        Toast.makeText(this, "No Data Available", Toast.LENGTH_SHORT).show()
                            withContext(Dispatchers.Main) {
                                adapter.setCustomerList(visitList)
                            }
                        Toast.makeText(
                            applicationContext,
                            "Error Occured",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                }

                is Resource.Error -> {
                    // Log.e("SYNC", "$label sync failed: ${result.message}")
                    // Toast.makeText(requireContext(), "$label failed: ${result.message}", Toast.LENGTH_SHORT).show()



                }

                else -> {}
            }
        }
    }

}