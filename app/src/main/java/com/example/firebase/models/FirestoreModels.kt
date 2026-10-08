package com.example.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class UserProfileDto(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val totalMastered: Long = 0L,
    val currentStreak: Long = 0L,
    val preferredLanguage: String = "all",
    val selectedSlot: String = "forest_paper",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

data class CustomPhraseDto(
    val id: String = "",
    val userId: String = "",
    val language: String = "ja",
    val nativeText: String = "",
    val romanization: String = "",
    val english: String = "",
    val category: String = "Personal Note",
    val culturalNote: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

data class FavoriteDto(
    val phraseId: String = "",
    val userId: String = "",
    @ServerTimestamp
    val savedAt: Timestamp? = null
)
