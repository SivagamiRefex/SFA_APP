package com.example.sfa.presentation.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sampleapp.sqlite.DBController
import com.example.sfa.R
import com.example.sfa.data.api.NetworkModule
import com.example.sfa.data.model.FileModel
import com.example.sfa.databinding.ActivityProductCatalogBinding
import com.example.sfa.presentation.ui.Adapter.FileViewAdapter
import com.example.sfa.presentation.viewmodel.ProductCatalogViewModel
import com.example.sfa.utils.Constant
import com.example.sfa.utils.Resource
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

@AndroidEntryPoint
class ProductCatalogActivity:AppCompatActivity() {
    private lateinit var binding: ActivityProductCatalogBinding
    private val productCatalogViewmodel: ProductCatalogViewModel by viewModels()
    private var fileList= ArrayList<FileModel>()
    private lateinit var fileViewAdapter:FileViewAdapter
    lateinit var dbController: DBController
    var customerLabel:String="Customer"
    private var layoutType = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    fun initView() {
        dbController = DBController(applicationContext)
        customerLabel = Constant.getSetup("customer_label","Customer",dbController,applicationContext)!!
        binding.llToolbar.tvTitle.text="Product Catalog"
        binding.llToolbar.menubtn.setImageResource(R.drawable.ic_back_arrow)
        binding.llToolbar.menubtn.setOnClickListener {
            this.onBackPressed()
        }
        getProductCatalogList()
        binding.llToolbar.twocatgory.visibility=View.VISIBLE

        binding.llToolbar.listview.setOnClickListener {
            setLayoutSelection(isList = true)
        }

        binding.llToolbar.cardview.setOnClickListener {
            setLayoutSelection(isList = false)
        }

        productCatalogViewmodel.getFileState.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data!!.status) {
                        fileList= result.data.data!!
                        Toast.makeText(applicationContext, result.data.message, Toast.LENGTH_SHORT).show()
                        var size: String? = null
                        val df = DecimalFormat("0.00")
                        var url :String?=null

                        if(fileList.size>0){
                            for (i in 0 until fileList.size) {

                                var model=fileList[i]
                                var fileSize=(model.fileSize).toInt()
                                if(fileSize>0){
                                    val sizeKb = 1024.0f
                                    val sizeMb = sizeKb * sizeKb
                                    val sizeGb = sizeMb * sizeKb
                                    val sizeTerra = sizeGb * sizeKb

                                    if (fileSize < sizeMb) size =
                                        df.format((fileSize / sizeKb).toDouble()) + " Kb"
                                    else if (fileSize < sizeGb) size =
                                        df.format((fileSize / sizeMb).toDouble()) + " Mb"
                                    else if (fileSize < sizeTerra) size =
                                        df.format((fileSize / sizeGb).toDouble()) + " Gb"

                                }else{
                                    size="0"
                                }
                                url = NetworkModule.baseFileUrl+model.fileName
                                model.url=url
                                model.fileSize= size!!

                            }
                            binding.nodata.visibility = View.GONE
                            binding.CircularCycle.visibility = View.VISIBLE
                            fileViewAdapter = FileViewAdapter(this, fileList, layoutType)
                            binding.CircularCycle.layoutManager = if (layoutType == 0) {
                                LinearLayoutManager(this)
                            } else {
                                GridLayoutManager(this, 2)
                            }
                            binding.CircularCycle.adapter = fileViewAdapter
                        }else{
                            showNoData()
                        }
                    } else {
                        Toast.makeText(applicationContext, result.data!!.message, Toast.LENGTH_SHORT).show()
                        showNoData()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message ?: "Error", Toast.LENGTH_SHORT).show()
                    showNoData()
                }
                is Resource.Loading -> {
                    // Show loading indicator
                }

            }
        }
    }
    fun getProductCatalogList(){
        if(Constant.isNetworkAvailable(applicationContext)) {
            productCatalogViewmodel.getFileList(
                SecureStorage.getString(applicationContext, StringConstants.AUTH_TOKEN)!!,
                SecureStorage.getString(applicationContext, StringConstants.SP_ID)!!,
                TimesUtil.getCurrentTime(TimesUtil.FORMAT1),
                SecureStorage.getInt(applicationContext, StringConstants.SP_TYPE)
            )
        }else{
            Toast.makeText(applicationContext,"Please Check Your Network Connection",Toast.LENGTH_SHORT).show()
        }

    }
    private fun setLayoutSelection(isList: Boolean) {
        layoutType = if (isList) 0 else 1

        if (isList) {
            binding.llToolbar.cardview.setBackgroundResource(R.color.grey_100)
            binding.llToolbar.cardview.alpha = 0.2f
            binding.llToolbar.listview.setBackgroundResource(R.color.white)
            binding.llToolbar.listview.alpha = 1f
        } else {
            binding.llToolbar.listview.setBackgroundResource(R.color.grey_100)
            binding.llToolbar.listview.alpha = 0.2f
            binding.llToolbar.cardview.setBackgroundResource(R.color.white)
            binding.llToolbar.cardview.alpha = 1f
        }

        fileViewAdapter = FileViewAdapter(this, fileList, layoutType)
        binding.CircularCycle.layoutManager = if (isList) {
            LinearLayoutManager(this)
        } else {
            GridLayoutManager(this, 2)
        }
        binding.CircularCycle.adapter = fileViewAdapter
    }
    private fun showNoData() {
        Toast.makeText(this, "No Data Available", Toast.LENGTH_SHORT).show()
        binding.CircularCycle.visibility = View.GONE
        binding.nodata.visibility = View.VISIBLE
    }



}