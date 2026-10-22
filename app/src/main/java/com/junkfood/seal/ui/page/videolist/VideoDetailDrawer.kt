@file:OptIn(ExperimentalMaterial3Api::class)

package com.junkfood.seal.ui.page.videolist

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.LocalFixedColorRoles
import com.junkfood.seal.ui.component.ActionSheetPrimaryButton
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.ToastUtil

@Composable
fun VideoDetailDrawer(
    sheetState: SheetState,
    info: DownloadedVideoInfo,
    isFileAvailable: Boolean = true,
    onDismissRequest: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val view = LocalView.current
    val context = LocalContext.current

    val onReDownload =
        remember(info) {
            {
                context.startActivity(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        setPackage(context.packageName)
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, info.videoUrl)
                    }
                )
            }
        }

    val shareTitle = stringResource(id = R.string.share)
    with(info) {
        VideoDetailDrawerImpl(
            sheetState = sheetState,
            title = videoTitle,
            author = videoAuthor,
            url = videoUrl,
            isFileAvailable = isFileAvailable,
            onReDownload = onReDownload,
            onDismissRequest = onDismissRequest,
            onDelete = {
                view.slightHapticFeedback()
                onDismissRequest()
                onDelete()
            },
            onOpenLink = {
                view.slightHapticFeedback()
                onDismissRequest()
                uriHandler.openUri(videoUrl)
            },
            onOpenFile = {
                view.slightHapticFeedback()
                FileUtil.openFile(path = videoPath) {
                    ToastUtil.makeToast(context.getString(R.string.file_unavailable))
                }
            },
            onShareFile = {
                view.slightHapticFeedback()
                FileUtil.createIntentForSharingFile(videoPath)?.runCatching {
                    context.startActivity(Intent.createChooser(this, shareTitle))
                }
            },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrawerPreview() {
    SealTheme {
        VideoDetailDrawerImpl(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onReDownload = {},
        )
    }
}

/** Same layout as the download queue's action sheet: title block, then a row of icon buttons */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailDrawerImpl(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String = stringResource(id = R.string.video_title_sample_text),
    author: String = stringResource(id = R.string.video_creator_sample_text),
    url: String = "https://www.example.com",
    onDismissRequest: () -> Unit = {},
    isFileAvailable: Boolean = true,
    onReDownload: (() -> Unit) = {},
    onDelete: () -> Unit = {},
    onOpenLink: () -> Unit = {},
    onOpenFile: () -> Unit = {},
    onShareFile: () -> Unit = {},
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val fixedColors = LocalFixedColorRoles.current
    SealModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(),
        content = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                SelectionContainer {
                    Text(text = title, style = MaterialTheme.typography.titleSmall)
                }
                if (author != "playlist" && author != "null") {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            LazyRow(
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                if (isFileAvailable) {
                    item(key = "PlayButton") {
                        ActionSheetPrimaryButton(
                            containerColor = fixedColors.primaryFixed,
                            contentColor = fixedColors.onPrimaryFixedVariant,
                            imageVector = Icons.Rounded.PlayArrow,
                            text = stringResource(R.string.open_file),
                            onClick = onOpenFile,
                        )
                    }
                    item(key = "ShareButton") {
                        ActionSheetPrimaryButton(
                            containerColor = fixedColors.secondaryFixed,
                            contentColor = fixedColors.onSecondaryFixedVariant,
                            imageVector = Icons.Rounded.Share,
                            text = stringResource(R.string.share),
                            onClick = onShareFile,
                        )
                    }
                } else {
                    item(key = "RedownloadButton") {
                        ActionSheetPrimaryButton(
                            containerColor = fixedColors.tertiaryFixed,
                            contentColor = fixedColors.onTertiaryFixedVariant,
                            imageVector = Icons.Outlined.FileDownload,
                            text = stringResource(R.string.redownload),
                            onClick = onReDownload,
                        )
                    }
                }
                item(key = "DeleteButton") {
                    ActionSheetPrimaryButton(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        outlineColor = MaterialTheme.colorScheme.outlineVariant,
                        imageVector = Icons.Outlined.Delete,
                        text = stringResource(R.string.delete),
                        onClick = onDelete,
                    )
                }
                item(key = "CopyURLButton") {
                    ActionSheetPrimaryButton(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        outlineColor = MaterialTheme.colorScheme.outlineVariant,
                        imageVector = Icons.Outlined.ContentCopy,
                        text = stringResource(R.string.copy_link),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(url))
                            ToastUtil.makeToast(context.getString(R.string.link_copied))
                        },
                    )
                }
                item(key = "OpenVideoURLButton") {
                    ActionSheetPrimaryButton(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        outlineColor = MaterialTheme.colorScheme.outlineVariant,
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        text = stringResource(R.string.open_url),
                        onClick = onOpenLink,
                    )
                }
            }
        },
    )
}
