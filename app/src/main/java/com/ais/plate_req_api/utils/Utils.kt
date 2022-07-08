package com.ais.plate_req_api.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun Context.getFileFromUri(uri: Uri): File {

    val file = File(this.filesDir, UUID.randomUUID().toString() + ".jpg")
    try {
        val inputStream =
            this.contentResolver.openInputStream(uri)
                ?: throw NullPointerException("file was null")
        val outputStream = FileOutputStream(file)
        inputStream.use { i ->
            outputStream.use { o ->
                i.copyTo(o, 1024)
            }
        }
    } catch (e: Exception) {
        Log.e(">>>>>>>>", e.message.toString())
    }
    return file
}
