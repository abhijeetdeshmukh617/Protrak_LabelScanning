import UIKit
import Vision
import MLKitBarcodeScanning
import MLKitVision
import MLKitTextRecognition

class MlkitExtractor {
    func process(template: String,data: [CapturedImage], completion: @escaping (String?) -> Void) {
        let dispatchGroup = DispatchGroup()
        var OcrDataList: [OcrImageData] = []
        var BarCodeDataList: [String] = []
        
        for (index, imageData) in data.enumerated() {
            guard let cgImage = imageData.image.cgImage else {
                //  print("Image \(index + 1) is invalid.")
                continue
            }
            
            dispatchGroup.enter()
            detectBarcodes(from: imageData.image) { results in
                DispatchQueue.main.async {
                    print("barcode >>> ----- \(results)")
                    BarCodeDataList.append(contentsOf: results)
                    dispatchGroup.leave()
                }
            }
            dispatchGroup.enter()
            detectOcrText(from: imageData) { results in
                DispatchQueue.main.async {
                    print(" OcrDataList >>> ----- ")
                    OcrDataList.append(contentsOf: results)
                    dispatchGroup.leave()
                }
            }
        }
        dispatchGroup.notify(queue: .main) {
            print("✅ All scans completed")
            let manager = ScanDataManager()
           let resultJson = manager.processScans(template: template,ocrDataList: OcrDataList, barCodeDataList: BarCodeDataList)
            completion(resultJson)
        }
    }

    
    func groupTextIntoLines(observations: [VNRecognizedTextObservation], yThreshold: CGFloat = 0.02) -> [[VNRecognizedText]] {
        var lineGroups: [[(VNRecognizedTextObservation, VNRecognizedText)]] = []

        for obs in observations {
            guard let text = obs.topCandidates(1).first else { continue }
            let midY = obs.boundingBox.midY

            if let index = lineGroups.firstIndex(where: {
                guard let firstObs = $0.first?.0 else { return false }
                return abs(firstObs.boundingBox.midY - midY) < yThreshold
            }) {
                lineGroups[index].append((obs, text))
            } else {
                lineGroups.append([(obs, text)])
            }
        }
        return lineGroups.map { $0.map { $0.1 } }
    }

    
}

