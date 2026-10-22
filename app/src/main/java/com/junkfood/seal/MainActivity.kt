package com.junkfood.seal

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.page.AppEntry
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.NOTIFICATION_PERMISSION_REQUESTED
import com.junkfood.seal.util.getWebUrl
import com.junkfood.seal.util.matchUrlsFromSharedText
import com.junkfood.seal.util.setLanguage
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

class MainActivity : AppCompatActivity() {
    private val dialogViewModel: DownloadDialogViewModel by viewModel()
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            NOTIFICATION_PERMISSION_REQUESTED.updateBoolean(true)
        }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < 33) {
            setLanguage(PreferenceUtil.getLocaleFromPreference())
        }
        enableEdgeToEdge()

        context = this.baseContext
        setContent {
            KoinContext {
                val windowSizeClass = calculateWindowSizeClass(this)
                SettingsProvider(windowWidthSizeClass = windowSizeClass.widthSizeClass) {
                    SealTheme(
                        darkTheme = LocalDarkTheme.current.isDarkTheme(),
                        isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    ) {
                        AppEntry(dialogViewModel = dialogViewModel)
                    }
                }
            }
        }

        // Cold start from a share or link: onNewIntent only covers an already-running activity.
        // Skipped on recreation (rotation) so the sheet doesn't reopen.
        if (savedInstanceState == null) handleSharedIntent(intent)

        requestNotificationPermissionIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }

    private fun handleSharedIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_VIEW) return
        val urls = intent.getSharedURLs()
        if (urls.isNullOrEmpty()) {
            Log.e(TAG, getString(R.string.share_fail_msg))
            return
        }
        dialogViewModel.postAction(DownloadDialogViewModel.Action.ShowSheet(urls))
    }

    private fun Intent.getSharedURLs(): List<String>? {
        val intent = this

        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.getWebUrl()?.let {
                    return listOf(it)
                }
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedContent ->
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    matchUrlsFromSharedText(sharedContent)
                }
            }

            else -> {
                null
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (NOTIFICATION_PERMISSION_REQUESTED.getBoolean()) return
        if (NotificationUtil.areNotificationsEnabled()) return

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
