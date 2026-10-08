package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.R
import com.example.data.PhraseEntity
import com.example.data.WallpaperPreferences
import java.io.File

object WallpaperRendererHelper {

    private var cachedForestBitmap: Bitmap? = null
    private var cachedCustomBitmap: Bitmap? = null
    private var lastCustomPath: String? = null

    fun render(
        context: Context,
        canvas: Canvas,
        width: Int,
        height: Int,
        phrase: PhraseEntity?,
        slot: WallpaperSlot,
        preferences: WallpaperPreferences,
        showSafeZoneOverlay: Boolean = false,
        showSimulatedLauncher: Boolean = false,
        alphaMultiplier: Float = 1.0f
    ) {
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw Background
        val bgPaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        when {
            slot == WallpaperSlot.FOREST_PAPER -> {
                drawForestPaperBackground(context, canvas, width, height, bgPaint)
                // Subtle darkening overlay to ensure text stands out with maximum contrast
                val dimPaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    alpha = 45
                }
                canvas.drawRect(0f, 0f, w, h, dimPaint)
            }

            slot == WallpaperSlot.CUSTOM_GALLERY -> {
                drawCustomGalleryBackground(context, canvas, width, height, preferences, bgPaint)
            }

            slot.isGradient -> {
                bgPaint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    slot.backgroundColor, slot.backgroundSecondaryColor,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, bgPaint)
            }

