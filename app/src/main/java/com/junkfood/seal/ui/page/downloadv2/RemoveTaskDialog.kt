package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.CheckBoxItem
import com.junkfood.seal.ui.component.SealDialog

/**
 * Confirms removing a task from the queue. For a completed task the checkbox deletes the
 * downloaded file; otherwise it deletes the partial/temporary files.
 */
@Composable
fun RemoveTaskDialog(
    title: String,
    isCompleted: Boolean,
    onRemoveConfirm: (deleteFiles: Boolean) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    // Partial files are useless once the task is gone, so default to cleaning them up
    var deleteFiles by remember { mutableStateOf(!isCompleted) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.remove)) },
        icon = { Icon(Icons.Outlined.Delete, null) },
        text = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    text = stringResource(R.string.remove_task_msg).format(title),
                )
                CheckBoxItem(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text =
                        stringResource(
                            if (isCompleted) R.string.delete_file else R.string.delete_temp_files
                        ),
                    checked = deleteFiles,
                    onValueChange = { deleteFiles = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onRemoveConfirm(deleteFiles)
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(text = stringResource(R.string.dismiss)) }
        },
    )
}
