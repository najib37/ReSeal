package com.junkfood.seal.util

import android.content.Context
import java.io.File

/**
 * Keeps the raw yt-dlp info JSON from a probe (format page, preset/fast task fetch) so the download
 * can run with `--load-info-json` instead of extracting the same video again. Keyed by extractor +
 * video id: playlist entries share the playlist URL, ids don't collide.
 *
 * Entries expire because format URLs do (YouTube's last ~6h); a failed download that used a cached
 * entry is retried from the URL by the caller.
 */
object InfoJsonCache {
    private const val MAX_AGE_MS = 2 * 60 * 60 * 1000L
    private const val MAX_ENTRIES = 8

    private data class Entry(val json: String, val createdAt: Long)

    // Access-ordered map = LRU; the JSON for one video can be a few hundred KB
    private val entries = LinkedHashMap<String, Entry>(MAX_ENTRIES, 0.75f, true)

    private fun keyOf(info: VideoInfo): String? =
        info.id.takeIf { it.isNotEmpty() }?.let { "${info.extractorKey}:$it" }

    @Synchronized
    fun put(info: VideoInfo, json: String) {
        val key = keyOf(info) ?: return
        entries[key] = Entry(json, System.currentTimeMillis())
        while (entries.size > MAX_ENTRIES) entries.remove(entries.keys.first())
    }

    @Synchronized
    fun remove(info: VideoInfo) {
        keyOf(info)?.let { entries.remove(it) }
    }

    @Synchronized
    private fun freshEntry(info: VideoInfo): Pair<String, Entry>? {
        val key = keyOf(info) ?: return null
        val entry = entries[key] ?: return null
        if (System.currentTimeMillis() - entry.createdAt > MAX_AGE_MS) {
            entries.remove(key)
            return null
        }
        return key to entry
    }

    /**
     * Writes a fresh cached entry for [info] to a file yt-dlp can load, or returns null. The file
     * is per task: two tasks for the same video (e.g. video + audio-only) must not delete each
     * other's copy.
     */
    fun fileFor(context: Context, info: VideoInfo, taskId: String): File? {
        val (key, entry) = freshEntry(info) ?: return null
        val dir = File(context.cacheDir, "info-json").apply { mkdirs() }
        val name = "${key}_${taskId.hashCode().toUInt().toString(16)}".replace(UNSAFE_CHARS, "_")
        return File(dir, "$name.info.json").apply { writeText(entry.json) }
    }

    private val UNSAFE_CHARS = Regex("[^A-Za-z0-9._-]")
}
