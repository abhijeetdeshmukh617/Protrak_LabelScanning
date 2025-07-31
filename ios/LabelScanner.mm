#import "LabelScanner.h"
#import "LabelScanner-Bridging-Header.h"
#import <React/RCTLog.h>
#import <LabelScanner/LabelScanner-Swift.h>

@implementation LabelScanner

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(multiply:(double)a
                  b:(double)b
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  double result = a * b;
  resolve(@(result));
}

RCT_EXPORT_METHOD(startScan:(NSString *)templateJson
                  delayTime:(double)delayTime
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  NSLog(@"📥 Received template: %@", templateJson);

  [ScanLauncher launchScanWithTemplateJson:templateJson
                                 delayTime:delayTime
                                   resolve:resolve
                                    reject:reject];
}

RCT_EXPORT_METHOD(scanQRBarcode:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  NSLog(@"📥 Starting QR/Barcode scan");

  [ScanLauncher launchScanforBarCodeWithResolve:resolve
                                         reject:reject];
}

@end




/*#import <AVFoundation/AVFoundation.h> 
#import "LabelScanner.h"
#import "LabelScanner-Bridging-Header.h"
#import <LabelScannerSpec/LabelScannerSpec.h>
#import <LabelScanner/LabelScanner-Swift.h>



@implementation LabelScanner
RCT_EXPORT_MODULE()

- (NSNumber *)multiply:(double)a b:(double)b {
    NSNumber *result = @(a * b);

    return result;
}


- (void)startScan:(NSString *)templateJson
        delayTime:(double)delayTime
          resolve:(RCTPromiseResolveBlock)resolve
           reject:(RCTPromiseRejectBlock)reject
{
    NSLog(@"📥 Received template: %@", templateJson);

   [ScanLauncher launchScanWithTemplateJson:templateJson
                                   delayTime:delayTime
                                     resolve:resolve
                                      reject:reject];
}


- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeLabelScannerSpecJSI>(params);
}

@end*/
