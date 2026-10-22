package com.junkfood.seal.ui.page.settings.network

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.USER_AGENT_STRING

data class Cookie(
    val domain: String = "",
    val name: String = "",
    val value: String = "",
    val includeSubdomains: Boolean = true,
    val path: String = "/",
    val secure: Boolean = true,
    val expiry: Long = 0L,
) {
    // Plain join, not connectWithDelimiter: an empty value must still be its own (7th) column
    fun toNetscapeCookieString(): String {
        return listOf(
                domain,
                includeSubdomains.toString().uppercase(),
                path,
                secure.toString().uppercase(),
                expiry.toString(),
                name,
                value,
            )
            .joinToString(separator = "\u0009")
    }
}

/** Sign-in browser for a cookie profile; the cookies it collects are exported when it closes. */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewPage(cookiesViewModel: CookiesViewModel, onDismissRequest: () -> Unit) {

    val state by cookiesViewModel.stateFlow.collectAsStateWithLifecycle()
    val websiteUrl = state.editingCookieProfile.url

    var pageTitle by remember { mutableStateOf("") }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Back navigates inside the site first, like a browser
    BackHandler(enabled = canGoBack) { webView?.goBack() }

    DisposableEffect(Unit) { onDispose { webView?.destroy() } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(pageTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            stringResource(id = androidx.appcompat.R.string.abc_action_mode_done),
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
            )
        },
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.run {
                        javaScriptCanOpenWindowsAutomatically = true
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        // A sign-in browser only needs the web; keep it off local files
                        allowFileAccess = false
                        allowContentAccess = false
                        USER_AGENT_STRING.updateString(userAgentString)
                    }
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webViewClient =
                        object : WebViewClient() {
                            // Only follow http(s); app-scheme redirects (intent:, market:) stay
                            // put
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean = request?.url?.scheme?.startsWith("http") != true

                            override fun doUpdateVisitedHistory(
                                view: WebView,
                                url: String?,
                                isReload: Boolean,
                            ) {
                                canGoBack = view.canGoBack()
                            }
                        }
                    webChromeClient =
                        object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                pageTitle = title.orEmpty()
                            }
                        }
                    webView = this
                    loadUrl(websiteUrl)
                }
            },
        )
    }
}
