package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.PhraseEntity
import com.example.data.PhraseSeedData
import com.example.ui.theme.LevelThemes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches updated app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Language Wallpaper", appName)
    }

    @Test
    fun `seed dataset contains phrases across all required levels`() {
        val seed = PhraseSeedData.initialPhrases
        assertTrue("Dataset expanded beyond 20 phrases", seed.size >= 50)

        val hsk1 = seed.filter { it.level == "HSK 1" }
        val hsk2 = seed.filter { it.level == "HSK 2" }
        val n3 = seed.filter { it.level == "JLPT N3" }
        val n2 = seed.filter { it.level == "JLPT N2" }
        val n1 = seed.filter { it.level == "JLPT N1" }

        assertTrue("HSK 1 phrases present", hsk1.isNotEmpty())
        assertTrue("HSK 2 phrases present", hsk2.isNotEmpty())
        assertTrue("JLPT N3 phrases present", n3.isNotEmpty())
        assertTrue("JLPT N2 phrases present", n2.isNotEmpty())
        assertTrue("JLPT N1 phrases present", n1.isNotEmpty())
    }

    @Test
    fun `level theme helper maps styles accurately`() {
        val hsk1Style = LevelThemes.getStyle("HSK 1")
        assertEquals("HSK 1", hsk1Style.levelKey)
        assertEquals("zh", hsk1Style.language)

        val n1Style = LevelThemes.getStyle("JLPT N1")
        assertEquals("JLPT N1", n1Style.levelKey)
        assertEquals("ja", n1Style.language)
    }

    @Test
    fun `test database operations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val dao = db.phraseDao()

        val testPhrase = PhraseEntity(
            id = "test-001",
            language = "ja",
            nativeText = "平和",
            romanization = "Heiwa",
            english = "Peace and harmony",
            category = "Wisdom",
            level = "JLPT N3"
        )
        dao.insertPhrase(testPhrase)

        val retrieved = dao.getPhraseById("test-001")
        assertNotNull(retrieved)
        assertEquals("平和", retrieved?.nativeText)
        assertEquals("JLPT N3", retrieved?.level)

        // Test favorite toggle
        dao.toggleFavorite("test-001", true)
        val updated = dao.getPhraseById("test-001")
        assertTrue(updated?.isFavorite == true)
    }
}
