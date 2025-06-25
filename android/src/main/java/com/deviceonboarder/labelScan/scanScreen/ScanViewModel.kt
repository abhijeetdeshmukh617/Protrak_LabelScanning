package com.deviceonboarder.labelScan.scanScreen

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.deviceonboarder.labelScan.analyzer.ScanDataManager
import com.deviceonboarder.labelScan.model.ScannedRawData
import com.deviceonboarder.labelScan.model.TemplateAttribute
import com.deviceonboarder.labelScan.util.Constants
import com.deviceonboarder.labelScan.util.SmoothedMutableLiveData
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.text.Text
import java.util.concurrent.Executor

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var executor: Executor
    lateinit var SelectedTemplateString: String
    private lateinit var templateAttrList: MutableList<TemplateAttribute>
    val sourceText = SmoothedMutableLiveData<ScannedRawData>(SMOOTHING_DURATION)
    val scannedBarCode = SmoothedMutableLiveData<List<Barcode>>(SMOOTHING_DURATION)
    val scanResultJson = SmoothedMutableLiveData<String>(SMOOTHING_DURATION)
    var timePending: Long = 0
    lateinit var imagePath: String
    var isScanning = false
    var scanDataManager = ScanDataManager()

    // converting selected template json object to TemplateAttribute class object
    fun convertJsonToObject(SelectedTemplateString: String) {
        val gson = Gson()
        templateAttrList =
            gson.fromJson(SelectedTemplateString, Array<TemplateAttribute>::class.java)
                .toMutableList()
    }

    companion object {
        private const val SMOOTHING_DURATION = 50L
    }

    // Processing scan data and creating result object json for sending back to react native
    fun processScan() {
        scanResultJson.setValue(
            scanDataManager.processScans(
                templateAttrList,
                imagePath
            )
        )
    }


    enum class MatchType {
        MATCH_FOUND,
        MATCH_IDENTIFIER_ONLY,
        MATCH_NONE
    }

// Adding BarCode and QrCode from scan data
    fun addCodeValue(barcodes: List<Barcode>) {
        barcodes.forEach { barcode ->
            val rawValue = barcode.rawValue
            Log.d(
                Constants.TAG_PROCESS_TEXT,
                String.format("BarCode type=%s, rawValue=%s", barcode.format, rawValue)
            )
            when (barcode.format) {
                FORMAT_QR_CODE, FORMAT_DATA_MATRIX -> scanDataManager.addScanQrCode(barcode)
                else -> scanDataManager.addScanBarCode(barcode)
            }
        }

    }


}




