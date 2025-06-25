import UIKit

class ImageStore {
    static let shared = ImageStore()
    private init() {}

    private(set) var images: [UIImage] = []

    func loadImages(named imageNames: [String]) {
        images.removeAll()
        for name in imageNames {
            if let img = UIImage(named: name) {
                images.append(img)
            } else {
                print("❌ Could not load image named \(name)")
            }
        }
    }
}
