package org.renxo.deeplinkapplication.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import kotlin.math.log

class ImageAnalyzer(private val callback: (String)->Unit) : ImageAnalysis.Analyzer {
    var isAnalyzeCompleted = false
    override fun analyze(imageProxy: ImageProxy) {
        scanBarcode(imageProxy)
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun scanBarcode(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnCompleteListener {
                    imageProxy.close()
                    if (it.isSuccessful) {
                        readBarcodeData(it.result as List<Barcode>)
                    } else {
                        it.exception?.printStackTrace()
                    }
                }
        }
    }

    private fun readBarcodeData(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
//            when (barcode.valueType) {
//                Barcode.TYPE_TEXT -> {
                    try {
                        barcode.displayValue?.let {

                            if(!isAnalyzeCompleted){
                                isAnalyzeCompleted = true
                                callback(it)
                            }

                            /*val json = JSONObject(it)
                            if (json.has("qr_code_type")) {
                                if (json.get("qr_code_type") == "my_v_safety") {
                                    if (json.has("id") && !isAnalyzeCompleted) {

                                    }
                                }
                            }*/
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
//                }
//            }
        }
    }
}