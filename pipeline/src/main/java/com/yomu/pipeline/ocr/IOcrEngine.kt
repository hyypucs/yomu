package com.yomu.pipeline.ocr

import android.graphics.Bitmap
import com.yomu.core.SourceLanguage

/**
 * Common interface for OCR engines supporting single or multi-language recognition.
 */
interface IOcrEngine {
    fun isModelLoaded(): Boolean
    fun extractText(region: Bitmap, sourceLanguage: SourceLanguage = SourceLanguage.DEFAULT): OcrResult?
}
