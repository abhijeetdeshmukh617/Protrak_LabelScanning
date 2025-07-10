#import "LabelScanner.h"
#import "LabelScanner-Bridging-Header.h"
#import <React/RCTLog.h>
#import <LabelScanner-Swift.h>

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


RCT_EXPORT_METHOD(scanQrBarCode:(double)a
                  b:(double)b
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  double result = a + b;
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

RCT_EXPORT_METHOD(scanBarcodeQRcode:(double)delayTime
                   resolver:(RCTPromiseResolveBlock)resolve 
                   rejecter:(RCTPromiseRejectBlock)reject)
{
  NSLog(@"📥 Received delay time: %f", delayTime);

  [ScanLauncher launchScanforBarCodeWithdelayTime:delayTime
                                          resolve:resolve
                                           reject:reject];
}


@end
