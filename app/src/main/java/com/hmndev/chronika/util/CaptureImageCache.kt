package com.hmndev.chronika.util

import android.graphics.Bitmap
import java.util.LinkedHashMap

/**
 * Small in-memory hand-off between the notification listener and the exporter.
 * Bitmaps are large, so only the most recent notifications keep their images.
 */
object CaptureImageCache {
    private const val MAX = 12
    private val cache = object : LinkedHashMap<String, List<Bitmap>>(MAX, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Bitmap>>?): Boolean {
            return size > MAX
        }
    }

    @Synchronized
    fun put(fingerprint: String, images: List<Bitmap>) {
        if (images.isEmpty()) return
        cache[fingerprint] = images
    }

    @Synchronized
    fun take(fingerprint: String): List<Bitmap> {
        return cache.remove(fingerprint).orEmpty()
    }
}
