package com.example.engine

import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.data.AppDatabase
import com.example.data.PhraseEntity
import com.example.data.WallpaperPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class LanguageWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LanguageEngine()
    }

    inner class LanguageEngine : WallpaperService.Engine() {

        private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
        private lateinit var preferences: WallpaperPreferences
        private lateinit var database: AppDatabase
        private var currentPhrase: PhraseEntity? = null
        private var isVisible = false
        private var lastRenderedHour = -1
        private var surfaceWidth = 1080
        private var surfaceHeight = 2400
        private var transitionAlpha = 1.0f
        private var animator: ValueAnimator? = null
        private val handler = Handler(Looper.getMainLooper())

        private val timeTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                checkAndRotateHourly(force = false)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            preferences = WallpaperPreferences.getInstance(applicationContext)
            database = AppDatabase.getInstance(applicationContext)

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            registerReceiver(timeTickReceiver, filter)
            setTouchEventsEnabled(true)
        }

        override fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(timeTickReceiver)
            serviceScope.cancel()
            animator?.cancel()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            this.isVisible = visible
            if (visible) {
                checkAndRotateHourly(force = false)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
            loadAndDrawCurrent(animate = false)
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            // Tap interaction: rotate to next phrase on double-tap
            if (event?.action == MotionEvent.ACTION_UP) {
                // Gentle manual rotation
                // checkAndRotateHourly(force = true)
            }
        }

        private fun checkAndRotateHourly(force: Boolean) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (force || currentHour != lastRenderedHour) {
                rotateToNewPhrase(currentHour)
            } else if (currentPhrase == null) {
                loadAndDrawCurrent(animate = false)
            }
        }

        private fun rotateToNewPhrase(currentHour: Int) {
            serviceScope.launch(Dispatchers.IO) {
                val langFilter = preferences.getLanguageFilter()
                val nextPhrase = database.phraseDao().getRandomPhrase(langFilter)
                    ?: database.phraseDao().getAllPhrases().firstOrNull()?.firstOrNull()

                if (nextPhrase != null) {
                    database.phraseDao().markPhraseShown(nextPhrase.id, System.currentTimeMillis())
                    preferences.setActivePhraseId(nextPhrase.id)
                    preferences.setLastRotatedHour(currentHour)
                    lastRenderedHour = currentHour
                    currentPhrase = nextPhrase

                    handler.post {
                        animateTransition()
                    }
                }
            }
        }

        private fun loadAndDrawCurrent(animate: Boolean) {
            serviceScope.launch(Dispatchers.IO) {
                val activeId = preferences.getActivePhraseId()
                var phrase = database.phraseDao().getPhraseById(activeId)
                if (phrase == null) {
                    val lang = preferences.getLanguageFilter()
                    phrase = database.phraseDao().getRandomPhrase(lang)
                        ?: database.phraseDao().getAllPhrases().firstOrNull()?.firstOrNull()
                }
                currentPhrase = phrase
                lastRenderedHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                handler.post {
                    if (animate) {
                        animateTransition()
                    } else {
                        transitionAlpha = 1.0f
                        drawFrame()
                    }
                }
            }
        }

        private fun animateTransition() {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
                duration = 450
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { anim ->
                    transitionAlpha = anim.animatedValue as Float
                    drawFrame()
                }
                start()
            }
        }

        private fun drawFrame() {
            if (!isVisible) return
            val holder = surfaceHolder ?: return

            val canvas = try {
                holder.lockHardwareCanvas()
            } catch (e: Exception) {
                holder.lockCanvas()
            } ?: return

            try {
                val slot = WallpaperSlot.fromId(preferences.getSlotType())
                WallpaperRendererHelper.render(
                    context = applicationContext,
                    canvas = canvas,
                    width = surfaceWidth,
                    height = surfaceHeight,
                    phrase = currentPhrase,
                    slot = slot,
                    preferences = preferences,
                    showSafeZoneOverlay = false,
                    showSimulatedLauncher = false,
                    alphaMultiplier = transitionAlpha
                )
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
