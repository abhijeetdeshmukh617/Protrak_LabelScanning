package com.deviceonboarder

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import com.deviceonboarder.labelScan.scanScreen.ScanActivity;
import com.deviceonboarder.labelScan.model.ScanPromiseResponse;
import com.deviceonboarder.labelScan.model.ScanResponseCode;
import com.deviceonboarder.labelScan.util.Constants;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = LabelScannerModule.NAME)
class LabelScannerModule(reactContext: ReactApplicationContext) :
  NativeLabelScannerSpec(reactContext),ActivityEventListener {
private var mPromise: Promise? = null
  override fun getName(): String {
    return NAME
  }

private var promise: Promise? = null


 init {
        reactContext.addActivityEventListener(this)
    }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }
/*
override fun startScan(templateJson: String, delayTime: Double): String {
  Log.d("Template json","Json : "+templateJson)
 val intent = Intent(reactApplicationContext, ScanActivity::class.java).apply {
    putExtra(Constants.INTENT_TEMPLATE_JSON_STRING, templateJson)
    putExtra(Constants.INTENT_SCAN_TIMER, delayTime)
   // putExtra(Constants.INTENT_KEY_IMAGE_PATH, "")
    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
}
    return "result"
  }  */



override fun startScan(templateJson: String, delayTime: Double, promise: Promise) {
    mPromise = promise
 Log.d("startScan","starting: ")
    val intent = Intent(reactApplicationContext, ScanActivity::class.java).apply {
        putExtra(Constants.INTENT_TEMPLATE_JSON_STRING, templateJson)
        putExtra(Constants.INTENT_SCAN_TIMER, delayTime)
        putExtra(Constants.INTENT_KEY_IMAGE_PATH, "")
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
Log.d("startScan","starting: ")
    val activity = currentActivity
    Log.d("activity","activity: "+activity)
    if (activity != null) {
        activity.startActivityForResult(intent, Constants.REQUEST_CODE)
        //finish()
    } else {
      Log.d("startScan","starting: ")
        promise.reject("NO_ACTIVITY", "No current activity found")
        mPromise = null
    }
}



override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("startScan", "scanComplete onActivityResult called: requestCode=$requestCode resultCode=$resultCode")
Log.d("startScan","requestCode: "+requestCode)
        if (requestCode == Constants.REQUEST_CODE) {
            Log.d("startScan","result data: "+data)
            val result = data?.getStringExtra(Constants.SCAN_RESULT)
            Log.d("startScan","result: "+result)
            if (resultCode == Activity.RESULT_OK && result != null) {
                mPromise?.resolve(result)
            } else {
                mPromise?.reject("SCAN_FAILED", "Scan failed or was canceled")
            }
            mPromise = null
        }
    }

    override fun onNewIntent(intent: Intent?) {
        // No-op unless you use deep linking or similar
    }

/* @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d("scanComplete",
                String.format("LabelScanModule received onActivityResult. requestCode=%d, resultCode=%d, data=%s",
                        requestCode, resultCode, data));
        if (requestCode == Constants.REQUEST_CODE) {
            ScanResponseCode code = ScanResponseCode.Companion.fromActivityReturnCode(resultCode);
            String scanResultJson = null;
            if (null != data) {
                scanResultJson = data.getStringExtra(Constants.SCAN_RESULT);
            }
            ScanPromiseResponse response = new ScanPromiseResponse(code, scanResultJson);
            WritableMap responseToReturn = response.getResponse();
            if (null != responseToReturn) {
                mPromise.resolve(responseToReturn);
            } else {
                /* Failure */
                String errMsg = String.format("Scan failed. Activity resultCode=%d", resultCode);
                Log.d("scanComplete", errMsg);
                mPromise.reject(new Error(errMsg));
            }
        }
    }

*/
  companion object {
    const val NAME = "LabelScanner"
  }
}
