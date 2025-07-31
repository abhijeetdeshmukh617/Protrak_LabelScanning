require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "LabelScanner"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://www.protrak.ai/"
  s.license      = { :type => "MIT" }
  s.authors      = { "Your Name" => "yourname@email.com" }

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/abhijeetdeshmukh617/Protrak_LabelScanning.git", :tag => "#{s.version}" }

  s.module_name = 'LabelScanner'
  s.requires_arc = true
  s.static_framework = true

  s.source_files = "ios/**/*.{h,m,mm,cpp,swift}"
  s.private_header_files = "ios/**/*.h"
  s.public_header_files = 'ios/**/*.{h}'
  s.header_mappings_dir = 'ios'
  s.preserve_paths = 'ios/build/generated/**/*'

  s.dependency 'GoogleMLKit/BarcodeScanning'
  s.dependency 'GoogleMLKit/TextRecognition'
  s.dependency 'GoogleMLKit/Vision'
  s.dependency "React-Core"
  s.dependency "React-Codegen"
  s.dependency "RCT-Folly"
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core"

  # ✅ Add explicit Swift version
  s.swift_version = '5.0'

  # ✅ Use pod_target_xcconfig to define module and header paths
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_OBJC_BRIDGING_HEADER' => '',
    'SWIFT_OBJC_INTERFACE_HEADER_NAME' => 'LabelScanner-Swift.h',
    'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../node_modules/@prorigo/deviceonboarder/ios/build/generated/ios"'
  } 

  install_modules_dependencies(s)
end
