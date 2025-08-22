import SwiftUI
import Photos

struct ContentView: View {
    var templateJson: String
    var delayTime: Double
    var caputureCount: Double
    var onScanCompleted: (String) -> Void
    
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var cameraManager = CameraManager()
    @State private var capturedImages: [CapturedImage] = []
    @State private var showCountdown = true
    @State private var countdown: Double = 0.0
    @State private var showPermissionAlert = false // ✅ Alert state
    
    private let overlayWidth: CGFloat = 300
    private let overlayHeight: CGFloat = 200
    
    private var captureLimit: Int {
        return Int(caputureCount)
    }
    
    let extractor = MlkitExtractor()
    
    var body: some View {
        ZStack {
            CameraPreview(session: cameraManager.session)
                .edgesIgnoringSafeArea(.all)
            
            Color.black.opacity(0.5)
                .mask(
                    Rectangle()
                        .fill(style: FillStyle(eoFill: true))
                        .overlay(
                            Rectangle()
                                .frame(width: overlayWidth, height: overlayHeight)
                                .blendMode(.destinationOut)
                        )
                )
                .compositingGroup()
            
            Rectangle()
                .stroke(Color.white, lineWidth: 3)
                .frame(width: overlayWidth, height: overlayHeight)
            
            VStack {
                Spacer()
                if capturedImages.count < captureLimit {
                    if showCountdown {
                        Text("Starting in \(Int(countdown)) seconds")
                            .font(.largeTitle)
                            .foregroundColor(.white)
                            .padding(.bottom, 20)
                    } else {
                        Text("Scanning")
                            .font(.largeTitle)
                            .foregroundColor(.white)
                            .padding(.bottom, 20)
                    }
                } else {
                    Text("Processing")
                        .font(.largeTitle)
                        .foregroundColor(.white)
                        .padding(.bottom, 20)
                }
            }
        }
        .onAppear {
            cameraManager.startSession()
            startCountdown()
            
            cameraManager.onImageCaptured = { image in
                DispatchQueue.main.async {
                    if let cropped = cropToOverlay(image: image) {
                        saveImageToPhotoLibrary(cropped) // ✅ Save
                        
                        // ❌ FIX: don’t pass nil, just skip fileURL or use optional
                        let captured = CapturedImage(
                            image: cropped,
                            fileURL: URL(fileURLWithPath: "")
                        )
                        capturedImages.append(captured)
                    }
                    
                    if capturedImages.count < captureLimit {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                            cameraManager.captureImage()
                        }
                    } else {
                        if templateJson.isEmpty {
                            extractor.extractQrBarCodes(data: capturedImages) { resultJson in
                                onScanCompleted(resultJson ?? "{}")
                                presentationMode.wrappedValue.dismiss()
                            }
                        } else {
                            extractor.process(template: templateJson, data: capturedImages) { resultJson in
                                onScanCompleted(resultJson ?? "{}")
                                presentationMode.wrappedValue.dismiss()
                            }
                        }
                    }
                }
            }
        }
        .onDisappear {
            cameraManager.stopSession()
        }
        // ✅ Alert for denied permission
        .alert(isPresented: $showPermissionAlert) {
            Alert(
                title: Text("Permission Needed"),
                message: Text("We need access to your Photo Library to save scanned images."),
                primaryButton: .default(Text("Open Settings")) {
                    if let appSettings = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(appSettings)
                    }
                },
                secondaryButton: .cancel()
            )
        }
    }
    
    // MARK: - Helpers
    
    private func startCountdown() {
        countdown = delayTime
        showCountdown = true
        
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { timer in
            if countdown > 1 {
                countdown -= 1
            } else {
                timer.invalidate()
                showCountdown = false
                startCapturingImages()
            }
        }
    }
    
    private func startCapturingImages() {
        capturedImages.removeAll()
        cameraManager.captureImage()
    }
    
    private func cropToOverlay(image: UIImage) -> UIImage? {
        guard let cgImage = image.cgImage else { return nil }
        
        // Camera image size
        let imageSize = CGSize(width: cgImage.width, height: cgImage.height)
        
        // Screen bounds
        let screenSize = UIScreen.main.bounds.size
        
        // Aspect fill scaling (like AVCaptureVideoPreviewLayer.videoGravity = .resizeAspectFill)
        let scale = max(imageSize.width / screenSize.width, imageSize.height / screenSize.height)
        
        // Effective size of the camera content that’s mapped onto the screen
        let scaledSize = CGSize(
            width: screenSize.width * scale,
            height: screenSize.height * scale
        )
        
        // Offset because image is center-cropped when aspect filled
        let xOffset = (scaledSize.width - imageSize.width) / 2
        let yOffset = (scaledSize.height - imageSize.height) / 2
        
        // Overlay rect (in screen coordinates)
        let overlayRect = CGRect(
            x: (screenSize.width - overlayWidth) / 2,
            y: (screenSize.height - overlayHeight) / 2,
            width: overlayWidth,
            height: overlayHeight
        )
        
        // Convert overlayRect → image coordinates
        let cropRect = CGRect(
            x: overlayRect.origin.x * scale - xOffset,
            y: overlayRect.origin.y * scale - yOffset,
            width: overlayRect.width * scale,
            height: overlayRect.height * scale
        ).integral
        
        // Crop
        if let croppedCGImage = cgImage.cropping(to: cropRect) {
            return UIImage(
                cgImage: croppedCGImage,
                scale: image.scale,
                orientation: image.imageOrientation
            )
        }
        return nil
    }
    
    /// ✅ Save cropped image to **Photos App (Gallery)** with proper permission handling
    private func saveImageToPhotoLibrary(_ image: UIImage) {
        PHPhotoLibrary.requestAuthorization { status in
            if status == .authorized {
                UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil)
                print("✅ Saved to Photos")
            } else if #available(iOS 14, *), status == .limited {
                UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil)
                print("✅ Saved (limited access)")
            } else {
                print("❌ No permission for Photo Library")
                DispatchQueue.main.async {
                    showPermissionAlert = true // ✅ show alert if denied
                }
            }
        }
    }
}
