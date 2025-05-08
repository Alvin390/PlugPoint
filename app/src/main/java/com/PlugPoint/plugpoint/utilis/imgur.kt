package com.PlugPoint.plugpoint.utilis

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImgurUtils {

    fun getFileFromUri(uri: Uri?, context: Context): File {
        requireNotNull(uri) { "URI cannot be null" }

        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}
