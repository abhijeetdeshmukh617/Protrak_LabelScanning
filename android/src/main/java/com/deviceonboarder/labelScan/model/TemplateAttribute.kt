package com.deviceonboarder.labelScan.model

import android.util.Log

class TemplateAttribute {
    companion object {
        val IDENTIFIER_MULTIPLE = "Multiple"
    }

    var identifier: String = ""

    //TODO - enum for attribute type
    var type: String = ""
    var ordinal: String = ""
    var prefix: String = ""
    val regex: String = ""
  //  var expression: ScanTemplate.STLValueRegexType = ScanTemplate.STLValueRegexType.NONE

    /* To be used when identifier is set to "Multiple" */
    var values: List<TemplateAttribute>? = null

    private fun getPrefixRegexStr(prefixStr: String): String {
        val specialCharsRegex = Regex("[^A-Za-z0-9\\s]")
        val specialCharsAndSpaceRegex = Regex("[^A-Za-z0-9]*\\s*")
        val prefixRegexStrFmt = "%s|%s|%s|%s"  //prefix-prefixVariant1-prefixVariant2
        var retVal = ""

        /* Get comma separated prefixes */
        val prefixList = prefixStr.split(",".toRegex())

        for (p in prefixList) {
            val prefixVal = p.trim()
            if (prefixVal.isNotEmpty()) {
                /** Handle special characters in prefix string.
                 * exa: P/N could be parsed as either P/N or PIN or PN */
                val prefixWithoutSymbols = specialCharsRegex.replace(prefixVal, "");
                val prefixWithoutSpacesAndSymbols =
                    specialCharsAndSpaceRegex.replace(prefixVal, "");
                val iVariantRegex = Regex("[/|]")
                val prefixWithI = iVariantRegex.replace(prefixVal, "I")
                /* compose prefix matching regex string using all prefix variants */
                var prefixRegexStr = prefixRegexStrFmt.format(
                    prefixVal, prefixWithoutSymbols,
                    prefixWithoutSpacesAndSymbols, prefixWithI
                )

                /* assign or append to regex string */
                if (retVal.isNullOrEmpty()) {
                    retVal = prefixRegexStr
                } else {
                    /* append all previous prefix regex str with current one by OR-ing them */
                    retVal = "%s|%s".format(retVal, prefixRegexStr)
                }
            }
        }
        return retVal
    }

    public fun getRegexToMatch(valueOnly: Boolean = false,regex: String): Regex? {
        if (prefix.isNullOrEmpty() ) {
            return null
        }

        val regexToScanWithPrefixStrFmt = "(%s)%s(%s)?" //prefix-whitespaces-value
        val regexToScanValOnlyStrFmt = "\\b(%s)\\b" //prefix-whitespaces-value

        val prefixRegexStr = getPrefixRegexStr(prefix)
         var regexToScanStr = ""
        if (valueOnly || prefixRegexStr.isNullOrEmpty()) {
            /* compose just the value regex string as word-block+value-regex+word-block */
            regexToScanStr = regexToScanValOnlyStrFmt.format(regex);
        }else {
            /* compose the whole regex string as prefix-regex + whitespaces + value-regex */
            regexToScanStr = regexToScanWithPrefixStrFmt.format(prefixRegexStr,
                "\\s*[^A-Za-z0-9]?\\s*", regex);
        }
        return Regex(regexToScanStr, setOf(RegexOption.IGNORE_CASE));
    }
}
