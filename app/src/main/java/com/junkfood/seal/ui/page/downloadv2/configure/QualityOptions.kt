package com.junkfood.seal.ui.page.downloadv2.configure

import com.junkfood.seal.util.Format
import kotlin.math.roundToInt

/**
 * One stop on a simple-mode quality slider. [index] points into the list the options were built
 * from, so a pick maps straight back onto the format page's existing selection state.
 */
data class QualityOption(val index: Int, val format: Format, val label: String)

/**
 * One option per resolution, lowest first. At each resolution the most compatible format wins:
 * MP4 with AVC (H.264) video, then any MP4, then the highest bitrate. Callers pass formats of a
 * single kind (video-only, or video+audio when a site has no video-only formats) so the slider
 * never mixes muxed and video-only formats.
 */
fun videoQualityOptions(formats: List<Format>): List<QualityOption> =
    formats
        .withIndex()
        .filter { (_, f) -> (f.height ?: 0.0) > 0 }
        .groupBy { (_, f) -> f.height!!.roundToInt() }
        .toSortedMap()
        .map { (height, candidates) ->
            val (index, best) =
                candidates.maxWith(
                    compareBy<IndexedValue<Format>>(
                        { it.value.isMp4() && it.value.vcodec.orEmpty().startsWith("avc1") },
                        { it.value.isMp4() },
                        { it.value.tbr ?: it.value.vbr ?: 0.0 },
                    )
                )
            val fps = best.fps?.roundToInt() ?: 0
            QualityOption(index, best, "${height}p" + if (fps > 30) "$fps" else "")
        }

/**
 * One option per audio bitrate level (rounded to 16 kbps), lowest first. M4A/AAC is preferred
 * because it merges cleanly into MP4; dynamic-range-compressed ("drc") duplicates are skipped
 * when a normal variant exists.
 */
fun audioQualityOptions(formats: List<Format>): List<QualityOption> {
    val indexed = formats.withIndex().filter { (_, f) -> (f.abr ?: f.tbr ?: 0.0) > 0 }
    val preferred = indexed.filterNot { it.value.formatId.orEmpty().contains("drc") }
    return preferred
        .ifEmpty { indexed }
        .groupBy { (_, f) -> ((f.abr ?: f.tbr!!) / 16).roundToInt() }
        .toSortedMap()
        .map { (_, candidates) ->
            val (index, best) =
                candidates.maxWith(
                    compareBy<IndexedValue<Format>>(
                        { it.value.acodec.orEmpty().startsWith("mp4a") },
                        { it.value.abr ?: it.value.tbr ?: 0.0 },
                    )
                )
            QualityOption(index, best, "${(best.abr ?: best.tbr ?: 0.0).roundToInt()} kbps")
        }
}

private fun Format.isMp4() = ext == "mp4" || ext == "m4a"
