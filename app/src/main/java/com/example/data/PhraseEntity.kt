package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phrases")
data class PhraseEntity(
    @PrimaryKey
    val id: String,
    val language: String, // "zh" or "ja"
    val nativeText: String, // e.g. "木漏れ日" or "千里之行 始于足下"
    val romanization: String, // e.g. "Komorebi" or "Qiān lǐ zhī xíng, shǐ yú zú xià"
    val english: String, // e.g. "Sunlight filtering through trees"
    val category: String, // "Mind & Nature", "Greetings", "Daily Life", "Wisdom", "Courtesy"
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val culturalNote: String = "",
    val isFavorite: Boolean = false,
    val timesShown: Int = 0,
    val lastShownAt: Long = 0L
)
