package com.yomu.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetLanguageTest {

    @Test
    fun `a stored code names its language`() {
        assertEquals(TargetLanguage.BRAZILIAN_PORTUGUESE, TargetLanguage.fromCode("pt-BR"))
        assertEquals(TargetLanguage.ENGLISH, TargetLanguage.fromCode("en"))
        assertEquals(TargetLanguage.KOREAN, TargetLanguage.fromCode("ko"))
        assertEquals(TargetLanguage.CHINESE, TargetLanguage.fromCode("zh"))
        assertEquals(TargetLanguage.JAPANESE, TargetLanguage.fromCode("ja"))
    }

    @Test
    fun `an unknown or missing code names no language`() {
        assertNull(TargetLanguage.fromCode("pt"))
        assertNull(TargetLanguage.fromCode(null))
    }

    @Test
    fun `each language carries the full name used in prompts`() {
        assertEquals(
            listOf("English", "Brazilian Portuguese", "Spanish", "French", "German", "Korean", "Chinese", "Japanese"),
            TargetLanguage.entries.map { it.promptName }
        )
    }
}
