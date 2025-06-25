import Foundation
import UIKit
import MLKitBarcodeScanning
import MLKitVision

class ScanDataProcessor {
    
    func processText(
        ocrDataList: [OcrImageData],
        templateAttrList: [Template],
        barcodeResult: String,
        barcodes: [String]
    ) -> String?{
        for (index, data) in ocrDataList.enumerated() {
            let imageData = data.ocrData
            let imagePath = data.imagePath
            let text = imageData.text
            let confidence = imageData.confidence
        }
        
        /*********************************************/
        var resultMap: [String: TemplateMatchResult] = [:] // key = identifier
        
        for (index, data) in ocrDataList.enumerated() {
            let imageData = data.ocrData
            let imagePath = data.imagePath
            let text = imageData.text
            let confidence = imageData.confidence
            
            for attr in templateAttrList {
                if(attr.type == "text"){
                    let prefixes = attr.prefix.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }
                    
                    for prefix in prefixes {
                        // Check if text starts with this prefix
                        if text.starts(with: prefix) {
                            // Remove prefix and try to extract value
                            let valuePart = text.dropFirst(prefix.count).trimmingCharacters(in: .whitespaces)
                            
                            let valueFromCurrentLine = TextProcessHelper.matchValue(valuePart, withRegex: attr.regex)
                            
                            // If value is found in current line, process it
                            if let matchedValue = valueFromCurrentLine, !matchedValue.isEmpty {
                                TextProcessHelper.updateIfBetterMatch(attr: attr, value: matchedValue, confidence: confidence, into: &resultMap)
                            } else if index + 1 < ocrDataList.count {
                                let imageData = ocrDataList[index + 1].ocrData
                                let imagePath = ocrDataList[index + 1].imagePath
                                // If no match in current line, check next line
                                let nextLineText = imageData.text
                                let nextConfidence = imageData.confidence
                                if let nextValue = TextProcessHelper.matchValue(nextLineText, withRegex: attr.regex), !nextValue.isEmpty {
                                    TextProcessHelper.updateIfBetterMatch(attr: attr, value: nextValue, confidence: nextConfidence, into: &resultMap)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        
        if let jsonData = barcodeResult.data(using: .utf8) {
            do {
                // Decode into array of dictionaries
                if let barcodeArray = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [[String: String]] {
                    
                    // Iterate through barcode entries
                    for barcode in barcodeArray {
                        if let identifier = barcode["identifier"],
                           let type = barcode["type"],
                           let value = barcode["value"] {
                            TextProcessHelper.updateBarcodeResults(identifier: identifier, value: value, type: type, into: &resultMap)
                            print("Identifier: \(identifier), Type: \(type), Value: \(value)")
                        }
                    }
                    
                } else {
                    print("❌ Failed to cast JSON to expected type.")
                }
            } catch {
                print("❌ JSON decoding error: \(error.localizedDescription)")
            }
        } else {
            print("❌ Failed to convert JSON string to Data.")
        }
        
        /************* best Image selection start**********************************/
        
        // Get best image (highest confidence)
        if let best = ocrDataList.max(by: { $0.ocrData.confidence < $1.ocrData.confidence }) {
            let bestPath = best.imagePath

            // Remove all other images
            print("✅ ocrDataList size: \(ocrDataList.count)")
            for data in ocrDataList {
                if data.imagePath != bestPath {
                  ///  deleteImage(at: data.imagePath)
                }
            }

            print("✅ Best image retained: \(bestPath.path)")
        }
        
        /************* best Image selection end**********************************/
        print("✅ oCR process done:")
        let analysisResults = Array(resultMap.values)
        if let jsonString = ScanResultModel.buildFinalJSONResult(analysisResults: analysisResults, ocrDataList: ocrDataList,barcodesValues: barcodes) {
            print(" OCR result \(jsonString) ")
            return jsonString
        }
            return nil
        /*************************************************/
    }
    
    func processBarcode(  template: String,
                          barcodes: [String],
                         templateAttrList: [Template],
                         ocrDataList: [OcrImageData]) ->String? {
        
        var results: [[String: String]] = []

        for barcode in barcodes {
            for pattern in templateAttrList {
                let regex = pattern.regex
                let identifier = pattern.identifier
                let type = pattern.type

                // Skip non-barcode types
                if type != "text" {
                    if let match = barcode.range(of: regex, options: .regularExpression) {
                        let value = String(barcode[match])
                        
                        // Check for duplicate identifier
                        let isAlreadyPresent = results.contains { $0["identifier"] == identifier }
                        if !isAlreadyPresent {
                            results.append([
                                "identifier": identifier,
                                "type": type,
                                "value": value
                            ])
                
                        }
                    }
                }
            }
        }

        print("✅ barcode process done:")
        print(" Barcode output \(results) ")

        if let jsonData = try? JSONSerialization.data(withJSONObject: results, options: .prettyPrinted),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            print(" Barcode result \(jsonString) ")
            let OcrResults = processText(
                  ocrDataList: ocrDataList,
                  templateAttrList: templateAttrList,
                  barcodeResult: jsonString,
                  barcodes : barcodes
            )
            return OcrResults
        }
       return nil
    }
        
}
                          
