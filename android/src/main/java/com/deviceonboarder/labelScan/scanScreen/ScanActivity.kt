package com.deviceonboarder.labelScan.scanScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.deviceonboarder.R
import com.deviceonboarder.labelScan.util.Constants
import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.deviceonboarder.labelScan.util.SmoothedMutableLiveData
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File


class ScanActivity : AppCompatActivity() {

    private val viewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_scan_screen)
        if (savedInstanceState == null) {
            if (savedInstanceState == null) {
                val imageCacheDir = getImageCacheDir(this)
                val file = File(imageCacheDir, "my_image.png")
                val scanType = intent.getStringExtra(Constants.SCAN_TYPE)
                val caputureCount = intent.getDoubleExtra(Constants.caputureCount,1.0).toInt()
                val TemplateJsonString = intent.getStringExtra(Constants.INTENT_TEMPLATE_JSON_STRING)
                val scanTimer = intent.getDoubleExtra(Constants.INTENT_SCAN_TIMER,0.0).toInt()
                val imagePath:String? =  file.absolutePath
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ScanFragment.newInstance(TemplateJsonString.toString(),scanTimer,imagePath,scanType, caputureCount))
                    .commitNow()
            }
        }
    }

    fun loadJsonFromAsset(context: Context,filename: String): String {
        val inputStream = context.assets.open(filename)
        val reader = BufferedReader(inputStream.reader())
        val jsonString = reader.readText()

        val root = JSONObject(jsonString)
        val templateObj = root.getJSONObject("template")
        val labels = templateObj.getJSONArray("label")
        return labels.toString()
    }

    fun getImageCacheDir(context: Context): File {
        val imageCacheDir = File(context.cacheDir, "image_cache")
        if (!imageCacheDir.exists()) {
            imageCacheDir.mkdirs()
        }
        return imageCacheDir
    }
}
