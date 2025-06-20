package com.example.sfa.presentation.ui.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.CheckInDataModel
import com.example.sfa.data.model.FormCreation
import com.example.sfa.data.model.PiechartDataModel
import com.example.sfa.databinding.FragmentHomeBinding
import com.example.sfa.presentation.ui.activity.AssignedTaskActivity
import com.example.sfa.presentation.ui.activity.CustomFormOneActivity
import com.example.sfa.presentation.ui.activity.CustomerVisitActivity
import com.example.sfa.presentation.ui.activity.FollowupTaskActivity
import com.example.sfa.presentation.ui.activity.FormCreationActivity
import com.example.sfa.presentation.ui.activity.FormResponsesActivity
import com.example.sfa.presentation.ui.activity.TaskCreationActivity
import com.example.sfa.presentation.viewmodel.CustomerVisitViewModel
import com.example.sfa.presentation.viewmodel.MydayplanViewModel
import com.example.sfa.presentation.viewmodel.TaskViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class HomeFragment: Fragment()  {
    private var _homeBinding: FragmentHomeBinding? = null
    private val homeBinding get() = _homeBinding!!
    private val mydayplanViewModel: MydayplanViewModel by viewModels()
    lateinit var dbController: DBController
    private val customerVisitViewModel: CustomerVisitViewModel by viewModels()
    var customerLabel:String="Customer"


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
        homeBinding.tvDate.text=TimesUtil.getCurrentTime(TimesUtil.FORMAT1)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,context)!!
        homeBinding.tvTcLabel.text="Visited $customerLabel : "
        //homeBinding..text="$customerLabel Visit"
        homeBinding. tvTcLabel.text="T${customerLabel.get(0).toString()} : "
        homeBinding.tvPcLabel.text="P${customerLabel.get(0).toString()} : "
        if(SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE)==2){
          homeBinding.tvDesig.text="Admin"
            homeBinding.cvTaskCreation.visibility=View.VISIBLE
            homeBinding.llForm.visibility=View.VISIBLE
            homeBinding.llFormResponses.visibility=View.VISIBLE
            homeBinding.cvCustomerVisit.visibility=View.GONE

        }else if(SecureStorage.getInt(requireContext(),StringConstants.SP_TYPE)==3){
            homeBinding.tvDesig.text="Manager"
            homeBinding.cvTaskCreation.visibility=View.VISIBLE
            homeBinding.llForm.visibility=View.VISIBLE
            homeBinding.llFormResponses.visibility=View.VISIBLE
            homeBinding.cvCustomerVisit.visibility=View.VISIBLE

        } else{
            homeBinding.tvDesig.text="Sales Person"
            homeBinding.cvTaskCreation.visibility=View.VISIBLE
            homeBinding.llForm.visibility=View.GONE
            homeBinding.cvResponsesView.visibility=View.GONE
            homeBinding.cvCustomerVisit.visibility=View.VISIBLE
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
            intent.putExtra("from","home")
            intent.putExtra("type", 0)
            startActivity(intent)
        }

        homeBinding.cvFollowupTask.setOnClickListener{
            val intent = Intent(requireActivity(), FollowupTaskActivity::class.java)
            startActivity(intent)
        }


        mydayplanViewModel.getMydayplanState.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        val gson = Gson()
                        val jsonString = gson.toJson(result.data.data!!)
                        Log.e("Login_data", "" + jsonString.toString())
                        if (!dbController.updateProduct(StringConstants.MYDAYPLAN_DATA, jsonString.toString())) {
                            dbController.addProduct(StringConstants.MYDAYPLAN_DATA, jsonString.toString())
                        }
                        val intent = Intent(requireActivity(), CustomerVisitActivity::class.java)
                        startActivity(intent)


                    }
                    else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }
                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    LoadingUtil.showLoading(requireContext())
                    // Show loading indicator
                }

            }
        }




        customerVisitViewModel.getPiechartCustData.observe(requireActivity()) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                      //  Toast.makeText(requireContext(), "Today Followup Task List Updated", Toast.LENGTH_SHORT).show()
                        val piechartDataModel: PiechartDataModel = result.data.data!!
                        homeBinding.tvTc1.text=piechartDataModel.totalCust.toString()
                        homeBinding.tvPc1.text=piechartDataModel.assignedCust.toString()
                        homeBinding.tvBalance.text=piechartDataModel.visitedCust.toString()
                        setPieCharts(piechartDataModel.totalCust,piechartDataModel.assignedCust,piechartDataModel.visitedCust)
                    } else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }

                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    LoadingUtil.showLoading(requireContext())
                    // Show loading indicator
                }

            }
        }
        val activity = resources.getStringArray(R.array.Activity)

            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, activity)

            _homeBinding!!.spActivity.adapter = adapter

            _homeBinding!!.spActivity.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {

                    when(activity[position]){
                        "Today"->{
                            homeBinding.tvSelectedDateRange.visibility=View.GONE
                            homeBinding.tvSelectedDateRange.text=""

                            getPieChartData(TimesUtil.getCurrentTime(TimesUtil.FORMAT1),TimesUtil.getCurrentTime(TimesUtil.FORMAT1))
                        }
                        "This Week"->{
                            homeBinding.tvSelectedDateRange.visibility=View.GONE
                            homeBinding.tvSelectedDateRange.text=""
                            val (startDate, endDate) = Constant.getCurrentWeekStartAndEnd()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            Log.e("week","Start of week: ${startDate.format(formatter)}")
                            Log.e("week","End of week: ${endDate.format(formatter)}")

                            getPieChartData(startDate.format(formatter),endDate.format(formatter))

                        }
                        "This Month"->{
                            homeBinding.tvSelectedDateRange.visibility=View.GONE
                            homeBinding.tvSelectedDateRange.text=""
                            val (startDate, endDate) = Constant.getCurrentMonthStartAndEnd()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            Log.e("month","Start of month: ${startDate.format(formatter)}")
                            Log.e("month","End of month: ${endDate.format(formatter)}")

                            getPieChartData(startDate.format(formatter),endDate.format(formatter))

                        }
                        "Last Week"->{
                            homeBinding.tvSelectedDateRange.visibility=View.GONE
                            homeBinding.tvSelectedDateRange.text=""
                            val (startDate, endDate) = Constant.getLastWeekStartAndEnd()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            Log.e("month","Start of month: ${startDate.format(formatter)}")
                            Log.e("month","End of month: ${endDate.format(formatter)}")

                            getPieChartData(startDate.format(formatter),endDate.format(formatter))

                        }
                        "Last Month"->{
                            homeBinding.tvSelectedDateRange.visibility=View.GONE
                            homeBinding.tvSelectedDateRange.text=""
                            val (startDate, endDate) = Constant.getLastMonthStartAndEnd()
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            Log.e("month","Start of month: ${startDate.format(formatter)}")
                            Log.e("month","End of month: ${endDate.format(formatter)}")

                            getPieChartData(startDate.format(formatter),endDate.format(formatter))

                        }

                        "Custom Range"->{
                            datePickerDialog()
                        }
                    }
                    //Toast.makeText(requireContext(),"Selected Item: "+activity[position], Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }


    }

    fun getPieChartData(fromdate:String,todate:String){
        if(Constant.isNetworkAvailable(requireContext())) {
            customerVisitViewModel.getPiechartCustData(
                SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN)!!,
                SecureStorage.getString(requireContext(), StringConstants.SP_ID)!!,
                SecureStorage.getInt(requireContext(), StringConstants.SP_TYPE)!!,
                fromdate, todate)

        }else{
            Toast.makeText(requireContext(),"Please check your network connection",Toast.LENGTH_SHORT).show()
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

    private fun setPieCharts(tcc:Int,pcc:Int,vc:Int) {
        val noOfEntry: MutableList<PieEntry> = ArrayList()
        val mpc: Float = pcc.toFloat()
        val mvc: Float = vc.toFloat()
        if (mpc != 0f) noOfEntry.add(PieEntry(mpc, 0))
        noOfEntry.add(PieEntry(mvc, 1))

        val colors = ArrayList<Int>()

        colors.add(requireActivity().resources.getColor(R.color.ref_blue))
        colors.add(requireActivity().resources.getColor(R.color.ref_orange))

        if (noOfEntry.size > 0) {
            homeBinding.chart1.setVisibility(View.VISIBLE)

            val dataSet = PieDataSet(noOfEntry, "Calls")
            colors.add(ColorTemplate.getHoloBlue())
            dataSet.colors = colors
            val data = PieData(dataSet)
            homeBinding.chart1.setDrawEntryLabels(false)
            homeBinding.chart1.setData(data)
            homeBinding.chart1.getDescription().setEnabled(false)
            homeBinding.chart1.animateXY(5000, 5000)
            homeBinding.chart1.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if ((e?.data.toString()).equals("1")) {
                        Toast.makeText(activity, "Visted $customerLabel", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(activity, "Planned $customerLabel", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected() {
                }
            })
        } else {
            homeBinding.chart1.setVisibility(View.GONE)
        }
    }
    private fun datePickerDialog() {
        Log.e("date dialog","Dialog Started ")
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Select a date range")
        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDateString = sdf.format(Date(startDate))
            val endDateString = sdf.format(Date(endDate))
            val selectedDateRange = "$startDateString to $endDateString"
            println("Selected Date Range: $selectedDateRange")
            homeBinding.tvSelectedDateRange.visibility=View.VISIBLE
            homeBinding.tvSelectedDateRange.text= selectedDateRange
            getPieChartData(startDateString,endDateString)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _homeBinding = null
    }

}