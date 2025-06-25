package com.deviceonboarder.labelScan.model

import android.app.Activity

enum class ScanResponseCode(val code: Int) {
    SUCCESS(0),
    USER_CANCELLED(1),
    FAILURE(2),
    PERMISSION_DENIED(3);

    companion object {
        fun fromActivityReturnCode(activityResult : Int) : ScanResponseCode {
            var code : ScanResponseCode = FAILURE
            when(activityResult) {
                Activity.RESULT_CANCELED -> code = USER_CANCELLED
                Activity.RESULT_OK -> code = SUCCESS
                Activity.CONTEXT_RESTRICTED -> code = PERMISSION_DENIED
                else -> code = FAILURE
            }
            return code
        }
    }
}