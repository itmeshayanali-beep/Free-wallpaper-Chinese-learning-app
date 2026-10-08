package com.example.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.firebase.models.CustomPhraseDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun `authenticated user can save and observe custom phrase`() = runBlocking {
        val email = "user_${System.currentTimeMillis()}@example.com"
        val uid = signInTestUser(email)

        val repository = FirebaseRepository(firestore)

        val testPhrase = CustomPhraseDto(
            id = "custom_${System.currentTimeMillis()}",
            userId = uid,
            language = "ja",
            nativeText = "木漏れ日",
            romanization = "Komorebi",
            english = "Sunlight through trees",
            category = "Nature",
            culturalNote = "Test note"
        )

        val saveResult = repository.saveCustomPhrase(testPhrase)
        assertTrue("Save custom phrase should succeed", saveResult.isSuccess)

        val phrases = repository.observeCustomPhrases().first()
        val found = phrases.firstOrNull { it.id == testPhrase.id }
        assertNotNull("Saved phrase should be in user custom phrases collection", found)
        assertEquals("木漏れ日", found?.nativeText)
    }

    @Test
    fun `authenticated user can save and observe favorite`() = runBlocking {
        val email = "fav_user_${System.currentTimeMillis()}@example.com"
        val uid = signInTestUser(email)

        val repository = FirebaseRepository(firestore)

        val phraseId = "ja-001"
        val favResult = repository.saveFavorite(phraseId)
        assertTrue("Save favorite should succeed", favResult.isSuccess)

        val favorites = repository.observeFavorites().first()
        val found = favorites.firstOrNull { it.phraseId == phraseId }
        assertNotNull("Saved favorite should exist", found)
        assertEquals(uid, found?.userId)
    }

    @Test(expected = IllegalStateException::class)
    fun `unauthenticated user throws when attempting to observe custom phrases`() {
        auth.signOut()
        val repository = FirebaseRepository(firestore)
        runBlocking {
            repository.observeCustomPhrases().first()
        }
    }
}
