package com.example.sfa.presentation.ui.activity

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.model.CustomDynamicDataModel
import com.example.sfa.data.model.SelectionModel
import com.example.sfa.data.model.VisitCusReportModel
import com.example.sfa.databinding.ActivityCustomFormBinding
import com.example.sfa.databinding.ActivityCustomformDetailBinding
import com.example.sfa.presentation.viewmodel.FormViewModel
import com.example.sfa.presentation.viewmodel.ImageUploadViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.LoadingUtil
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
@AndroidEntryPoint
class CustomFormDetailActivity:AppCompatActivity() {
    private lateinit var binding: ActivityCustomformDetailBinding
    private lateinit var dbController: DBController
    private val formViewModel: FormViewModel by viewModels()
    private val imageUploadViewModel: ImageUploadViewModel by viewModels()


    var moduleId: Int = 0
    var moduleName: String = ""
    var store_list = ArrayList<CustomDynamicDataModel>()
    var group_list = ArrayList<CustomDynamicDataModel>()
    var master_list = ArrayList<CustomDynamicDataModel>()
    var dialog: DatePickerDialog? = null
    val imageViewMap = mutableMapOf<Int, ImageView>()
    val textViewMap = mutableMapOf<Int, TextView>()
    val tickImgViewMap = mutableMapOf<Int, ImageView>()
    val wrongImgViewMap = mutableMapOf<Int, ImageView>()
    var picturePathFinal1: String? = null
    var currentScreenTimeStamp: String = ""
    var retailerCode: String = ""
    var currenttime: String = ""
    var isDateShow: Boolean = false
    var isTimeShow: Boolean = false
    var isFromDateEmpty: Boolean = true
    var isToDateEmpty: Boolean = true
    var isDateclicked: Boolean = false
    var isTimeclicked: Boolean = false
    var isFromTimeEmpty: Boolean = true
    var isToTimeEmpty: Boolean = true
    var secFlag: Int = 0
    val CAMERA_PERMISSION_CODE: Int = 101
    private lateinit var imageUri: Uri
    private var cameraLauncher: ActivityResultLauncher<Intent>? = null
    val ACTIVITY_REQUEST_CODE: Int = 2
    var selectedKey=0
    var custId=""
    var type=""
    var customerLabel:String="Customer"
    var screenType=0
    var screenFrom="visit"
    var checkInId="0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomformDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.layoutToolbar.tvTitle.text = "Form Entry"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dbController = DBController(applicationContext)

        val intent = intent
        if (intent.hasExtra("title")) {
            title = intent.getStringExtra("title")
        }
        if (intent.hasExtra("moduleName")) {
            moduleName = intent.getStringExtra("moduleName")!!
        }
        if (intent.hasExtra("moduleId")) {
            moduleId = intent.getIntExtra("moduleId", 0)
        }
        if (intent.hasExtra("custId")) {
            custId = intent.getStringExtra("custId")!!
        }
        if (intent.hasExtra("screenType")) {
            screenType = intent.getIntExtra("screenType", 0)
        }
        if (intent.hasExtra("screenFrom")) {
            screenFrom = intent.getStringExtra("screenFrom")!!
        }
        if (intent.hasExtra("checkInId")) {
            checkInId = intent.getStringExtra("checkInId")!!
        }

