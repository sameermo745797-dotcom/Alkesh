package com.example.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperHelper {
    private const val TAG = "WallpaperHelper"

    suspend fun setDeviceWallpaper(
        context: Context,
        target: WallpaperTarget
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.rudra_wallpaper_1080p)
                ?: return@withContext false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val whichFlag = when (target) {
                    WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                    WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                    WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, whichFlag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper", e)
            false
        }
    }
}

enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH
}
