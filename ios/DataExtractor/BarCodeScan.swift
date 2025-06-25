import SwiftUI
import Vision
import UIKit


func detectBarcodes(from image: UIImage, completion: @escaping ([String]) -> Void) {
    guard let cgImage = image.cgImage else {
        completion([])
        return
    }

    let request = VNDetectBarcodesRequest { request, error in
        guard error == nil else {
            print("Barcode detection error: \(error!.localizedDescription)")
            completion([])
            return
        }

        let barcodes = request.results?.compactMap { result -> String? in
            guard let barcode = result as? VNBarcodeObservation else { return nil }
            return barcode.payloadStringValue
        } ?? []
        print("completion(barcodes) detect ")
        completion(barcodes)
    }

    let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])

    DispatchQueue.global(qos: .userInitiated).async {
        do {
            try handler.perform([request])
        } catch {
            print("Failed to perform barcode request: \(error)")
            completion([])
        }
    }
}
