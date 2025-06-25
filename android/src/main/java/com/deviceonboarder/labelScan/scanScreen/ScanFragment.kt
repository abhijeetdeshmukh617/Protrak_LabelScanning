package com.deviceonboarder.labelScan.scanScreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy.FALLBACK_RULE_AUTO
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.deviceonboarder.R
import com.deviceonboarder.labelScan.analyzer.MlkitImageAnalyzer
import com.deviceonboarder.labelScan.model.CropPercentage
import com.deviceonboarder.labelScan.util.Constants
import com.deviceonboarder.labelScan.util.ScopedExecutor
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import android.provider.Settings
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File


class ScanFragment : Fragment() {

    companion object {
        const val imagePath = ""

        fun newInstance(ARG1: String, ARG2: Int, ARG3: String?): ScanFragment {
            Log.d("newInstance","newInstance ARG1 "+ARG1)
            Log.d("newInstance","newInstance ARG2 "+ARG2)
            Log.d("newInstance","newInstance ARG3 "+ARG3)
            val fragment = ScanFragment()
            val bundle = Bundle().apply {
                putString(Constants.INTENT_TEMPLATE_JSON_STRING, ARG1)
                putInt(Constants.INTENT_SCAN_TIMER, ARG2)
                putString(Constants.INTENT_KEY_IMAGE_PATH, ARG3)
            }
            fragment.arguments = bundle
            return fragment
        }

        val cropPercentage = CropPercentage()
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    var screenAspectRatio: Int = 0
    private var imageAnalyzer: ImageAnalysis? = null
    private var displayId: Int = -1
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private var camera: Camera? = null
    private lateinit var overlay: SurfaceView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var scopedExecutor: ScopedExecutor
    private lateinit var processingText: TextView
    private lateinit var progressBar: ProgressBar
    var ocrScanCount: Int = 0;
    var barcodeScanCount: Int = 0;
    private lateinit var imageAnalysis: ImageAnalysis
   lateinit var countDownTimer: CountDownTimer 
    private lateinit  var screenSize : Size


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageCacheDir = getImageCacheDir(requireContext())
        val file = File(imageCacheDir, "my_image.png")

       // viewModel.SelectedTemplateString = loadJsonFromAsset(requireContext(),"template.json")
      //  viewModel.timePending = 6L
      //  viewModel.imagePath= file.absolutePath
        Log.d("startScan","viewModel SelectedTemplateString "+arguments?.getString(Constants.INTENT_TEMPLATE_JSON_STRING))
        Log.d("startScan","viewModel timePending "+arguments?.getInt(Constants.INTENT_SCAN_TIMER))
        Log.d("startScan","viewModel imagePath "+arguments?.getString(Constants.INTENT_KEY_IMAGE_PATH))
        viewModel.SelectedTemplateString = arguments?.getString(Constants.INTENT_TEMPLATE_JSON_STRING).toString()
        viewModel.timePending = 5L //= arguments?.getInt(Constants.INTENT_SCAN_TIMER)!!.toLong()
        viewModel.imagePath= arguments?.getString(Constants.INTENT_KEY_IMAGE_PATH)!!
        viewModel.convertJsonToObject(viewModel.SelectedTemplateString)

        if (null != savedInstanceState) {
            viewModel.timePending = savedInstanceState.getLong(Constants.TIMER_INSTANCE)
            viewModel.isScanning = savedInstanceState.getBoolean(Constants.IS_SCANNING)
            viewModel.imagePath = savedInstanceState.getString(Constants.INTENT_KEY_IMAGE_PATH)!!
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view = inflater.inflate(R.layout.main_fragment, container, false)

        if (!isGPSEnabled()) {
            requestGPSEnable()
        }

        overlay = view.findViewById(R.id.overlay)
        processingText = view.findViewById(R.id.processing_text)
        progressBar = view.findViewById(R.id.progressBar)

        processingText.visibility = View.GONE
        progressBar.visibility = View.GONE
        return view
    }

    fun isGPSEnabled(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun requestGPSEnable() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.viewfinder)
        init()
        viewModel.isScanning = false
        viewModel.executor = cameraExecutor
        Log.d("startScan","allPermissionsGranted: "+allPermissionsGranted())
        if (allPermissionsGranted()) {
            viewFinder.post {
                displayId = viewFinder.display.displayId
                //  openCamera()
                Log.d("startScan / scanCamera", "Camera permission available")
                setCropPercentages()
            }
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    // Set crop percentages for camera block to set the scan area within the camera frame
    private fun setCropPercentages() {
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenAspectRatio = aspectRatio(metrics.heightPixels, metrics.widthPixels)
            cropPercentage.heightCropPercent = Constants.LANDSCAPE_HEIGHT_CROP_PERCENT
            cropPercentage.widthCropPercent = Constants.LANDSCAPE_WIDTH_CROP_PERCENT
            screenSize = Size(Constants.LANDSCAPE_SCREEN_WIDTH,Constants.LANDSCAPE_SCREEN_HEIGHT)
        } else {
            cropPercentage.heightCropPercent = Constants.PORTRAIT_HEIGHT_CROP_PERCENT
            cropPercentage.widthCropPercent = Constants.PORTRAIT_WIDTH_CROP_PERCENT
            screenSize = Size(Constants.PORTRAIT_SCREEN_WIDTH,Constants.PORTRAIT_SCREEN_HEIGHT)
            screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }
        initiateCamera()
    }


    private fun initiateCamera() {
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenAspectRatio = aspectRatio(metrics.heightPixels, metrics.widthPixels)
        } else {
            screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }
        viewModel.scanDataManager.getScanText().clear()
        viewModel.scanDataManager.getScanQrCode().clear()
        viewModel.scanDataManager.getScanBarCode().clear()
        ocrScanCount = 0
        barcodeScanCount = 0
        setUpCamera()
    }

