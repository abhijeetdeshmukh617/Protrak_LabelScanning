import SwiftUI
import Photos

struct ContentView: View {
    var templateJson: String
    var delayTime: Double
    var onScanCompleted: (String) -> Void
    @Environment(\.presentationMode) var presentationMode
    @StateObject private var cameraManager = CameraManager()
    @State private var capturedImages: [CapturedImage] = []
    @State private var hasStartedCapturing = false
    @State private var countdown = 5
    @State private var showCountdown = true
    @State private var countdownScale: CGFloat = 1.0

    let extractor = MlkitExtractor()

    var body: some View {
        ZStack {
            // ✅ Camera feed
            CameraPreview(session: cameraManager.session)
                .ignoresSafeArea()

            // ✅ Capture box
            Rectangle()
                .stroke(Color.white, lineWidth: 3)
                .frame(width: 300, height: 200)

            VStack {
                Spacer()

                // ✅ Animated countdown
                if capturedImages.count < 7 {
                    if showCountdown {
                    Text("Starting in \(countdown) seconds")
                        .font(.largeTitle)
                        .foregroundColor(.white)
                       // .scaleEffect(countdownScale)
                      //  .animation(.easeInOut(duration: 0.5), value: countdownScale)
                      /*  .onChange(of: countdown) { _ in
                            countdownScale = 1.3
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                                countdownScale = 1.0
                            }
                        }*/
                        .padding(.bottom, 20)
                }else{
                    Text("Scanning")
                        .font(.largeTitle)
                        .foregroundColor(.white)
                        .padding(.bottom, 20)
                }
                // ✅ Completion indicator
                  //  if capturedImages.count >= 7 {
                } else {    Text("Processing")
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
                    if let fileURL = saveImageToDisk(image) {
                        let captured = CapturedImage(image: image, fileURL: fileURL)
                        capturedImages.append(captured)
                    }

                    saveImageToPhotoLibrary(image)

                    if capturedImages.count < 7 {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                            cameraManager.captureImage()
                        }
                    } else {
                        extractor.process(template: templateJson, data: capturedImages) { resultJson in
                            print("📦 Result from content view: \(resultJson ?? "{}")")
                            onScanCompleted(resultJson ?? "{}")
                             presentationMode.wrappedValue.dismiss()
                        }
                    }
                }
            }

        }
        .onDisappear {
            cameraManager.stopSession()
        }
    }

    private func startCountdown() {
        countdown = 5
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

    private func saveImageToPhotoLibrary(_ image: UIImage) {
        PHPhotoLibrary.shared().performChanges({
            PHAssetChangeRequest.creationRequestForAsset(from: image)
        }) { success, error in
            if let error = error {
                print("❌ Error saving image: \(error)")
            } else {
                print("✅ Image saved.")
            }
        }
    }
}
