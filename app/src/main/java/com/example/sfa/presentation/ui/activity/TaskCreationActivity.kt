package com.example.sfa.presentation.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.ActivityAddcustomerBinding
import com.example.sfa.databinding.ActivityTaskCreationBinding
import com.example.sfa.presentation.ui.fragment.SelectionBottomSheetFragment
import com.example.sfa.presentation.viewmodel.CustomerViewModel
import com.example.sfa.presentation.viewmodel.TaskViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar
@AndroidEntryPoint
class TaskCreationActivity:AppCompatActivity() {
    private lateinit var binding: ActivityTaskCreationBinding
    private val taskViewModel: TaskViewModel by viewModels()

    lateinit var dbController: DBController
    private  var salespersonList=ArrayList<SelectionModel>()
    private  var customerList=ArrayList<SelectionModel>()
    private  var routeList=ArrayList<SelectionModel>()
    var salespersonId:String=""
    var salespersonName:String=""
    var customerId:String=""
    var customerName:String=""
    var routeId:String=""
    var routeName:String=""
    var customerLabel:String="Customer"
    var geoAddress:String=""
    private  var latitude:String=""
    private  var longitude:String=""
    var  currentDt:String=""
    var  startDate:String=""
    var  endDate:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView() {
        dbController = DBController(applicationContext)
        customerLabel =
            Constant.getSetup("customer_label", "Customer", dbController, applicationContext)!!
        //binding.tvCustomerLabel.text = "$customerLabel Name"
        binding.tvCustomerName.hint = "Select  $customerLabel Name"
        getSalespersonList()
        getCustomerList()
        getRouteList()
        currentDt = TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        startDate = TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        endDate = TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        binding.tvStartDate.text = startDate
        binding.tvEndDte.text = endDate
        binding.layoutToolbar.tvTitle.text="Task Creation"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        binding.cvRoute.setOnClickListener {
            try {

                Log.e("cvroute","route1")
                SelectionBottomSheetFragment(
                    title = "Select a Route",
                    itemList = routeList
                ) { selected ->
                    binding.tvRoute.text = selected.name
                    routeId = selected.id
                    routeName = selected.name
                    Toast.makeText(
                        applicationContext,
                        "Selected: ${selected.name}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }.show(supportFragmentManager, "MySelectionSheet")
            }catch (e:Exception){
                Log.e("route error:",e.message.toString())
            }
        }
        binding.cvSalesperson.setOnClickListener {
            SelectionBottomSheetFragment(
                title = "Select a Person",
                itemList = salespersonList
            ) { selected ->
                binding.tvSalesperson.text = selected.name
                salespersonId = selected.id
                salespersonName = selected.name
                Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT)
                    .show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }
        binding.cvCustomer.setOnClickListener {
            SelectionBottomSheetFragment(
                title = "Select a Customer",
                itemList = customerList
            ) { selected ->
                binding.tvCustomerName.text = selected.name
                customerId = selected.id
                customerName = selected.name
                latitude = selected.lat.toString()
                longitude = selected.long.toString()
                geoAddress = selected.address
                Toast.makeText(applicationContext, "Selected: ${selected.name}", Toast.LENGTH_SHORT)
                    .show()
            }.show(supportFragmentManager, "MySelectionSheet")
        }

        binding.tvStartDate.setOnClickListener(View.OnClickListener {
            val day: Int
            val month: Int
            val year: Int
            if (binding.tvStartDate.getText().toString() != "") {
                val dateArray: Array<String> =
                    binding.tvStartDate.getText().toString().split("-".toRegex())
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
                    startDate = _pickedDate // _date +"/"+_month+"/"+_year;
                    binding.tvStartDate.setText(startDate)
                }, year, month, day
            )
            // dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
            dialog.datePicker.minDate = System.currentTimeMillis()

            dialog.show()
        })

        binding.tvEndDte.setOnClickListener(View.OnClickListener {
            val day: Int
            val month: Int
            val year: Int
            if (binding.tvEndDte.getText().toString() != "") {
                val dateArray: Array<String> =
                    binding.tvEndDte.getText().toString().split("-".toRegex())
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
                    endDate = _pickedDate // _date +"/"+_month+"/"+_year;
                    binding.tvEndDte.setText(endDate)
                }, year, month, day
            )
            // dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
            if (binding.tvStartDate.getText().toString() != "") {
                val dateArray: Array<String> =
                    binding.tvStartDate.getText().toString().split("-".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()
                val syear = dateArray[0].toInt()
                val smonth = dateArray[1].toInt()
                val sday = dateArray[2].toInt()
                val mCalendar = Calendar.getInstance()
                mCalendar.set(syear, smonth - 1, sday)
                dialog.datePicker.minDate = mCalendar.timeInMillis

            }

            dialog.show()
        })

        binding.btnCreateTask.setOnClickListener {
            if (binding.etTask.text.toString() == "") {
                Toast.makeText(applicationContext, "Enter Task Name", Toast.LENGTH_LONG).show()
            } else if (binding.tvSalesperson.text.toString().equals("")) {
                Toast.makeText(applicationContext, "Select the Salesperson", Toast.LENGTH_LONG)
                    .show()
            } else if (binding.tvCustomerName.text.toString().equals("")) {
                Toast.makeText(applicationContext, "Select the Customer", Toast.LENGTH_LONG).show()
            } else {
                saveTask()
            }

        }

        taskViewModel.saveTaskState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {

                    if (result.data!!.status) {
                        Toast.makeText(applicationContext, "Task Created Successfully", Toast.LENGTH_SHORT).show()
                        finish()
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

    fun getSalespersonList(){
        var dataResponse = dbController.getResponse(StringConstants.SALESPERSON_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {
            salespersonList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    if(!jsonObject.getString("Sp_Id").equals(SecureStorage.getString(applicationContext,StringConstants.SP_ID))){
                        val selectionModel: SelectionModel = SelectionModel(
                        jsonObject.getString("Sp_Id"),
                        jsonObject.getString("Sp_Name"))
                        salespersonList.add(selectionModel)

                    }
                }
                Log.e("commonlist", "SalespersonList: $salespersonList")
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

                    customerList.add(selectionModel)
                }
                Log.e("commonlist", "customerlist: $customerList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(applicationContext, "Customer List not available", Toast.LENGTH_SHORT).show()
        }
    }
    fun getRouteList(){
        var dataResponse = dbController.getResponse(StringConstants.ROUTE_DATA)
        if (dataResponse != null && !dataResponse.equals("")) {
            routeList.clear()
            try {
                val jsonArray = JSONArray(dataResponse)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val selectionModel: SelectionModel = SelectionModel(
                        jsonObject.getString("Route_Id"),
                        jsonObject.getString("Rout_Name")
                    )
                    routeList.add(selectionModel)
                }
                Log.e("commonlist", "routelist: $routeList")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(applicationContext, "Route List not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveTask(){
        if (Constant.isNetworkAvailable(applicationContext)) {

            val jsonObject = JsonObject()
            jsonObject.addProperty("taskName", binding.etTask.text.toString())
            jsonObject.addProperty("assignedToId",salespersonId)
            jsonObject.addProperty("assignedToName",salespersonName)
            jsonObject.addProperty("routeId",routeId)
            jsonObject.addProperty("routeName",routeName)
            jsonObject.addProperty("customerId",customerId)
            jsonObject.addProperty("customerName",customerName)
            jsonObject.addProperty("CustomerAddr", geoAddress)
            jsonObject.addProperty("CustomerLat", latitude)
            jsonObject.addProperty("CustomerLong", longitude)
            jsonObject.addProperty("startDate", startDate)
            jsonObject.addProperty("endDate", endDate)
            jsonObject.addProperty("taskDetail", binding.etTaskDetail.text.toString())
            jsonObject.addProperty("createdDate", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
            jsonObject.addProperty("assignedById", SecureStorage.getString(applicationContext, StringConstants.SP_ID))
            Log.e("token", "" + SecureStorage.getString(applicationContext, StringConstants.AUTH_TOKEN)!!)
            Log.e("task data", "" + jsonObject.toString())
            taskViewModel.saveTask(
                SecureStorage.getString(applicationContext, StringConstants.AUTH_TOKEN)!!, jsonObject
            )

        }
    }
}