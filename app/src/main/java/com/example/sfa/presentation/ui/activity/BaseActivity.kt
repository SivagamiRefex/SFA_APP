package com.example.sfa.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sfa.data.model.SyncModel
import com.example.sfa.utils.StringConstants
import com.google.gson.JsonObject
import pl.droidsonroids.gif.GifImageView

open class BaseActivity:AppCompatActivity() {
    var masterSyncList: ArrayList<SyncModel> = ArrayList<SyncModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_customer_visit)

        addData()
    }
    fun addData(){
        val jsonObject1 = JsonObject()
        masterSyncList.add(
            SyncModel(
                1, "customer",
                StringConstants.CUSTOMER_DATA, jsonObject1, false
            )
        )
        val jsonObject2 = JsonObject()
        masterSyncList.add(SyncModel(
            2, "route",
            StringConstants.ROUTE_DATA, jsonObject2, false))


        val jsonObject3 = JsonObject()
        masterSyncList.add(
            SyncModel(
                3, "spdata",
                StringConstants.SALESPERSON_DATA, jsonObject3, false))

        val jsonObject4 = JsonObject()
        masterSyncList.add(SyncModel(
            4, "sourceoflead",
            StringConstants.SOURCEOFLEAD_DATA, jsonObject4, false))

        val jsonObject5 = JsonObject()
        masterSyncList.add(
            SyncModel(
                5, "custommoduleList",
                StringConstants.CUSTOM_MODULE_DATA, jsonObject5, false
            )
        )
        val jsonObject6 = JsonObject()
        masterSyncList.add(
            SyncModel(
                6, "allcustomfielddata",
                StringConstants.CUSTOM_FORM_DATA, jsonObject6, false
            )
        )

        val jsonObject7 = JsonObject()
        masterSyncList.add(
            SyncModel(
                7, "setup",
                StringConstants.SETUP_DATA, jsonObject7, false))
    }

}