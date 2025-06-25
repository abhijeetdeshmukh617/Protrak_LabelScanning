class BarcodeStore {
    static let shared = BarcodeStore()
    private init() {}

    var barcodeResults: [[String]] = []
}
