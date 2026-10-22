package com.junkfood.seal.util

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READONLY
import android.net.Uri
import android.webkit.CookieManager
import androidx.annotation.CheckResult
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.ui.page.settings.network.Cookie
import com.junkfood.seal.util.PreferenceUtil.COOKIE_HEADER
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Cookie profiles: the WebView cookie jar, Netscape cookies files and the database. */
object CookieUtil {

    private object CookieScheme {
        const val NAME = "name"
        const val VALUE = "value"
        const val SECURE = "is_secure"
        const val EXPIRY = "expires_utc"
        const val HOST = "host_key"
        const val PATH = "path"
    }

    // Chromium stores expires_utc as microseconds since 1601-01-01; 0 means a session cookie
    private const val CHROMIUM_EPOCH_OFFSET_SECONDS = 11_644_473_600L

    suspend fun getCookiesContentFromDatabase(): Result<String> = runCatching {
        val profiles = DatabaseUtil.getCookieProfileList()
        if (profiles.isEmpty()) throw Exception("There is no cookies in the database!")
        profiles
            .joinToString(separator = "") { it.content }
            .ifEmpty { throw Exception("There is no cookies in the database!") }
    }

    /**
     * Exports the WebView cookie jar as a Netscape cookies file. Reads Chromium's private Cookies
     * DB (the only source with domain/path/expiry/secure); if that fails or is empty, e.g. after a
     * WebView schema change, falls back to the public [CookieManager] for [url], which only gives
     * name=value pairs, so they are written as session cookies for that host.
     */
    @CheckResult
    fun extractCookiesFromWebView(url: String? = null): Result<String> =
        readWebViewCookieDb().recoverCatching { dbError ->
            val pageUrl = url?.takeIf { it.isNotBlank() } ?: throw dbError
            cookiesFromCookieManager(pageUrl).ifEmpty { throw dbError }.toCookiesFileContent()
        }

    private fun cookiesFromCookieManager(url: String): List<Cookie> {
        val uri = Uri.parse(url)
        val host = uri.host ?: return emptyList()
        val header = CookieManager.getInstance().getCookie(url) ?: return emptyList()
        return header.split(';').mapNotNull { pair ->
            val name = pair.substringBefore('=').trim()
            if (name.isEmpty()) return@mapNotNull null
            Cookie(
                domain = host,
                name = name,
                value = pair.substringAfter('=', "").trim(),
                includeSubdomains = false,
                secure = uri.scheme == "https",
            )
        }
    }

    private fun readWebViewCookieDb(): Result<String> = runCatching {
        CookieManager.getInstance().flush()
        SQLiteDatabase.openDatabase(
                context.dataDir.resolve("app_webview/Default/Cookies").absolutePath,
                null,
                OPEN_READONLY,
            )
            .use { db ->
                val projection =
                    arrayOf(
                        CookieScheme.HOST,
                        CookieScheme.EXPIRY,
                        CookieScheme.PATH,
                        CookieScheme.NAME,
                        CookieScheme.VALUE,
                        CookieScheme.SECURE,
                    )
                val cookieList = mutableListOf<Cookie>()
                db.query("cookies", projection, null, null, null, null, null).use { cursor ->
                    with(cursor) {
                        while (moveToNext()) {
                            val hostKey = getString(getColumnIndexOrThrow(CookieScheme.HOST))
                            // Chromium marks domain cookies with a leading dot; others are
                            // host-only
                            cookieList.add(
                                Cookie(
                                    domain = hostKey,
                                    name = getString(getColumnIndexOrThrow(CookieScheme.NAME)),
                                    value = getString(getColumnIndexOrThrow(CookieScheme.VALUE)),
                                    includeSubdomains = hostKey.startsWith('.'),
                                    path = getString(getColumnIndexOrThrow(CookieScheme.PATH)),
                                    secure =
                                        getLong(getColumnIndexOrThrow(CookieScheme.SECURE)) == 1L,
                                    expiry =
                                        chromiumTimeToUnixSeconds(
                                            getLong(getColumnIndexOrThrow(CookieScheme.EXPIRY))
                                        ),
                                )
                            )
                        }
                    }
                }
                if (cookieList.isEmpty()) throw Exception("No cookies found in WebView")
                cookieList.toCookiesFileContent()
            }
    }

    private fun chromiumTimeToUnixSeconds(value: Long): Long =
        if (value <= 0) 0 else (value / 1_000_000 - CHROMIUM_EPOCH_OFFSET_SECONDS).coerceAtLeast(0)

    // Profiles saved before the conversion fix hold raw Chromium times (~1.3e16); Unix seconds
    // won't reach 1e13 for ~300,000 years
    private fun normalizeExpiry(value: Long): Long =
        if (value > 10_000_000_000_000L) chromiumTimeToUnixSeconds(value) else value

    suspend fun syncCookiesToWebView() {
        withContext(Dispatchers.IO) {
            DatabaseUtil.getCookieProfileList().forEach { profile ->
                parseNetscapeCookies(profile.content).forEach { cookie ->
                    val domain = cookie.domain.removePrefix(".")
                    val url = "https://$domain${cookie.path}"
                    CookieManager.getInstance().setCookie(url, cookie.toSetCookieHeader())
                }
            }
            CookieManager.getInstance().flush()
        }
    }

    private fun Cookie.toSetCookieHeader(): String {
        val expiresPart = if (expiry > 0) "; Expires=${formatHttpDate(expiry)}" else ""
        val securePart = if (secure) "; Secure" else ""
        // Without a Domain attribute the cookie stays host-only, as it was in the source jar
        val domainPart = if (includeSubdomains) "; Domain=$domain" else ""
        return "$name=$value$domainPart; Path=$path$expiresPart$securePart"
    }

    private fun formatHttpDate(unixSeconds: Long): String =
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("GMT") }
            .format(Date(unixSeconds * 1000L))

    fun List<Cookie>.toCookiesFileContent(): String =
        this.fold(StringBuilder(COOKIE_HEADER)) { acc, cookie ->
                acc.append(cookie.toNetscapeCookieString()).append("\n")
            }
            .toString()

    fun parseNetscapeCookies(content: String): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        content.lines().forEach { line ->
            // Only strip line endings: a cookie with an empty value ends in a tab that must survive
            val trimmed = line.trimEnd('\r', '\n').trimStart()
            if (trimmed.isBlank()) return@forEach
            val processed =
                if (trimmed.startsWith("#HttpOnly_")) trimmed.removePrefix("#HttpOnly_")
                else if (trimmed.startsWith("#")) return@forEach else trimmed
            val fields = processed.split("\t")
            if (fields.size >= 7) {
                cookies.add(
                    Cookie(
                        domain = fields[0],
                        includeSubdomains = fields[1].uppercase() == "TRUE",
                        path = fields[2],
                        secure = fields[3].uppercase() == "TRUE",
                        expiry = normalizeExpiry(fields[4].toLongOrNull() ?: 0L),
                        name = fields[5],
                        value = fields[6],
                    )
                )
            }
        }
        return cookies
    }
}
