package com.huanchengfly.tieba.post.components.dialogs

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme

class LoadingDialog(context: Context) : AlertDialog(context) {
    private var loadingTipText by mutableStateOf(context.getString(R.string.text_loading))

    init {
        setCancelable(false)
        setView(
            ComposeView(context).apply {
                setContent {
                    TiebaLiteTheme {
                        LoadingDialogContent(tipText = loadingTipText)
                    }
                }
            }
        )
    }

    fun setTipText(@StringRes resId: Int) {
        loadingTipText = context.getString(resId)
    }

    fun setTipText(text: String) {
        loadingTipText = text
    }
}

@Composable
private fun LoadingDialogContent(
    tipText: String,
) {
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator()
        Text(text = tipText)
    }
}
