package com.example

import com.example.data.PhraseSeedData
import com.example.engine.WallpaperSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test initial seed phrases validity`() {
        val phrases = PhraseSeedData.initialPhrases
        assertTrue("Seed phrases must not be empty", phrases.isNotEmpty())
        assertTrue("Must contain at least 10 phrases", phrases.size >= 10)

        val hasJapanese = phrases.any { it.language == "ja" }
        val hasChinese = phrases.any { it.language == "zh" }
        assertTrue("Must include Japanese phrases", hasJapanese)
        assertTrue("Must include Chinese phrases", hasChinese)

        phrases.forEach { phrase ->
            assertNotNull(phrase.id)
            assertTrue("Native text should not be blank", phrase.nativeText.isNotBlank())
            assertTrue("Romanization should not be blank", phrase.romanization.isNotBlank())
            assertTrue("English meaning should not be blank", phrase.english.isNotBlank())
        }
    }

    @Test
    fun `test wallpaper slots parsing and fallbacks`() {
        assertEquals(WallpaperSlot.FOREST_PAPER, WallpaperSlot.fromId("forest_paper"))
        assertEquals(WallpaperSlot.CUSTOM_GALLERY, WallpaperSlot.fromId("custom_gallery"))
        assertEquals(WallpaperSlot.WASHI, WallpaperSlot.fromId("washi"))
        assertEquals(WallpaperSlot.CHARCOAL, WallpaperSlot.fromId("charcoal"))
        assertEquals(WallpaperSlot.ZEN_MIST, WallpaperSlot.fromId("zen_mist"))
        // Fallback for unknown id should be FOREST_PAPER
        assertEquals(WallpaperSlot.FOREST_PAPER, WallpaperSlot.fromId("unknown_custom_slot"))
    }
}
