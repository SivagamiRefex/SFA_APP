package com.example.sfa.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.sfa.presentation.receiver.AlmReceiver


object AlarmUtils {

    private const val ALARM_INTERVAL: Long = 2 * 60 * 1000 // 15 minutes
    private const val ALARM_REQUEST_CODE = 12345

    @SuppressLint("ObsoleteSdkInt", "ScheduleExactAlarm")
    fun setRepeatingAlarm(context: Context) {
        Log.e("onreceive", "almrmutil")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = getAlarmIntent(context)

        val triggerTime = System.currentTimeMillis() + ALARM_INTERVAL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("onreceive", "almrmutil1")

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                alarmIntent
            )
        } else {
            Log.e("onreceive", "almrmutil2")

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                alarmIntent
            )
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = getAlarmIntent(context)
        alarmManager.cancel(alarmIntent)
    }

    private fun getAlarmIntent(context: Context): PendingIntent {
        Log.e("onreceive", "almrmutil3")

        val intent = Intent(context, AlmReceiver::class.java)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.e("onreceive", "almrmutil4")

            PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_MUTABLE)

            // Log.e("step:","2.1");
        } else {
            Log.e("onreceive", "almrmutil5")

            PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)
            // Log.e("step:","2.2");
        }
        Log.e("onreceive", "almrmutil6")

       // PendingIntent.getBroadcast( context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )
        return pendingIntent
    }
}