require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "LabelScanner"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://www.protrak.ai/"
  s.license      = { :type => "MIT", :file => "FILE_LICENSE"}
  s.authors      = { "Your Name" => "yourname@email.com"}

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/abhijeetdeshmukh617/Protrak_LabelScanning.git", :tag => "#{s.version}" }

  s.module_name = 'LabelScanner'
  s.requires_arc = true
  s.static_framework = true
  s.source_files = "ios/**/*.{h,m,mm,cpp,swift}"
  s.private_header_files = "ios/**/*.h"

  s.dependency 'GoogleMLKit/BarcodeScanning'
  s.dependency 'GoogleMLKit/TextRecognition'
  s.dependency 'GoogleMLKit/Vision'
  
 install_modules_dependencies(s)
end
