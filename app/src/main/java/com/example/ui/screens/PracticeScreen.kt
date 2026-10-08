package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhraseEntity
import com.example.ui.MainViewModel
import com.example.ui.components.WeeklyProgressChart
import com.example.ui.theme.LevelThemes

@Composable
fun PracticeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Weekly Progress, 1: Flashcards, 2: Quiz
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Learning & Analytics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Track weekly mastery progress and practice hourly wallpaper phrases",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Practice Mode Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Progress", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Flashcards", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    viewModel.initNextQuizQuestion()
                },
                text = { Text("Quiz", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTab) {
            0 -> WeeklyProgressChart(modifier = Modifier.fillMaxWidth())
            1 -> FlashcardsView(viewModel = viewModel)
            2 -> QuizChallengeView(viewModel = viewModel)
        }
    }
}

@Composable
fun FlashcardsView(viewModel: MainViewModel) {
    val allPhrases by viewModel.allPhrases.collectAsState()
    var selectedLevel by remember { mutableStateOf("all") }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    // Filter phrases by selected level
    val phrases = remember(allPhrases, selectedLevel) {
        if (selectedLevel == "all") allPhrases
        else allPhrases.filter { it.level.equals(selectedLevel, ignoreCase = true) }
    }

    val currentPhrase = if (phrases.isNotEmpty()) {
        phrases[currentIndex.coerceIn(0, phrases.size - 1)]
    } else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Level Filter Chips for Flashcard Practice
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedLevel == "all",
                onClick = {
                    selectedLevel = "all"
                    currentIndex = 0
                    isFlipped = false
                },
                label = { Text("All (${allPhrases.size})") }
            )
            LevelThemes.allLevelKeys.forEach { lvlKey ->
                val style = LevelThemes.getStyle(lvlKey)
                val count = allPhrases.count { it.level.equals(lvlKey, ignoreCase = true) }
                val isSelected = selectedLevel == lvlKey
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedLevel = if (isSelected) "all" else lvlKey
                        currentIndex = 0
                        isFlipped = false
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(style.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$lvlKey ($count)")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = style.getContainerColor(isDark),
                        selectedLabelColor = style.getTextColor(isDark)
                    )
                )
            }
        }

        if (currentPhrase == null) {
            Text("No phrases available for $selectedLevel.", modifier = Modifier.padding(40.dp))
            return
        }

        val levelStyle = LevelThemes.getStyle(currentPhrase.level)

        val rotation by animateFloatAsState(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = tween(durationMillis = 400),
            label = "card_flip"
        )

        // Flashcard Container
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { isFlipped = !isFlipped }
                .testTag("practice_flashcard"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front of card (Native Glyphs)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${if (currentPhrase.language == "ja") "🇯🇵" else "🇨🇳"} ${currentPhrase.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Distinct Level Badge Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = levelStyle.getContainerColor(isDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, levelStyle.borderColor)
                            ) {
                                Text(
                                    text = currentPhrase.level,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = levelStyle.getTextColor(isDark),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = currentPhrase.nativeText,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (currentPhrase.nativeText.length > 7) 24.sp else 32.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Tap card to reveal pronunciation & meaning",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    // Back of card (Romanization & Meaning)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        // Level Indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = levelStyle.getContainerColor(isDark)
                        ) {
                            Text(
                                text = "${currentPhrase.level} • ${currentPhrase.category}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = levelStyle.getTextColor(isDark),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = currentPhrase.romanization,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = levelStyle.primaryColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentPhrase.english,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (currentPhrase.culturalNote.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "💡 ${currentPhrase.culturalNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card navigation controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.speakPhrase(currentPhrase) }) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Listen",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "${currentIndex + 1} / ${phrases.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FilledTonalButton(
                onClick = {
                    isFlipped = false
                    currentIndex = (currentIndex + 1) % phrases.size
                },
                modifier = Modifier.testTag("next_flashcard_button")
            ) {
                Text("Next Card")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun QuizChallengeView(viewModel: MainViewModel) {
    val question by viewModel.quizQuestion.collectAsState()
    val options by viewModel.quizOptions.collectAsState()
    val selectedAnswer by viewModel.selectedQuizAnswer.collectAsState()
    val isCorrect by viewModel.isQuizAnswerCorrect.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val streak by viewModel.quizStreak.collectAsState()

    var quizLevelFilter by remember { mutableStateOf("all") }
    val isDark = isSystemInDarkTheme()

    if (question == null) {
        Text("Preparing quiz question...", modifier = Modifier.padding(40.dp))
        return
    }

    val levelStyle = LevelThemes.getStyle(question!!.level)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Level Filter for Quiz Challenge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = quizLevelFilter == "all",
                onClick = {
                    quizLevelFilter = "all"
                    viewModel.initNextQuizQuestion("all")
                },
                label = { Text("All Levels") }
            )
            LevelThemes.allLevelKeys.forEach { lvlKey ->
                val style = LevelThemes.getStyle(lvlKey)
                val isSelected = quizLevelFilter == lvlKey
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        quizLevelFilter = if (isSelected) "all" else lvlKey
                        viewModel.initNextQuizQuestion(quizLevelFilter)
                    },
                    label = { Text(lvlKey) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = style.getContainerColor(isDark),
                        selectedLabelColor = style.getTextColor(isDark)
                    )
                )
            }
        }

        // Quiz Score & Streak Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Score: $score",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Streak: $streak",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Target Question Card with Level Badge
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "What is the meaning of:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = levelStyle.getContainerColor(isDark)
                    ) {
                        Text(
                            text = question!!.level,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = levelStyle.getTextColor(isDark),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question!!.nativeText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = question!!.romanization,
                    style = MaterialTheme.typography.titleMedium,
                    color = levelStyle.primaryColor,
                    modifier = Modifier.padding(top = 4.dp)
                )

                IconButton(onClick = { viewModel.speakPhrase(question!!) }) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Hear pronunciation", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List
        options.forEachIndexed { index, option ->
            val isSelected = selectedAnswer == option
            val isTarget = option == question!!.english
            val isAnswered = selectedAnswer != null

            val backgroundColor = when {
                !isAnswered -> MaterialTheme.colorScheme.surface
                isTarget -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                isSelected && !isTarget -> Color(0xFFC62828).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                isTarget -> Color(0xFF2E7D32)
                isSelected && !isTarget -> Color(0xFFC62828)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isAnswered) {
                        viewModel.submitQuizAnswer(option)
                    }
                    .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                    .testTag("quiz_option_$index"),
                color = backgroundColor,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected || isTarget) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (isAnswered) {
                        if (isTarget) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF2E7D32))
                        } else if (isSelected) {
                            Icon(Icons.Default.Close, contentDescription = "Wrong", tint = Color(0xFFC62828))
                        }
                    }
                }
            }
        }

        // Feedback Banner & Next Button
        AnimatedVisibility(visible = selectedAnswer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bannerText = if (isCorrect == true) "🎉 Correct! Keep it up!" else "Not quite! Correct: ${question!!.english}"
                val bannerColor = if (isCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828)

                Text(
                    text = bannerText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = bannerColor,
                    textAlign = TextAlign.Center
                )

                if (question!!.culturalNote.isNotEmpty()) {
                    Text(
                        text = "💡 ${question!!.culturalNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.initNextQuizQuestion(quizLevelFilter) },
                    modifier = Modifier.testTag("next_quiz_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next Question")
                }
            }
        }
    }
}
