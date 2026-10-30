package com.yomu.app.translation

import android.content.SharedPreferences
import com.yomu.core.Constants
import com.yomu.core.SourceLanguage
import javax.inject.Inject

class SourceLanguageStore @Inject constructor(private val prefs: SharedPreferences) {

    fun load(): SourceLanguage {
        val stored = runCatching { prefs.getString(Constants.PREF_SOURCE_LANGUAGE, null) }.getOrNull()
        return SourceLanguage.fromCode(stored)
    }

    fun save(language: SourceLanguage) =
        prefs.edit().putString(Constants.PREF_SOURCE_LANGUAGE, language.code).apply()
}
