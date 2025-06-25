package com.deviceonboarder.labelScan.util

class Constants {

    companion object {
        const val RESPONSE_CODE = "responseCode"
        const val RESPONSE_JSON = "responseJson"
        const val REQUEST_CODE = 1880
        const val SCAN_RESULT = "ScanResultModel"
        const val TIMER_INSTANCE = "timer_instance"
        const val IS_SCANNING = "isScanning"
        const val OCR_THRESHOLDCOUNT = 7
        const val INTENT_TEMPLATE_JSON_STRING = "TEMPLATE_JSON_STRING"
        const val INTENT_SCAN_TIMER = "SCAN_TIMER"
        const val TAG_PROCESS_SCAN_ANALYSIS = "startScan / ProcessScanAnalysis"
        const val TAG_PROCESS_CODE = "startScan / ProcessCode"
        const val TAG_PROCESS_TEXT = "startScan / ProcessText"
        const val TAG_PROCESS_RESULT = "startScan / ProcessResult"
        const val TAG_CAMERA = "startScan / Camera"
        const val TAG_IMAGE_ANALYZER = "MlkitImageAnalyzer"
        const val LANDSCAPE_SCREEN_WIDTH = 640
        const val LANDSCAPE_SCREEN_HEIGHT = 480
        const val PORTRAIT_SCREEN_WIDTH = 480
        const val PORTRAIT_SCREEN_HEIGHT = 640
        const val LANDSCAPE_HEIGHT_CROP_PERCENT = 30
        const val LANDSCAPE_WIDTH_CROP_PERCENT = 25
        const val PORTRAIT_HEIGHT_CROP_PERCENT = 72
        const val PORTRAIT_WIDTH_CROP_PERCENT = 8
        const val INTENT_KEY_IMAGE_PATH = "IMAGE_PATH"
    }
}