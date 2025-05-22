package com.example.sampleapp.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

object FileUtils {
    fun getPath(context: Context, uri: Uri): String? {
        var result: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val idx = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = it.getString(idx)
            it.close()
        }
        return result
    }
}
