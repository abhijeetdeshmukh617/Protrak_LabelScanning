package com.deviceonboarder.labelScan.model

/** ScanResultModel
 * Contains the result object structure which is used to create result object and result json
**/
class ScanResultModel {

    lateinit var template: String
    lateinit var technician: Technician
    lateinit var scanAnalysis: ArrayList<ScanAnalysis>
    lateinit var scanTranscript: ScanTranscript
    lateinit var scanImagePath: String
    class Technician {
        lateinit var deviceType: String
        lateinit var localIdentifier: String
        lateinit var tenantIdentifier: String
        lateinit var networkDomain: String
        lateinit var deviceName: String
    }

    data class AnalysisCompareResult(val status: Boolean, val result: Int)

    class ScanAnalysis {
        var identifier: String = ""
        var ordinal: String = ""
        var type: String = ""
        var value: String = ""
        @Transient
        var image: String = ""
        @Transient
        var scannedText: String = ""
        @Transient
        var confidenceScore: Float = 0.0f

        /**
         * @return (Boolean, Int) --> (status, result).
         * compareResult is set to 1 if this is better than other, 0 if both are same, else -1
         */
        public fun compareResult(other: ScanAnalysis) : AnalysisCompareResult {
            var result : Int = -1;
            var couldCompare = false;
            if (this.identifier.equals(other.identifier)) {
                couldCompare = true
                if (this.confidenceScore > other.confidenceScore)
                    result = 1
                else if(this.confidenceScore == other.confidenceScore)
                    result = 0
                else
                    result = -1
            }
            return AnalysisCompareResult(couldCompare, result)
        }
    }

    data class ScanTextPosition (val block: Int, val line: Int) {
        override fun hashCode(): Int {
            return block.hashCode()+line.hashCode()
        }
        override fun equals(other: Any?): Boolean {
            return (this === other) ||
                    ( other is ScanTextPosition
                    && block == other.block
                    && line == other.line)
        }
    }

    data class ScanTranscriptOcrEntry (val line: String, val confidence: Float)  {
        /**
         * @return (Boolean, Int) --> (status, result).
         * compareResult is set to 1 if this is better than other, 0 if both are same, else -1
         */
        public fun compareEntry(other: ScanTranscriptOcrEntry) : AnalysisCompareResult {
            var result : Int = -1;
            var couldCompare = false;
            if (this.line.trim().equals(other.line.trim())) {
                couldCompare = true
                if (this.confidence > other.confidence)
                    result = 1
                else if(this.confidence == other.confidence)
                    result = 0
                else
                    result = -1
            }
            return AnalysisCompareResult(couldCompare, result)
        }
    }

    class ScanTranscript {
        lateinit var ocrValues: OcrValues
        lateinit var qrValues: QrValues
        lateinit var bcValues: BcValues
    }

    class OcrValues {
        var numBlocks: Int = 0
        lateinit var blocks: List<Blocks>
    }

    class Blocks {
        var numLines: Int = 0
        var lines: List<String>? = null
    }

    class QrValues {
        var numCodes: Int=0
        lateinit var qrcodes: List<String>
    }

    class SerialNumber {
        lateinit var Serial_Number: String
    }

    class BcValues {
        var numCodes: Int=0
        lateinit var barcodes: List<String>
    }
}