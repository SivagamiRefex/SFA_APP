package com.example.sampleapp.sqlite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBController(context: Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private val DATABASE_NAME = "dbSynMaster"
        private val DATABASE_VERSION = 1
        val TABLE_NAME = "tblSynMaster"
        private val columnName: String = "columnName" // column name
        private val ID: String = "ID" // auto generated ID column
        const val columnValue: String = "columnValue" // column value
        const val TABLE_ORDER: String = "table_order"
        const val DATA_KEY: String = "dataKey" // column name
        const val DATA_RESPONSE: String = "dataResponse" // column name
        const val AXN_KEY: String = "axnKey" // column name
        const val IS_ORDER_KEY: String = "isOrder" // column name
        const val IS_UPDATED_TO_SERVER: String = "isUpdatedToServer"
        const val FORM_DATA_SAVE: Int = 2
        const val CUSTOMER_CHECK_IN_OUT=4
        const val ALL_ORDER: Int = 3
        const val TABLE_IMAGE: String = "table_image"
        const val IMAGE_PATH: String = "imagePath"

// column name
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val query =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + ID + " integer primary key, " + columnName + " text, " + columnValue + " text )"
        if (db != null) {
            db.execSQL(query)
        }
        val orderQuery =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER + "(" + ID + " integer primary key, " + DATA_KEY + " text, " + DATA_RESPONSE + " text, " + IS_UPDATED_TO_SERVER + " text, " + AXN_KEY + " text, " + IS_ORDER_KEY + " integer " + ")"
        if (db != null) {
            db.execSQL(orderQuery)
        }
        val audioQuery =
            "CREATE TABLE IF NOT EXISTS " + DBController.TABLE_IMAGE + "(" + ID + " integer primary key, " + DATA_KEY + " text, " + IS_UPDATED_TO_SERVER + " text, " + DBController.IMAGE_PATH + " text " + ")"
        if (db != null) {
            db.execSQL(audioQuery)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)

       var  query = "DROP TABLE IF EXISTS " + TABLE_ORDER
        db?.execSQL(query)

        query = "DROP TABLE IF EXISTS " + DBController.TABLE_IMAGE
        db?.execSQL(query)


    }


    fun updateProduct(tableName: String, tableValue: String?): Boolean {
        try {
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(columnName, tableName)
            cv.put(columnValue, tableValue)

            val args = arrayOf(tableName)
            val value = db.update(TABLE_NAME, cv, columnName + " = ?", args)

            Log.e("local storage data","updateProduct: $value")

            //            db.close();
            return value > 0
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
    }


    fun addProduct(tableName: String?, tableValue: String?): Boolean {
        try {
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(columnName, tableName)
            cv.put(columnValue, tableValue)
            val value = db.insert(TABLE_NAME, null, cv)
            Log.e("local storage data","addProduct: value $value")
            //            db.close();
            return true
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            return false
        }
    }

    @SuppressLint("Range")
    fun getResponse(keyName: String): String {
        val value = ""
        val args = arrayOf(keyName)

        val database = this.writableDatabase
        val cursor = database.rawQuery(
            "SELECT * FROM " + TABLE_NAME + " WHERE " +columnName + " = ?",
            args
        )
        try {
            cursor!!.moveToFirst()

            Log.e("local storage data", "getResponse: keyName : $keyName")

            Log.e("local storage data", "getResponse: " + cursor.count)

            if (cursor.count > 0 && cursor.columnCount > 0) return cursor.getString(
                cursor.getColumnIndex(columnValue)
            )

            //close cursor & database
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null && !cursor.isClosed) cursor.close()

            //database.close();
        }
        return value
    }

    fun clearDatabase(TABLE_NAME: String) {
        val db = this.readableDatabase

        val clearDBQuery = "DELETE FROM $TABLE_NAME"
        db.execSQL(clearDBQuery)
    }


    fun addDataOfflineCalls(tableName: String?, tableValue: String?, axnKey: String?, isOrder: Int): Boolean {
        try {
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(DATA_KEY, tableName)
            cv.put(DATA_RESPONSE, tableValue)
            cv.put(IS_UPDATED_TO_SERVER, "0")
            cv.put(AXN_KEY, axnKey)
            cv.put(IS_ORDER_KEY, isOrder)
            val value = db.insert(TABLE_ORDER, null, cv)
            Log.e("dbcontroller", "addDataOfflineCalls: value $value")
            return true
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            return false
        }
    }

    fun getAllDataKey(type: Int): ArrayList<HashMap<String, String>> {
        val formList = ArrayList<HashMap<String, String>>()
        val database = this.writableDatabase
        val cursor = database.rawQuery("SELECT * FROM " + TABLE_ORDER, null)
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(3) == "0" && (type == DBController.ALL_ORDER || cursor.getInt(5) == type)) {
                    val map = HashMap<String, String>()
                    map[ID] = cursor.getString(0)
                    map[DATA_KEY] = cursor.getString(1)
                    map[DATA_RESPONSE] = cursor.getString(2)
                    map[IS_UPDATED_TO_SERVER] = cursor.getString(3)
                    map[AXN_KEY] = cursor.getString(4)
                    formList.add(map)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        database.close()
        return formList
    }

    fun updateDataOfflineCalls(tableName: String): Boolean {
        try {
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(DATA_KEY, tableName)
            cv.put(IS_UPDATED_TO_SERVER, "1")
            val args = arrayOf(tableName)
            val value = db.update(TABLE_ORDER, cv, DATA_KEY + " = ?", args)
            Log.e("dbcontroller", "updateDataOfflineCalls: $value")
            return value > 0
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            return false
        }
    }

    fun getImagePath(): java.util.ArrayList<java.util.HashMap<String, String>> {
        val imagePathList = java.util.ArrayList<java.util.HashMap<String, String>>()
        val database = this.writableDatabase
        val cursor = database.rawQuery("SELECT * FROM " + DBController.TABLE_IMAGE, null)
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(2) == "0" && cursor.getString(1) != null && cursor.getString(1) != "") {
                    val map = java.util.HashMap<String, String>()
                    map[ID] = cursor.getString(0)
                    map[DATA_KEY] = cursor.getString(1)
                    map[IS_UPDATED_TO_SERVER] = cursor.getString(2)
                    map[DBController.IMAGE_PATH] = cursor.getString(3)
                    imagePathList.add(map)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        database.close()

        // return contact list
        return imagePathList
    }
    fun addImageFile(tableName: String?, imagePath: String?): Boolean {
        try {
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(DATA_KEY, tableName)
            cv.put(IS_UPDATED_TO_SERVER, "0")
            cv.put(IMAGE_PATH, imagePath)
            val value = db.insert(TABLE_IMAGE, null, cv)

            Log.e("DBCONTROLLER", "addAudioFile: value $value")

            //            db.close();
            return true
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            return false
        }
    }

    fun updateImageOffline(tableName: String): Boolean {
        try {
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(DATA_KEY, tableName)
            cv.put(IS_UPDATED_TO_SERVER, "1")
            val args = arrayOf(tableName)
            val value = db.update(DBController.TABLE_IMAGE, cv, DATA_KEY + " = ?", args)

            Log.e("dbcontroller", "updateAudioOffline: $value")

            //            db.close();
            return value > 0
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            return false
        }
    }


    fun getData(): java.util.ArrayList<java.util.HashMap<String, String>> {
        val productList = java.util.ArrayList<java.util.HashMap<String, String>>()
        val database = this.writableDatabase
        val cursor = database.rawQuery("SELECT * FROM " + DBController.TABLE_NAME, null)
        if (cursor.moveToFirst()) {
            do {
                val map = java.util.HashMap<String, String>()
                map["id"] = cursor.getString(0)
                map["product"] = cursor.getString(1)
                map["category"] = cursor.getString(2)
                productList.add(map)
            } while (cursor.moveToNext())
        }

        cursor.close()
        database.close()

        // return contact list
        return productList
    }





}