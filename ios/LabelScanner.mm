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
                  caputureCount:(double)caputureCount
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  [ScanLauncher launchScanWithTemplateJson:templateJson
                                 delayTime:delayTime
                             caputureCount:caputureCount
                                   resolve:resolve
                                    reject:reject];
}

RCT_EXPORT_METHOD(scanQRBarcode:(double)caputureCount
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  [ScanLauncher launchScanforBarCodeWithCaputureCount:caputureCount
                                              resolve:resolve
                                               reject:reject];
}

@end
