package com.yomu.pipeline.ocr

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yomu.core.SourceLanguage
import java.util.concurrent.TimeUnit

/**
 * Multi-language OCR engine utilizing Google ML Kit for Korean, Chinese, Japanese, and English.
 */
class MlKitOcrEngine : IOcrEngine {

    private val recognizers by lazy {
        mapOf(
            SourceLanguage.KOREAN to TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build()),
            SourceLanguage.CHINESE to TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build()),
            SourceLanguage.JAPANESE to TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build()),
            SourceLanguage.ENGLISH to TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        )
    }

    override fun isModelLoaded(): Boolean = true

    override fun extractText(region: Bitmap, sourceLanguage: SourceLanguage): OcrResult? {
        val recognizer = recognizers[sourceLanguage] ?: recognizers[SourceLanguage.ENGLISH] ?: return null
        return try {
            val image = InputImage.fromBitmap(region, 0)
            val task = recognizer.process(image)
            val visionText = Tasks.await(task, 2, TimeUnit.SECONDS)
            val text = visionText.text.trim()
            OcrResult(
                text = text,
                confidence = 0.95f,
                boundingBox = floatArrayOf(0f, 0f, region.width.toFloat(), region.height.toFloat())
            )
        } catch (_: Exception) {
            null
        }
    }
}
