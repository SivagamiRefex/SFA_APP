package com.example.sfa.data.model

data class SelectionModel(
    var id: String,
    var name: String) {

    var address: String = ""
    var lat: Double = 0.0
    var long: Double = 0.0
    var selectedPersons: List<String> = mutableListOf()
    var selectedPerson:String?=null
    var selectedSite:String?=null
    var selectedSitesList: List<String> = mutableListOf()
    var planNo:Int=0
}
