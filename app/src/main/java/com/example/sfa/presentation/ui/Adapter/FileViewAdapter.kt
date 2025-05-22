package com.example.sfa.presentation.ui.Adapter

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.sfa.R
import com.example.sfa.data.model.FileModel
import com.example.sfa.databinding.ItemCircularCardViewBinding
import com.example.sfa.databinding.ItemCircularViewBinding
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.net.URL



class FileViewAdapter(
    private val context: Context,
    private val circularViewModelList: ArrayList<FileModel>,
    private val layoutType: Int
) : RecyclerView.Adapter<FileViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    private var receiver: BroadcastReceiver? = null
    private var downloadManager: DownloadManager? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (layoutType == 0) {
            val binding = ItemCircularViewBinding.inflate(inflater, parent, false)
            MyViewHolder(binding)
        } else {
            val binding = ItemCircularCardViewBinding.inflate(inflater, parent, false)
            MyViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = circularViewModelList[position]

        val url = try {
            URL(item.url)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            null
        }

        val filename = url?.path ?: ""
        val filetype = filename.substringAfterLast('.', "")
        val cacheDir = context.cacheDir
        val filecheck = File(cacheDir, item.fileName)

        if (layoutType == 0) {
            val binding = holder.binding as ItemCircularViewBinding
            bindCommonViews(binding.download, binding.view, binding.share, filecheck)
            binding.filetypeimage.setBackgroundResource(getFileIcon(filetype))
            binding.filename.text = item.fileSubject
            binding.filesize.text = item.fileSize

            binding.download.setOnClickListener {
                receiver?.let { context.unregisterReceiver(it) }
                DownloadFiles(position, item.url, holder).execute()
            }

            binding.view.setOnClickListener {
                viewfile(filecheck)
            }

            binding.share.setOnClickListener {
                shareFile(filecheck)
            }

        } else {
            val binding = holder.binding as ItemCircularCardViewBinding
            bindCommonViews(binding.download, binding.view, binding.share, filecheck)
            binding.filetypeimage.setBackgroundResource(getFileIcon(filetype))
            binding.filename.text = item.fileSubject
            binding.filesize.text = item.fileSize

            binding.download.setOnClickListener {
                receiver?.let { context.unregisterReceiver(it) }
                DownloadFiles(position, item.url, holder).execute()
            }

            binding.view.setOnClickListener {
                viewfile(filecheck)
            }

            binding.share.setOnClickListener {
                shareFile(filecheck)
            }
        }
    }

    override fun getItemCount(): Int = circularViewModelList.size

    private fun bindCommonViews(
        downloadView: View,
        viewView: View,
        shareView: View,
        file: File
    ) {
        if (file.exists()) {
            downloadView.visibility = View.GONE
            viewView.visibility = View.VISIBLE
            shareView.visibility = View.VISIBLE
        } else {
            downloadView.visibility = View.VISIBLE
            viewView.visibility = View.GONE
            shareView.visibility = View.GONE
        }
    }

    private fun getFileIcon(filetype: String): Int {
        return when (filetype.lowercase()) {
            "pdf" -> R.drawable.icons_pdf
            "xlsx", "csv", "xls" -> R.drawable.icons_excel_csv
            "docx", "doc" -> R.drawable.icons_word_doc
            "mp4", "avi", "mov" -> R.drawable.icons_video
            "jpeg", "jpg", "png" -> R.drawable.icons_jpg
            else -> R.drawable.icons_files
        }
    }

    private fun viewfile(file: File) {
        val ext = file.name.substringAfterLast(".")
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        val path = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(path, mime)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(Intent.createChooser(openIntent, "View the file"))
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    private fun shareFile(file: File) {
        val path = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, path)
        }
        context.startActivity(Intent.createChooser(sharingIntent, "Share the file"))
    }

    inner class DownloadFiles(
        private val position: Int,
        private val path: String,
        private val holder: MyViewHolder
    ) : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String? {
            try {
                val url = URL(path)
                val connection = url.openConnection()
                val length = connection.contentLength
                val inputStream = BufferedInputStream(url.openStream(), length)
                val cacheDir = context.cacheDir
                if (!cacheDir.exists()) cacheDir.mkdir()
                val cacheFile = File(cacheDir, circularViewModelList[position].fileName)
                FileOutputStream(cacheFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var dataSize: Int
                    while (inputStream.read(buffer).also { dataSize = it } != -1) {
                        outputStream.write(buffer, 0, dataSize)
                    }
                    outputStream.flush()
                }
                inputStream.close()
            } catch (e: Exception) {
                Log.e("DownloadFiles", "Error: ${e.message}")
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            Toast.makeText(context, "File Download complete.", Toast.LENGTH_SHORT).show()
            notifyItemChanged(position) // Refresh UI
        }
    }
}
