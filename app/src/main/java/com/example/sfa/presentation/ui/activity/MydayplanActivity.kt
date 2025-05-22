package com.example.sfa.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sfa.databinding.ActivityAddcustomerBinding

class MydayplanActivity:AppCompatActivity() {
    private lateinit var binding: ActivityAddcustomerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddcustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView() {

    }


}