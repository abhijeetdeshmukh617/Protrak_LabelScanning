struct TemplateMatchResult : Codable{
    var identifier: String
    var type: String
    var value: String
    var confidence: Float
}


struct ResultModel : Codable{
    var identifier: String
    var type: String
    var value: String
}
