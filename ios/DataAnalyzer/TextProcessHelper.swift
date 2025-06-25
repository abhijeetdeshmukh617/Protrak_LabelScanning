import Foundation

struct TextProcessHelper {
    
    /// Tries to extract a matching value using regex
    static func matchValue(_ valuePart: String, withRegex pattern: String) -> String? {
        guard let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) else { return nil }
        let range = NSRange(location: 0, length: valuePart.utf16.count)
        if let match = regex.firstMatch(in: String(valuePart), options: [], range: range) {
            if let matchRange = Range(match.range, in: valuePart) {
                return String(valuePart[matchRange])
            }
        }
        return nil
    }

    /// Updates the result map only if the new confidence is higher
    static func updateIfBetterMatch(attr: Template, value: String, confidence: Float, into map: inout [String: TemplateMatchResult]) {
        let existing = map[attr.identifier]
        if existing == nil || confidence > existing!.confidence {
            map[attr.identifier] = TemplateMatchResult(
                identifier: attr.identifier,
                type: attr.type,
                value: value,
                confidence: confidence
            )
        }
    }
    
    static func updateBarcodeResults(identifier: String, value: String, type: String, into map: inout [String: TemplateMatchResult]) {
            map[identifier] = TemplateMatchResult(
                identifier: identifier,
                type: type,
                value: value,
                confidence: 1.0
            )
    }
}
