package com.yomu.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceLanguageTest {

    @Test
    fun `stored code resolves to correct source language`() {
        assertEquals(SourceLanguage.JAPANESE, SourceLanguage.fromCode("ja"))
        assertEquals(SourceLanguage.ENGLISH, SourceLanguage.fromCode("en"))
        assertEquals(SourceLanguage.CHINESE, SourceLanguage.fromCode("zh"))
        assertEquals(SourceLanguage.KOREAN, SourceLanguage.fromCode("ko"))
    }

    @Test
    fun `unknown or null code defaults to JAPANESE`() {
        assertEquals(SourceLanguage.JAPANESE, SourceLanguage.fromCode("xyz"))
        assertEquals(SourceLanguage.JAPANESE, SourceLanguage.fromCode(null))
    }

    @Test
    fun `all supported source languages have prompt names`() {
        assertEquals(
            listOf("Japanese", "English", "Chinese", "Korean"),
            SourceLanguage.entries.map { it.promptName }
        )
    }
}
