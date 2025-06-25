import SwiftUI
import Photos

func saveImageToDisk(_ image: UIImage) -> URL? {
    guard let data = image.jpegData(compressionQuality: 1.0) else {
        print("❌ Failed to convert image to JPEG data")
        return nil
    }
    
    let filename = UUID().uuidString + ".jpg"
    let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    let fileURL = documentsURL.appendingPathComponent(filename)
    
    do {
        try data.write(to: fileURL)
        print("✅ Image saved to disk at: \(fileURL)")
        return fileURL
    } catch {
        print("❌ Error saving image to disk: \(error)")
        return nil
    }
}

func deleteImage(at url: URL) {
    print("🗑️ isFileURL: \(url.isFileURL)")
    print("Exists before deletion: \(FileManager.default.fileExists(atPath: url.path))")
 //   if FileManager.default.fileExists(atPath: url.absoluteString) {
        do {
            try FileManager.default.removeItem(at: url)
            print("🗑️ Deleted image at path: \(url.absoluteString)")
        } catch {
            print("❌ Failed to delete image at path \(url.absoluteString): \(error)")
        }
    /*} else {
        print("⚠️ File does not exist at path: \(url.absoluteString)")
    }*/
}


func encodeOcrImageDataList(_ list: [OcrImageData]) -> String {
    let encoder = JSONEncoder()
    encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
    do {
        let data = try encoder.encode(list)
        return String(data: data, encoding: .utf8) ?? "[]"
    } catch {
        print("Failed to encode OcrImageData list: \(error)")
        return "[]"
    }
}
