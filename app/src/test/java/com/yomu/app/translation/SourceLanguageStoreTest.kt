package com.yomu.app.translation

import com.yomu.core.Constants
import com.yomu.core.SourceLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceLanguageStoreTest {

    private val prefs = MapSharedPreferences()
    private val store = SourceLanguageStore(prefs)

    @Test
    fun `nothing stored loads default Japanese`() {
        assertEquals(SourceLanguage.DEFAULT, store.load())
    }

    @Test
    fun `every saved source language loads back`() {
        SourceLanguage.entries.forEach { language ->
            store.save(language)
            assertEquals(language, SourceLanguageStore(prefs).load())
        }
    }

    @Test
    fun `choice is stored under PREF_SOURCE_LANGUAGE`() {
        store.save(SourceLanguage.KOREAN)
        assertEquals("ko", prefs.values[Constants.PREF_SOURCE_LANGUAGE])
    }
}
