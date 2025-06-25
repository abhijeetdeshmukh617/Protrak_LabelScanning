package com.deviceonboarder.labelScan.analyzer

import android.util.Log
import com.deviceonboarder.labelScan.model.ScanResultModel
import com.deviceonboarder.labelScan.scanScreen.ScanViewModel
import com.deviceonboarder.labelScan.util.Constants
import com.deviceonboarder.labelScan.util.ScanImageUtil
import java.io.File

/** ScanResult
 * 1. Adding data in scannedResultMap if match found using method addScanAnalysisIfMatchFound()
 * 2. Creating result object from scannedResultMap using method createResultObject()
 **/
class ScanResult {

    fun addScanAnalysisIfMatchFound(
        scanAnalysis: ScanResultModel.ScanAnalysis,
        scannedResultMap: HashMap<String, ScanResultModel.ScanAnalysis>,
    )
            : ScanViewModel.MatchType {
        Log.d("ScanAnalysis","ScanAnalysis ------------")
        var matchType = ScanViewModel.MatchType.MATCH_NONE
        Log.d("ScanAnalysis","ScanAnalysis scanAnalysis.identifier "+scanAnalysis.identifier)
        Log.d("ScanAnalysis","ScanAnalysis scanAnalysis.value "+scanAnalysis.value)
        if (scanAnalysis.identifier.isNotEmpty()) {
            if (scanAnalysis.value.isNotEmpty()) {
                val existingResult =
                    scannedResultMap[scanAnalysis.identifier]
                existingResult?.let {
                    val (status, result) = it.compareResult(scanAnalysis)
                    Log.d("ScanAnalysis","ScanAnalysis status "+status)
                    Log.d("ScanAnalysis","ScanAnalysis result "+result)
                    if (status && 0 > result) {
                        scannedResultMap[scanAnalysis.identifier] =
                            scanAnalysis
                        Log.d(
                            Constants.TAG_PROCESS_SCAN_ANALYSIS,
                            String.format(
                                "compare confidence to add for = %s, data=%s, confidence = %f",
                                scanAnalysis.identifier,
                                scanAnalysis.value,
                                scanAnalysis.confidenceScore
                            )
                        )
                    }
                } ?: run {
                    // increement counter
                    scannedResultMap.put(
                        scanAnalysis.identifier,
                        scanAnalysis
                    )
                    Log.d(
                        Constants.TAG_PROCESS_SCAN_ANALYSIS,
                        String.format(
                            "Added scan result for = %s, data=%s, confidence = %f",
                            scanAnalysis.identifier, scanAnalysis.value,
                            scanAnalysis.confidenceScore

                        )
                    )
                }
                matchType = ScanViewModel.MatchType.MATCH_FOUND
            } else {
                matchType = ScanViewModel.MatchType.MATCH_IDENTIFIER_ONLY
            }
        }
        Log.d("ScanAnalysis","ScanAnalysis matchType "+matchType)
        return matchType;
    }

    fun createResultObject(
        scannedResultMap:
        HashMap<String, ScanResultModel.ScanAnalysis>,
        scannedTranscriptMap:
        HashMap<ScanResultModel.ScanTextPosition, ScanResultModel.ScanTranscriptOcrEntry>,
        scannedTranscriptQRValues: ScanResultModel.QrValues,
        scannedTranscriptBCValues: ScanResultModel.BcValues,
        scanDataManager: ScanDataManager,
        scanResultImagePath : String
    ): ScanResultModel {
        var scanResult = ScanResultModel()
        var scanTranscript = ScanResultModel.ScanTranscript()
        var ocrValues = ScanResultModel.OcrValues()

        if (scannedResultMap.isNotEmpty()) {
            scanResult.scanAnalysis = ArrayList(scannedResultMap.values)
        }
          var blocks = ArrayList<ScanResultModel.Blocks>()
        for (transcript in scannedTranscriptMap) {
            var blockPos = transcript.key.block
            var linePos = transcript.key.line
            var value = transcript.value.line
            //var confidence = transcript.value.confidence
            Log.d(
                Constants.TAG_PROCESS_RESULT,
                String.format(
                    "Parsing transcript. blockpos = %d, linepos=%d, value =%s",
                    blockPos,
                    linePos,
                    value
                )
            )
            if (blockPos >= blocks.size) {
                for (ib in blocks.size until blockPos + 1) {
                    blocks.add(ScanResultModel.Blocks())
                }
            }
            if (null == blocks[blockPos].lines) {
                blocks[blockPos].lines = ArrayList<String>()
            }
            var blockLines = blocks[blockPos].lines as ArrayList<String>
            if (linePos >= blockLines.size) {
                for (il in blockLines.size until linePos) {
                    blockLines.add("")
                }
                blockLines.add(value)
            } else {
                blockLines[linePos] = value
            }
            blocks[blockPos].numLines = blockLines.size

        }
        ocrValues.blocks = blocks
        ocrValues.numBlocks = blocks.size
        scanTranscript.ocrValues = ocrValues
        scanTranscript.bcValues = scannedTranscriptBCValues
        scanTranscript.qrValues = scannedTranscriptQRValues
        scanResult.scanTranscript = scanTranscript
        scanResult.scanImagePath = analyzeBestImage(scanDataManager, scanResultImagePath)
       Log.d("startScan", " result1 : "+scanResult)
        return scanResult
    }

    // Check for the scanData with the maximum number of identifier matched
    // and delete the images for the remaining scanData
    fun analyzeBestImage(scanDataManager: ScanDataManager, scanResultImagePath: String): String {
        var retImagePath : String = scanResultImagePath
        val maxScanData = scanDataManager.getScanText().maxBy { it.numOfMatches }
        for (scanData in scanDataManager.getScanText()) {
            if (maxScanData !== scanData) {
                ScanImageUtil.deleteImage(scanData.imagePath)
            }
        }
        try {
            val newFile = File(scanResultImagePath)
            val oldFile = File(maxScanData.imagePath)
            oldFile.renameTo(newFile)
        } catch (ex : Exception) {
            retImagePath = maxScanData.imagePath
        }
        return retImagePath
    }

}
