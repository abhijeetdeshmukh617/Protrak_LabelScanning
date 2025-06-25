package com.deviceonboarder.labelScan.model

import com.google.gson.annotations.SerializedName

class ScanTemplate {

    enum class STLabelType(val labelType: Int) {
        TEXT(1),
        QRCODE(2),
        BARCODE(3)
    }

    enum class STLValueRegexType(val regexType: String) {
        NONE(""),
        @SerializedName("alphanumeric-string")
        ALPHA_NUMERIC("alphanumeric-string"),
        @SerializedName("mac-address", alternate = ["XX XX XX XX XX XX", "XX:XX:XX:XX:XX:XX"])
        MAC_ADDRESS("mac-address"),
        @SerializedName("serial-number")
        SERIAL_NUMBER("serial-number"),
        @SerializedName("ip-address", alternate = ["XXX.XXX.XXX.XXX"])
        IP_ADDRESS("ip-address"),
        @SerializedName("imei")
        IMEI("imei"),
        @SerializedName("any")
        ANY_VALUE("any"),
    }

    class STLabel {
        lateinit var identifier: String
        lateinit var type: STLabelType
        var ordinal: Int = 0
        lateinit var prefix: String
        lateinit var regexType: STLValueRegexType
    }

    lateinit var name: String
    var labels: ArrayList<STLabel> = arrayListOf()
    lateinit var image: String
}
