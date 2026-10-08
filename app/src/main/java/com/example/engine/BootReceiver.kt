package com.example.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.WallpaperPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            // Re-sync preferences or trigger initial wallpaper setup check
            WallpaperPreferences.getInstance(context)
        }
    }
}
