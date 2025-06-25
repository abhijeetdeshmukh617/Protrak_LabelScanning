class TextStore {
    static let shared = TextStore()
    private init() {}

    private(set) var ocrResults: [[String]] = []

    func store(results: [[String]]) {
        self.ocrResults = results
    }
}
