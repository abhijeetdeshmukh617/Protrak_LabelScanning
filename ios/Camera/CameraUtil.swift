import SwiftUI
import Photos

func cropImageToCaptureBox(_ image: UIImage, in previewSize: CGSize) -> UIImage? {
    // Define the capture box size in preview coordinates
    let captureBoxSize = CGSize(width: 300, height: 200)

    // Calculate the origin to center the capture box in preview
    let originX = (previewSize.width - captureBoxSize.width) / 2
    let originY = (previewSize.height - captureBoxSize.height) / 2
    let captureRect = CGRect(origin: CGPoint(x: originX, y: originY), size: captureBoxSize)

    // Image and preview coordinate systems might differ.
    // Assuming previewSize matches your preview view's size.
    
    // Calculate scale between image and preview
    let scaleX = image.size.width / previewSize.width
    let scaleY = image.size.height / previewSize.height

    // Calculate cropping rectangle in image coordinates
    let cropRect = CGRect(
        x: captureRect.origin.x * scaleX,
        y: captureRect.origin.y * scaleY,
        width: captureRect.size.width * scaleX,
        height: captureRect.size.height * scaleY
    ).integral

    // Crop the CGImage
    guard let cgImage = image.cgImage?.cropping(to: cropRect) else { return nil }

    return UIImage(cgImage: cgImage, scale: image.scale, orientation: image.imageOrientation)
}
