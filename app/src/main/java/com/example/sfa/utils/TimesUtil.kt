package com.example.sfa.utils

import android.content.Context

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimesUtil {

    const val FORMAT: String = "yyyy-MM-dd HH:mm:ss"

    const val FORMAT1: String = "yyyy-MM-dd"

    const val FORMAT2: String = "dd/MM/yyyy"


    const val FORMAT3: String = "dd/MM/yyyy HH:mm:ss"



    const val FORMAT5: String = "dd-MMM-yyyy"

    const val FORMAT6: String = "EEE"

    const val FORMAT4: String = "MMM"

    const val FORMAT7: String = "yyyy"

    const val FORMAT8: String = "dd"

    const val FORMAT9: String = "MM"

    const val FORMAT10: String = " [ MMMM - yyyy ] "

    const val FORMAT11: String = "MM/dd/yyyy"

    const val FORMAT12: String = "MM/dd/yy"

    const val FORMAT13: String = "dd MMMM"

    const val FORMAT14: String = "MMMM"

    const val FORMAT15: String = "HH:mm:ss"

    const val FORMAT16: String = "yyyy-MM"

    const val FORMAT17: String = "MMM - yyyy"


    fun getTimeStamp(date: String?, format: String?): Long {
        var date2: Date? = null
        try {
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            date2 = sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date2!!.time
    }

    fun getCurrentTimeStamp(format: String?): String {
        var stringDate = ""
        val timestampMilliseconds = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        simpleDateFormat.timeZone = TimeZone.getDefault()
        stringDate = simpleDateFormat.format(Date(timestampMilliseconds))

        return stringDate
    }




    fun getCurrentTimeStamp(timeStamp: Long, format: String?): String {
        val simpleDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        simpleDateFormat.timeZone = TimeZone.getDefault()
        return simpleDateFormat.format(Date(timeStamp))
    }

    fun getCurrentTime(format: String?): String {
//       2021-01-20 11:33:05
        val timestampMilliseconds = System.currentTimeMillis()

        val simpleDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        simpleDateFormat.timeZone = TimeZone.getDefault()
        val stringDate = simpleDateFormat.format(Date(timestampMilliseconds))

        return stringDate
    }

    fun getFormattedDate(currentFormat: String?, requiredFormat: String?, date: String?): String {
//       2021-01-20 11:33:05

        val currentDateFormat = SimpleDateFormat(currentFormat, Locale.ENGLISH)
        val requiredDateFormat = SimpleDateFormat(requiredFormat, Locale.ENGLISH)
        currentDateFormat.timeZone = TimeZone.getDefault()
        var outputDate = ""
        try {
            val convertedDate = currentDateFormat.parse(date)
            outputDate = requiredDateFormat.format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return outputDate
    }

    fun formatdate(fdate: String?): String {
        if (fdate == null || fdate == "") return ""

        var datetime = ""
        val inputFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val convertedDate = inputFormat.parse(fdate)
            datetime = d.format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return datetime
    }


    fun minDifference(date1: Date, date2: Date): Int {
        val MILLI_TO_HOUR = 1000 * 60
        return (date2.time - date1.time).toInt() / MILLI_TO_HOUR
    }


    fun dayDifference(date1: Date, date2: Date): Int {
        //        final int MILLI_TO_DAY = 1000 * 60 * 60 * 24;
//        (int) (date2.getTime() - date1.getTime()) / MILLI_TO_DAY

        return TimeUnit.MILLISECONDS.toDays(date2.time - date1.time).toInt()
    }

    fun getDate(format: String?, dateString: String?): Date? {
        val formatter1 = SimpleDateFormat(format, Locale.ENGLISH)
        var date: Date? = null
        try {
            date = formatter1.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }


    fun getDayOfWeek(date: String, pattern: String?): String? {
        var finalDay: String? = null
        try {
            val input_date = date
            val format1 =
                SimpleDateFormat(TimesUtil.FORMAT2, Locale.ENGLISH)
            val dt1 = format1.parse(input_date)
            val format2: DateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
            finalDay = format2.format(dt1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return finalDay
    }

    fun getDayFromMonth(date: String): String? {
        var finalDay: String? = null
        try {
            val input_date = date
            val format1 =
                SimpleDateFormat(TimesUtil.FORMAT1, Locale.ENGLISH)
            val dt1 = format1.parse(input_date)
            val format2: DateFormat =
                SimpleDateFormat(TimesUtil.FORMAT4, Locale.ENGLISH)
            finalDay = format2.format(dt1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return finalDay
    }

    fun getDayFromDate(date: String): String? {
        var finalDay: String? = null
        try {
            val input_date = date
            val format1 =
                SimpleDateFormat(TimesUtil.FORMAT1, Locale.ENGLISH)
            val dt1 = format1.parse(input_date)
            val format2: DateFormat = SimpleDateFormat("d", Locale.ENGLISH)
            finalDay = format2.format(dt1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return finalDay
    }

    fun getDayFromDate(date: Calendar): String? {
        var finalDay: String? = null
        val format2: DateFormat = SimpleDateFormat("d", Locale.ENGLISH)
        finalDay = format2.format(date.time)
        return finalDay
    }

    fun getDateFromCalendar(date: Calendar): String? {
        var finalDay: String? = null
        val format2: DateFormat =
            SimpleDateFormat(TimesUtil.FORMAT1, Locale.ENGLISH)
        finalDay = format2.format(date.time)
        return finalDay
    }


    fun compareCurrentAndLoginDate(dateTime: String?): Int {
        val dateFormat: DateFormat =
            SimpleDateFormat(TimesUtil.FORMAT1, Locale.ENGLISH)
        try {
            if (dateTime != null && dateTime != "") {
                val date1 = dateFormat.parse(dateTime)
                val date =
                    dateFormat.parse(TimesUtil.getCurrentTime(TimesUtil.FORMAT1))
                return date1.compareTo(date)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 1
    }

   /* fun addLoginDate() {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        //        calendar.set(Calendar.DAY_OF_MONTH, -2);
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val strDate = dateFormat.format(calendar.time)
       // Constant.getInstance().setValue(strDate, StringConstants.LOGIN_DATE)
    }*/

    fun addLoginDate(applicationContext:Context) {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        //        calendar.set(Calendar.DAY_OF_MONTH, -2);
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val strDate = dateFormat.format(calendar.time)
        SecureStorage.setString(applicationContext,StringConstants.LOGIN_DATE,strDate)

    }


}