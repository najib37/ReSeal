package com.junkfood.seal.util

import com.junkfood.seal.ui.page.downloadv2.configure.audioQualityOptions
import com.junkfood.seal.ui.page.downloadv2.configure.videoQualityOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QualityOptionsTest {

    private fun video(id: String, height: Double, ext: String, vcodec: String, tbr: Double) =
        Format(formatId = id, height = height, ext = ext, vcodec = vcodec, acodec = "none", tbr = tbr)

    private fun audio(id: String, abr: Double, ext: String, acodec: String) =
        Format(formatId = id, abr = abr, ext = ext, acodec = acodec, vcodec = "none")

    @Test
    fun `one option per resolution, lowest first, preferring mp4 avc`() {
        val formats =
            listOf(
                video("248", 1080.0, "webm", "vp9", 3000.0),
                video("137", 1080.0, "mp4", "avc1.640028", 2500.0),
                video("136", 720.0, "mp4", "avc1.4d401f", 1200.0),
                video("247", 720.0, "webm", "vp9", 1500.0),
            )
        val options = videoQualityOptions(formats)

        assertEquals(listOf("720p", "1080p"), options.map { it.label })
        assertEquals(listOf("136", "137"), options.map { it.format.formatId })
        // index maps back into the source list
        assertEquals(listOf(2, 1), options.map { it.index })
    }

    @Test
    fun `formats without a height are ignored`() {
        val options = videoQualityOptions(listOf(Format(formatId = "x", vcodec = "avc1")))
        assertTrue(options.isEmpty())
    }

    @Test
    fun `audio prefers m4a and skips drc duplicates`() {
        val formats =
            listOf(
                audio("251", 130.0, "webm", "opus"),
                audio("140", 129.0, "m4a", "mp4a.40.2"),
                audio("140-drc", 129.0, "m4a", "mp4a.40.2"),
                audio("249", 48.0, "webm", "opus"),
            )
        val options = audioQualityOptions(formats)

        assertEquals(listOf("249", "140"), options.map { it.format.formatId })
        assertEquals(listOf("48 kbps", "129 kbps"), options.map { it.label })
    }
}
