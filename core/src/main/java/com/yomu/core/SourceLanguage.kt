package com.yomu.core

/**
 * The source language of the manga text to recognize and translate.
 * Supports Japanese, English, Chinese, and Korean.
 */
enum class SourceLanguage(val code: String, val promptName: String) {
    JAPANESE("ja", "Japanese"),
    ENGLISH("en", "English"),
    CHINESE("zh", "Chinese"),
    KOREAN("ko", "Korean");

    companion object {
        val DEFAULT = JAPANESE

        fun fromCode(code: String?): SourceLanguage =
            entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}
