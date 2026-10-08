package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PhraseEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.CalloutAccent
import com.example.ui.theme.CalloutDarkBorder
import com.example.ui.theme.CalloutDarkContainer
import com.example.ui.theme.CalloutLightBorder
import com.example.ui.theme.CalloutLightContainer
import com.example.ui.theme.LevelThemes
import com.example.ui.theme.ReadingGuideBorderDark
import com.example.ui.theme.ReadingGuideBorderLight
import com.example.ui.theme.ReadingGuideContainerDark
import com.example.ui.theme.ReadingGuideContainerLight

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allPhrases by viewModel.allPhrases.collectAsState()
    val currentPhrase by viewModel.currentPhrase.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguageFilter by remember { mutableStateOf("all") } // "all", "ja", "zh", "fav"
    var selectedLevelFilter by remember { mutableStateOf("all") } // "all", "HSK 1", "HSK 2", "JLPT N3", "JLPT N2", "JLPT N1"
    var selectedCategoryFilter by remember { mutableStateOf("all") }
    var showAddDialog by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    // Dynamic categories based on dataset
    val categories = remember(allPhrases) {
        val distinctCats = allPhrases.map { it.category }.distinct().sorted()
        listOf("all") + distinctCats
    }

    // Filter phrases according to user criteria
    val filteredPhrases = remember(
        allPhrases,
        searchQuery,
        selectedLanguageFilter,
        selectedLevelFilter,
        selectedCategoryFilter
    ) {
        allPhrases.filter { phrase ->
            val matchesQuery = searchQuery.isBlank() ||
                    phrase.nativeText.contains(searchQuery, ignoreCase = true) ||
                    phrase.romanization.contains(searchQuery, ignoreCase = true) ||
                    phrase.english.contains(searchQuery, ignoreCase = true) ||
                    phrase.culturalNote.contains(searchQuery, ignoreCase = true)

            val matchesLang = when (selectedLanguageFilter) {
                "ja" -> phrase.language == "ja"
                "zh" -> phrase.language == "zh"
                "fav" -> phrase.isFavorite
                else -> true
            }

            val matchesLevel = selectedLevelFilter == "all" ||
                    phrase.level.equals(selectedLevelFilter, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == "all" ||
                    phrase.category.equals(selectedCategoryFilter, ignoreCase = true)

            matchesQuery && matchesLang && matchesLevel && matchesCategory
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_phrases_input"),
                placeholder = { Text("Search Hanzi, Kanji, Pinyin, Romaji, or English...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // 2. Language Filter Chips (All, Japanese, Chinese, Bookmarks)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedLanguageFilter == "all",
                        onClick = {
                            selectedLanguageFilter = "all"
                        },
                        label = { Text("All (${allPhrases.size})") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                item {
                    val count = allPhrases.count { it.language == "ja" }
                    FilterChip(
                        selected = selectedLanguageFilter == "ja",
                        onClick = {
                            selectedLanguageFilter = "ja"
                            if (selectedLevelFilter.startsWith("HSK")) {
                                selectedLevelFilter = "all"
                            }
                        },
                        label = { Text("🇯🇵 Japanese ($count)") },
                        modifier = Modifier.testTag("filter_ja")
                    )
                }
                item {
                    val count = allPhrases.count { it.language == "zh" }
                    FilterChip(
                        selected = selectedLanguageFilter == "zh",
                        onClick = {
                            selectedLanguageFilter = "zh"
                            if (selectedLevelFilter.startsWith("JLPT")) {
                                selectedLevelFilter = "all"
                            }
                        },
                        label = { Text("🇨🇳 Chinese ($count)") },
                        modifier = Modifier.testTag("filter_zh")
                    )
                }
                item {
                    val favCount = allPhrases.count { it.isFavorite }
                    FilterChip(
                        selected = selectedLanguageFilter == "fav",
                        onClick = { selectedLanguageFilter = "fav" },
                        label = { Text("★ Bookmarks ($favCount)") },
                        modifier = Modifier.testTag("filter_fav")
                    )
                }
            }

            // 3. Difficulty Level Selector Chips (HSK 1, HSK 2, JLPT N3, JLPT N2, JLPT N1)
            Text(
                text = "DIFFICULTY LEVEL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedLevelFilter == "all",
                        onClick = { selectedLevelFilter = "all" },
                        label = { Text("All Levels") },
                        modifier = Modifier.testTag("level_filter_all")
                    )
                }

                // Show appropriate levels based on active language or show all
                val visibleLevels = when (selectedLanguageFilter) {
                    "ja" -> LevelThemes.japaneseLevelKeys
                    "zh" -> LevelThemes.chineseLevelKeys
                    else -> LevelThemes.allLevelKeys
                }

                items(visibleLevels) { levelKey ->
                    val style = LevelThemes.getStyle(levelKey)
                    val isSelected = selectedLevelFilter == levelKey
                    val levelCount = allPhrases.count { it.level.equals(levelKey, ignoreCase = true) }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedLevelFilter = if (isSelected) "all" else levelKey
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(style.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$levelKey ($levelCount)",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = style.getContainerColor(isDark),
                            selectedLabelColor = style.getTextColor(isDark)
                        ),
                        modifier = Modifier.testTag("level_filter_${levelKey.replace(" ", "_")}")
                    )
                }
            }

            // 4. Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { cat ->
                    val label = if (cat == "all") "All Categories" else cat
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            // 5. Phrases List with styled card views
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                if (filteredPhrases.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No phrases matched your filters",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Try clearing search or picking 'All Levels' / 'All Categories'",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        selectedLanguageFilter = "all"
                                        selectedLevelFilter = "all"
                                        selectedCategoryFilter = "all"
                                    }
                                ) {
                                    Text("Reset All Filters")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredPhrases, key = { it.id }) { phrase ->
                        PhraseCardItem(
                            phrase = phrase,
                            isCurrentlyActive = phrase.id == currentPhrase?.id,
                            onApplyWallpaper = {
                                viewModel.selectPhrase(phrase)
                                onNavigateToStudio()
                            },
                            onSpeak = { viewModel.speakPhrase(phrase) },
                            onToggleFavorite = { viewModel.toggleFavorite(phrase) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Custom Phrase
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_phrase_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add custom phrase")
        }
    }

    // Add Custom Phrase Dialog
    if (showAddDialog) {
        AddPhraseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { nativeText, romaji, eng, lang, cat, level, note ->
                viewModel.addCustomPhrase(
                    nativeText = nativeText,
                    romanization = romaji,
                    english = eng,
                    language = lang,
                    category = cat,
                    level = level,
                    culturalNote = note
                )
                showAddDialog = false
            }
        )
    }
}

