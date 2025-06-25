import UIKit

class ScanImageUtil {
    
    /// Save a UIImage as PNG to the specified file path.
    static func saveImage(_ image: UIImage, to path: String) {
        guard let data = image.pngData() else {
            print("Failed to get PNG data from image.")
            return
        }
        
        let fileURL = URL(fileURLWithPath: path)
        
        do {
            try data.write(to: fileURL)
            print("Image saved to \(path)")
        } catch {
            print("Error saving image: \(error)")
        }
    }

    /// Delete a file at the specified path if it exists.
    static func deleteImage(at path: String) {
        let fileManager = FileManager.default
        let fileURL = URL(fileURLWithPath: path)

        if fileManager.fileExists(atPath: fileURL.path) {
            do {
                try fileManager.removeItem(at: fileURL)
                print("Deleted image at \(path)")
            } catch {
                print("Error deleting file: \(error)")
            }
        }
    }
}
