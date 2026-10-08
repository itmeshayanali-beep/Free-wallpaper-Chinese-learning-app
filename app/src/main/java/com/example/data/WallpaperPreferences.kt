package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "hourly_language_wallpaper_prefs",
        Context.MODE_PRIVATE
    )

    private val _slotTypeFlow = MutableStateFlow(getSlotType())
    val slotTypeFlow: StateFlow<String> = _slotTypeFlow.asStateFlow()

    private val _languageFilterFlow = MutableStateFlow(getLanguageFilter())
    val languageFilterFlow: StateFlow<String> = _languageFilterFlow.asStateFlow()

    private val _levelFilterFlow = MutableStateFlow(getLevelFilter())
    val levelFilterFlow: StateFlow<String> = _levelFilterFlow.asStateFlow()

    private val _activePhraseIdFlow = MutableStateFlow(getActivePhraseId())
    val activePhraseIdFlow: StateFlow<String> = _activePhraseIdFlow.asStateFlow()

    fun getSlotType(): String = prefs.getString("slot_type", "forest_paper") ?: "forest_paper"
    fun setSlotType(slot: String) {
        prefs.edit().putString("slot_type", slot).apply()
        _slotTypeFlow.value = slot
    }

    fun getLanguageFilter(): String = prefs.getString("language_filter", "all") ?: "all"
    fun setLanguageFilter(lang: String) {
        prefs.edit().putString("language_filter", lang).apply()
        _languageFilterFlow.value = lang
    }

    fun getLevelFilter(): String = prefs.getString("level_filter", "all") ?: "all"
    fun setLevelFilter(level: String) {
        prefs.edit().putString("level_filter", level).apply()
        _levelFilterFlow.value = level
    }

    fun getShowRomanization(): Boolean = prefs.getBoolean("show_romanization", true)
    fun setShowRomanization(show: Boolean) {
        prefs.edit().putBoolean("show_romanization", show).apply()
    }

    fun getShowEnglish(): Boolean = prefs.getBoolean("show_english", true)
    fun setShowEnglish(show: Boolean) {
        prefs.edit().putBoolean("show_english", show).apply()
    }

    fun getShowCategoryTag(): Boolean = prefs.getBoolean("show_category_tag", true)
    fun setShowCategoryTag(show: Boolean) {
        prefs.edit().putBoolean("show_category_tag", show).apply()
    }

    fun getFontScale(): Float = prefs.getFloat("font_scale", 1.0f)
    fun setFontScale(scale: Float) {
        prefs.edit().putFloat("font_scale", scale).apply()
    }

    fun getVerticalOffsetDp(): Float = prefs.getFloat("vertical_offset_dp", 0.0f)
    fun setVerticalOffsetDp(offset: Float) {
        prefs.edit().putFloat("vertical_offset_dp", offset).apply()
    }

    fun getActivePhraseId(): String = prefs.getString("active_phrase_id", "ja-001") ?: "ja-001"
    fun setActivePhraseId(id: String) {
        prefs.edit().putString("active_phrase_id", id).apply()
        _activePhraseIdFlow.value = id
    }

    fun getLastRotatedHour(): Int = prefs.getInt("last_rotated_hour", -1)
    fun setLastRotatedHour(hour: Int) {
        prefs.edit().putInt("last_rotated_hour", hour).apply()
    }

    // Custom Gallery Background & Scrim Dimming
    fun getCustomBackgroundImagePath(): String? = prefs.getString("custom_bg_path", null)
    fun setCustomBackgroundImagePath(path: String?) {
        prefs.edit().putString("custom_bg_path", path).apply()
    }

    fun getBackgroundDim(): Float = prefs.getFloat("bg_dim_scrim", 0.35f)
    fun setBackgroundDim(dim: Float) {
        prefs.edit().putFloat("bg_dim_scrim", dim.coerceIn(0.0f, 0.85f)).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: WallpaperPreferences? = null

        fun getInstance(context: Context): WallpaperPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = WallpaperPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