        binding.layoutToolbar.tvTitle.text=title
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,this)!!

        binding.tvNameLabel.text="$customerLabel Name"
        binding.tvAddrLabel.text="$customerLabel Address"
        binding.tvContactLabel.text="$customerLabel Contact No"

        if(screenType==1){
            binding.cvCustomerDetail.visibility= View.VISIBLE
            getCustomerDetail(custId)
        }else{
            binding.cvCustomerDetail.visibility= View.GONE

        }
        currentScreenTimeStamp = "admin" + "-" + TimesUtil.getTimeStamp(
            TimesUtil.getCurrentTime(TimesUtil.FORMAT),
            TimesUtil.FORMAT
        )
        currenttime=TimesUtil.getCurrentTime(TimesUtil.FORMAT)
        binding.tvFormNo.text = "Form No : $currentScreenTimeStamp"
        binding.tvHeadName.text = moduleName
        lifecycleScope.launch(Dispatchers.IO) {

            getCustomData(moduleId)
        }

        binding.btnSubmit!!.setOnClickListener(View.OnClickListener {
            var checked_All = true
            var count = 0
            for (i in store_list.indices) {
                if ((store_list[i].data == null) || (store_list[i].data.trim()
                        .isEmpty()) && (!isFromDateEmpty || !isToDateEmpty || store_list[i].mandatory === 1)
                ) {
                    count++
                    try {
                        if (store_list[i].mandatory === 1 && (store_list[i].data == null || store_list[i].data
                                .trim().isEmpty())
                        ) {
                            Toast.makeText(
                                applicationContext,
                                "Please fill all the mandatory fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            checked_All = false
                            break
                        } else if (isDateclicked && isDateShow && (isFromDateEmpty)) {
                            Toast.makeText(
                                applicationContext,
                                "Please fill all Date fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            checked_All = false
                            break
                        } else if (isDateclicked && isDateShow && (isToDateEmpty)) {
                            Toast.makeText(
                                applicationContext,
                                "Please fill all Date fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            checked_All = false
                            break
                        } else if (isTimeclicked && isTimeShow && (isFromTimeEmpty || isToTimeEmpty)) {
                            Toast.makeText(
                                applicationContext,
                                "Please fill all Time fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            checked_All = false
                            break
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
            if (checked_All) {
                saveDynamicData()
            }
        })

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && imageUri != null) {
                try {
                    if(imageViewMap.containsKey(selectedKey)) {
                        uploadImage(this, imageUri, "My Image Description",selectedKey!!.toInt(),"Camera")
                    }


                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading image! ${e.toString()}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image URI is null!", Toast.LENGTH_SHORT).show()
            }

            imageUploadViewModel.uploadImgState.observe(this) { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data!!.status) {
                            //Log.d("Upload", "Success: ${json.getString("message")}")
                            val filePath = getRealPathFromUri(imageUri)

                            if (type.equals("Camera")) {
                                val imageViewToModify = imageViewMap[selectedKey]
                                val textViewToModify = textViewMap[selectedKey]
                                textViewToModify!!.visibility = View.VISIBLE
                                val tickImgToModify = tickImgViewMap[selectedKey]
                                val wrongImgToModify = wrongImgViewMap[selectedKey]
                                if (imageViewToModify != null) {

                                    tickImgToModify!!.visibility = View.VISIBLE
                                    wrongImgToModify!!.visibility = View.INVISIBLE
                                    var imgNm = File(filePath).name.toString()
                                    textViewToModify.setText(imgNm)
                                    /*  Glide.with(applicationContext)
                                          .load(ApiClient.baseImgUrl + imgNm)
                                          .placeholder(R.drawable.ic_add_customer_visit)
                                          .error(R.drawable.ic_warninghead)
                                          .into(ivShowImage)*/

                                    for (i in store_list.indices) {
                                        val reqId =
                                            if (selectedKey == 0) "00" else selectedKey.toString()
                                        if (store_list[i].imgKey != null && store_list[i].imgKey
                                                .equals(reqId)
                                        ) {
                                            store_list[i].data = imgNm
                                        }
                                    }
                                }
                            }else{

                                if(imageViewMap.containsKey(selectedKey)) {
                                    val imageViewToModify = imageViewMap[selectedKey]
                                    val textViewToModify = textViewMap[selectedKey]
                                    textViewToModify!!.visibility = View.VISIBLE
                                    val tickImgToModify = tickImgViewMap[selectedKey]
                                    val wrongImgToModify = wrongImgViewMap[selectedKey]
                                    if (imageViewToModify != null) {

                                        tickImgToModify!!.visibility = View.VISIBLE
                                        wrongImgToModify!!.visibility = View.INVISIBLE
                                        var imgNm = File(filePath).name.toString()
                                        textViewToModify.setText(imgNm)
                                        /*  Glide.with(applicationContext)
                                          .load(ApiClient.baseImgUrl + imgNm)
                                          .placeholder(R.drawable.ic_add_customer_visit)
                                          .error(R.drawable.ic_warninghead)
                                          .into(ivShowImage)*/

                                        for (i in store_list.indices) {
                                            val reqId =
                                                if (selectedKey == 0) "00" else selectedKey.toString()
                                            if (store_list[i].imgKey != null && store_list[i].imgKey
                                                    .equals(reqId)
                                            ) {
                                                store_list[i].data = imgNm
                                            }
                                        }
                                    }
                                }
                            }

                            Log.d("ImagePath", "Selected Image Path: $filePath")
                            Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                        }
                        LoadingUtil.hideLoading()

                    }
                    is Resource.Error -> {
                        LoadingUtil.hideLoading()
                        Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        LoadingUtil.showLoading(this)
                        // Show loading indicator
                    }

                }
            }


            formViewModel.saveCustomformState.observe(this) { result ->
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
                        // Show loading indicator
                        LoadingUtil.showLoading(this)

                    }

                }
            }
        }
    }
    private fun openCamera(key:Int) {
        Log.e("imgkey",""+key)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Captured by Camera")
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyCameraApp") // Saves in DCIM/Pictures
        }

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        if (imageUri != null) {
            refreshGallery(imageUri!!)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                putExtra("key",key)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            selectedKey=key
            type="Camera"
            cameraLauncher!!.launch(intent)
        } else {
            Toast.makeText(this, "Failed to create image file!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun refreshGallery(uri: Uri) {
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scanIntent.data = uri
        sendBroadcast(scanIntent)
    }

    fun getCustomerDetail(custId:String){
        var dataResponse=dbController.getResponse(StringConstants.CUSTOMER_DATA)
        if(dataResponse!=null && !dataResponse.equals("")){
            val jsonArray: JSONArray = JSONArray(dataResponse)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                if( jsonObject.getString("Cust_Id").equals(custId)){
                    binding.tvCustomerName.text=jsonObject.getString("Cust_Name")
                    binding.tvCustomerAddr.text=jsonObject.getString("Cust_Billing_Address")
                    binding.tvCustomerCondactNo.text=jsonObject.getString("Cust_Phone")
                }

            }
        }
    }
    suspend fun getCustomData(moduleId: Int) {
        val result = formViewModel.getCustomFieldData(SecureStorage.getString(applicationContext,com.example.sfa.utils.StringConstants.AUTH_TOKEN)!!,
            SecureStorage.getString(applicationContext,com.example.sfa.utils.StringConstants.SP_ID)!!,moduleId)
        when (result) {
            is Resource.Success -> {
                LoadingUtil.hideLoading()

                var json = JSONTokener(result.data!!.string()).nextValue()
                var jsonArray = JSONArray()

                if (json is JSONArray) {
                    jsonArray = JSONArray(result.data)

                } else if (json is JSONObject) {

                    if (json.getBoolean("status")) {
                        val jsonObject1 = json.getJSONObject("data")
                        val array = JSONArray(jsonObject1.getString("customGrp"))
                        val jsonArray = JSONArray(jsonObject1.getString("customData"))

                        withContext(Dispatchers.Main) {
                            loadData(array, jsonArray)

                        }

                    } else {

                        binding.tvNoData.visibility = View.VISIBLE
                        binding.tvNoData.visibility = View.GONE

                    }
                }
            }
            is Resource.Error -> {
                LoadingUtil.hideLoading()
                binding.tvNoData.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE

            }is Resource.Loading -> {

             LoadingUtil.showLoading(this)
            }
            else -> {}
        }
    }
    fun uploadImage(context: Context, imageUri: Uri, description: String, key:Int, typ:String){

        if(Constant.isNetworkAvailable(applicationContext)){
            imageUploadViewModel.uploadImage(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                context,imageUri,description)
        }
    }
    fun showPhotoDialog(key:Int){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_upload_image)
        val ibClose = dialog.findViewById<ImageButton>(R.id.ib_close)
        val ibCamera = dialog.findViewById<ImageButton>(R.id.ib_camera)
        val ibGallery = dialog.findViewById<ImageButton>(R.id.ib_gallery)
        ibClose.setOnClickListener { dialog.dismiss() }

        ibCamera.setOnClickListener {
            checkPermissionsAndOpenCamera(key)
            dialog.dismiss()
        }

        ibGallery.setOnClickListener {
            checkImageAccess(key)
            dialog.dismiss()
        }
        dialog.show()

    }
    private fun getRealPathFromUri(uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }

        return filePath
    }
    private fun checkPermissionsAndOpenCamera(key:Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera(key)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }


    private fun checkImageAccess(key:Int){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                openGallery(key)
            }
        } else {
            openGallery(key)
        }
    }

    fun openGallery(key:Int) {
        selectedKey=key
        type="Gallery"
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra("key",key)
        intent.type = "image/*"
        startActivityForResult(intent, PICTURE_ID_GALLERY)
    }

    companion object {
        private const val PICTURE_ID_GALLERY = 123
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (resultCode == RESULT_OK) {
           if (requestCode ==PICTURE_ID_GALLERY) {
                val imageUri: Uri? = data!!.data
                Log.d("CameraResult", "data received: "+data.toString())

                // val key = data.getIntExtra("key", 0)
                if (imageUri != null) {
                    uploadImage(this, imageUri, "My Image Description",selectedKey,"Gallery")

                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun loadData(array: JSONArray, jsonArray: JSONArray) {
        try {
            binding.tvNoData.visibility = View.VISIBLE
            binding.tvFormNo.visibility = View.GONE

            if (array.length() > 0) {
                for (jk in 0 until array.length()) {
                    val jsonObject2 = array.getJSONObject(jk)
                    val grpId = jsonObject2.getInt("FieldGroupId")
                    val grpName = jsonObject2.getString("FieldGroupName")
                    val fgTableName = jsonObject2.getString("FGTableName")

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    val verticalMargin = 10
                    val horizontalMargin = 15
                    val verticalMarginInDp =
                        (verticalMargin * resources.displayMetrics.density).toInt()
                    val horizontalMarginInDp =
                        (horizontalMargin * resources.displayMetrics.density).toInt()
                    params.setMargins(
                        horizontalMarginInDp,
                        verticalMarginInDp,
                        horizontalMarginInDp,
                        verticalMarginInDp
                    )

                    val textView30 = TextView(applicationContext).apply {
                        setTextColor(resources.getColor(R.color.primary, null))
                        textSize = 18f
                        setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                        setPadding(
                            (18 * resources.displayMetrics.density).toInt(),
                            0, 0, 0
                        )
                        text = grpName
                    }

                    binding.llDynamicData.addView(textView30)
                    textView30.visibility = View.GONE

                    val dataModel = CustomDynamicDataModel(
                        data = "",
                        mandatory = 0,
                        fldGrpId = grpId,
                        moduleId = 0,
                        fldType = "",
                        grpTableName = fgTableName,
                        fldGrpName = grpName,
                        fldSrcFld = "",
                        moduleName = ""
                    )
                    group_list.add(dataModel)

                    if (jsonArray.length() > 0) {
                        binding.tvNoData.visibility = View.GONE
                        binding.tvFormNo.visibility = View.VISIBLE

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            var fieldGroupId = 0
                            if (jsonObject.getString("FieldGroupId") != null && jsonObject.getString(
                                    "FieldGroupId"
                                ) != "null"
                            ) {
                                fieldGroupId = jsonObject.getInt("FieldGroupId")
                            }

                            if (grpId == fieldGroupId) {
                                textView30.visibility = View.VISIBLE
                                val headingLabel = jsonObject.getString("Field_Name")
                                val typeToAdd = jsonObject.getString("Fld_Type")
                                val moduleId = jsonObject.getString("ModuleId")
                                // val symbolCurrency = jsonObject.getString("Fld_Symbol")
                                val columnStore = jsonObject.getString("Field_Col")
                                val srcName = jsonObject.getString("Fld_Src_Name")
                                val srcField = jsonObject.getString("Fld_Src_Field")
                                val textLength = jsonObject.getInt("Fld_Length")
                                val mandate = jsonObject.getInt("Mandate")
                                val flag = jsonObject.getInt("flag")
                                val tableName = jsonObject.getString("FGTableName")
                                if(screenType==1) {
                                    binding.btnSubmit!!.visibility = View.VISIBLE
                                }else{
                                    binding.btnSubmit!!.visibility = View.GONE
                                }

                                // Handle the edittext for text/phone number
                                if (typeToAdd.contains("TA") || typeToAdd == "N" || typeToAdd == "NP" ||
                                    typeToAdd == "TAS" || typeToAdd == "TAM"
                                ) {

                                    val card = CardView(applicationContext).apply {
                                        layoutParams = params
                                        radius = 5f
                                        setCardBackgroundColor(Color.WHITE)
                                        maxCardElevation = 15f
                                        cardElevation = 10f
                                    }

                                    val textView = TextView(applicationContext).apply {
                                        setTextColor(Color.BLACK)
                                        textSize = 15f
                                        setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                        setPadding(
                                            (18 * resources.displayMetrics.density).toInt(),
                                            0, 0, 0
                                        )

                                        // Set the text with HTML formatting if required
                                        val formattedText: CharSequence = if (mandate == 1) {
                                            Html.fromHtml("$headingLabel<font color='red'> *</font>", Html.FROM_HTML_MODE_LEGACY)
                                        } else {
                                            headingLabel
                                        }

                                        // Set the formatted text to the TextView
                                        text = formattedText
                                    }


                                    val editText = EditText(applicationContext).apply {
                                        hint = "Enter the Data"
                                        textSize = 14f
                                        setTextColor(Color.BLACK)
                                        setPadding(
                                            (20 * resources.displayMetrics.density).toInt(),
                                            (15 * resources.displayMetrics.density).toInt(),
                                            (20 * resources.displayMetrics.density).toInt(),
                                            (15 * resources.displayMetrics.density).toInt()
                                        )
                                        setBackgroundColor(Color.TRANSPARENT)

                                        if (typeToAdd == "N" || typeToAdd == "NP") {
                                            setSingleLine(true)
                                            filters = arrayOf(InputFilter.LengthFilter(textLength))
                                            inputType =
                                                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                        } else if (typeToAdd == "TAS") {
                                            setSingleLine(true)
                                            filters = arrayOf(InputFilter.LengthFilter(textLength))
                                            inputType = InputType.TYPE_CLASS_TEXT
                                        } else {
                                            setSingleLine(false)
                                            filters = arrayOf(InputFilter.LengthFilter(textLength))
                                            inputType =
                                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                        }
                                    }

                                    val dynamicDataModel = CustomDynamicDataModel(
                                        data = "",  // The actual value will be set from the EditText input
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(), // You may want to set moduleId based on jsonObject or other sources
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId // Assuming moduleId can be mapped to moduleName
                                    )
                                    dynamicDataModel.column=columnStore

                                    editText.addTextChangedListener(object : TextWatcher {
                                        override fun beforeTextChanged(
                                            charSequence: CharSequence?,
                                            start: Int,
                                            count: Int,
                                            after: Int
                                        ) {
                                        }

                                        override fun onTextChanged(
                                            charSequence: CharSequence?,
                                            start: Int,
                                            before: Int,
                                            count: Int
                                        ) {
                                        }

                                        override fun afterTextChanged(editable: Editable?) {
                                            dynamicDataModel.data = editable.toString()
                                        }
                                    })

                                    // Put the EditText inside CardView
                                    card.addView(editText)
                                    if(screenType==1) {
                                        editText.isEnabled=true
                                        editText.hint="Enter the data"
                                    }else{
                                        editText.isEnabled=false
                                        editText.hint=""
                                    }
                                    store_list.add(dynamicDataModel)
                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                else if (typeToAdd == "L") {

                                    // Create a new TextView
                                    val textView = TextView(applicationContext)

                                    // Set TextView attributes
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(),
                                        0,
                                        0,
                                        0
                                    )

                                    // Prepare the text with HTML formatting if needed
                                    val text = "$headingLabel<font color='red'> *</font>"
                                    if (mandate == 1) {
                                        textView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
                                    } else {
                                        textView.text = headingLabel
                                    }

                                    // Create the dynamic data model and set attributes
                                    val dynamicDataModel = CustomDynamicDataModel(
                                        data = "",  // The actual value will be set from the EditText input
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(), // You may want to set moduleId based on jsonObject or other sources
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId // Assuming moduleId can be mapped to moduleName
                                    )

                                    dynamicDataModel.column=columnStore
                                    // Add the data model to the store list
                                    store_list.add(dynamicDataModel)

                                    // Add the TextView to the dynamic layout
                                    binding.llDynamicData.addView(textView)
                                }
                                else if (typeToAdd == "DR") {
                                    val textView = TextView(applicationContext)
                                    isDateShow = true
                                    val card = CardView(applicationContext)
                                    val layout = LinearLayout(applicationContext)
                                    val fromDate = TextView(applicationContext)
                                    val toDate = TextView(applicationContext)
                                    val between = TextView(applicationContext)

                                    val dataModel = CustomDynamicDataModel(
                                        data = "",  // The actual value will be set from the EditText input
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(), // You may want to set moduleId based on jsonObject or other sources
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId // Assuming moduleId can be mapped to moduleName
                                    )  // Assuming dynamicDataModel is a data class or model you already defined
                                    dataModel.column=columnStore
                                    card.layoutParams = params
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.gravity = Gravity.CENTER
                                    layout.weightSum = 3f

                                    val param1 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                    )

                                    fromDate.layoutParams = param1
                                    between.layoutParams = param1
                                    toDate.layoutParams = param1

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(),
                                        0,
                                        0,
                                        0
                                    )

                                    val text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    fromDate.hint = "Select From Date"
                                    fromDate.gravity = Gravity.CENTER
                                    fromDate.textSize = 14f
                                    fromDate.setTextColor(Color.BLACK)
                                    fromDate.setPadding(
                                        (0 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt(),
                                        (0 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt()
                                    )
                                    fromDate.setBackgroundColor(Color.TRANSPARENT)

                                    fromDate.setOnClickListener {
                                        val c = Calendar.getInstance()
                                        var day = c.get(Calendar.DAY_OF_MONTH)
                                        var month = c.get(Calendar.MONTH)
                                        var year = c.get(Calendar.YEAR)

                                        if (fromDate.text.isNotEmpty()) {
                                            val dateArray = fromDate.text.toString().split("/")
                                            day = dateArray[0].toInt()
                                            month = dateArray[1].toInt() - 1
                                            year = dateArray[2].toInt()
                                        }

                                        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                                            val _year = year.toString()
                                            val _month = (month + 1).takeIf { it < 10 }?.let { "0$it" } ?: (month + 1).toString()
                                            val _date = dayOfMonth.takeIf { it < 10 }?.let { "0$it" } ?: dayOfMonth.toString()
                                            val _pickedDate = "$year-$_month-$_date"

                                            fromDate.text = "$_date/$_month/$_year"
                                            isDateclicked = true
                                            isFromDateEmpty = false
                                            dataModel.data = "${fromDate.text}  to  ${toDate.text}"
                                        }, year, month, day)

                                        dialog.show()
                                    }

                                    toDate.hint = "Select To Date"
                                    toDate.gravity = Gravity.CENTER
                                    toDate.textSize = 14f
                                    toDate.setTextColor(Color.BLACK)
                                    toDate.setPadding(
                                        (0 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt(),
                                        (0 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt()
                                    )
                                    toDate.setBackgroundColor(Color.TRANSPARENT)

                                    toDate.setOnClickListener {
                                        val c = Calendar.getInstance()
                                        var day = c.get(Calendar.DAY_OF_MONTH)
                                        var month = c.get(Calendar.MONTH)
                                        var year = c.get(Calendar.YEAR)

                                        if (toDate.text.isNotEmpty()) {
                                            val dateArray = toDate.text.toString().split("/")
                                            day = dateArray[0].toInt()
                                            month = dateArray[1].toInt() - 1
                                            year = dateArray[2].toInt()
                                        }

                                        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                                            val _year = year.toString()
                                            val _month = (month + 1).takeIf { it < 10 }?.let { "0$it" } ?: (month + 1).toString()
                                            val _date = dayOfMonth.takeIf { it < 10 }?.let { "0$it" } ?: dayOfMonth.toString()
                                            val _pickedDate = "$year-$_month-$_date"

                                            toDate.text = "$_date/$_month/$_year"
                                            isToDateEmpty = false
                                            isDateclicked = true
                                            dataModel.data = "${fromDate.text}  to  ${toDate.text}"
                                        }, year, month, day)

                                        dialog.show()
                                    }

                                    between.text = "  to  "
                                    between.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    between.gravity = Gravity.CENTER
                                    between.textSize = 14f
                                    between.setTextColor(Color.BLACK)



                                    store_list.add(dataModel)

                                    layout.addView(fromDate)
                                    layout.addView(between)
                                    layout.addView(toDate)

                                    card.addView(layout)

                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                else if (typeToAdd == "D") {
                                    val textView = TextView(applicationContext)

                                    val card = CardView(applicationContext)
                                    val layout = LinearLayout(applicationContext)
                                    val fromDate = TextView(applicationContext)
                                    val dataModel = CustomDynamicDataModel(
                                        data = "",  // The actual value will be set from the EditText input
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(), // You may want to set moduleId based on jsonObject or other sources
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId // Assuming moduleId can be mapped to moduleName
                                    )   // Assuming dynamicDataModel is a data class or model you already defined
                                    dataModel.column=columnStore
                                    card.layoutParams = params
                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.gravity = Gravity.CENTER
                                    // layout.weightSum = 3f // Removed as it was commented out in your code

                                    val param1 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                    )

                                    fromDate.layoutParams = param1

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(),
                                        0,
                                        0,
                                        0
                                    )

                                    val text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    fromDate.hint = "Select The Date"
                                    fromDate.gravity = Gravity.START
                                    fromDate.textSize = 14f
                                    fromDate.setTextColor(Color.BLACK)
                                    fromDate.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt(),
                                        (0 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt()
                                    )
                                    fromDate.setBackgroundColor(Color.TRANSPARENT)

                                    fromDate.setOnClickListener {
                                        var day = 0
                                        var month = 0
                                        var year = 0
                                        if (fromDate.text.isNotEmpty()) {
                                            val dateArray = fromDate.text.toString().split("/")
                                            day = dateArray[0].toInt()
                                            month = dateArray[1].toInt() - 1
                                            year = dateArray[2].toInt()
                                        } else {
                                            val c = Calendar.getInstance()
                                            day = c.get(Calendar.DAY_OF_MONTH)
                                            month = c.get(Calendar.MONTH)
                                            year = c.get(Calendar.YEAR)
                                        }

                                        val dialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                                            val _year = year.toString()
                                            val _month = if (month + 1 < 10) "0${month + 1}" else (month + 1).toString()
                                            val _date = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                                            val _pickedDate = "$year-$_month-$_date"
                                            fromDate.text = "$_date/$_month/$_year"
                                            dataModel.data = fromDate.text.toString()
                                        }, year, month, day)

                                        dialog.show()
                                    }


                                    store_list.add(dataModel)

                                    layout.addView(fromDate)
                                    card.addView(layout)

                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                else if (typeToAdd == "S" ||typeToAdd == "SSO" || typeToAdd == "SSM") {
                                    val card = CardView(applicationContext)
                                    val textView = TextView(applicationContext)
                                    // Need to change this to the length of getting value from the server
                                    val selectionSpinner = Spinner(applicationContext)
                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(),
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId)

                                    dataModel.column=columnStore
                                    card.layoutParams = params
                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(),
                                        0,
                                        0,
                                        0
                                    )

                                    val text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    val strList = ArrayList<String>()
                                    var srcSplit: Array<String> = arrayOf()
                                    var selectionModels = arrayOf<SelectionModel>()

                                    if (typeToAdd == "SSO"||typeToAdd == "S") {

                                        srcSplit = srcField.split(",").toTypedArray()
                                        strList.addAll(srcSplit)
                                        selectionModels = Array(strList.size + 1) {
                                            SelectionModel("","")
                                        }
                                        selectionModels[0].apply {
                                            name = "Select Data"
                                            id = "0"
                                        }
                                        strList.forEachIndexed { index, item ->
                                            val pos = index + 1
                                            selectionModels[pos] =  SelectionModel("","").apply {
                                                name = item
                                                id = pos.toString()
                                            }
                                        }
                                    }

                                    val arrayAdapter = SpinAdapter(applicationContext, android.R.layout.simple_spinner_item, selectionModels)
                                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    selectionSpinner.adapter = arrayAdapter
                                    selectionSpinner.setPadding(
                                        (20 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt(),
                                        (20 * resources.displayMetrics.density).toInt(),
                                        (15 * resources.displayMetrics.density).toInt()
                                    )



                                    selectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                                            if (selectionSpinner.selectedItemPosition > 0) {
                                                val mod = arrayAdapter.getItem(position)
                                                if (typeToAdd == "SSM") {
                                                    dataModel.data = mod?.id ?: ""
                                                } else {
                                                    dataModel.data = mod?.name ?: ""
                                                }
                                            }
                                        }

                                        override fun onNothingSelected(adapterView: AdapterView<*>) {
                                            dataModel.data = ""
                                        }
                                    }

                                    store_list.add(dataModel)

                                    // Put the TextView inside CardView
                                    card.addView(selectionSpinner)

                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                else if (typeToAdd == "TR") {

                                    isTimeShow = true
                                    val textView = TextView(applicationContext)
                                    val card = CardView(applicationContext)
                                    val layout = LinearLayout(applicationContext)
                                    val fromDate = TextView(applicationContext)
                                    val toDate = TextView(applicationContext)
                                    val between = TextView(applicationContext)
                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId =moduleId.toInt(),
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId)
                                    dataModel.column=columnStore
                                    card.layoutParams = params
                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.gravity = Gravity.CENTER
                                    layout.weightSum = 3f

                                    val param1 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                    )

                                    fromDate.layoutParams = param1
                                    between.layoutParams = param1
                                    toDate.layoutParams = param1

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                        ).toInt(), 0, 0, 0
                                    )
                                    val text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel


                                    fromDate.hint = "Select From Time"
                                    fromDate.gravity = Gravity.CENTER
                                    fromDate.textSize = 14f
                                    fromDate.setTextColor(Color.BLACK)
                                    fromDate.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
                                        ).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)
                                            .toInt(),
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
                                        ).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)
                                            .toInt()
                                    )
                                    fromDate.setBackgroundColor(Color.TRANSPARENT)
                                    fromDate.setOnClickListener {
                                        var hours = 0
                                        var minutes = 0
                                        if (fromDate.text.toString().isNotEmpty()) {
                                            try {
                                                val sdf = SimpleDateFormat("hh:mm aa")
                                                val date = sdf.parse(fromDate.text.toString())
                                                hours = date.hours
                                                minutes = date.minutes
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } else {
                                            val c = Calendar.getInstance()
                                            hours = c.get(Calendar.HOUR_OF_DAY)
                                            minutes = c.get(Calendar.MINUTE)
                                        }

                                        val dpd = TimePickerDialog(
                                            this@CustomFormDetailActivity,
                                            { _, hourOfDay, minute ->
                                                val time = Time(hourOfDay, minute, 0)
                                                val simpleDateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
                                                val s = simpleDateFormat.format(time)
                                                fromDate.text = s
                                                isTimeclicked = true
                                                isFromTimeEmpty = false
                                                dataModel.data = "${fromDate.text}  to  ${toDate.text}"
                                            },
                                            hours,
                                            minutes,
                                            false
                                        )
                                        dpd.show()
                                    }

                                    toDate.hint = "Select To Time"
                                    toDate.gravity = Gravity.CENTER
                                    toDate.textSize = 14f
                                    toDate.setTextColor(Color.BLACK)
                                    toDate.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
                                        ).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)
                                            .toInt(),
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
                                        ).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)
                                            .toInt()
                                    )
                                    toDate.setBackgroundColor(Color.TRANSPARENT)
                                    toDate.setOnClickListener {
                                        var hours = 0
                                        var minutes = 0
                                        if (toDate.text.toString().isNotEmpty()) {
                                            try {
                                                val sdf = SimpleDateFormat("hh:mm aa")
                                                val date = sdf.parse(toDate.text.toString())
                                                hours = date.hours
                                                minutes = date.minutes
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } else {
                                            val c = Calendar.getInstance()
                                            hours = c.get(Calendar.HOUR_OF_DAY)
                                            minutes = c.get(Calendar.MINUTE)
                                        }

                                        val dpd = TimePickerDialog(
                                            this@CustomFormDetailActivity,
                                            { _, hourOfDay, minute ->
                                                val time = Time(hourOfDay, minute, 0)
                                                val simpleDateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
                                                val s = simpleDateFormat.format(time)
                                                toDate.text = s
                                                isTimeclicked = true
                                                isToTimeEmpty = false
                                                dataModel.data = "${fromDate.text}  to  ${toDate.text}"
                                            },
                                            hours,
                                            minutes,
                                            false
                                        )
                                        dpd.show()
                                    }

                                    between.text = "  to  "
                                    between.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    between.gravity = Gravity.CENTER
                                    between.textSize = 14f
                                    between.setTextColor(Color.BLACK)


                                    store_list.add(dataModel)

                                    layout.addView(fromDate)
                                    layout.addView(between)
                                    layout.addView(toDate)

                                    // Put the TextView inside CardView

                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                else if (typeToAdd == "T") {

                                    val textView = TextView(applicationContext)
                                    val card = CardView(applicationContext)
                                    val layout = LinearLayout(applicationContext)
                                    val fromDate = TextView(applicationContext)
                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(),
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId)
                                    dataModel.column=columnStore
                                    card.layoutParams = params
                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.gravity = Gravity.CENTER

                                    val param1 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                    )
                                    fromDate.layoutParams = param1

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                        ).toInt(), 0, 0, 0
                                    )

                                    val text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    fromDate.hint = "Select Time"
                                    fromDate.gravity = Gravity.START
                                    fromDate.textSize = 14f
                                    fromDate.setTextColor(Color.BLACK)
                                    fromDate.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                        ).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)
                                            .toInt(),
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
                                        ).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics)
                                            .toInt()
                                    )
                                    fromDate.setBackgroundColor(Color.TRANSPARENT)
                                    fromDate.setOnClickListener {
                                        var hours = 0
                                        var minutes = 0
                                        if (fromDate.text.toString().isNotEmpty()) {
                                            try {
                                                val sdf = SimpleDateFormat("hh:mm aa")
                                                val date = sdf.parse(fromDate.text.toString())
                                                hours = date.hours
                                                minutes = date.minutes
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } else {
                                            val c = Calendar.getInstance()
                                            hours = c.get(Calendar.HOUR_OF_DAY)
                                            minutes = c.get(Calendar.MINUTE)
                                        }

                                        val dpd = TimePickerDialog(
                                            this@CustomFormDetailActivity,
                                            { _, hourOfDay, minute ->
                                                val time = Time(hourOfDay, minute, 0)
                                                val simpleDateFormat = SimpleDateFormat("hh:mm aa", Locale.getDefault())
                                                val s = simpleDateFormat.format(time)
                                                fromDate.text = s
                                                dataModel.data = fromDate.text.toString()
                                            },
                                            hours,
                                            minutes,
                                            false
                                        )
                                        dpd.show()
                                    }


                                    store_list.add(dataModel)

                                    layout.addView(fromDate)

                                    // Put the TextView inside CardView
                                    card.addView(layout)

                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                else if (typeToAdd == "R" ||typeToAdd == "RO" || typeToAdd == "RM") {
                                    val allData = ArrayList<String>()
                                    val card = CardView(applicationContext)
                                    val textView = TextView(applicationContext)
                                    val radioGroup = RadioGroup(applicationContext)
                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(),
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId)
                                    dataModel.column=columnStore
                                    radioGroup.layoutParams = params
                                    card.layoutParams = params

                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                        ).toInt(), 0, 0, 0
                                    )

                                    val text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    radioGroup.orientation = LinearLayout.VERTICAL

                                    val strList = ArrayList<String>()
                                    val model = mutableListOf<SelectionModel>()
                                    if (typeToAdd == "RO"||typeToAdd == "R") {


                                        val srcSplit = srcField.split(",")
                                        strList.addAll(srcSplit)
                                        for (k in strList.indices) {
                                            val selectionModel =   SelectionModel("","").apply {
                                                name = strList[k]
                                                id = (k + 1).toString()
                                            }
                                            model.add(selectionModel)
                                        }
                                    }

                                    val rb = Array(model.size) { RadioButton(applicationContext) }
                                    for (j in model.indices) {
                                        rb[j] = RadioButton(applicationContext)

                                        if (typeToAdd.equals("RM", ignoreCase = true)) {
                                            rb[j].text = "${model[j].name}-${model[j].id}"
                                        } else {
                                            rb[j].text = model[j].name
                                        }
                                        radioGroup.addView(rb[j])
                                    }

                                    radioGroup.setOnCheckedChangeListener { _, i ->
                                        try {
                                            val radioButton = radioGroup.findViewById<RadioButton>(i)
                                            if (typeToAdd == "RM") {
                                                val data = radioButton.text.toString()
                                                val splitData = data.split("-")
                                                val code = splitData[1]
                                                dataModel.data = code
                                            } else {
                                                dataModel.data = radioButton.text.toString()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }


                                    store_list.add(dataModel)

                                    card.addView(radioGroup)
                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }
                                /*else if (typeToAdd == "C" ||typeToAdd == "CO" || typeToAdd == "CM" || typeToAdd == "SMO" || typeToAdd == "SMM") {
                                    val card = CardView(applicationContext)
                                    val textView = TextView(applicationContext)
                                    val checkBoxContainer = LinearLayout(applicationContext)

                                    checkBoxContainer.layoutParams = params
                                    checkBoxContainer.orientation = LinearLayout.VERTICAL
                                    card.layoutParams = params

                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                        ).toInt(), 0, 0, 0
                                    )

                                    var text = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = 0,
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId)
                                    val strList = ArrayList<String>()
                                    val model = mutableListOf<CommonDataModel>()


                                        val srcSplit = srcField.split(",")
                                        strList.addAll(srcSplit)
                                        for (k in strList.indices) {
                                            val selectionModel = CommonDataModel("","","","","","","","","",0,0).apply {
                                                Name = strList[k]
                                                Id = (k + 1).toString()
                                            }
                                            model.add(selectionModel)
                                        }


                                    for (j in model.indices) {
                                        val checkBox = CheckBox(applicationContext).apply {
                                            text = model[j].Name
                                            id = j
                                            tag = "$i"
                                        }

                                        checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                                            val selectPos = compoundButton.id

                                            if (isChecked) {
                                                if (typeToAdd == "CM" || typeToAdd == "SMM") {
                                                    if (dataModel.data.isNullOrEmpty()) {
                                                        dataModel.data = model[selectPos].Id
                                                    } else {
                                                        dataModel.data = "${dataModel.data},${model[selectPos].Id}"
                                                    }
                                                } else {
                                                    if (dataModel.data.isNullOrEmpty()) {
                                                        dataModel.data = compoundButton.text.toString()
                                                    } else {
                                                        dataModel.data = "${dataModel.data},${compoundButton.text}"
                                                    }
                                                }

                                                // Update store_list with the new data model
                                                store_list.forEachIndexed { index, storeDataModel ->
                                                    if (storeDataModel.tag == compoundButton.tag) {
                                                        store_list[index] = dataModel
                                                    }
                                                }
                                            } else {
                                                val index = store_list.indexOf(dataModel)
                                                var data = store_list[index].data.toString()

                                                if (typeToAdd == "CM" || typeToAdd == "SMM") {
                                                    val name = model[selectPos].Id
                                                    data = data.replace(",${name},", ",")
                                                        .replace(",${name}", "")
                                                        .replace("${name},", "")
                                                        .replace(name, "")
                                                    dataModel.data = data
                                                } else {
                                                    data = data.replace(",${compoundButton.text},", ",")
                                                        .replace(",${compoundButton.text}", "")
                                                        .replace("${compoundButton.text},", "")
                                                        .replace(compoundButton.text.toString(), "")
                                                    dataModel.data = data
                                                }

                                                store_list[index] = dataModel
                                            }
                                        }

                                        checkBoxContainer.addView(checkBox)
                                    }
                                    store_list.add(dataModel)
                                    card.addView(checkBoxContainer)
                                    dynamic_data_layout.addView(textView)
                                    dynamic_data_layout.addView(card)
                                }*/
                                else if (typeToAdd == "C" || typeToAdd == "CO" || typeToAdd == "CM" || typeToAdd == "SMO" || typeToAdd == "SMM") {
                                    val card = CardView(applicationContext)
                                    val textView = TextView(applicationContext)
                                    val checkBoxContainer = LinearLayout(applicationContext)

                                    checkBoxContainer.layoutParams = params
                                    checkBoxContainer.orientation = LinearLayout.VERTICAL
                                    card.layoutParams = params

                                    // Setting different attributes
                                    card.radius = 5f
                                    card.setCardBackgroundColor(Color.WHITE)
                                    card.maxCardElevation = 15f
                                    card.cardElevation = 10f

                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                        ).toInt(), 0, 0, 0
                                    )

                                    var textt = "$headingLabel<font color='red'> *</font>"
                                    textView.text = if (mandate == 1) Html.fromHtml(textt, Html.FROM_HTML_MODE_LEGACY) else headingLabel

                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(),
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId
                                    )
                                    dataModel.column=columnStore
                                    val strList = ArrayList<String>()
                                    val model = mutableListOf<SelectionModel>()

                                    // Split srcField into the list
                                    val srcSplit = srcField.split(",")
                                    strList.addAll(srcSplit)

                                    // Populate the model
                                    for (k in strList.indices) {
                                        val selectionModel = SelectionModel("", "").apply {
                                            name = strList[k]
                                            id = (k + 1).toString()
                                        }
                                        model.add(selectionModel)
                                    }

                                    // Add CheckBoxes dynamically based on the model
                                    for (j in model.indices) {
                                        val checkBox = CheckBox(applicationContext).apply {
                                            text = model[j].name
                                            id = j
                                            tag = "$i"
                                            setTextColor(Color.BLACK)  // Make sure the text is visible
                                        }

                                        // Set listener for CheckBox
                                        checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                                            val selectPos = compoundButton.id

                                            if (isChecked) {
                                                // Update data model with selected value
                                                if (typeToAdd == "CM" || typeToAdd == "SMM") {
                                                    if (dataModel.data.isNullOrEmpty()) {
                                                        dataModel.data = model[selectPos].id
                                                    } else {
                                                        dataModel.data = "${dataModel.data},${model[selectPos].id}"
                                                    }
                                                } else {
                                                    if (dataModel.data.isNullOrEmpty()) {
                                                        dataModel.data = compoundButton.text.toString()
                                                        Log.e("checkbox data:",compoundButton.text.toString())
                                                    } else {
                                                        dataModel.data = "${dataModel.data},${compoundButton.text}"
                                                        Log.e("checkbox data:",dataModel.data)
                                                    }
                                                }

                                                // Update store_list with the new data model
                                                store_list.forEachIndexed { index, storeDataModel ->
                                                    if (storeDataModel.tag == compoundButton.tag) {
                                                        store_list[index] = dataModel
                                                    }
                                                }
                                            } else {
                                                val index = store_list.indexOf(dataModel)
                                                var data = store_list[index].data.toString()

                                                if (typeToAdd == "CM" || typeToAdd == "SMM") {
                                                    val name = model[selectPos].id
                                                    data = data.replace(",${name},", ",")
                                                        .replace(",${name}", "")
                                                        .replace("${name},", "")
                                                        .replace(name, "")
                                                    dataModel.data = data
                                                } else {
                                                    data = data.replace(",${compoundButton.text},", ",")
                                                        .replace(",${compoundButton.text}", "")
                                                        .replace("${compoundButton.text},", "")
                                                        .replace(compoundButton.text.toString(), "")
                                                    dataModel.data = data
                                                }

                                                store_list[index] = dataModel
                                            }
                                        }

                                        checkBoxContainer.addView(checkBox)
                                    }

                                    // Add the data model to the list
                                    store_list.add(dataModel)

                                    // Add the card view and text view to the layout
                                    card.addView(checkBoxContainer)

                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(card)
                                }

                                else if (typeToAdd == "FSC" || typeToAdd == "FC" || typeToAdd == "FS") {
                                    val card = CardView(applicationContext)
                                    val textView = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext).apply {
                                        orientation = LinearLayout.HORIZONTAL
                                        gravity = Gravity.CENTER
                                        weightSum = 2f

                                    }
                                    val imageview = ImageView(applicationContext)
                                    val tickImg = ImageView(applicationContext)
                                    val wrongImg = ImageView(applicationContext)
                                    val tvFileName = TextView(applicationContext)
                                    val layoutParamss = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        val horizontalMargin = 15 // Change this value as needed
                                        val horizontalMarginInDp = TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, horizontalMargin.toFloat(), resources.displayMetrics
                                        ).toInt()
                                        setMargins(horizontalMarginInDp, 0, horizontalMarginInDp, 0) // Left and Right margins
                                    }
                                    layout.layoutParams = layoutParamss

                                    val param1 = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        val verticalMargin = 15
                                        val horizontalMargin = 15
                                        val verticalMarginInDp = TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, verticalMargin.toFloat(), resources.displayMetrics
                                        ).toInt()
                                        val horizontalMarginInDp = TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, horizontalMargin.toFloat(), resources.displayMetrics
                                        ).toInt()
                                        setMargins(horizontalMarginInDp, verticalMarginInDp, horizontalMarginInDp, verticalMarginInDp)
                                    }

                                    val textParam = LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.7f
                                    )

                                    val imageParam = LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f
                                    )

                                    val imageParam1 = LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.1f
                                    )

                                    // CardView styling
                                    card.apply {
                                        radius = 5f
                                        setCardBackgroundColor(Color.WHITE)
                                        maxCardElevation = 15f
                                        cardElevation = 10f
                                        layoutParams = textParam
                                    }

                                    tvFileName.apply {
                                        setTextColor(Color.BLACK)
                                        textSize = 15f
                                        gravity = Gravity.START
                                        layoutParams = param1
                                    }
                                    card.addView(tvFileName)
                                    layout.addView(card)

                                    tickImg.apply {
                                        setImageResource(R.drawable.img_tick)
                                        layoutParams = imageParam1
                                        visibility = View.INVISIBLE
                                    }
                                    layout.addView(tickImg)

                                    wrongImg.apply {
                                        setImageResource(R.drawable.img_wrong)
                                        layoutParams = imageParam1
                                        visibility = View.INVISIBLE
                                    }
                                    layout.addView(wrongImg)

                                    imageview.apply {
                                        setImageResource(R.drawable.ic_upload_file)
                                        layoutParams = imageParam1
                                    }
                                    layout.addView(imageview)

                                    textView.apply {
                                        setTextColor(Color.BLACK)
                                        textSize = 15f
                                        setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                        setPadding(
                                            TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP, 18f, resources.displayMetrics
                                            ).toInt(), 0, 0, 0
                                        )
                                        val text = "$headingLabel<font color='red'> *</font>"
                                        textView.text = if (mandate == 1) Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY) else headingLabel
                                    }

                                    val key = "$i$jk"

                                    imageview.setOnClickListener {
                                        if (Constant.isNetworkAvailable(applicationContext)) {
                                            when (typeToAdd) {
                                                "FC" -> checkPermissionsAndOpenCamera(key.toInt())
                                                "FS" -> checkImageAccess(key.toInt())
                                                else -> showPhotoDialog( key.toInt())
                                            }
                                        } else {
                                            Toast.makeText(applicationContext, "Please check the internet connectivity", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    // Map ImageView and TextView for later use

                                    imageViewMap[key.toInt()] = imageview // Use hashCode for unique integer key
                                    textViewMap[key.toInt()] = tvFileName
                                    tickImgViewMap[key.toInt()] = tickImg
                                    wrongImgViewMap[key.toInt()] = wrongImg

                                    // Data model creation and adding to store_list

                                    val dataModel = CustomDynamicDataModel(
                                        data = "",
                                        mandatory = mandate,
                                        fldGrpId = fieldGroupId,
                                        moduleId = moduleId.toInt(),
                                        fldType = typeToAdd,
                                        grpTableName = tableName,
                                        fldGrpName = grpName,
                                        fldSrcFld = srcField,
                                        moduleName = moduleId)
                                    dataModel.column=columnStore
                                    dataModel.imgKey=key

                                    store_list.add(dataModel)

                                    // Add views to layout
                                    binding.llDynamicData.addView(textView)
                                    binding.llDynamicData.addView(layout)

                                    if(screenType==1){
                                        imageview.isEnabled=true
                                    }else{
                                        imageview.isEnabled=false
                                    }
                                }


                            }
                        }
                    }
                }
            } else {
                binding.tvNoData.visibility = View.VISIBLE
                binding.btnSubmit!!.visibility = View.GONE
                binding.tvFormNo.visibility = View.GONE
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    class SpinAdapter(
        context: Context,
        textViewResourceId: Int,
        private val values: Array<SelectionModel>
    ) : ArrayAdapter<SelectionModel>(context, textViewResourceId, values) {

        private val context: Context = context

        override fun getCount(): Int {
            return values.size
        }

        override fun getItem(position: Int):SelectionModel {
            return values[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val label = super.getView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            label.text = values[position].name
            return label
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val label = super.getDropDownView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            label.text = values[position].name
            return label
        }
    }

    fun saveDynamicData(){
        if(Constant.isNetworkAvailable(applicationContext)) {
            val jsonArray = JsonArray()
            val activityReportAppObject = JsonObject()
            activityReportAppObject.addProperty(
                "sf_code",
                SecureStorage.getString(applicationContext, StringConstants.SP_ID)
            )
            activityReportAppObject.addProperty("eKey", currentScreenTimeStamp)
            activityReportAppObject.addProperty("custCode", custId)
            activityReportAppObject.addProperty("currenttime", currenttime)
            activityReportAppObject.addProperty("checkInId", checkInId)
            val jsonObject1 = JsonObject()
            jsonObject1.add("common_dynamic_data", activityReportAppObject)
            jsonArray.add(jsonObject1)
            Log.e("common_dynamic_data: ", " $activityReportAppObject")

            val activitySampleReportArray = JsonArray()
            try {
                var i = 0
                while (group_list.size > i) {
                    val jResult = JsonObject() // main object
                    val grpId: Int = group_list[i].fldGrpId
                    val fieldGroupTableNm: String = group_list[i].grpTableName
                    val jArray = JsonArray()
                    var j = 0
                    while (store_list.size > j) {
                        val jGroup = JsonObject()
                        val model: CustomDynamicDataModel = store_list[j]
                        if (model.data != null && group_list[i].fldGrpId === model.fldGrpId) {
                            try {
                                jGroup.addProperty("column_name", model.column)
                                jGroup.addProperty("data_value", model.data)
                                // jGroup.addProperty("table_name", model.getGrpTableName());
                                jArray.add(jGroup)
                            } catch (e: JsonSyntaxException) {
                                e.printStackTrace()
                            }
                        }
                        j++
                    }
                    jResult.addProperty("groupId", grpId)
                    jResult.addProperty("grpTableName", fieldGroupTableNm)
                    jResult.add("itemdetail", jArray)
                    activitySampleReportArray.add(jResult)
                    i++
                }
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
            val jsonObject3 = JsonObject()
            jsonObject3.add("dynamic_data_detail", activitySampleReportArray)
            jsonArray.add(jsonObject3)
            var spId = SecureStorage.getString(applicationContext, StringConstants.SP_ID)
            Log.e("jsonData", jsonArray.toString())
            formViewModel.saveCustomForm(SecureStorage.getString(applicationContext,StringConstants.AUTH_TOKEN)!!,
                Constant.toRequestBody(jsonArray),SecureStorage.getString(applicationContext,StringConstants.SP_ID)!!)

        }else{
            Toast.makeText(applicationContext,"Please check your network connection",Toast.LENGTH_SHORT).show()
        }
    }



}
