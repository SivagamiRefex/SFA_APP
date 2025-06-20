package com.example.sfa

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.sfa.utils.Constant
import com.example.sfa.utils.DeveloperOptionUtil
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class MyApp: Application(), LifecycleObserver {
    var activeRoute: String? = null
    var isAppInForeground: Boolean = false
    var getDashbrd: Boolean = false

    private val TAG = MyApp::class.java.simpleName
    private var activeScreen: WeakReference<Activity>? = null
    private var sContext: Context? = null

    companion object {
        var sharedInstance: MyApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        sharedInstance = this
        sContext = applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        setupActivityListener()


    }

    private fun setupActivityListener() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activeScreen = WeakReference(activity)
                if (!Constant.isNetworkAvailable(activity.applicationContext)) {
                    Toast.makeText(activity, "Please Check Your Network Connection", Toast.LENGTH_SHORT).show()
                }

               /* if (DeveloperOptionUtil.isDeveloperOptionsEnabled(activity)) {
                    DeveloperOptionUtil.showDeveloperOptionsAlert(activity)
                }*/
            }

            override fun onActivityStarted(activity: Activity) {
                activeScreen = WeakReference(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                activeScreen = WeakReference(activity)
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                if (!Constant.isNetworkAvailable(activity.applicationContext)) {
                    Toast.makeText(activity, "Please Check Your Network Connection", Toast.LENGTH_SHORT).show()
                }
             /*   if (DeveloperOptionUtil.isDeveloperOptionsEnabled(activity)) {
                    DeveloperOptionUtil.showDeveloperOptionsAlert(activity)
                }*/
            }

            override fun onActivityPaused(activity: Activity) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            }

            override fun onActivityStopped(activity: Activity) {
                if (activeScreen?.get() === activity) {
                    activeScreen = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                if (activeScreen?.get() === activity) {
                    activeScreen = null
                }
            }
        })
    }

    fun getContext(): Context? {
        return sContext
    }

    fun getActiveScreen(): Activity? {
        return activeScreen?.get()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isAppInForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isAppInForeground = true
    }

    fun printUsrLog(id: String, msg: String?) {
        if (Constant.DEBUG_MODE) {
            val activeClass = activeScreen?.get()?.javaClass?.simpleName
            Log.v("Print Log $activeClass : $id", msg ?: "No message")
        }
    }
    fun getApplication(): MyApp? {
        return sharedInstance
    }



}