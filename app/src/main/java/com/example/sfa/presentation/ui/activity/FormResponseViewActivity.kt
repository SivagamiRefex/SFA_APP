package com.example.sfa.presentation.ui.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.api.NetworkModule
import com.example.sfa.data.model.CustomDynamicDataModel
import com.example.sfa.databinding.ActivityFormResponseViewReportBinding
import com.example.sfa.databinding.ActivityFormResponsesBinding
import com.example.sfa.presentation.viewmodel.FormViewModel
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
@AndroidEntryPoint
class FormResponseViewActivity:AppCompatActivity() {
    private lateinit var binding: ActivityFormResponseViewReportBinding
    private lateinit var dbController: DBController
    private val formViewModel: FormViewModel by viewModels()
    var moduleId: Int = 0
    var moduleName: String = ""
    var entryId: String = ""
    private var spId: String = ""
    private var spName: String = ""
    var group_list = ArrayList<CustomDynamicDataModel>()
    var master_list = ArrayList<CustomDynamicDataModel>()
    var entryBy=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormResponseViewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.layoutToolbar.tvTitle.text = "Form Responses View"
        binding.layoutToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.layoutToolbar.menubtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        dbController = DBController(applicationContext)

        val intent = intent
        if (intent.hasExtra("moduleId")) {
            moduleId = intent.getIntExtra("moduleId",0)!!
        }
        if (intent.hasExtra("entryId")) {
            entryId = intent.getStringExtra("entryId")!!
        }
        if (intent.hasExtra("spId")) {
            spId = intent.getStringExtra("spId").toString()
        }
        if (intent.hasExtra("moduleName")) {
            moduleName = intent.getStringExtra("moduleName").toString()
        }
        if (intent.hasExtra("entryBy")) {
            entryBy = intent.getStringExtra("entryBy").toString()
        }
        binding.layoutToolbar.tvTitle.text = "$moduleName Reponse Details"
        lifecycleScope.launch(Dispatchers.IO) {
            getResponseDetails(moduleId, entryId)
        }
        if(!entryBy.equals("")) {
            binding.tvEntryBy.visibility= View.VISIBLE
            binding.tvEntryByLabel.visibility= View.VISIBLE
            binding.tvEntryBy.text= entryBy
        }
    }

    suspend fun getResponseDetails(moduleId: Int,enryId:String) {
        val result = formViewModel.getFormResponseDetails(
            SecureStorage.getString(applicationContext,com.example.sfa.utils.StringConstants.AUTH_TOKEN)!!,moduleId,enryId,
            SecureStorage.getString(applicationContext,com.example.sfa.utils.StringConstants.SP_ID)!!)
        when (result) {
            is Resource.Success -> {
                var json = JSONTokener(result.data!!.string()).nextValue()
                var jsonArray = JSONArray()

                if (json is JSONArray) {
                    jsonArray = JSONArray(result.data)

                } else if (json is JSONObject) {

                    if (json.getBoolean("status")) {

                        val jsonObject1 = json.getJSONObject("data")
                        val array = JSONArray(jsonObject1.getString("customGrp"))
                        val jsonArray = JSONArray(jsonObject1.getString("Dynamic_View"))

                        withContext(Dispatchers.Main) {
                            loadData(array, jsonArray)
                            binding.tvNoData.visibility=View.GONE
                        }

                    } else {

                        binding.tvNoData.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, "Empty Data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            is Resource.Error -> {
                binding.tvNoData.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE

            }
            else -> {}
        }
    }

    fun loadData(array: JSONArray, jsonArray2: JSONArray) {
        try {
            binding.llDynamicData.visibility = View.VISIBLE
            if (array.length() > 0) {
                for (jk in 0 until array.length()) {
                    val jsonObject2 = array.getJSONObject(jk)
                    val grpId = jsonObject2.getInt("FieldGroupId")
                    val grpName = jsonObject2.getString("FieldGroupName")
                    val fgTableName = jsonObject2.getString("FGTableName")

                    val paramss = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    val vertical_margin = 10
                    val Horizontal_margin = 15
                    val vertical_marginInDp = (vertical_margin * resources.displayMetrics.density).toInt()
                    val Horizontal_marginInDp = (Horizontal_margin * resources.displayMetrics.density).toInt()

                    paramss.setMargins(Horizontal_marginInDp, vertical_marginInDp, Horizontal_marginInDp, vertical_marginInDp)

                    val textView30 = TextView(this)
                    textView30.setTextColor(resources.getColor(R.color.primary, null))
                    textView30.textSize = 18f
                    textView30.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                    textView30.setPadding(
                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                    )
                    textView30.text = grpName

                    binding.llDynamicData.addView(textView30)
                    textView30.visibility = View.GONE

                    // Creating a CustomDynamicDataModel instance
                    val dataModel = CustomDynamicDataModel(
                        data = "", // You'll set this value later
                        mandatory = 0,  // You will set it based on the data
                        fldGrpId = grpId,
                        moduleId = 0,  // You will set this value later
                        fldType = "",  // You will set this value later
                        grpTableName = fgTableName,
                        fldGrpName = grpName,
                        fldSrcFld = "", // You will set this value later
                        moduleName = "" // You will set this value later
                    )

                    group_list.add(dataModel)

                    if (jsonArray2.length() > 0) {
                        for (i in 0 until jsonArray2.length()) {
                            val jsonObject = jsonArray2.getJSONObject(i)

                            var fieldGroupId = 0
                            if (!jsonObject.getString("FieldGroupId").equals("null", ignoreCase = true)) {
                                fieldGroupId = jsonObject.getInt("FieldGroupId")
                            }

                            if (grpId == fieldGroupId) {
                                val Heading_Label = jsonObject.getString("Field_Name")
                                val Type_to_Add = jsonObject.getString("Fld_Type")
                                val moduleId = jsonObject.getInt("ModuleId")  // Get ModuleId
                                val Column_Store = jsonObject.getString("Field_Col")
                                val Src_Name = jsonObject.getString("Fld_Src_Name")
                                val Src_Field = jsonObject.getString("Fld_Src_Field")
                                val Text_Length = jsonObject.getInt("Fld_Length")
                                val mandate = jsonObject.getInt("Mandate")
                                val flag = jsonObject.getInt("flag")
                                val data_value = jsonObject.getString("data_value")

                                //val amount = extractAmount(data_value)
                                //val formAmount = Symbol_Currency + amount
                                val tableName = jsonObject.getString("FGTableName")

                                textView30.visibility = View.VISIBLE

                                val params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )

                                val vertical_margin1 = 5
                                val Horizontal_margin1 = 0
                                val vertical_marginInDp1 = (vertical_margin1 * resources.displayMetrics.density).toInt()
                                val Horizontal_marginInDp1 = (Horizontal_margin1 * resources.displayMetrics.density).toInt()

                                params.setMargins(Horizontal_marginInDp1, vertical_marginInDp1, Horizontal_marginInDp1, vertical_marginInDp1)

                                // Update the CustomDynamicDataModel instance with the values
                                dataModel.data = data_value
                                dataModel.mandatory = mandate
                                dataModel.moduleId = moduleId
                                dataModel.fldType = Type_to_Add
                                dataModel.fldSrcFld = Src_Field
                                // dataModel.moduleName = moduleId.toString()  // If there's a module name value, you can replace this with that.

                                // EditText for text/phone number
                                if (Type_to_Add.contains("T")||Type_to_Add.contains("TA") || Type_to_Add == "N" || Type_to_Add == "TAS" || Type_to_Add == "NP" || Type_to_Add == "TAM") {
                                    val layout = LinearLayout(applicationContext)
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)

                                    layout.orientation = LinearLayout.VERTICAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )
                                    textView.text = "$Heading_Label: "

                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView1.text = if (data_value != "") data_value else "-"

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "DR") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "

                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    if (data_value.isNotEmpty()) {
                                        textView1.text = data_value
                                    } else {
                                        textView1.text = "-"
                                    }

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "D") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "

                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView1.text = if (data_value.isNotEmpty()) data_value else "-"

                                    layout.addView(textView)
                                    layout.addView(textView1)

                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "S"||Type_to_Add == "SS"||Type_to_Add == "SSO" || Type_to_Add == "SSM") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "

                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    /*if (Type_to_Add == "SSM") {
                                        val data = getHasMapList(Src_Name, Src_Field)
                                        if (data_value.isNotEmpty()) {
                                            textView1.text = data[data_value] ?: "-"
                                        } else {
                                            textView1.text = "-"
                                        }
                                    } else {*/
                                    if (data_value.isNotEmpty()) {
                                        textView1.text = data_value
                                    } else {
                                        textView1.text = "-"
                                    }
                                    // }

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "TR") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "
                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    if (data_value.isNotEmpty()) {
                                        textView1.text = data_value
                                    } else {
                                        textView1.text = "-"
                                    }

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }
                                else if (Type_to_Add == "T") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "
                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    if (data_value.isNotEmpty()) {
                                        textView1.text = data_value
                                    } else {
                                        textView1.text = "-"
                                    }

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "R" || Type_to_Add == "RO" || Type_to_Add == "RM") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "
                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    /* if (Type_to_Add == "RM") {
                                         val data = getHasMapList(Src_Name, Src_Field)
                                         if (data_value.isNotEmpty()) {
                                             textView1.text = data[data_value] ?: "-"
                                         } else {
                                             textView1.text = "-"
                                         }
                                     } else {*/
                                    textView1.text = if (data_value.isNotEmpty()) data_value else "-"
                                    // }

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "C" ||Type_to_Add == "CO" || Type_to_Add == "CM" || Type_to_Add == "SM" ||Type_to_Add == "SMO" || Type_to_Add == "SMM") {
                                    val textView = TextView(applicationContext)
                                    val textView1 = TextView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    textView1.layoutParams = param
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "
                                    textView1.textSize = 15f
                                    textView1.gravity = Gravity.START
                                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                                    textView1.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    /*if (Type_to_Add == "CM" || Type_to_Add == "SMM") {
                                    val srcSplit: Array<String>
                                    var showData = ""
                                    val data = getHasMapList(Src_Name, Src_Field)

                                    if (data_value.isNotEmpty()) {
                                        srcSplit = data_value.split(",").toTypedArray()
                                        val strList = ArrayList(srcSplit.asList())

                                        for (item in strList) {
                                            showData += data[item] ?: "-" // Handle null safely
                                        }
                                        textView1.text = showData
                                    } else {
                                        textView1.text = "-"
                                    }
                                } else {*/
                                    textView1.text = if (data_value.isNotEmpty()) data_value else "-"
                                    // }

                                    layout.addView(textView)
                                    layout.addView(textView1)
                                    binding.llDynamicData.addView(layout)
                                }else if (Type_to_Add == "FSC" || Type_to_Add == "FC" || Type_to_Add == "FS") {

                                    val textView = TextView(applicationContext)
                                    val imageView = ImageView(applicationContext)
                                    val layout = LinearLayout(applicationContext)

                                    layout.orientation = LinearLayout.HORIZONTAL
                                    layout.layoutParams = params

                                    val param = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )

                                    textView.layoutParams = param
                                    imageView.layoutParams = LinearLayout.LayoutParams(
                                        (100 * resources.displayMetrics.density).toInt(),
                                        (100 * resources.displayMetrics.density).toInt()
                                    )
                                    textView.setTextColor(Color.BLACK)
                                    textView.textSize = 15f
                                    textView.gravity = Gravity.START
                                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
                                    textView.setPadding(
                                        (18 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )

                                    textView.text = "$Heading_Label: "
                                    imageView.foregroundGravity = Gravity.START
                                    imageView.setPadding(
                                        (5 * resources.displayMetrics.density).toInt(), 0, 0, 0
                                    )
                                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                                    if (data_value.isNotEmpty()) {
                                        previewphoto(imageView, data_value)
                                        layout.addView(textView)
                                        layout.addView(imageView)
                                        binding.llDynamicData.addView(layout)

                                        imageView.setOnClickListener{
                                            showImagePreviewDialog(data_value)
                                        }
                                    } else {
                                        textView.text = "-"
                                        textView.text = "$Heading_Label: "
                                        layout.addView(textView)
                                        binding.llDynamicData.addView(layout)
                                    }
                                }







                            }
                        }
                    }

                    binding.llDynamicData.visibility = View.VISIBLE
                    binding.tvNoData.visibility = View.GONE
                }
            } else {
                binding.tvNoData.visibility = View.VISIBLE
                binding.llDynamicData.visibility = View.GONE
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }


    }
    private fun showImagePreviewDialog(imageUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_preview)

        val imageView = dialog.findViewById<ImageView>(R.id.imageView)
        val ivClose = dialog.findViewById<ImageView>(R.id.iv_close)
        Glide.with(this).load(NetworkModule.baseImgUrl+imageUrl).into(imageView)
        dialog.show()
        ivClose.setOnClickListener{
            dialog.cancel()
        }
    }

    fun previewphoto(imageView: ImageView, fileName:String){
        Glide.with(this).load(NetworkModule.baseImgUrl+fileName).into(imageView)
    }
}