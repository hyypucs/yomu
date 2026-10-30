package com.yomu.pipeline.ocr

import android.graphics.Bitmap
import com.yomu.core.SourceLanguage
import com.yomu.ml.OnnxRuntime

/**
 * Composite OCR engine that routes recognition to MlKitOcrEngine for Korean/multi-language
 * or to native ONNX OcrEngine for Japanese/downloaded models.
 */
class CompositeOcrEngine(
    private val onnxOcrEngine: OcrEngine,
    private val mlKitOcrEngine: MlKitOcrEngine = MlKitOcrEngine()
) : IOcrEngine {

    fun loadModel(encoderPath: String, decoderPath: String, vocabPath: String): Boolean =
        onnxOcrEngine.loadModel(encoderPath, decoderPath, vocabPath)

    fun release() {
        onnxOcrEngine.release()
    }

    override fun isModelLoaded(): Boolean = true

    override fun extractText(region: Bitmap, sourceLanguage: SourceLanguage): OcrResult? {
        return when (sourceLanguage) {
            SourceLanguage.KOREAN -> mlKitOcrEngine.extractText(region, sourceLanguage)
            SourceLanguage.ENGLISH -> mlKitOcrEngine.extractText(region, sourceLanguage)
            SourceLanguage.CHINESE -> mlKitOcrEngine.extractText(region, sourceLanguage)
            SourceLanguage.JAPANESE -> {
                if (onnxOcrEngine.isModelLoaded()) {
                    onnxOcrEngine.extractText(region, sourceLanguage)
                        ?: mlKitOcrEngine.extractText(region, sourceLanguage)
                } else {
                    mlKitOcrEngine.extractText(region, sourceLanguage)
                }
            }
        }
    }
}
