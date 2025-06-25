/*
 * Copyright 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.deviceonboarder.labelScan.analyzer

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.deviceonboarder.labelScan.model.ScannedRawData
import com.deviceonboarder.labelScan.scanScreen.ScanFragment.Companion.cropPercentage
import com.deviceonboarder.labelScan.util.Constants
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.deviceonboarder.labelScan.util.ImageUtils
import com.deviceonboarder.labelScan.util.ScanImageUtil
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executor

/**
 * Analyzes the frames passed in from the camera and returns any detected text within the requested
 * crop region.
 */
class MlkitImageAnalyzer(
    private val executor: Executor,
    private val scanDataManager : ScanDataManager,
    private val imagePath : String,
    private val scannedData: MutableLiveData<ScannedRawData>,
    private val barcodeResult: MutableLiveData<List<Barcode>>,
) : ImageAnalysis.Analyzer {
    private val detector =
        TextRecognition.getClient(TextRecognizerOptions.Builder().setExecutor(executor).build())
    init {
        // TODO - ideally we should have detector closed
        //lifecycle.addObserver(detector)
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        // We requested a setTargetAspectRatio, but it's not guaranteed that's what the camera
        // stack is able to support, so we calculate the actual ratio from the first frame to
        // know how to appropriately crop the image we want to analyze.
        val imageHeight = mediaImage.height
        val imageWidth = mediaImage.width
        val convertImageToBitmap = ImageUtils.convertYuv420888ImageToBitmap(mediaImage)
        val cropRect = Rect(0, 0, imageWidth, imageHeight)
        // If the image has a way wider aspect ratio than expected, crop less of the height so we
        // don't end up cropping too much of the image. If the image has a way taller aspect ratio
        // than expected, we don't have to make any changes to our cropping so we don't handle it
        // here.
        /*   val currentCropPercentages = imageCropPercentages.value ?: return
           if (actualAspectRatio > 3) {
               val originalHeightCropPercentage = currentCropPercentages.first
               val originalWidthCropPercentage = currentCropPercentages.second
               imageCropPercentages.value =
                   Pair(originalHeightCropPercentage / 2, originalWidthCropPercentage)
           }*/

        // If the image is rotated by 90 (or 270) degrees, swap height and width when calculating
        // the crop.
        try {
            val heightCropPercent = cropPercentage.heightCropPercent
            val widthCropPercent = cropPercentage.widthCropPercent
            val (widthCrop, heightCrop) = when (rotationDegrees) {
                90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
                else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
            }
            if (imageWidth > 0 && imageHeight > 0 && widthCrop > 0 && heightCrop > 0) {
                cropRect.inset(
                    (imageWidth * widthCrop / 2).toInt(),
                    (imageHeight * heightCrop / 2).toInt()
                )
                val croppedBitmap =
                    ImageUtils.rotateAndCrop(convertImageToBitmap, rotationDegrees, cropRect)
                getTextFromBitmap(
                     croppedBitmap
                ).addOnCompleteListener {
                    getBarCodeFromBitmap(
                        InputImage.fromBitmap(
                            croppedBitmap,
                            0
                        )
                    ).addOnCompleteListener {
                        imageProxy.close()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
        }
    }

    private fun getTextFromBitmap(
        bitmap: Bitmap
    ): Task<Text> {
       val image= InputImage.fromBitmap(bitmap,0)
        // Pass image to an ML Kit Vision API
        return detector.process(image)
            .addOnSuccessListener { rawTextData ->
                Log.d(Constants.TAG_IMAGE_ANALYZER, "Text recognition model downloaded")
                Log.d(Constants.TAG_IMAGE_ANALYZER, "**********************************")

                Log.d(Constants.TAG_IMAGE_ANALYZER, "------------------------------------")

                // Task completed successfully
                val scanImagePath = imagePath + scanDataManager.getScanText().size
                ScanImageUtil.SaveImage(bitmap, scanImagePath)
                scannedData.value = ScannedRawData(rawTextData, scanImagePath, 0)
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                Log.e(Constants.TAG_IMAGE_ANALYZER, "Text recognition error", exception)
                val message = getErrorMessage(exception)
                message?.let {
                    Log.e(
                        Constants.TAG_IMAGE_ANALYZER,
                        String.format("Text recognition error exception.message = '%s'", it)
                    )
                }
            }
    }

    private fun getBarCodeFromBitmap(image: InputImage): Task<List<Barcode>> {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        var barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)
        return barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                Log.d("startScan / Analyzer", String.format("Num of barcodes = %d", barcodes.size))
                barcodeResult.value = barcodes
            }
            .addOnFailureListener {
                val message = getErrorMessage(it)
                message?.let {
                    Log.e("startScan / Analyzer", String.format("Barcode analyzer failed - '%s'", it))
                }
            }
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }


}