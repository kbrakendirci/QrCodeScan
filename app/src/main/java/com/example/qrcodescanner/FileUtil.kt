package com.example.qrcodescanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import android.util.Base64
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtil {


    fun createImageFile(context: Context): File? {
        return try {
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (ex: Exception) {
            null
        }
    }

    fun getImage(imagePath: String): ByteArray {
        val file = File(imagePath)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes
    }

    fun ByteArray.toBitmap(quality: Int = 40): Bitmap? {
        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        return bitmap
    }

    fun Bitmap.getRotatedImage(imagePath: String): Bitmap? {

        val orientation: Int = ExifInterface(imagePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(270f)
            ExifInterface.ORIENTATION_NORMAL -> this
            else -> this
        }
    }

    private fun Bitmap.rotateImage(angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }



}