package com.deviceonboarder.labelScan.util

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScanImageUtil {

    companion object {
        fun SaveImage(bitmap: Bitmap, imagePath: String) {
            val imageFile = File(imagePath)
            try {
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun deleteImage(filePath: String) {
            val fileToDelete = File(filePath)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }
    }
}