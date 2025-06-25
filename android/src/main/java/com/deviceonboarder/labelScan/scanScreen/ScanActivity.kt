package com.deviceonboarder.labelScan.scanScreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.viewModels
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
               // val TemplateJsonString:String = intent.getStringExtra(Constants.INTENT_TEMPLATE_JSON_STRING).toString()
              //  val scanTimer:Int = intent.getIntExtra(Constants.INTENT_SCAN_TIMER,5)
              //  val imagePath:String? =  intent.getStringExtra(Constants.INTENT_KEY_IMAGE_PATH)
                val imageCacheDir = getImageCacheDir(this)
                val file = File(imageCacheDir, "my_image.png")
                val TemplateJsonString = intent.getStringExtra(Constants.INTENT_TEMPLATE_JSON_STRING)
                val scanTimer = 5  //intent.getIntExtra(Constants.INTENT_SCAN_TIMER,5)
              //  val imagePath = intent.getStringExtra("imagePath")
                Log.d("startScan","from library TemplateJsonString "+TemplateJsonString)
                Log.d("startScan","from library  scanTimer "+scanTimer)
               // Log.d("newInstance","from library  imagePath "+imagePath)

                //val TemplateJsonString:String = loadJsonFromAsset(this,"template.json")
               // val scanTimer = 6
                val imagePath:String? =  file.absolutePath
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ScanFragment.newInstance(TemplateJsonString.toString(),scanTimer,imagePath))
                    .commitNow()
            }


            /*
            val TemplateJsonString: String = ""//readJsonFromAssets(this)
            val scanTimer: Int = 5
            val imagePath: String? = ""//getImageCacheFile(this, "scan.png")
            Log.d("Log from library", "TemplateJsonString : " + TemplateJsonString)
            Log.d("Log from library", "scanTimer : " + scanTimer)
            Log.d("Log from library", "imagePath : " + imagePath)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ScanFragment.newInstance(TemplateJsonString,scanTimer,imagePath))
                .commitNow()*/


        /*    val resultIntent = Intent().apply {
                putExtra("result_json", viewModel.scanResultJson.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()*/
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
/*
    companion object {
        fun start(context: Context, data: String) {
            Log.d("Log from library", "data : " + data)
        }

}
        fun readJsonFromAssets(context: Context): String {
            try {
                val inputStream = context.assets.open("template.json")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                val obj = JSONObject(jsonString)
                val templateObj = obj.getJSONObject("template")
                val labels = templateObj.getJSONArray("label")
                return labels.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }

        fun getImageCacheFile(context: Context, filename: String): String {
            val dir = File(context.cacheDir, "image_cache")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            return file.absolutePath
        }
*/
}
