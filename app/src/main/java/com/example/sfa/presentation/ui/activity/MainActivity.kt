package com.example.sfa.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sfa.R
import com.example.sfa.databinding.ActivityMainBinding
import com.example.sfa.presentation.ui.fragment.HomeFragment
import com.example.sfa.presentation.ui.fragment.MasterSyncFragment
import com.example.sfa.presentation.ui.fragment.MenuFragment
import com.example.sfa.presentation.ui.fragment.ReportFragment
import com.example.sfa.utils.Constant
import com.example.sfa.utils.SecureStorage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    var isFirstTime=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        replaceFragment(HomeFragment())
         Log.e("token",""+SecureStorage.getToken(applicationContext))
        if (intent.hasExtra("isFirstTime"))
            isFirstTime = intent.getBooleanExtra("isFirstTime", false)


         if(Constant.isFirstTimeSynAll || isFirstTime) {
             replaceFragment(MasterSyncFragment())
        }else{
            replaceFragment(HomeFragment())

        }
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.reports -> replaceFragment(ReportFragment())
                R.id.menu -> replaceFragment(MenuFragment())
                R.id.sync->replaceFragment(MasterSyncFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment).commit()
    }
}