/**
 * Modern High-Contrast Card View with Distinct Sections:
 * 1. Header Bar: Flag, Category, Distinct Level Badge Pill with Theme Color, Active status, Audio & Favorite actions
 * 2. Original Language Section: Prominent, high-contrast CJK typography (Kanji / Hanzi)
 * 3. Pronunciation & Reading Guide Section: Dedicated tinted container with phonetic tone marks
 * 4. Translation & Meaning Section: Clean, readable English translation
 * 5. Cultural Context Callout Section: Warm accent callout banner
 * 6. Quick Action Footer: Set as Wallpaper action button
 */
@Composable
fun PhraseCardItem(
    phrase: PhraseEntity,
    isCurrentlyActive: Boolean,
    onApplyWallpaper: () -> Unit,
    onSpeak: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val levelStyle = LevelThemes.getStyle(phrase.level)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isCurrentlyActive) 2.dp else 1.dp,
                color = if (isCurrentlyActive) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFF2E3240) else Color(0xFFE5E2DB),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("phrase_item_${phrase.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCurrentlyActive)
                if (isDark) Color(0xFF232532) else Color(0xFFFFF9EC)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isCurrentlyActive) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // =========================================================================
            // SECTION 1: HEADER BAR (Language Flag, Category, Level Badge, Actions)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Language Flag & Category Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${if (phrase.language == "ja") "🇯🇵" else "🇨🇳"} ${phrase.category}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // DISTINCT THEME LEVEL BADGE PILL
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = levelStyle.getContainerColor(isDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, levelStyle.borderColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(levelStyle.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = phrase.level,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = levelStyle.getTextColor(isDark)
                            )
                        }
                    }

                    // Active on Wallpaper Pill
                    if (isCurrentlyActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "ON WALLPAPER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Action Buttons (Speak + Bookmark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("speak_button_${phrase.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Pronounce phrase",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("bookmark_button_${phrase.id}")
                    ) {
                        Icon(
                            imageVector = if (phrase.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark phrase",
                            tint = if (phrase.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // =========================================================================
            // SECTION 2: ORIGINAL LANGUAGE SECTION (Kanji / Hanzi Glyphs)
            // =========================================================================
            Text(
                text = phrase.nativeText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = if (phrase.nativeText.length > 7) 24.sp else 30.sp,
                    lineHeight = if (phrase.nativeText.length > 7) 32.sp else 38.sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("phrase_native_${phrase.id}")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // =========================================================================
            // SECTION 3: PRONUNCIATION / READING GUIDE SECTION
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) ReadingGuideContainerDark else ReadingGuideContainerLight)
                    .border(
                        width = 1.dp,
                        color = if (isDark) ReadingGuideBorderDark else ReadingGuideBorderLight,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = levelStyle.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = phrase.romanization,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) levelStyle.textColorDark else levelStyle.textColorLight,
                        modifier = Modifier.testTag("phrase_romaji_${phrase.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // =========================================================================
            // SECTION 4: TRANSLATION & MEANING SECTION
            // =========================================================================
            Text(
                text = phrase.english,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .testTag("phrase_english_${phrase.id}")
            )

            // =========================================================================
            // SECTION 5: CULTURAL CONTEXT CALLOUT SECTION
            // =========================================================================
            if (phrase.culturalNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) CalloutDarkContainer else CalloutLightContainer)
                        .border(
                            width = 1.dp,
                            color = if (isDark) CalloutDarkBorder else CalloutLightBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = CalloutAccent,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = phrase.culturalNote,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Normal,
                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFF78350F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = if (isDark) Color(0xFF2C2F3D) else Color(0xFFEBE8E1),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // =========================================================================
            // SECTION 6: QUICK ACTION FOOTER (Set on Wallpaper)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (phrase.timesShown > 0) "Shown on wallpaper ${phrase.timesShown}x" else "Ready for hourly rotation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onApplyWallpaper,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("apply_wallpaper_${phrase.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCurrentlyActive) "Viewing" else "Apply Wallpaper",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Dialog to add a custom phrase with level selector (HSK 1, HSK 2, JLPT N3, JLPT N2, JLPT N1)
 */
@Composable
fun AddPhraseDialog(
    onDismiss: () -> Unit,
    onAdd: (nativeText: String, romaji: String, eng: String, lang: String, cat: String, level: String, note: String) -> Unit
) {
    var nativeText by remember { mutableStateOf("") }
    var romanization by remember { mutableStateOf("") }
    var english by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("ja") }
    var category by remember { mutableStateOf("Wisdom") }
    var level by remember { mutableStateOf("JLPT N3") }
    var culturalNote by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Phrase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Language selection
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = language == "ja",
                        onClick = {
                            language = "ja"
                            if (level.startsWith("HSK")) level = "JLPT N3"
                        },
                        label = { Text("🇯🇵 Japanese") }
                    )
                    FilterChip(
                        selected = language == "zh",
                        onClick = {
                            language = "zh"
                            if (level.startsWith("JLPT")) level = "HSK 1"
                        },
                        label = { Text("🇨🇳 Chinese") }
                    )
                }

                // Level Selection
                Text(
                    text = "Select Level:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                val availableLevels = if (language == "zh") LevelThemes.chineseLevelKeys else LevelThemes.japaneseLevelKeys
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(availableLevels) { lvlKey ->
                        val style = LevelThemes.getStyle(lvlKey)
                        FilterChip(
                            selected = level == lvlKey,
                            onClick = { level = lvlKey },
                            label = { Text(lvlKey) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = style.getContainerColor(isDark),
                                selectedLabelColor = style.getTextColor(isDark)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = nativeText,
                    onValueChange = { nativeText = it },
                    label = { Text("Native Glyphs (Kanji / Hanzi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_native_text")
                )
                OutlinedTextField(
                    value = romanization,
                    onValueChange = { romanization = it },
                    label = { Text("Pronunciation (Romaji / Pinyin)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_romaji")
                )
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("English Translation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_english")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Wisdom, Daily Life)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = culturalNote,
                    onValueChange = { culturalNote = it },
                    label = { Text("Usage / Cultural Note (Optional)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nativeText.isNotBlank() && english.isNotBlank()) {
                        onAdd(nativeText, romanization, english, language, category, level, culturalNote)
                    }
                },
                enabled = nativeText.isNotBlank() && english.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_button")
            ) {
                Text("Add to Library")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
