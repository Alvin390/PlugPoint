package com.PlugPoint.plugpoint.utilis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/** Compresses an image Uri to ~80% JPEG quality before upload.
 * Returns the compressed [File]. Callers can then create multipart bodies.
 */
object ImageCompressUtils {
    fun compressUri(context: Context, uri: Uri, maxWidth: Int = 1080, quality: Int = 80): File {
        val inputStream = context.contentResolver.openInputStream(uri) ?: error("Unable to open image")
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        val ratio: Float = originalBitmap.width.toFloat() / originalBitmap.height
        val targetWidth = maxWidth
        val targetHeight = (targetWidth / ratio).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

        val compressedFile = File.createTempFile("plugpoint_comp", ".jpg", context.cacheDir)
        FileOutputStream(compressedFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        return compressedFile
    }
}
