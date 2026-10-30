package com.yomu.core

/**
 * The source language of the manga text to recognize and translate.
 * Supports Japanese, English, Chinese, and Korean.
 */
enum class SourceLanguage(val code: String, val promptName: String, val displayName: String) {
    JAPANESE("ja", "Japanese", "일본어 (日本語)"),
    ENGLISH("en", "English", "영어 (English)"),
    CHINESE("zh", "Chinese", "중국어 (中文)"),
    KOREAN("ko", "Korean", "한국어 (한국어)");

    companion object {
        val DEFAULT = JAPANESE

        fun fromCode(code: String?): SourceLanguage =
            entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}
