package com.deviceonboarder.labelScan.analyzer

import android.util.Log
import com.deviceonboarder.labelScan.model.ScanResultModel
import com.deviceonboarder.labelScan.model.ScannedRawData
import com.deviceonboarder.labelScan.model.TemplateAttribute
import com.deviceonboarder.labelScan.scanScreen.ScanViewModel
import com.deviceonboarder.labelScan.util.Constants
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.text.Text

/** ScanDataProcessor
 * 1. Processes the scan data using methods processText(), processQRCodes(), processBarCodes()
 * 2. Matching the data line by line with the selected template regex using matchRegexForLine from ScanUtil class
 * 3. Added the result data in addScanAnalysisIfMatchFound from ScanResult Class
 **/
class ScanDataProcessor {

    var scanUtil = ScanUtil()
    var scanResult = ScanResult()

    fun processText(
        scannedResultMap: HashMap<String, ScanResultModel.ScanAnalysis>,
        scannedTranscriptMap: HashMap<ScanResultModel.ScanTextPosition, ScanResultModel.ScanTranscriptOcrEntry>,
        scanDataManager: ScanDataManager, templateAttrList: MutableList<TemplateAttribute>
    ) {
       val scanRawTextList = scanDataManager.getScanText()
        if (scanRawTextList.isNotEmpty()) {
            for ((index, scanText) in scanRawTextList.withIndex()) {
                Log.d(Constants.TAG_PROCESS_TEXT, String.format("Raw Block = %s", scanText.Text.text))
                var blockIndex = 0;
                while (blockIndex < scanText.Text.textBlocks.size) {
                    var block = scanText.Text.textBlocks[blockIndex]
                    Log.d(
                        Constants.TAG_PROCESS_TEXT,
                        String.format(
                            "block %d-th, Num of lines=%d, text=%s",
                            blockIndex,
                            block.lines.size,
                            block.text
                        )
                    )
                    var lineIndex = 0
                    while (lineIndex < block.lines.size) {
                        val line = block.lines[lineIndex]
                        Log.d(
                            Constants.TAG_PROCESS_TEXT,
                            String.format(
                                "checking  %d-th block, %d-th line, line.confidence = %f, text=%s",
                                blockIndex,
                                lineIndex,
                                line.confidence,
                                line.text
                            )
                        )
                        val scanTextPos = ScanResultModel.ScanTextPosition(blockIndex, lineIndex)
                        val scanTranscriptEntry = scannedTranscriptMap[scanTextPos]
                        scanTranscriptEntry?.let() {
                            if (scanTranscriptEntry.confidence < line.confidence) {
                                scannedTranscriptMap[scanTextPos] =
                                    ScanResultModel.ScanTranscriptOcrEntry(
                                        line.text,
                                        line.confidence
                                    )
                            }
                        } ?: run {
                            scannedTranscriptMap[scanTextPos] =
                                ScanResultModel.ScanTranscriptOcrEntry(line.text, line.confidence)
                        }
                        Log.d(Constants.TAG_PROCESS_TEXT, "current scanned line = " + line.text)
                        var matchType = ScanViewModel.MatchType.MATCH_NONE
                        if (templateAttrList.isNotEmpty()) {
                            var templateAttrIterator = templateAttrList.iterator()
                            while (templateAttrIterator.hasNext()) {
                                var templateAttr = templateAttrIterator.next()
                                Log.d(
                                    Constants.TAG_PROCESS_TEXT,
                                    "checking attribute = " + templateAttr.prefix
                                )
                                if (templateAttr.type == "text") {
                                    /** If in the previous iteration identifier was found but value was not
                                     * matched then use the same scanAnalysis do not create new object
                                     * In other words, create new scanAnalysis object only when
                                     * scanAnalysis.value from prev iteration is not NULL
                                     */
                                    var scanAnalysis = ScanResultModel.ScanAnalysis()
                                    Log.d( Constants.TAG_PROCESS_TEXT,"Line to regex : "+line.text)
                                     var scanRegex = templateAttr.getRegexToMatch(false, templateAttr.regex)
                                    Log.d(
                                        Constants.TAG_PROCESS_TEXT,
                                        String.format("Regex to use = %s", scanRegex.toString())
                                    )
                                    scanAnalysis = scanUtil.matchRegexForLine(
                                        scanAnalysis,
                                        scanRegex?.toString() ?: "",
                                        arrayOf(
                                            ScanUtil.Companion.ScannedLine(
                                                line.text,
                                                line.confidence
                                            )
                                        ),
                                        templateAttr
                                    )
                                    matchType =
                                        scanResult.addScanAnalysisIfMatchFound(
                                            scanAnalysis,
                                            scannedResultMap
                                        )
                                    if (ScanViewModel.MatchType.MATCH_FOUND == matchType) {
                                        Log.d("ScanTextList","updateScanTextList MatchType.MATCH_FOUND : 112 " +index)
                                        updateScanData(scanDataManager,index)
                                        break
                                    } else if (ScanViewModel.MatchType.MATCH_IDENTIFIER_ONLY == matchType) {
                                        Log.d(
                                            Constants.TAG_PROCESS_TEXT,
                                            String.format(
                                                "Identifier '%s' found. Data not found",
                                                scanAnalysis.identifier
                                            )
                                        )
                                        var nextLine: Text.Line? = null
                                        /** Identifier found but value is missing on this line.
                                         * Check the value in the next line as well
                                         */
                                        lineIndex++
                                        if (lineIndex < block.lines.size) {
                                            Log.d(
                                                Constants.TAG_PROCESS_TEXT,
                                                String.format(
                                                    "Checking for value in next line of the same block -'%s'",
                                                    block.lines[lineIndex].text
                                                )
                                            )
                                            nextLine = block.lines[lineIndex]
                                        } else {
                                            blockIndex++
                                            if (blockIndex < scanText.Text.textBlocks.size) {
                                                block = scanText.Text.textBlocks[blockIndex]
                                                lineIndex = 0
                                                nextLine =
                                                    block.lines[lineIndex]
                                                Log.d(
                                                    Constants.TAG_PROCESS_TEXT,
                                                    String.format(
                                                        "Checking for value in first line of next block as identifier was present in last line of previous block -'%s'",
                                                        nextLine.text
                                                    )
                                                )
                                            }
                                        }
                                        nextLine?.let {
                                            scanAnalysis = scanUtil.matchRegexForLine(
                                                scanAnalysis,
                                                scanRegex?.toString() ?: "",
                                                arrayOf(
                                                    ScanUtil.Companion.ScannedLine(
                                                        line.text,
                                                        line.confidence
                                                    ),
                                                    ScanUtil.Companion.ScannedLine(
                                                        it.text,
                                                        it.confidence
                                                    )
                                                ),
                                                templateAttr
                                            )
                                            matchType =
                                                scanResult.addScanAnalysisIfMatchFound(
                                                    scanAnalysis,
                                                    scannedResultMap
                                                )
                                            if (ScanViewModel.MatchType.MATCH_FOUND == matchType) {
                                                Log.d("ScanTextList","updateScanTextList MatchType.MATCH_FOUND : 175 " +index)

                                                updateScanData(scanDataManager,index)
                                                Log.d(
                                                    Constants.TAG_PROCESS_TEXT,
                                                    String.format(
                                                        "Match found. Value '%s' found in next line for Identifier '%s'",
                                                        scanAnalysis.value, scanAnalysis.identifier
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                if (ScanViewModel.MatchType.MATCH_FOUND == matchType || ScanViewModel.MatchType.MATCH_IDENTIFIER_ONLY == matchType) {
                                    break
                                }
                            }
                        }
                        lineIndex++
                    }
                    blockIndex++
                }
                       Log.d(
                    Constants.TAG_PROCESS_TEXT,
                    String.format(
                        "scannedResultMap.size  list size = %d,\nMap= %s",
                        scannedResultMap.size, scannedResultMap
                    )
                )
                Log.d(
                    Constants.TAG_PROCESS_TEXT,
                    String.format(
                        "ScannedtranscriptMap size=%d\nMap= %s",
                        scannedTranscriptMap.size, scannedTranscriptMap
                    )
                )
            }
        }
    }

    fun processQRCodes(
        scannedResultMap: HashMap<String, ScanResultModel.ScanAnalysis>,
        scannedTranscriptQRs: ScanResultModel.QrValues,
        qrCodes: ArrayList<Barcode>,
        templateAttrList: MutableList<TemplateAttribute>
    ) {
        Log.d(Constants.TAG_PROCESS_CODE, String.format("scanning QRcodes = %d", qrCodes.size))
        var qrCodeSet: MutableSet<String> = mutableSetOf()
        for (code in qrCodes) {
            Log.d(
                Constants.TAG_PROCESS_CODE,
                String.format("Current scanned QRCode = %s", code.rawValue)
            )
            (code.rawValue)?.let { qrCodeVal ->
                Log.d(Constants.TAG_PROCESS_CODE, String.format("Adding '%s' to QR-Code set", qrCodeVal))
                qrCodeSet.add(qrCodeVal)
                var matchType = ScanViewModel.MatchType.MATCH_NONE
                if (templateAttrList.isNotEmpty()) {
                    var templateAttrIterator = templateAttrList.iterator()
                    while (templateAttrIterator.hasNext()) {
                        var templateAttr = templateAttrIterator.next()
                        if (templateAttr.type == "qrcode") {
                            Log.d(
                                Constants.TAG_PROCESS_CODE,
                                String.format(
                                    "checking attribute, identifier= %s, prefix=%s",
                                    templateAttr.identifier, templateAttr.prefix
                                )
                            )
                            if (TemplateAttribute.IDENTIFIER_MULTIPLE.contentEquals(
                                    templateAttr.identifier,
                                    true
                                )
                            ) {
                                (templateAttr.values)?.let { attrList ->
                                    //Split the value string into multiple texts separated by whitespace or special char
                                    val qrCodeValueList = qrCodeVal.split("[\\s]+".toRegex())
                                    Log.d(
                                        Constants.TAG_PROCESS_CODE, String.format(
                                            "QRCode value split = %s, num of values=%d",
                                            qrCodeValueList, qrCodeValueList.size
                                        )
                                    )
                                    //Match up each value with each record under values
                                    var valIndex = 0
                                    while (valIndex < qrCodeValueList.size && valIndex < attrList.size) {
                                        val text = qrCodeValueList[valIndex]
                                        val attr = attrList[valIndex]
                                        Log.d(
                                            Constants.TAG_PROCESS_CODE,
                                            String.format(
                                                "Checking if value '%s' matches identifier '%s' in qrcode",
                                                text, attr.identifier
                                            )
                                        )
                                        matchType =
                                            processAttrFromBarCodeText(
                                                attr,
                                                text,
                                                scannedResultMap,

                                            )
                                        if (ScanViewModel.MatchType.MATCH_FOUND == matchType) {
                                            Log.d(
                                                Constants.TAG_PROCESS_CODE,
                                                String.format(
                                                    "Match found for identifier '%s' in '%s'",
                                                    attr.identifier, text
                                                )
                                            )
                                        }
                                        valIndex++
                                    }
                                } ?: run {
                                }
                            } else {
                                matchType = processAttrFromBarCodeText(
                                    templateAttr,
                                    qrCodeVal,
                                    scannedResultMap
                                )
                                if (ScanViewModel.MatchType.MATCH_FOUND == matchType) {
                                    break
                                }
                            }
                        }
                    }
                }
            } ?: run {
            }
        }
        scannedTranscriptQRs.qrcodes = qrCodeSet.toList()
        scannedTranscriptQRs.numCodes = scannedTranscriptQRs.qrcodes.size
    }

    fun processBarCodes(
        scannedResultMap: HashMap<String, ScanResultModel.ScanAnalysis>,
        scannedTranscriptBCs: ScanResultModel.BcValues,
        barcodes: ArrayList<Barcode>,
        templateAttrList: MutableList<TemplateAttribute>
    ) {
        Log.d(Constants.TAG_PROCESS_CODE, String.format("scanning barcodes = %d", barcodes.size))
        var barCodeSet: MutableSet<String> = mutableSetOf()
        for (code in barcodes) {
            Log.d(Constants.TAG_PROCESS_CODE, String.format("Current scanned Bar Code = %s", code.rawValue))
            (code.rawValue)?.let { qrCodeVal ->
                Log.d(Constants.TAG_PROCESS_CODE, String.format("Adding '%s' to Bar-Code set", qrCodeVal))
                barCodeSet.add(qrCodeVal)
                var matchType = ScanViewModel.MatchType.MATCH_NONE
                if (templateAttrList.isNotEmpty()) {
                    var templateAttrIterator = templateAttrList.iterator()
                    while (templateAttrIterator.hasNext()) {
                        var templateAttr = templateAttrIterator.next()
                        if (templateAttr.type == "barcode") {
                            Log.d(
                                Constants.TAG_PROCESS_CODE,
                                String.format(
                                    "checking attribute, identifier= %s, prefix=%s",
                                    templateAttr.identifier, templateAttr.prefix
                                )
                            )
                            if (TemplateAttribute.IDENTIFIER_MULTIPLE.contentEquals(
                                    templateAttr.identifier,
                                    true
                                )
                            ) {
                                (templateAttr.values)?.let { attrList ->
                                    //Split the value string into multiple texts separated by whitespace or special char
                                    val barCodeValueList = qrCodeVal.split("[\\s]+".toRegex())
                                    Log.d(
                                        Constants.TAG_PROCESS_CODE, String.format(
                                            "QRCode value split = %s, num of values=%d",
                                            barCodeValueList, barCodeValueList.size
                                        )
                                    )
                                    //Match up each value with each record under values
                                    var valIndex = 0
                                    while (valIndex < barCodeValueList.size && valIndex < attrList.size) {
                                        val text = barCodeValueList[valIndex]
                                        val attr = attrList[valIndex]
                                        Log.d(
                                            Constants.TAG_PROCESS_CODE,
                                            String.format(
                                                "Checking if value '%s' matches identifier '%s' in qrcode",
                                                text, attr.identifier
                                            )
                                        )
                                        matchType =
                                            processAttrFromBarCodeText(attr, text, scannedResultMap)
                                        if (ScanViewModel.MatchType.MATCH_FOUND == matchType) {
                                            Log.d(
                                                Constants.TAG_PROCESS_CODE,
                                                String.format(
                                                    "Match found for identifier '%s' in '%s'",
                                                    attr.identifier, text
                                                )
                                            )
                                        }
                                        valIndex++
                                    }
                                } ?: run {
                                }
                            } else {
                                matchType = processAttrFromBarCodeText(
                                    templateAttr,
                                    qrCodeVal,
                                    scannedResultMap
                                )
                                if (ScanViewModel.MatchType.MATCH_FOUND == matchType) {
                                    break
                                }
                            }
                        }
                    }
                }
            } ?: run {
            }
        }
        scannedTranscriptBCs.barcodes = barCodeSet.toList()
        scannedTranscriptBCs.numCodes = scannedTranscriptBCs.barcodes.size
    }


    fun processAttrFromBarCodeText(
        templateAttr: TemplateAttribute, text: String,
        scannedResultMap: HashMap<String, ScanResultModel.ScanAnalysis>
    )
            : ScanViewModel.MatchType {
        var matchType: ScanViewModel.MatchType =
            ScanViewModel.MatchType.MATCH_NONE
        var scanAnalysis = ScanResultModel.ScanAnalysis()
         var scanRegex = templateAttr.getRegexToMatch(false, templateAttr.regex)
        Log.d(
            Constants.TAG_PROCESS_CODE,
            String.format(
                "Regex to use = '%s'",
                scanRegex?.toString() ?: ""
            )
        )
        val line = ScanUtil.Companion.ScannedLine(text, 1.0f)
        // matching data for the selected template regex
        scanAnalysis = scanUtil.matchRegexForLine(
            scanAnalysis,
            scanRegex?.toString() ?: "",
            arrayOf(line),
            templateAttr
        )
        // Added data in result object if match found
        matchType = scanResult.addScanAnalysisIfMatchFound(scanAnalysis, scannedResultMap)
        return matchType
    }

    fun updateScanData(scanDataManager: ScanDataManager,index: Int) {
           scanDataManager.updateScanText(index)
    }

}