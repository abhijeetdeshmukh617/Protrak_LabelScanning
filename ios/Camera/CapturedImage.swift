import SwiftUI
import Photos

struct CapturedImage {
    let image: UIImage
    let fileURL: URL
}

struct OcrImageData : Codable{
    let ocrData: OcrData
    let imagePath: URL
}
