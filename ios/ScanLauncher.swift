import SwiftUI;
import React

@objc public class ScanLauncher: NSObject {
  @objc(launchScanWithTemplateJson:delayTime:resolve:reject:)
  public static func launchScan(templateJson: String,
                                 delayTime: Double,
                                 resolve: @escaping RCTPromiseResolveBlock,
                                 reject: @escaping RCTPromiseRejectBlock) {

        print("🚀 launchScan() called")
        print("📥 templateJson: \(templateJson)")
        print("⏱️ delayTime: \(delayTime)")

        DispatchQueue.main.async {
            let rootVC = UIApplication.shared.delegate?.window??.rootViewController
            let hostingController = UIHostingController(rootView: ContentView(templateJson: templateJson,
                    delayTime: delayTime,onScanCompleted: { resultJson in
              resolve(resultJson)
            }))
            rootVC?.present(hostingController, animated: true, completion: nil)
        }
    }
}
