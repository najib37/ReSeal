package com.junkfood.seal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for the pure parsing helpers: shared-text URL matching, file names and cookies.txt. */
class ParsingTest {

    @Test
    fun `finds every url in shared text`() {
        val text = "watch https://youtu.be/abc and https://example.com/v?id=1 later"
        assertEquals(
            listOf("https://youtu.be/abc", "https://example.com/v?id=1"),
            findURLsFromString(text),
        )
    }

    @Test
    fun `first match only returns one url`() {
        val text = "https://a.com/1 https://b.com/2"
        assertEquals(listOf("https://a.com/1"), findURLsFromString(text, firstMatchOnly = true))
    }

    @Test
    fun `no url returns empty list`() {
        assertTrue(findURLsFromString("nothing to see here").isEmpty())
    }

    @Test
    fun `sanitize replaces characters FAT32 rejects`() {
        assertEquals("a_b_c_d_e_f_g_h_i", FileUtil.sanitizeFileName("a\\b/c:d*e?f\"g<h>i"))
    }

    @Test
    fun `sanitize trims trailing dots and spaces and keeps unicode`() {
        assertEquals("日本語 title", FileUtil.sanitizeFileName("日本語 title. . "))
    }

    @Test
    fun `sanitize never returns an empty name`() {
        assertEquals("download", FileUtil.sanitizeFileName(" ..."))
    }

    @Test
    fun `parses netscape cookies including HttpOnly and empty values`() {
        val content =
            listOf(
                    "# Netscape HTTP Cookie File",
                    "",
                    ".youtube.com\tTRUE\t/\tTRUE\t1700000000\tSID\tabc",
                    "#HttpOnly_.youtube.com\tTRUE\t/\tFALSE\t0\tHSID\txyz",
                    "example.com\tFALSE\t/\tFALSE\t0\tempty\t",
                    "malformed line",
                )
                .joinToString("\r\n")

        val cookies = CookieUtil.parseNetscapeCookies(content)

        assertEquals(listOf("SID", "HSID", "empty"), cookies.map { it.name })
        assertEquals("abc", cookies[0].value)
        assertTrue(cookies[0].secure)
        assertEquals(1700000000L, cookies[0].expiry)
        assertEquals(".youtube.com", cookies[1].domain)
        assertFalse(cookies[1].secure)
        assertEquals("", cookies[2].value)
    }
}
