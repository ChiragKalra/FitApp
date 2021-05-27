package com.bruhascended.fitapp.capturefood

import android.content.Context
import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bruhascended.classifier.ImageStreamClassifier
import com.bruhascended.classifier.RunTimeAnalyzer
import org.tensorflow.lite.support.label.Category
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow

class ImageStreamAnalyzer (
    private val context: Context,
    private val listener: (predictions: Array<String>) -> Unit
): ImageAnalysis.Analyzer {

    companion object {
        const val EXP_AVG_EXPONENT = 4
    }

    private lateinit var classifier: ImageStreamClassifier
    private val runTimeAnalyzer = RunTimeAnalyzer()

    private lateinit var weightedAverages: Array<Double>
    private var runningBeta = 1.0

    init {
        // initialise classifier in another thread to not block main
        Thread {
            classifier = ImageStreamClassifier(context)
            weightedAverages = Array(classifier.outputCount) {0.0}
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
        if (::classifier.isInitialized) {
            runTimeAnalyzer.log()
            var bm = proxy.image?.toBitmap()
            if (bm != null) {
                val dim = min(bm.width, bm.height)
                bm = Bitmap.createBitmap(bm, 0, 0, dim, dim)
                val categories = classifier.fetchResults(bm)
                runningAverage(categories)
                feedPredictions(categories)
            }
        }
        proxy.close()
    }

    private fun runningAverage (categories: Array<Category>) {
        val currBeta = getBeta()
        runningBeta *= currBeta
        for (i in weightedAverages.indices) {
            val curr = categories[i].score.pow(EXP_AVG_EXPONENT)
            weightedAverages[i] = weightedAverages[i]*currBeta + curr*(1-currBeta)
            weightedAverages[i] /= 1 - runningBeta
        }
    }

    private fun feedPredictions (categories: Array<Category>) {
        val indexes = HashMap<String, Int>().apply {
            categories.forEachIndexed { i, it ->
                put(it.label, i)
            }
        }
        categories.sortByDescending { weightedAverages[indexes[it.label]!!] }
        val predictions = Array(4) {
            categories[it].label
        }
        listener(predictions)
    }

    /*
        * return the number of prediction records to store in memory to compute a combined probability
        * @param
        *  the time length that has to be considered
     */
    private fun getBeta (timeInMilli: Long = 2500): Double {
        val time = runTimeAnalyzer.getAverage() ?: 200
        return 1 - 1 / ceil(timeInMilli.toDouble() / time)
    }

    fun close () {
        classifier.close()
    }
}