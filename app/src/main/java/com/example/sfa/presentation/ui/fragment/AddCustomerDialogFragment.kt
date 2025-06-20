package com.example.sfa.presentation.ui.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.databinding.ActivityAddcustomerBinding
import com.example.sfa.presentation.ui.listener.OnCustomerAddedListener
import com.example.sfa.presentation.viewmodel.CustomerViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.EmailValidator
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.LocationProvider
import com.example.sfa.utils.PermissionUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class AddCustomerDialogFragment : DialogFragment() {

    private var _binding: ActivityAddcustomerBinding? = null
    private val binding get() = _binding!!

    private val custViewModel: CustomerViewModel by viewModels()
    lateinit var dbController: DBController

    private var sourceList = ArrayList<SelectionModel>()
    private var sourceOfLeadId: String = "0"
    private var sourceOfLeadName: String = ""
    private var customerLabel: String = "Customer"
    private var geoAddress: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var listener: OnCustomerAddedListener? = null

    fun setOnCustomerAddedListener(listener: OnCustomerAddedListener) {
        this.listener = listener
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityAddcustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        dbController = DBController(requireContext())
        customerLabel = Constant.getSetup("customer_label", "Customer", dbController, requireContext()) ?: "Customer"

        // Setup UI
        binding.tvCustomerLabelName.text = "$customerLabel Name"
        binding.etNewCustomer.hint = "Enter $customerLabel Name"
        binding.layoutToolbar.tvTitle.text = "Add $customerLabel"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_close)
        binding.layoutToolbar.menubtn.setOnClickListener {
            dismiss()
        }

        binding.ibRefresh.setOnClickListener {
            getCurrentLocation()
        }

        getSourceOfLeadList()
        getCurrentLocation()

        binding.cvSourceOfLead.setOnClickListener {
            SelectionBottomSheetFragment(title = "Select the Source", itemList = sourceList) { selected ->
                binding.tvSourceOfLead.text = selected.name
                sourceOfLeadId = selected.id
                sourceOfLeadName = selected.name
                binding.cvOthers.visibility = if (selected.id == "4") View.VISIBLE else View.GONE
            }.show(childFragmentManager, "MySelectionSheet")
        }

        binding.etAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    latitude = ""
                    longitude = ""
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSave.setOnClickListener {
            validateAndSave()
        }

        observeSaveCustomer()

    }

    private fun observeSaveCustomer() {
        custViewModel.saveCustState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    LoadingUtil.hideLoading()
                    if (result.data!!.status) {
                        Toast.makeText(requireContext(), "$customerLabel Saved Successfully", Toast.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                          try{

                            getCustomer()
                        } catch (e: CancellationException) {
                            Log.e("Cancelled", "Coroutine was cancelled: ${e.message}")
                        } catch (e: Exception) {
                            Log.e("Exception", "Exception: ${e.message}")
                        }}
                    } else {
                        Toast.makeText(requireContext(), result.data!!.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Error -> {
                    LoadingUtil.hideLoading()
                    Toast.makeText(requireContext(), result.message ?: "Error", Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> LoadingUtil.showLoading(requireContext())
            }
        }
    }

    private fun validateAndSave() {
        with(binding) {
            val name = etNewCustomer.text.toString()
            val addr = etAddress.text.toString()
            val city = etCity.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()

            if (name.isEmpty()) showToast("Enter $customerLabel Name")
            else if (addr.isEmpty()) showToast("Enter $customerLabel Address")
            else if (city.isEmpty()) showToast("Enter $customerLabel City")
            else if (phone.isEmpty()) showToast("Enter $customerLabel Phone")
            else if (!isValidPhoneNumber(phone)) showToast("Enter Valid Phone Number")
            else if (email.isNotEmpty() && !EmailValidator.isValid(email)) showToast("Enter Valid Email")
            else {
                saveCustomer(
                    name, addr, city, email, phone,
                    etPannumber.text.toString(), etGstno.text.toString(),
                    etOrganization.text.toString()
                )
            }
        }
    }

    private fun saveCustomer(name: String, addr: String, city: String, email: String, phone: String, panNo: String, gstNo: String, organizationName: String) {
        if (Constant.isNetworkAvailable(requireContext())) {
            val jsonObject = JsonObject().apply {
                addProperty("name", name)
                addProperty("billingAddr", addr)
                addProperty("city", city)
                addProperty("eMail", email)
                addProperty("phone", phone)
                addProperty("organizationNm", organizationName)
                addProperty("panNo", panNo)
                addProperty("gstNo", gstNo)
                addProperty("lat", latitude)
                addProperty("long", longitude)
                addProperty("geoAddr", geoAddress)
                addProperty("createdDt", TimesUtil.getCurrentTime(TimesUtil.FORMAT))
                addProperty("sourceOfLeadId", sourceOfLeadId)
                addProperty("sourceOfLeadName", if (sourceOfLeadId == "4") binding.tvOtherSourceOfLead.text.toString() else binding.tvSourceOfLead.text.toString())
                addProperty("spId", SecureStorage.getString(requireContext(), StringConstants.SP_ID))
            }

            val token = SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN) ?: return
            custViewModel.saveCustomer(token, jsonObject)
        }
    }

    private suspend fun getCustomer() {
        val jsonObject = JsonObject().apply {
            addProperty("spName", SecureStorage.getString(requireContext(), StringConstants.SP_NAME))
            addProperty("spType", SecureStorage.getInt(requireContext(), StringConstants.SP_TYPE))
            addProperty("spId", SecureStorage.getString(requireContext(), StringConstants.SP_ID))
        }

        val token = SecureStorage.getString(requireContext(), StringConstants.AUTH_TOKEN) ?: return
        val results = custViewModel.getCustomer(token, jsonObject)
        if (results is Resource.Success) {
           /* try {
                val responseStr = results.data?.string() ?: ""
                val json = JSONTokener(responseStr).nextValue()

                val jsonArray = if (json is JSONObject && json.getBoolean("status")) json.getJSONArray("data") else JSONArray()

                    if (!dbController.updateProduct(
                            StringConstants.CUSTOMER_DATA,
                            jsonArray.toString()
                        )
                    ) {
                        dbController.addProduct(StringConstants.CUSTOMER_DATA, jsonArray.toString())
                    }
                    dismiss()

            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }*/
            try {
                var json = JSONTokener(results.data!!.string()).nextValue()
                var jsonArray = JSONArray()
                if (json is JSONObject) {
                    if (json.getBoolean("status")) {
                        if (json is JSONObject) {
                            jsonArray = json.getJSONArray("data")
                            //withContext(Dispatchers.Main) {

                            if (!dbController.updateProduct(
                                    StringConstants.CUSTOMER_DATA,
                                    jsonArray.toString()
                                )
                            ) {
                                dbController.addProduct(
                                    StringConstants.CUSTOMER_DATA,
                                    jsonArray.toString()
                                )
                            }
                            dismiss()
                            // }

                        }
                    }
                }
            }catch (e:Exception){
                Toast.makeText(requireContext(),"Error: "+e.message,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (!PermissionUtil.isLocationPermissionGranted(requireContext())) {
            PermissionUtil.requestLocationPermission(requireActivity())
        } else {
            val locationProvider = LocationProvider(requireContext())
            locationProvider.getCurrentLocation { location ->
                location?.let {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val list = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                    geoAddress = list!![0].getAddressLine(0)
                    binding.etAddress.setText(geoAddress)
                }
            }
        }
    }

    private fun getSourceOfLeadList() {
        dbController.getResponse(StringConstants.SOURCEOFLEAD_DATA)?.takeIf { it.isNotEmpty() }?.let {
            try {
                sourceList.clear()
                val jsonArray = JSONArray(it)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    sourceList.add(SelectionModel(jsonObject.getString("id"), jsonObject.getString("name")))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } ?: showToast("Source of Lead List not available")
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun isValidPhoneNumber(mobileNum: String): Boolean{
        if(mobileNum.equals("0000000000")||mobileNum.get(0).toString().equals("0"))
        {
            return false
        }else if(mobileNum.length == 10 && android.util.Patterns.PHONE.matcher(mobileNum).matches() ) {
            return true
        }else {
            return false
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onCustomerAdded()
    }
}
