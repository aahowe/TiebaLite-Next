package com.huanchengfly.tieba.post.components.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.setPadding
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.utils.PermissionUtils

class RequestPermissionTipDialog(context: Context, permission: PermissionUtils.PermissionData) :
    AlertDialog(context, R.style.Dialog_RequestPermissionTip) {
    init {
        setCancelable(false)
        val permissionName = PermissionUtils.transformText(context, permission.permissions).first()
        setView(
            ComposeView(context).apply {
                setContent {
                    TiebaLiteTheme {
                        RequestPermissionTipContent(
                            title = context.getString(
                                R.string.title_request_permission_tip_dialog,
                                permissionName
                            ),
                            message = context.getString(
                                R.string.message_request_permission_tip_dialog,
                                permission.desc
                            )
                        )
                    }
                }
            }
        )
    }

    override fun show() {
        super.show()
        window?.let {
            it.attributes = it.attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                it.decorView.setPadding(16f.dpToPx())
            }
            it.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        }
    }
}

@Composable
private fun RequestPermissionTipContent(
    title: String,
    message: String,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.body2,
        )
    }
}
