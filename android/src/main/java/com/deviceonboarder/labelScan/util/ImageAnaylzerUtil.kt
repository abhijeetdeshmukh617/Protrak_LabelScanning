package com.deviceonboarder.labelScan.util

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class ImageAnaylzerUtil {
/*
    private val result: MutableLiveData<String>

    private fun recognizeTextOnDevice(
        image: InputImage
    ): Task<Text> {
        // Pass image to an ML Kit Vision API
        return detector.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                result.value = visionText.text
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                Log.e(TAG, "Text recognition error", exception)
                val message = getErrorMessage(exception)
                message?.let {

                }
            }
    }*/

    private fun getBarCodeText(image: InputImage): Task<List<Barcode>> {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        var barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)
        return barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach { barcode ->
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints
                    val rawValue = barcode.rawValue
                    /*
                    barcode.format
                    * 256 - QRCODE
                    1 -BAR CODE*/

                    Log.d(TAG, "getBarCodeText  Value :  " + barcode.valueType+" -->displayValue "+barcode.displayValue+" -->format "+barcode.format+"  =>  "+rawValue)
                }
            }
            .addOnFailureListener {
                val message = getErrorMessage(it)
                message?.let {

                }
            }
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

    companion object {
        private const val TAG = "MlkitImageAnalyzer"
    }
}