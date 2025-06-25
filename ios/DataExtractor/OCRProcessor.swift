import UIKit
import Vision
import MLKitBarcodeScanning
import MLKitVision
import MLKitTextRecognition

func detectOcrText(from data: CapturedImage, completion: @escaping ([OcrImageData]) -> Void) {
    var ocrDataList: [OcrImageData] = []

    guard let cgImage = data.image.cgImage else {
        print("Failed to get CGImage from UIImage")
        completion([])
        return
    }

    let request = VNRecognizeTextRequest { request, error in
        guard error == nil else {
            print("Error during OCR: \(error!.localizedDescription)")
            completion([])
            return
        }

        guard let observations = request.results as? [VNRecognizedTextObservation] else {
            print("No text observations found")
            completion([])
            return
        }

        let groupedLines = groupTextIntoLines(observations: observations)

        for line in groupedLines {
            for word in line {
                let wordEntry = OcrData(text: word.string, confidence: word.confidence)
                let newOcrImageData = OcrImageData(ocrData: wordEntry, imagePath: data.fileURL) // Replace with actual URL if needed
                ocrDataList.append(newOcrImageData)
            }
        }
        print("completion(ocrDataList) detect :")
        completion(ocrDataList)
    }

    request.recognitionLevel = VNRequestTextRecognitionLevel.accurate
    request.usesLanguageCorrection = true
    request.recognitionLanguages = ["en-US"]


    let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
    do {
        try handler.perform([request])
    } catch {
        print("Failed to perform OCR: \(error.localizedDescription)")
        completion([])
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

