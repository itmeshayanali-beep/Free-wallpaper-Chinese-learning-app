package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Metadata and dynamic color palette provider for each language difficulty level:
 * - Chinese: HSK 1, HSK 2
 * - Japanese: JLPT N3, JLPT N2, JLPT N1
 */
data class LevelBadgeStyle(
    val levelKey: String,
    val shortLabel: String,
    val fullTitle: String,
    val language: String, // "zh" or "ja"
    val description: String,
    val primaryColor: Color,
    val textColorLight: Color,
    val textColorDark: Color,
    val containerLight: Color,
    val containerDark: Color,
    val borderColor: Color
) {
    fun getTextColor(isDark: Boolean): Color = if (isDark) textColorDark else textColorLight
    fun getContainerColor(isDark: Boolean): Color = if (isDark) containerDark else containerLight
}

object LevelThemes {

    val HSK1 = LevelBadgeStyle(
        levelKey = "HSK 1",
        shortLabel = "HSK 1",
        fullTitle = "HSK 1 • Beginner Foundation",
        language = "zh",
        description = "Essential high-frequency vocabulary, basic greetings, and core sentence patterns.",
        primaryColor = LevelHsk1Primary,
        textColorLight = LevelHsk1Text,
        textColorDark = LevelHsk1TextDark,
        containerLight = LevelHsk1Container,
        containerDark = LevelHsk1ContainerDark,
        borderColor = LevelHsk1Border
    )

    val HSK2 = LevelBadgeStyle(
        levelKey = "HSK 2",
        shortLabel = "HSK 2",
        fullTitle = "HSK 2 • Elementary Fluency",
        language = "zh",
        description = "Everyday communicative fluency, rich idioms, health, and travel expressions.",
        primaryColor = LevelHsk2Primary,
        textColorLight = LevelHsk2Text,
        textColorDark = LevelHsk2TextDark,
        containerLight = LevelHsk2Container,
        containerDark = LevelHsk2ContainerDark,
        borderColor = LevelHsk2Border
    )

    val JLPT_N3 = LevelBadgeStyle(
        levelKey = "JLPT N3",
        shortLabel = "N3",
        fullTitle = "JLPT N3 • Intermediate Bridge",
        language = "ja",
        description = "Bridge to authentic natural Japanese: daily proverbs, mindset, and practical wisdom.",
        primaryColor = LevelN3Primary,
        textColorLight = LevelN3Text,
        textColorDark = LevelN3TextDark,
        containerLight = LevelN3Container,
        containerDark = LevelN3ContainerDark,
        borderColor = LevelN3Border
    )

    val JLPT_N2 = LevelBadgeStyle(
        levelKey = "JLPT N2",
        shortLabel = "N2",
        fullTitle = "JLPT N2 • Upper Intermediate",
        language = "ja",
        description = "Four-character idioms (Yojijukugo), nuanced philosophy, and workplace discourse.",
        primaryColor = LevelN2Primary,
        textColorLight = LevelN2Text,
        textColorDark = LevelN2TextDark,
        containerLight = LevelN2Container,
        containerDark = LevelN2ContainerDark,
        borderColor = LevelN2Border
    )

    val JLPT_N1 = LevelBadgeStyle(
        levelKey = "JLPT N1",
        shortLabel = "N1",
        fullTitle = "JLPT N1 • Advanced Mastery",
        language = "ja",
        description = "Profound Japanese aesthetics (Wabi-sabi, Yūgen, Mono no aware) and classical literature.",
        primaryColor = LevelN1Primary,
        textColorLight = LevelN1Text,
        textColorDark = LevelN1TextDark,
        containerLight = LevelN1Container,
        containerDark = LevelN1ContainerDark,
        borderColor = LevelN1Border
    )

    val allLevels = listOf(HSK1, HSK2, JLPT_N3, JLPT_N2, JLPT_N1)
    val allLevelKeys = listOf("HSK 1", "HSK 2", "JLPT N3", "JLPT N2", "JLPT N1")
    val chineseLevelKeys = listOf("HSK 1", "HSK 2")
    val japaneseLevelKeys = listOf("JLPT N3", "JLPT N2", "JLPT N1")

    fun getStyle(levelKey: String): LevelBadgeStyle {
        return when (levelKey.uppercase().trim()) {
            "HSK 1", "HSK1", "BEGINNER" -> HSK1
            "HSK 2", "HSK2" -> HSK2
            "JLPT N3", "N3", "INTERMEDIATE" -> JLPT_N3
            "JLPT N2", "N2" -> JLPT_N2
            "JLPT N1", "N1", "ADVANCED" -> JLPT_N1
            else -> JLPT_N3
        }
    }
}
