import Foundation

// MARK: - ScanTemplate Model
class ScanTemplate: Codable {
    
    enum STLabelType: Int, Codable {
        case text = 1
        case qrCode = 2
        case barcode = 3
    }

    enum STLValueRegexType: String, Codable {
        case none = ""
        case alphaNumeric = "alphanumeric-string"
        case macAddress = "mac-address"
        case serialNumber = "serial-number"
        case ipAddress = "ip-address"
        case imei = "imei"
        case anyValue = "any"

        // Support alternate formats manually if needed
        static func from(rawValue: String) -> STLValueRegexType {
            switch rawValue.lowercased() {
            case "alphanumeric-string": return .alphaNumeric
            case "mac-address", "xx xx xx xx xx xx", "xx:xx:xx:xx:xx:xx": return .macAddress
            case "serial-number": return .serialNumber
            case "ip-address", "xxx.xxx.xxx.xxx": return .ipAddress
            case "imei": return .imei
            case "any": return .anyValue
            default: return .none
            }
        }
    }

    class STLabel: Codable {
        var identifier: String = ""
        var type: STLabelType = .text
        var ordinal: Int = 0
        var prefix: String = ""
        var regexType: STLValueRegexType = .none
    }

    var name: String = ""
    var labels: [STLabel] = []
    var image: String = ""
}
