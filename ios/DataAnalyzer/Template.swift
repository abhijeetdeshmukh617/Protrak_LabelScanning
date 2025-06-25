import Foundation


struct TemplateWrapper: Codable {
    let header: Header
    let template: TemplateSection
}

struct Header: Codable {
    let Version: String
    let Name: String
}

struct TemplateSection: Codable {
    let name: String
    let label: [Template]
}

class Template: Codable {
    var identifier: String = ""
    var type: String = ""
   // var ordinal: String = ""
    var prefix: String = ""
    var regex: String = ""
    var values: [Template]? = nil
}
