package com.deviceonboarder.labelScan.model

import com.deviceonboarder.labelScan.util.Constants
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

class ScanPromiseResponse {
    lateinit var responseCode : ScanResponseCode
    var responseJSON : String?

    constructor(responseCode: ScanResponseCode, responseJSON: String?) {
        this.responseCode = responseCode
        this.responseJSON = responseJSON
    }

    fun getResponse() : WritableMap? {
        when(responseCode) {
            ScanResponseCode.FAILURE -> return null
            else -> {
                val map = Arguments.createMap()
                map.putInt(Constants.RESPONSE_CODE, responseCode.code)
                responseJSON?.let {
                    map.putString(Constants.RESPONSE_JSON, it)
                }
                return map
            }
        }
    }
}