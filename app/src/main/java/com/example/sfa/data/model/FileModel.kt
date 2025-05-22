package com.example.sfa.data.model

import com.google.gson.annotations.SerializedName

data class FileModel(@SerializedName("FileName") var fileName:String,
    @SerializedName("FileSubject") var fileSubject:String,
    @SerializedName("FileID") var fileId:Int,
    @SerializedName("FileSize") var fileSize:String,
    @SerializedName("FilePath") var filePath:String){
    var url :String=""
}