            else -> {
                bgPaint.color = slot.backgroundColor
                canvas.drawRect(0f, 0f, w, h, bgPaint)

                // Subtle inner framing line
                val framePaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = slot.subtleBorderColor
                    alpha = (60 * alphaMultiplier).toInt().coerceIn(0, 255)
                }
                val margin = w * 0.05f
                canvas.drawRoundRect(
                    margin, margin * 1.5f,
                    w - margin, h - margin * 1.5f,
                    24f, 24f, framePaint
                )
            }
        }

        val fontScale = preferences.getFontScale()
        val verticalOffsetPx = preferences.getVerticalOffsetDp() * (context.resources.displayMetrics.density)

        // 2. Compute Safe-Zone Boundaries
        val topSafe = h * 0.20f
        val bottomSafe = h * 0.76f
        val centerY = (topSafe + bottomSafe) / 2f + verticalOffsetPx

        if (phrase != null) {
            val alphaInt = (255 * alphaMultiplier).toInt().coerceIn(0, 255)

            // --- Layer 1: Category & Language Pill Badge ---
            if (preferences.getShowCategoryTag()) {
                val langTag = if (phrase.language == "ja") "🇯🇵" else "🇨🇳"
                val categoryText = "$langTag ${phrase.level.uppercase()} · ${phrase.category.uppercase()}"

                val badgeTextPaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 12f * context.resources.displayMetrics.scaledDensity * fontScale
                    color = slot.badgeTextColor
                    alpha = (230 * alphaMultiplier).toInt().coerceIn(0, 255)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    letterSpacing = 0.12f
                    textAlign = Paint.Align.CENTER
                }

                val badgeTextBounds = Rect()
                badgeTextPaint.getTextBounds(categoryText, 0, categoryText.length, badgeTextBounds)
                val badgeWidth = badgeTextBounds.width() + 36f
                val badgeHeight = badgeTextBounds.height() + 20f
                val badgeY = centerY - 140f * fontScale

                val badgeBgPaint = Paint().apply {
                    isAntiAlias = true
                    color = slot.badgeBgColor
                    alpha = (200 * alphaMultiplier).toInt().coerceIn(0, 255)
                }

                val badgeRect = RectF(
                    w / 2f - badgeWidth / 2f,
                    badgeY - badgeHeight / 2f,
                    w / 2f + badgeWidth / 2f,
                    badgeY + badgeHeight / 2f
                )
                canvas.drawRoundRect(badgeRect, 16f, 16f, badgeBgPaint)
                canvas.drawText(categoryText, w / 2f, badgeY + badgeTextBounds.height() / 2f - 3f, badgeTextPaint)
            }

            // --- Layer 2: Hero Native Glyphs (Kanji / Hanzi) ---
            val heroPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = (if (phrase.nativeText.length > 6) 34f else 46f) *
                        context.resources.displayMetrics.scaledDensity * fontScale
                color = slot.heroTextColor
                alpha = alphaInt
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                letterSpacing = 0.08f
                textAlign = Paint.Align.CENTER
                // Soft shadow for enhanced legibility on photo wallpapers
                if (slot == WallpaperSlot.CUSTOM_GALLERY || slot == WallpaperSlot.FOREST_PAPER) {
                    setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
                }
            }

            val heroY = centerY - 45f * fontScale
            canvas.drawText(phrase.nativeText, w / 2f, heroY, heroPaint)

            // --- Layer 3: Romanization (Bridge / Pronunciation) ---
            if (preferences.getShowRomanization()) {
                val bridgePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 18f * context.resources.displayMetrics.scaledDensity * fontScale
                    color = slot.bridgeTextColor
                    alpha = (245 * alphaMultiplier).toInt().coerceIn(0, 255)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    letterSpacing = 0.04f
                    textAlign = Paint.Align.CENTER
                    if (slot == WallpaperSlot.CUSTOM_GALLERY || slot == WallpaperSlot.FOREST_PAPER) {
                        setShadowLayer(6f, 0f, 3f, android.graphics.Color.BLACK)
                    }
                }
                val bridgeY = centerY + 10f * fontScale
                canvas.drawText(phrase.romanization, w / 2f, bridgeY, bridgePaint)
            }

            // --- Layer 4: English Meaning (Anchor Layer) ---
            if (preferences.getShowEnglish()) {
                val anchorPaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 15f * context.resources.displayMetrics.scaledDensity * fontScale
                    color = slot.anchorTextColor
                    alpha = (230 * alphaMultiplier).toInt().coerceIn(0, 255)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                    if (slot == WallpaperSlot.CUSTOM_GALLERY || slot == WallpaperSlot.FOREST_PAPER) {
                        setShadowLayer(5f, 0f, 2f, android.graphics.Color.BLACK)
                    }
                }

                val anchorY = centerY + 50f * fontScale
                val maxTextWidth = (w * 0.78f).toInt()

                val staticLayout = StaticLayout.Builder.obtain(
                    phrase.english, 0, phrase.english.length, anchorPaint, maxTextWidth
                )
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(4f, 1f)
                    .build()

                canvas.save()
                canvas.translate(w / 2f - maxTextWidth / 2f, anchorY)
                staticLayout.draw(canvas)
                canvas.restore()
            }

            // --- Layer 5: Cultural Note ---
            if (phrase.culturalNote.isNotEmpty()) {
                val notePaint = TextPaint().apply {
                    isAntiAlias = true
                    textSize = 11.5f * context.resources.displayMetrics.scaledDensity * fontScale
                    color = slot.anchorTextColor
                    alpha = (160 * alphaMultiplier).toInt().coerceIn(0, 255)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                    textAlign = Paint.Align.CENTER
                    if (slot == WallpaperSlot.CUSTOM_GALLERY || slot == WallpaperSlot.FOREST_PAPER) {
                        setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                    }
                }
                val noteY = centerY + 125f * fontScale
                val noteMaxWidth = (w * 0.75f).toInt()
                val noteLayout = StaticLayout.Builder.obtain(
                    "“${phrase.culturalNote}”", 0, phrase.culturalNote.length + 2, notePaint, noteMaxWidth
                )
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .build()

                canvas.save()
                canvas.translate(w / 2f - noteMaxWidth / 2f, noteY)
                noteLayout.draw(canvas)
                canvas.restore()
            }
        }

        // --- Simulated Launcher Overlay ---
        if (showSimulatedLauncher) {
            drawSimulatedLauncher(context, canvas, w, h, slot)
        }

        // --- Safe-Zone Visual Boundaries ---
        if (showSafeZoneOverlay) {
            drawSafeZoneGuides(canvas, w, h, topSafe, bottomSafe)
        }
    }

    private fun drawForestPaperBackground(
        context: Context,
        canvas: Canvas,
        w: Int,
        h: Int,
        paint: Paint
    ) {
        val bmp = cachedForestBitmap ?: try {
            BitmapFactory.decodeResource(context.resources, R.drawable.bg_forest_paper)?.also {
                cachedForestBitmap = it
            }
        } catch (e: Throwable) {
            null
        }

        if (bmp != null) {
            drawCenterCrop(canvas, bmp, w, h, paint)
        } else {
            paint.color = WallpaperSlot.FOREST_PAPER.backgroundColor
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        }
    }

    private fun drawCustomGalleryBackground(
        context: Context,
        canvas: Canvas,
        w: Int,
        h: Int,
        preferences: WallpaperPreferences,
        paint: Paint
    ) {
        val customPath = preferences.getCustomBackgroundImagePath()
        val bmp = if (customPath != null && File(customPath).exists()) {
            if (customPath != lastCustomPath || cachedCustomBitmap == null) {
                cachedCustomBitmap = BitmapFactory.decodeFile(customPath)
                lastCustomPath = customPath
            }
            cachedCustomBitmap
        } else null

        if (bmp != null) {
            drawCenterCrop(canvas, bmp, w, h, paint)
            // Apply adjustable dimming scrim for contrast
            val dim = preferences.getBackgroundDim()
            if (dim > 0.05f) {
                val scrimPaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    alpha = (dim * 255).toInt().coerceIn(0, 255)
                }
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrimPaint)
            }
        } else {
            // Placeholder slate background when no custom photo picked yet
            paint.color = android.graphics.Color.parseColor("#1C2024")
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

            val hintPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 14f * context.resources.displayMetrics.scaledDensity
                color = android.graphics.Color.parseColor("#9E9E9E")
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No custom image selected yet", w / 2f, h * 0.5f, hintPaint)
            canvas.drawText("Tap 'Select from Gallery' in Themes", w / 2f, h * 0.5f + 30f, hintPaint)
        }
    }

    private fun drawCenterCrop(canvas: Canvas, bitmap: Bitmap, viewWidth: Int, viewHeight: Int, paint: Paint) {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val vw = viewWidth.toFloat()
        val vh = viewHeight.toFloat()

        val scale = maxOf(vw / bw, vh / bh)
        val scaledW = bw * scale
        val scaledH = bh * scale

        val left = (vw - scaledW) / 2f
        val top = (vh - scaledH) / 2f

        val destRect = RectF(left, top, left + scaledW, top + scaledH)
        canvas.drawBitmap(bitmap, null, destRect, paint)
    }

    private fun drawSimulatedLauncher(
        context: Context,
        canvas: Canvas,
        w: Float,
        h: Float,
        slot: WallpaperSlot
    ) {
        val density = context.resources.displayMetrics.density
        val isDark = slot != WallpaperSlot.WASHI
        val overlayColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

        val statusPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 13f * density
            color = overlayColor
            alpha = 180
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("14:00", 24f * density, 36f * density, statusPaint)

        val pillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * density
            color = overlayColor
            alpha = 160
        }
        canvas.drawRoundRect(
            w - 48f * density, 24f * density,
            w - 24f * density, 38f * density,
            4f, 4f, pillPaint
        )

        val widgetPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 38f * density
            color = overlayColor
            alpha = 140
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("14:00", w / 2f, h * 0.12f, widgetPaint)

        val datePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f * density
            color = overlayColor
            alpha = 120
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Wednesday, Oct 25", w / 2f, h * 0.15f, datePaint)

        val dockY = h - 50f * density
        val iconCount = 5
        val iconSpacing = w / (iconCount + 1)
        val dockCirclePaint = Paint().apply {
            isAntiAlias = true
            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            alpha = 40
        }
        for (i in 1..iconCount) {
            val cx = iconSpacing * i
            canvas.drawCircle(cx, dockY, 20f * density, dockCirclePaint)
        }

        val navPaint = Paint().apply {
            isAntiAlias = true
            color = overlayColor
            alpha = 100
            strokeWidth = 3f * density
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(w * 0.35f, h - 14f * density, w * 0.65f, h - 14f * density, navPaint)
    }

    private fun drawSafeZoneGuides(
        canvas: Canvas,
        w: Float,
        h: Float,
        topSafe: Float,
        bottomSafe: Float
    ) {
        val guidePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = android.graphics.Color.parseColor("#40FF5722")
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 12f), 0f)
        }

        canvas.drawLine(0f, topSafe, w, topSafe, guidePaint)
        canvas.drawLine(0f, bottomSafe, w, bottomSafe, guidePaint)

        val labelPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 24f
            color = android.graphics.Color.parseColor("#FF5722")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText("▲ WIDGET SAFE BOUNDARY (TOP 20%)", 24f, topSafe - 12f, labelPaint)
        canvas.drawText("▼ DOCK SAFE BOUNDARY (BOTTOM 24%)", 24f, bottomSafe + 30f, labelPaint)
    }
}
