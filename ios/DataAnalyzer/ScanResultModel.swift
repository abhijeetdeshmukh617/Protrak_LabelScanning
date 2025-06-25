import Foundation

struct ScanResultModel: Codable {
    var template: String = ""
    var scanAnalysis: [ResultModel] = []
    var scanTranscript: ScanTranscript = ScanTranscript()
    var scanImagePath: String = ""

    static func buildFinalJSONResult(analysisResults: [TemplateMatchResult], ocrDataList: [OcrImageData],barcodesValues: [String]) -> String? {
       let cleanResults: [ResultModel] = analysisResults.map {
                ResultModel(identifier: $0.identifier, type: $0.type, value: $0.value)
            }
        var barcodes = BcValues()
        barcodes.barcodes = barcodesValues
        barcodes.numCodes = barcodes.barcodes.count
        let finalResult = ScanResultModel(
            scanAnalysis: cleanResults,
            scanTranscript: ScanTranscript(
                ocrValues: encodeOcrImageDataList(ocrDataList),
                    qrValues: QrValues(),                       // default init for qrValues
                bcValues: barcodes
            )
        )

        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]

        do {
            let jsonData = try encoder.encode(finalResult)
            return String(data: jsonData, encoding: .utf8)
        } catch {
            print("❌ Failed to encode final result to JSON: \(error)")
            return nil
        }
    }
    
    struct AnalysisCompareResult : Codable{
        let status: Bool
        let result: Int
    }

    struct ScanAnalysis: Codable {
        var identifier: String = ""
        var ordinal: String = ""
        var type: String = ""
        var value: String = ""
        var image: String = "" // Consider excluding if transient
        var scannedText: String = "" // Consider excluding if transient
        var confidenceScore: Float = 0.0


        func compareResult(to other: ScanAnalysis) -> AnalysisCompareResult {
            var result = -1
            var couldCompare = false
            if self.identifier == other.identifier {
                couldCompare = true
                if self.confidenceScore > other.confidenceScore {
                    result = 1
                } else if self.confidenceScore == other.confidenceScore {
                    result = 0
                } else {
                    result = -1
                }
            }
            return AnalysisCompareResult(status: couldCompare, result: result)
        }
    }


    
    struct ScanTextPosition: Hashable, Codable {
        let block: Int
        let line: Int
    }

    struct ScanTranscriptOcrEntry: Codable {
        let line: String
        let confidence: Float

        func compareEntry(to other: ScanTranscriptOcrEntry) -> AnalysisCompareResult {
            var result = -1
            var couldCompare = false
            if self.line.trimmingCharacters(in: .whitespacesAndNewlines) ==
                other.line.trimmingCharacters(in: .whitespacesAndNewlines) {
                couldCompare = true
                if self.confidence > other.confidence {
                    result = 1
                } else if self.confidence == other.confidence {
                    result = 0
                } else {
                    result = -1
                }
            }
            return AnalysisCompareResult(status: couldCompare, result: result)
        }
    }

    struct ScanTranscript: Codable {
        var ocrValues:String = ""
        var qrValues: QrValues = QrValues()
        var bcValues: BcValues = BcValues()
    }

   

    struct Blocks: Codable {
        var numLines: Int = 0
        var lines: [String]? = nil
    }

    struct QrValues: Codable {
        var numCodes: Int = 0
        var qrcodes: [String] = []
    }

    struct SerialNumber: Codable {
        var Serial_Number: String = ""
    }

    struct BcValues: Codable {
        var numCodes: Int = 0
        var barcodes: [String] = []
    }

    enum MatchType {
        case matchFound
        case matchIdentifierOnly
        case matchNone
    }
}
