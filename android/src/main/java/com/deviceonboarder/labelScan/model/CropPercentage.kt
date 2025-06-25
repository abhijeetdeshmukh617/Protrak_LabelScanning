package com.deviceonboarder.labelScan.model

import android.content.Context
import android.content.res.Configuration

/** CropPercentage
 * Contains the heightCropPercent,widthCropPercent for the camera block to set the scan area within the camera frame
 **/
class CropPercentage {

    var heightCropPercent: Int = 72
        set(value) {
            field = value
        }
        get() {
            return field
        }

    var widthCropPercent: Int = 8
        set(value) {
            field = value
        }
        get() {
            return field
        }
}