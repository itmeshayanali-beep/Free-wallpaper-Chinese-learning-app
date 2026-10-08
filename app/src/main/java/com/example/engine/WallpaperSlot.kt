package com.example.engine

enum class WallpaperSlot(
    val id: String,
    val title: String,
    val subtitle: String,
    val backgroundColor: Int,
    val backgroundSecondaryColor: Int,
    val isGradient: Boolean,
    val isDrawableAsset: Boolean = false,
    val isCustomGallery: Boolean = false,
    val heroTextColor: Int,
    val bridgeTextColor: Int,
    val anchorTextColor: Int,
    val badgeBgColor: Int,
    val badgeTextColor: Int,
    val subtleBorderColor: Int
) {
    FOREST_PAPER(
        id = "forest_paper",
        title = "Emerald Origami",
        subtitle = "Textured deep pine origami paper with luminous bone & gold typography",
        backgroundColor = 0xFF142B24.toInt(),
        backgroundSecondaryColor = 0xFF0E1E19.toInt(),
        isGradient = false,
        isDrawableAsset = true,
        isCustomGallery = false,
        heroTextColor = 0xFFF5F3E9.toInt(),
        bridgeTextColor = 0xFFE2C475.toInt(),
        anchorTextColor = 0xFFD8DEDC.toInt(),
        badgeBgColor = 0xFF1B382F.toInt(),
        badgeTextColor = 0xFF9CD1BC.toInt(),
        subtleBorderColor = 0xFF2A5245.toInt()
    ),
    WASHI(
        id = "washi",
        title = "Muted Washi",
        subtitle = "Warm handmade paper with charcoal ink and terracotta accents",
        backgroundColor = 0xFFF9F8F5.toInt(),
        backgroundSecondaryColor = 0xFFF1EFEA.toInt(),
        isGradient = false,
        isDrawableAsset = false,
        isCustomGallery = false,
        heroTextColor = 0xFF1C1B1A.toInt(),
        bridgeTextColor = 0xFF8B5E3C.toInt(),
        anchorTextColor = 0xFF5D5955.toInt(),
        badgeBgColor = 0xFFEAE6DE.toInt(),
        badgeTextColor = 0xFF7A5133.toInt(),
        subtleBorderColor = 0xFFDED9CE.toInt()
    ),
    CHARCOAL(
        id = "charcoal",
        title = "OLED Charcoal",
        subtitle = "True pitch black for battery saving and distraction-free elegance",
        backgroundColor = 0xFF121214.toInt(),
        backgroundSecondaryColor = 0xFF1A1C20.toInt(),
        isGradient = false,
        isDrawableAsset = false,
        isCustomGallery = false,
        heroTextColor = 0xFFE8E6E3.toInt(),
        bridgeTextColor = 0xFF94A3B8.toInt(),
        anchorTextColor = 0xFFA3A39E.toInt(),
        badgeBgColor = 0xFF262933.toInt(),
        badgeTextColor = 0xFFCBD5E1.toInt(),
        subtleBorderColor = 0xFF2E3342.toInt()
    ),
    ZEN_MIST(
        id = "zen_mist",
        title = "Zen Mist",
        subtitle = "Calm twilight indigo gradient with glowing silver & lavender glyphs",
        backgroundColor = 0xFF161822.toInt(),
        backgroundSecondaryColor = 0xFF262A3C.toInt(),
        isGradient = true,
        isDrawableAsset = false,
        isCustomGallery = false,
        heroTextColor = 0xFFEDE9FE.toInt(),
        bridgeTextColor = 0xFFA5B4FC.toInt(),
        anchorTextColor = 0xFFC7D2FE.toInt(),
        badgeBgColor = 0xFF32374E.toInt(),
        badgeTextColor = 0xFFC4B5FD.toInt(),
        subtleBorderColor = 0xFF3F4663.toInt()
    ),
    CUSTOM_GALLERY(
        id = "custom_gallery",
        title = "Custom Gallery",
        subtitle = "Your own photo or downloaded wallpaper with automatic text contrast",
        backgroundColor = 0xFF1A1A1A.toInt(),
        backgroundSecondaryColor = 0xFF1A1A1A.toInt(),
        isGradient = false,
        isDrawableAsset = false,
        isCustomGallery = true,
        heroTextColor = 0xFFFFFFFF.toInt(),
        bridgeTextColor = 0xFFFFD54F.toInt(),
        anchorTextColor = 0xFFF5F5F5.toInt(),
        badgeBgColor = 0x88000000.toInt(),
        badgeTextColor = 0xFFEEEEEE.toInt(),
        subtleBorderColor = 0x44FFFFFF.toInt()
    );

    companion object {
        fun fromId(id: String): WallpaperSlot {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FOREST_PAPER
        }
    }
}
