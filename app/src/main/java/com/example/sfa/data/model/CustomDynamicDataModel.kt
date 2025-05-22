package com.example.sfa.data.model

data class CustomDynamicDataModel(var data:String,
                                  var mandatory:Int, val fldGrpId:Int, var moduleId:Int,
                                  var fldType: String, val grpTableName: String, val fldGrpName: String,
                                  var fldSrcFld: String, var moduleName: String) {


     var imgKey: String? = null
     var fldSrcName: String? = null
    var tag:String?=null
    var column:String?=null



}