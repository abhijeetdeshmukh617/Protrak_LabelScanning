package com.deviceonboarder.labelScan.analyzer

import android.content.Context
import android.util.Log
import com.deviceonboarder.labelScan.model.ScanResultModel
import com.deviceonboarder.labelScan.model.ScannedRawData
import com.deviceonboarder.labelScan.model.TemplateAttribute
import com.deviceonboarder.labelScan.util.Constants
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.text.Text

/** ScanDataManager
 * 1. Contains scan data such as text, barcode, Qrcode
 * 2. Processes the data with the help of ScanDataProcessor class
 * 3. Creates result object using ScanResult class
 * 4. Contains Getter and Setter methods for text, barcode, Qrcode
 */
class ScanDataManager {

    var scannedDataList = ArrayList<ScannedRawData>()
    var qrCodeList = ArrayList<Barcode>()
    var barCodeList = ArrayList<Barcode>()
    var scanResult = ScanResult()
    var scanDataProcessor = ScanDataProcessor()

    fun processScans(
        templateAttrList: MutableList<TemplateAttribute>,
        scanResultImagePath : String
    ): String {

        val scannedResultMap =
            HashMap<String, ScanResultModel.ScanAnalysis>()
        val scannedTranscriptOcrMap =
            HashMap<ScanResultModel.ScanTextPosition, ScanResultModel.ScanTranscriptOcrEntry>()
        var scannedTranscriptQRValues = ScanResultModel.QrValues()
        var scannedTranscriptBCValues = ScanResultModel.BcValues()

        // Processing the Scan Data using processText,processQRCodes,processBarCodes
     /*   scanDataProcessor.processBarCodes(
            scannedResultMap,
            scannedTranscriptBCValues,
            barCodeList,
            templateAttrList
        )*/
        scanDataProcessor.processText(
            scannedResultMap,
            scannedTranscriptOcrMap,
            this,
            templateAttrList
        )
     /*   scanDataProcessor.processQRCodes(
            scannedResultMap,
            scannedTranscriptQRValues,
            qrCodeList,
            templateAttrList
        )*/


        Log.d(
            Constants.TAG_PROCESS_TEXT,
            String.format("num of scanned Results = %d", scannedResultMap.size)
        )
        // Creates the result object for the processed data
        val scanResult = scanResult.createResultObject(
            scannedResultMap, scannedTranscriptOcrMap,
            scannedTranscriptQRValues, scannedTranscriptBCValues, this,
            scanResultImagePath)
        // Creates Json object for the result object for sending the json to react native
        val scanResultJsonStr = Gson().toJson(scanResult)
        Log.d(
            Constants.TAG_PROCESS_TEXT,
            String.format("Result json String = %s", scanResultJsonStr)
        )
        return scanResultJsonStr
    }

    fun addScanText(scannedRawData: ScannedRawData) {
        scannedDataList.add(scannedRawData)
    }

    fun updateScanText(position: Int) {
        scannedDataList[position].numOfMatches++
    }

    fun addScanQrCode(barcode: Barcode) {
        qrCodeList.add(barcode)
    }

    fun addScanBarCode(barcode: Barcode) {
        barCodeList.add(barcode)
    }

    fun getScanText(): ArrayList<ScannedRawData> {
        return scannedDataList
    }

    fun getScanQrCode(): ArrayList<Barcode> {
        return qrCodeList
    }

    fun getScanBarCode(): ArrayList<Barcode> {
        return barCodeList
    }
}