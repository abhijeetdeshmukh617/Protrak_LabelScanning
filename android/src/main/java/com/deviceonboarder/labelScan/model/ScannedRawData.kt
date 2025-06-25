package com.deviceonboarder.labelScan.model

import com.google.mlkit.vision.text.Text

data class ScannedRawData(
    var Text: Text,
    var imagePath: String,
    var numOfMatches: Int
)