import Foundation

struct ScanTranscript: Codable {
    let bcValues: BarcodeInfo
    let ocrValues: OcrValues
}

struct BarcodeInfo: Codable {
    let numCodes: Int
}

struct OcrValues: Codable {
    let ocrDataList: [OcrData]
}


