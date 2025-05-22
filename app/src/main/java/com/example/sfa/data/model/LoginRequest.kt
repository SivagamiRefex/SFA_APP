package com.example.sfa.data.model

data class LoginRequest(val username: String, val password: String
                        ,val loginDate: String,
                        val deviceRegId: String, val lat: String?, val long: String?)
