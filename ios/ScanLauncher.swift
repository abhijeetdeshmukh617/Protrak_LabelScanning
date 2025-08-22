import SwiftUI
import React

@objc public class ScanLauncher: NSObject {
    @objc(launchScanWithTemplateJson:delayTime:caputureCount:resolve:reject:)
    public static func launchScan(templateJson: String,
                                  delayTime: Double,
                                  caputureCount: Double,
                                  resolve: @escaping RCTPromiseResolveBlock,
                                  reject: @escaping RCTPromiseRejectBlock) {
        DispatchQueue.main.async {
            let rootVC = UIApplication.shared.delegate?.window??.rootViewController
            let hostingController = UIHostingController(rootView: ContentView(templateJson: templateJson,
                                                                             delayTime: delayTime,
                                                                             caputureCount: caputureCount,
                                                                             onScanCompleted: { resultJson in
                resolve(resultJson)
            }))
            rootVC?.present(hostingController, animated: true, completion: nil)
        }
    }

    @objc(launchScanforBarCodeWithCaputureCount:resolve:reject:)
    public static func launchScanforBarCode(caputureCount: Double,
                                            resolve: @escaping RCTPromiseResolveBlock,
                                            reject: @escaping RCTPromiseRejectBlock) {
                                                        DispatchQueue.main.async {
            let rootVC = UIApplication.shared.delegate?.window??.rootViewController
            let hostingController = UIHostingController(rootView: ContentView(templateJson: "",
                                                                             delayTime: 0,
                                                                             caputureCount: caputureCount,
                                                                             onScanCompleted: { resultJson in
                resolve(resultJson)
            }))
            rootVC?.present(hostingController, animated: true, completion: nil)
        }
    }
}
