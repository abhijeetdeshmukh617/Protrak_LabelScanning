#import <AVFoundation/AVFoundation.h> 
#import "LabelScanner.h"
#import "LabelScanner-Bridging-Header.h"
#import <LabelScannerSpec/LabelScannerSpec.h>
#import "LabelScanner-Swift.h"


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

@end
