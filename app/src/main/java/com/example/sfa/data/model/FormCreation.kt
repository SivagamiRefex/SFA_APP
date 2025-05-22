package com.example.sfa.data.model

data class FormCreation(
    var formName: String = "",
    val sections: MutableList<Section> = mutableListOf()
)

data class Section(
    var sectionName: String = "",
    val questions: MutableList<Question> = mutableListOf()
){
    var sectionTable:String=""
    var sectionTableId:Int=0
    var sectionFlag:Int=0

}

data class Question(
    var label: String = "",
    var hint: String = "",
    var value: String = ""
){
    var questionColumn:String=""
    var questionColumnId:Int=0
    var questionFlag:Int=0

}

