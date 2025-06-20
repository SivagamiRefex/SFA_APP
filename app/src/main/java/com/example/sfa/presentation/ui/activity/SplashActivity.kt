package com.example.sfa.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sfa.R
import com.example.sfa.databinding.ActivitySplashBinding
import com.example.sfa.utils.DeveloperOptionUtil
import com.example.sfa.utils.SecureStorage
import com.example.sfa.utils.StringConstants
import com.example.sfa.utils.TimesUtil

class SplashActivity:AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    fun initView(){
        Glide.with(this).load(R.drawable.sfa_gif).into(binding.ivGif)

       /* if (DeveloperOptionUtil.isDeveloperOptionsEnabled(this)) {
            DeveloperOptionUtil.showDeveloperOptionsAlert(this)
        }else {
            navigateToDashboard()
        }*/

        navigateToDashboard()
    }

    private fun navigateToDashboard() {
        Handler().postDelayed({ navigate() }, 1000)
    }
    fun navigate(){
        if(SecureStorage.getBoolean(applicationContext,StringConstants.IS_USER_LOGED_IN) == true &&
            TimesUtil.compareCurrentAndLoginDate(SecureStorage.getString(applicationContext,StringConstants.LOGIN_DATE)) >= 0){
            val intent = Intent(this@SplashActivity, MainActivity::class.java);
            startActivity(intent);
            finish()
        }else{
            if(TimesUtil.compareCurrentAndLoginDate(SecureStorage.getString(applicationContext,StringConstants.LOGIN_DATE)) < 0) {
                SecureStorage.setBoolean(applicationContext,StringConstants.IS_USER_LOGED_IN,false)
            }
            val intent = Intent(this@SplashActivity, LoginActivity::class.java);
            startActivity(intent)
            finish()

        }
    }
}