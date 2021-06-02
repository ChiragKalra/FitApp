package com.bruhascended.fitapp.ui.capturefood

import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bruhascended.classifier.foodimage.ImageClassifier
import com.bruhascended.classifier.RunTimeAnalyzer
import com.bruhascended.classifier.foodimage.ImageStreamClassifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.tensorflow.lite.support.label.Category
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ImageAnalyzer (
    private val context: Context,
    private val predictionsListener: (predictions: Array<String>) -> Unit
): ImageAnalysis.Analyzer {

    companion object {
        const val MIN_LATENCY_MILLI = 400L
        const val PREDICTION_DURATION_MILLI = 1250L
    }

    private lateinit var streamClassifier: ImageStreamClassifier

    init {
        // initialise classifier in another thread to not block main
        Thread {
            streamClassifier = ImageStreamClassifier(
                context,
                MIN_LATENCY_MILLI,
                PREDICTION_DURATION_MILLI
            )
        }.start()
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze (proxy: ImageProxy) {
        // check if classifier is initialised
        if (::streamClassifier.isInitialized) {
            var bm = proxy.image?.toBitmap()
            if (bm != null) {
                val dim = min(bm.width, bm.height)
                bm = Bitmap.createBitmap(bm, 0, 0, dim, dim)
                val predictions = streamClassifier.fetchResults(bm)
                predictionsListener(predictions)
            }
        }
        runBlocking {
            delay(streamClassifier.getLatencyCorrection())
            proxy.close()
        }
    }

    fun close () {
        streamClassifier.close()
    }
}