import Foundation
import MLKitBarcodeScanning
import MLKitVision

class ScanDataManager {
    private var qrCodeList: [Barcode] = []
    private var barCodeList: [Barcode] = []
    private let scanDataProcessor = ScanDataProcessor()
    
    func processScans(
        template: String,
        ocrDataList: [OcrImageData],
        barCodeDataList: [String]
    ) -> String? {
         
        let processor = ScanDataProcessor()
        var templateAttrList: [Template] = []
      if let attributes = loadTemplate(template: template) {
            templateAttrList = attributes
            print("✅ Loaded \(templateAttrList.count) template attributes.")
        } else {
            print("❌ Failed to load template attributes.")
        }
        
        let results = processor.processBarcode(
            template: template,
            barcodes: barCodeDataList,
            templateAttrList: templateAttrList,
            ocrDataList: ocrDataList)
        
        return results
        
    }
    struct TemplateWrapper: Codable {
        let label: [Template]
    }

    struct TemplateSection: Codable {
        let name: String
        let label: [Template]
    }

    func loadTemplate(template: String) -> [Template]? {
      guard let jsonData = template.data(using: .utf8) else {
          print("❌ Failed to convert template string to Data")
        return nil
      }
        do {
            let decoder = JSONDecoder()
             let templates = try decoder.decode([Template].self, from: jsonData)
           // let wrapper = try decoder.decode(TemplateWrapper.self, from: data)
          //  let labels = wrapper.label
            print("templates : \(templates)")
            return templates
        } catch {
            print("❌ Failed to decode JSON: \(error)")
            return nil
        }
    }

    func addScanQrCode(_ barcode: Barcode) {
        qrCodeList.append(barcode)
    }

    func addScanBarCode(_ barcode: Barcode) {
        barCodeList.append(barcode)
    }

    func getScanQrCode() -> [Barcode] {
        return qrCodeList
    }

    func getScanBarCode() -> [Barcode] {
        return barCodeList
    }
}
