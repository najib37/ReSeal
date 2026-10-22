package com.junkfood.seal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.junkfood.seal.util.getWebUrl
import com.junkfood.seal.util.matchUrlsFromSharedText

private const val TAG = "QuickDownloadActivity"

/**
 * Share/"open with" target. It used to show its own overlay sheet, which let downloads start
 * while the user stayed in the other app. Now it hands the links to [MainActivity] so every share
 * opens Seal and the configure sheet or download queue is visible.
 */
class QuickDownloadActivity : ComponentActivity() {

    private fun Intent.getSharedURLs(): List<String>? =
        when (action) {
            Intent.ACTION_VIEW -> getWebUrl()?.let { listOf(it) }
            Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT)?.let(::matchUrlsFromSharedText)
            else -> null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val urls = intent.getSharedURLs()

        if (urls.isNullOrEmpty()) {
            Log.e(TAG, getString(R.string.share_fail_msg))
        } else {
            // Forward as validated text; MainActivity re-extracts the http(s) URLs from it
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, urls.joinToString("\n"))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
        finish()
    }
}
