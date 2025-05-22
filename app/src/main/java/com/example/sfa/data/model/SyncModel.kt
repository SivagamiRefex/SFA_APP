package com.example.sfa.data.model

import android.widget.TextView
import com.google.gson.JsonObject
import pl.droidsonroids.gif.GifImageView

data class SyncModel(val id:Int,
                     val axn:String,
                     val tableName:String,
                     val inputValue:JsonObject,
                     var isLoadFromPreference:Boolean){


    var gifImageView: GifImageView? = null
    var textView: TextView? = null
    var count:Int=0
    var isDataSynced = false


}
/*private GifImageView gifImageView;
private TextView failImageView;
private boolean isHQSync;
private boolean isDataSynced = false;
private int count = 0;*/
