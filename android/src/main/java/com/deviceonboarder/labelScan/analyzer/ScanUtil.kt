package com.deviceonboarder.labelScan.analyzer

import android.util.Log
import com.deviceonboarder.labelScan.model.ScanResultModel
import com.deviceonboarder.labelScan.model.TemplateAttribute

/** ScanUtil
 * 1. Contains Utils methods for matching the data line by line with the regex from the selected template attribute
 * 2. Contains a class ScannedLine used to compare the confidence level of data line by line
 **/
class ScanUtil {

    fun matchRegexForLine(
        scanAnalysis: ScanResultModel.ScanAnalysis,
        regex: String,
        lines: Array<ScannedLine>,
        templateAttr: TemplateAttribute
    ): ScanResultModel.ScanAnalysis {
        var scanRegex = regex.toRegex()
        var lineText: String = ""
        var lineConfidence: Float = 0.0f
        /* Combine all lines if received more than 1 */
        for (l in lines) {
            Log.d(
                "startScan / MatchRegex",
                String.format(
                    "adding line to line-text for regex match = %s",
                    l.text
                )
            )
            lineText += l.text + " "
            lineConfidence += l.confidence
        }
        lineText = lineText.trim()
        lineConfidence /= lines.size
        Log.d(
            "startScan / MatchRegex",
            String.format(
                "applying regex %s against line = %s",
                scanRegex.toString(),
                lineText
            )
        )
        scanRegex?.let {
            var scanMatch = scanRegex.matchEntire(lineText)
            Log.d(
                "startScan / MatchRegex",
                String.format(
                    "scanMatch= %s",
                    scanMatch
                )
            )
            scanMatch?.let() { entireMatchResult ->
                Log.d(
                    "startScan / MatchRegex",
                    String.format(
                        "Group-Count=%d, Group-Values=%s",
                        entireMatchResult.groupValues.size, entireMatchResult.groupValues
                    )
                )
                lateinit var prefix: String
                lateinit var data: String
                /* If prefix was empty then just get data else get both data and prefix */
                if (templateAttr.prefix.isNullOrEmpty()) {
                    var (group1) = entireMatchResult.destructured
                    data = group1
                    prefix = ""
                } else if(entireMatchResult.groupValues.size > 1){
                    var (group1, group2) = entireMatchResult.destructured
                    prefix = group1
                    data = group2
                }else{
                    prefix = ""
                    data = ""
                }
                Log.d(
                    "startScan / MatchRegex",
                    String.format(
                        "Match found prefix = %s, data=%s",
                        prefix,
                        data
                    )
                )
                scanAnalysis.value = data.trim()
                scanAnalysis.confidenceScore = lineConfidence
                scanAnalysis.identifier = templateAttr.identifier
                scanAnalysis.ordinal = templateAttr.ordinal
                scanAnalysis.type = templateAttr.type
                scanAnalysis.scannedText = lineText
            } ?: run {
                Log.d(
                    "startScan / MatchRegex",
                    String.format("No matching group found in entire-match. Try finding any match in text")
                )
                var scanMatch = scanRegex.find(lineText)
                scanMatch?.let() { findMatchResult ->
                    Log.d(
                        "startScan / MatchRegex",
                        String.format(
                            "Group-Count=%d, Group-Values=%s",
                            findMatchResult.groupValues.size, findMatchResult.groupValues
                        )
                    )
                    lateinit var prefix: String
                    lateinit var data: String
                    /* If prefix was empty then just get data else get both data and prefix */
                    Log.d(
                        "startScan / MatchRegex",
                        String.format(
                            "findMatchResult, prefix=%s",
                            templateAttr.prefix,

                        )
                    )
                    if (templateAttr.prefix.isNullOrEmpty()) {
                        var (group1) = findMatchResult.destructured
                        data = group1
                        prefix = ""
                    } else if(findMatchResult.groupValues.size > 1){
                        Log.d(
                            "startScan / MatchRegex",
                            String.format(
                                "findMatchResult, prefix=%s  Group-Count=%d",
                                findMatchResult,
                                findMatchResult.groupValues.size

                                )
                        )
                        var (group1, group2) = findMatchResult.destructured
                        prefix = group1
                        data = group2
                    }else{
                        prefix = ""
                        data = ""
                    }
                    Log.d(
                        "startScan / MatchRegex",
                        String.format(
                            "Match found prefix = %s, data=%s",
                            prefix,
                            data
                        )
                    )
                    scanAnalysis.value = data.trim()
                    scanAnalysis.confidenceScore = lineConfidence
                    scanAnalysis.identifier = templateAttr.identifier
                    scanAnalysis.ordinal = templateAttr.ordinal
                    scanAnalysis.type = templateAttr.type
                    scanAnalysis.scannedText = lineText

                    if (scanAnalysis.value.isNullOrEmpty()) {
                        matchValueRegexFromLine(
                            scanAnalysis,
                            templateAttr.getRegexToMatch(true,templateAttr.regex.toString()).toString(),// templateAttr.regex,
                            lineText, templateAttr
                        )
                    }
                } ?: run {
                    Log.d(
                        "startScan / MatchRegex",
                        String.format("No matching group found at any index in text")
                    )
                }
            }
        } ?: run {
            /**
             * Store the whole text as value when Regex is NULL
             * i.e. when prefix and expression are not provided
             */
            Log.d(
                "startScan / MatchRegex", String.format(
                    "Regex to match is %s. Store the line-text '%s' as value for identifier '%s'",
                    scanRegex ?: "null", lineText, templateAttr.identifier
                )
            )
            scanAnalysis.value = lineText
            scanAnalysis.confidenceScore = lineConfidence
            scanAnalysis.identifier = templateAttr.identifier
            scanAnalysis.ordinal = templateAttr.ordinal
            scanAnalysis.type = templateAttr.type
            scanAnalysis.scannedText = lineText
        }
        return scanAnalysis
    }

    fun matchValueRegexFromLine(
        scanAnalysis: ScanResultModel.ScanAnalysis,
        regex: String,
        lineText: String,
        templateAttr: TemplateAttribute
    ): ScanResultModel.ScanAnalysis {
        var scanRegex = regex.toRegex()
        Log.d("scanRegex"," matchValueRegexFromLine: "+scanRegex)
        scanRegex?.let { regex: Regex ->
            var scanMatch = regex.find(lineText)
            Log.d(
                "startScan / MatchValueRegex",
                String.format(
                    "Extracting value from line '%s', using expr '%s'",
                    lineText, regex.toString()
                )
            )
            scanMatch?.let() { findMatchResult ->
                Log.d(
                    "startScan / MatchValueRegex",
                    String.format(
                        "Group-Count=%d, Group-Values=%s",
                        findMatchResult.groupValues.size, findMatchResult.groupValues
                    )
                )
                if(findMatchResult.groupValues.size > 1) {
                var (data) = findMatchResult.destructured
                scanAnalysis.value = data
                Log.d("startScan / MatchValueRegex", String.format("Data found = %s", data))}
                else{
                    scanAnalysis.value = ""
                }
            }
        }

        return scanAnalysis;
    }
// ScannedLine class for checking confidence level of data line by line
    companion object {
        class ScannedLine(val text: String, val confidence: Float) {
        }
    }
}