    fun checkTimer() {
        if (viewModel.timePending > 0L) {
            drawOverlay(
                String.format(
                    getString(R.string.preparing_camera_fmt),
                    viewModel.timePending.toString()
                )
            )
        } else {
            drawOverlay(getString(R.string.overlay_help))
        }
        scanTimer()
    }

    private fun init() {
        cameraExecutor = Executors.newCachedThreadPool()
        scopedExecutor = ScopedExecutor(cameraExecutor)
    }

    private fun stopCamera() {
        cameraExecutor.shutdown()
        scopedExecutor.shutdown()
    }

    // Draw camera overLay for creating scan area block
    private fun drawOverlay(overLayText: String) {
        overlay.apply {
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSPARENT)
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    holder?.let {
                        drawOverlay(
                            it,
                            overLayText
                        )
                    }
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {}

                override fun surfaceCreated(holder: SurfaceHolder) {
                    holder?.let {
                        drawOverlay(
                            it,
                            overLayText
                        )
                    }
                }
            })
        }
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = try {
                cameraProviderFuture.get()
                // Log.d(Constants.TAG_CAMERA,"camera initialization done1")
            } catch (e: ExecutionException) {
                throw IllegalStateException("Camera initialization failed.", e.cause!!)
            }
            Log.d(Constants.TAG_CAMERA, "camera initialization done2")
            // Build and bind the camera use cases
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by comparing absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = ln(max(width, height).toDouble() / min(width, height))
        if (abs(previewRatio - ln(RATIO_4_3_VALUE))
            <= abs(previewRatio - ln(RATIO_16_9_VALUE))
        ) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9

    }

    // Check the threshold scan count for number of scan's attempted
    fun checkThresholdAndStop(ocrScanCount: Int, barcodeScanCount: Int) {
        Log.d(
            Constants.TAG_PROCESS_TEXT,
            "checkThresholdAndStop : barcodeScanCount " + barcodeScanCount + " ocrScanCount " + ocrScanCount
        )
        if (Constants.OCR_THRESHOLDCOUNT <= ocrScanCount) {
            stopCamera()
            viewFinder.visibility = View.GONE
            overlay.visibility = View.GONE
            processingText.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            viewModel.processScan()
        }
    }

    fun setResolutionConfiguration(): ResolutionSelector {
        val resolutionSelectorBuilder = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy(screenAspectRatio, FALLBACK_RULE_AUTO))
          //  .setResolutionStrategy(ResolutionStrategy(screenSize,FALLBACK_RULE_CLOSEST_HIGHER))
        return resolutionSelectorBuilder.build()
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        Log.d(Constants.TAG_CAMERA, "camera initialization bindCameraUseCases")
        val rotation = viewFinder.display.rotation

        Log.d(Constants.TAG_CAMERA, "rotation : " + rotation)
        val preview = Preview.Builder()
            .setResolutionSelector(setResolutionConfiguration())
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .setResolutionSelector(setResolutionConfiguration())
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                imageAnalysis = it
                startAnalyzer()
            }

        viewModel.sourceText.observe(viewLifecycleOwner, Observer {
            Log.d("sourceText", "count :  " + ocrScanCount)
            viewModel.scanDataManager.addScanText(it)
            ocrScanCount++
            checkThresholdAndStop(ocrScanCount, barcodeScanCount)
        })
        viewModel.scannedBarCode.observe(viewLifecycleOwner, Observer {
            viewModel.addCodeValue(it)
            barcodeScanCount++
            checkThresholdAndStop(ocrScanCount, barcodeScanCount)
        })

        viewModel.scanResultJson.observe(viewLifecycleOwner, Observer {
            onScanComplete(it, null == it)
        })

        // Select back camera since text detection does not work with front camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
            preview.setSurfaceProvider(viewFinder.surfaceProvider)
            checkTimer()
        } catch (exc: IllegalStateException) {
            Log.e("TAG", "Use case binding failed. This must be running on main thread.", exc)
        }
    }


    private fun drawOverlay(
        holder: SurfaceHolder,
        overlayText: String
    ) {
        val canvas = holder.lockCanvas()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val bgPaint = Paint().apply {
            alpha = 140
        }
        canvas.drawPaint(bgPaint)
        val rectPaint = Paint()
        rectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = Color.WHITE
        val outlinePaint = Paint()
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.color = Color.WHITE
        outlinePaint.strokeWidth = 4f
        val surfaceWidth = holder.surfaceFrame.width()
        val surfaceHeight = holder.surfaceFrame.height()
        val cornerRadius = 25f
        val rectTop = surfaceHeight * cropPercentage.heightCropPercent / 2 / 100f
        val rectLeft = surfaceWidth * cropPercentage.widthCropPercent / 2 / 100f
        val rectRight = surfaceWidth * (1 - cropPercentage.widthCropPercent / 2 / 100f)
        val rectBottom = surfaceHeight * (1 - cropPercentage.heightCropPercent / 2 / 100f)
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, rectPaint
        )
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, outlinePaint
        )
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 40F
        val textBounds = Rect()
        textPaint.getTextBounds(overlayText, 0, overlayText.length, textBounds)
        val textX = (surfaceWidth - textBounds.width()) / 2f
        val textY = rectBottom + textBounds.height() + 15f // put text below rect and 15f padding
        canvas.drawText(overlayText, textX, textY, textPaint)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post {
                    Log.d("startScan / scanCamera", "Camera permissions granted")
                    setCropPercentages()
                }
            } else {
              /*  Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()*/
                onPermissonDenied()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
   /* private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }*/

    private fun allPermissionsGranted() :Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

      /*  val coarseLocationPermission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val fineLocationPermission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
*/
        return (cameraPermission ) //&& (coarseLocationPermission || fineLocationPermission))
    }

    private fun onScanComplete(scanResultJson: String, isError: Boolean) {
        Log.d("startScan / onScanComplete", "received onScanComplete call")
        val data = Intent();
        if (isError) {
            Log.d("startScan / onScanComplete", "Error in scanning")
            //activity?.setResult(Activity.RESULT_FIRST_USER, data)
        } else {
            data.putExtra("ScanResultModel", scanResultJson);
            Log.d(
                "startScan / onScanComplete",
                String.format(
                    "Received data from scan. setting activity result data.\nData json=%s\ndata=%s",
                    scanResultJson, data
                )
            )
            activity?.setResult(Activity.RESULT_OK, data)
        }
        viewModel.isScanning == false
        Log.d("startScan / onScanComplete", "Finishing the camera activity")
        activity?.finish()
    }
    @SuppressLint("SuspiciousIndentation")
    private fun onPermissonDenied() {
        val data = Intent();
            data.putExtra("ScanResultModel", "");

            activity?.setResult(Activity.CONTEXT_RESTRICTED, data)

        viewModel.isScanning == false

        activity?.finish()
    }

    fun startAnalyzer() {
        if (!viewModel.isScanning && viewModel.timePending <= 0L) {
            viewModel.isScanning = true
            imageAnalysis.setAnalyzer(
                scopedExecutor, MlkitImageAnalyzer(
                    cameraExecutor,
                    viewModel.scanDataManager,
                    viewModel.imagePath,
                    viewModel.sourceText,
                    viewModel.scannedBarCode
                )
            )
        }
    }
// scantimer for adding delay before scan starts
    fun scanTimer() {
        countDownTimer = object : CountDownTimer(viewModel.timePending * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                viewModel.timePending = millisUntilFinished / 1000
                Log.d("CountDownTimer", "timePending  " + viewModel.timePending)
                if (overlay.holder.surface.isValid) {
                    drawOverlay(
                        overlay.holder,
                        String.format(
                            getString(R.string.preparing_camera_fmt),
                            viewModel.timePending.toString()
                        )
                    )
                }
            }

            override fun onFinish() {
                if (overlay.holder.surface.isValid) {
                    drawOverlay(
                        overlay.holder,
                        getString(R.string.overlay_help)
                    )
                }
                startAnalyzer()
            }
        }.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(Constants.TIMER_INSTANCE, viewModel.timePending)
        outState.putBoolean(Constants.IS_SCANNING, viewModel.isScanning)
        outState.putString(Constants.INTENT_KEY_IMAGE_PATH, viewModel.imagePath)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            scopedExecutor.shutdown()
            countDownTimer.cancel()
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
    }


}


