package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {

    @Query("SELECT * FROM phrases ORDER BY id ASC")
    fun getAllPhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE isFavorite = 1 ORDER BY lastShownAt DESC")
    fun getFavoritePhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE language = :lang ORDER BY id ASC")
    fun getPhrasesByLanguage(lang: String): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE level = :level ORDER BY id ASC")
    fun getPhrasesByLevel(level: String): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE (:lang = 'all' OR language = :lang) AND (:level = 'all' OR level = :level) ORDER BY id ASC")
    fun getFilteredPhrases(lang: String, level: String): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE id = :id LIMIT 1")
    suspend fun getPhraseById(id: String): PhraseEntity?

    @Query("SELECT * FROM phrases WHERE id = :id LIMIT 1")
    fun getPhraseByIdFlow(id: String): Flow<PhraseEntity?>

    @Query("SELECT COUNT(*) FROM phrases")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<PhraseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity)

    @Update
    suspend fun updatePhrase(phrase: PhraseEntity)

    @Query("UPDATE phrases SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)

    @Query("UPDATE phrases SET timesShown = timesShown + 1, lastShownAt = :timestamp WHERE id = :id")
    suspend fun markPhraseShown(id: String, timestamp: Long)

    @Query("SELECT * FROM phrases WHERE (:lang = 'all' OR language = :lang) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPhrase(lang: String): PhraseEntity?

    @Query("SELECT * FROM phrases WHERE (:lang = 'all' OR language = :lang) AND (:level = 'all' OR level = :level) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPhraseWithFilters(lang: String, level: String): PhraseEntity?
}
