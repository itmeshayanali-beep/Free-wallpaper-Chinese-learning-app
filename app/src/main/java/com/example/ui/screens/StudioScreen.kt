package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.R
import com.example.data.PhraseEntity
import com.example.data.WallpaperPreferences
import com.example.engine.WallpaperRendererHelper
import com.example.engine.WallpaperSlot
import com.example.ui.MainViewModel
import com.example.ui.theme.LevelThemes
import java.io.File

@Composable
fun StudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPhrase by viewModel.currentPhrase.collectAsState()
    val currentSlot by viewModel.currentSlot.collectAsState()
    val showSafeZones by viewModel.showSafeZones.collectAsState()
    val showSimulatedLauncher by viewModel.showSimulatedLauncher.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val showRomanization by viewModel.showRomanization.collectAsState()
    val showEnglish by viewModel.showEnglish.collectAsState()
    val showCategoryTag by viewModel.showCategoryTag.collectAsState()
    val customBgPath by viewModel.customBgPath.collectAsState()
    val backgroundDim by viewModel.backgroundDim.collectAsState()
    val levelFilter by viewModel.levelFilter.collectAsState()
    val isDark = isSystemInDarkTheme()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Headline & Subtitle
        Text(
            text = "Hourly Language Wallpaper",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Passive micro-learning right on your Android lock & home screen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Interactive Live Phone Frame Preview
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .aspectRatio(9f / 16f)
                .shadow(16.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .border(3.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                .testTag("wallpaper_preview_frame"),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = remember(
                currentPhrase,
                currentSlot,
                showSafeZones,
                showSimulatedLauncher,
                fontScale,
                showRomanization,
                showEnglish,
                showCategoryTag,
                customBgPath,
                backgroundDim
            ) {
                renderPreviewBitmap(
                    context = context,
                    width = 720,
                    height = 1280,
                    phrase = currentPhrase,
                    slot = currentSlot,
                    preferences = WallpaperPreferences.getInstance(context),
                    showSafeZoneOverlay = showSafeZones,
                    showSimulatedLauncher = showSimulatedLauncher
                )
            }

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Interactive Live Wallpaper Preview",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Overlay Toggle Row (Simulated Launcher & Safe Zones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = showSimulatedLauncher,
                onClick = { viewModel.toggleSimulatedLauncher() },
                label = { Text("Launcher Overlay", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.padding(end = 8.dp).testTag("toggle_launcher_overlay")
            )
            FilterChip(
                selected = showSafeZones,
                onClick = { viewModel.toggleSafeZones() },
                label = { Text("Safe Zones", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("toggle_safe_zones")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Theme Slot Selector Pill Row (Horizontally scrollable to support all aesthetic themes)
        Text(
            text = "Select Aesthetic Theme",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WallpaperSlot.entries.forEach { slot ->
                val isSelected = slot == currentSlot
                Surface(
                    modifier = Modifier
                        .width(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.selectSlot(slot) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("slot_button_${slot.id}"),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = if (isSelected) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(slot.backgroundColor))
                                .border(1.dp, Color(slot.subtleBorderColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                slot == WallpaperSlot.FOREST_PAPER -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.bg_forest_paper),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                slot == WallpaperSlot.CUSTOM_GALLERY -> {
                                    Icon(
                                        imageVector = Icons.Default.Collections,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slot.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Level Rotation Filter Row
        Text(
            text = "Filter Hourly Rotation By Level",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = levelFilter == "all",
                onClick = { viewModel.selectLevelFilter("all") },
                label = { Text("All Levels") }
            )
            LevelThemes.allLevelKeys.forEach { lvlKey ->
                val style = LevelThemes.getStyle(lvlKey)
                val isSelected = levelFilter == lvlKey
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectLevelFilter(if (isSelected) "all" else lvlKey) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(style.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(lvlKey)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = style.getContainerColor(isDark),
                        selectedLabelColor = style.getTextColor(isDark)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current Phrase Details Card with Audio & Favorite Controls
        currentPhrase?.let { phrase ->
            val levelStyle = LevelThemes.getStyle(phrase.level)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("current_phrase_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${if (phrase.language == "ja") "🇯🇵" else "🇨🇳"} ${phrase.category}",
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                        }

                        Row {
                            IconButton(
                                onClick = { viewModel.speakPhrase(phrase) },
                                modifier = Modifier.testTag("audio_speak_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Pronounce phrase",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleFavorite(phrase) },
                                modifier = Modifier.testTag("favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (phrase.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Toggle favorite",
                                    tint = if (phrase.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = phrase.nativeText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = phrase.romanization,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = phrase.english,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (phrase.culturalNote.isNotEmpty()) {
                        Text(
                            text = "💡 ${phrase.culturalNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row: Next Hour & Set Live Wallpaper CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = { viewModel.nextHourPhrase() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("shuffle_hour_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Next Hour", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val intent = viewModel.createSetWallpaperIntent()
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp)
                    .testTag("set_live_wallpaper_cta"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Wallpaper", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun renderPreviewBitmap(
    context: Context,
    width: Int,
    height: Int,
    phrase: PhraseEntity?,
    slot: WallpaperSlot,
    preferences: WallpaperPreferences,
    showSafeZoneOverlay: Boolean,
    showSimulatedLauncher: Boolean
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    WallpaperRendererHelper.render(
        context = context,
        canvas = canvas,
        width = width,
        height = height,
        phrase = phrase,
        slot = slot,
        preferences = preferences,
        showSafeZoneOverlay = showSafeZoneOverlay,
        showSimulatedLauncher = showSimulatedLauncher,
        alphaMultiplier = 1.0f
    )
    return bitmap
}
