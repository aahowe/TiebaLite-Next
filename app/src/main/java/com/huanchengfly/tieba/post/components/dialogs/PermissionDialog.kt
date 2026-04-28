package com.huanchengfly.tieba.post.components.dialogs

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.interfaces.OnDeniedCallback
import com.huanchengfly.tieba.post.interfaces.OnGrantedCallback
import com.huanchengfly.tieba.post.models.PermissionBean
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil

class PermissionDialog(
    context: Context,
    private var permissionBean: PermissionBean,
) : AlertDialog(context, false, null) {
    private var isForever by mutableStateOf(false)
    private var onGrantedCallback: OnGrantedCallback? = null
    private var onDeniedCallback: OnDeniedCallback? = null

    init {
        setView(
            ComposeView(context).apply {
                setContent {
                    TiebaLiteTheme {
                        PermissionDialogContent(
                            title = permissionBean.title,
                            icon = permissionBean.icon,
                            isForever = isForever,
                            onForeverChange = { isForever = it },
                            onAllow = ::allow,
                            onDeny = ::deny,
                        )
                    }
                }
            }
        )
    }

    fun getPermissionBean(): PermissionBean = permissionBean

    fun setPermissionBean(permissionBean: PermissionBean): PermissionDialog {
        this.permissionBean = permissionBean
        return this
    }

    fun getOnGrantedCallback(): OnGrantedCallback? = onGrantedCallback

    fun setOnGrantedCallback(onGrantedCallback: OnGrantedCallback?): PermissionDialog {
        this.onGrantedCallback = onGrantedCallback
        return this
    }

    fun getOnDeniedCallback(): OnDeniedCallback? = onDeniedCallback

    fun setOnDeniedCallback(onDeniedCallback: OnDeniedCallback?): PermissionDialog {
        this.onDeniedCallback = onDeniedCallback
        return this
    }

    @SuppressLint("ApplySharedPref")
    private fun allow() {
        onGrantedCallback?.onGranted(isForever)
        if (isForever) {
            SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_PERMISSION)
                .edit()
                .putInt(permissionKey, STATE_ALLOW)
                .commit()
        }
        dismiss()
    }

    @SuppressLint("ApplySharedPref")
    private fun deny() {
        onDeniedCallback?.onDenied(isForever)
        if (isForever) {
            SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_PERMISSION)
                .edit()
                .putInt(permissionKey, STATE_DENIED)
                .commit()
        }
        dismiss()
    }

    override fun show() {
        when (SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_PERMISSION).getInt(permissionKey, STATE_UNSET)) {
            STATE_ALLOW -> onGrantedCallback?.onGranted(true)
            STATE_DENIED -> onDeniedCallback?.onDenied(true)
            else -> super.show()
        }
    }

    private val permissionKey: String
        get() = "${permissionBean.data}_${permissionBean.id}"

    object CustomPermission {
        const val PERMISSION_LOCATION = 0
        const val PERMISSION_START_APP = 1
        const val PERMISSION_CLIPBOARD_COPY = 2
    }

    companion object {
        const val STATE_DENIED = 2
        const val STATE_ALLOW = 1
        const val STATE_UNSET = 0
    }
}

@Composable
private fun PermissionDialogContent(
    title: String,
    @DrawableRes icon: Int,
    isForever: Boolean,
    onForeverChange: (Boolean) -> Unit,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = ExtendedTheme.colors.primary,
                modifier = Modifier.size(36.dp),
            )
            Text(
                text = title,
                color = ExtendedTheme.colors.text,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isForever,
                    onCheckedChange = onForeverChange,
                )
                Text(
                    text = stringResource(id = R.string.title_not_ask),
                    color = ExtendedTheme.colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDeny) {
                Text(text = stringResource(id = R.string.button_denied))
            }
            Button(onClick = onAllow) {
                Text(text = stringResource(id = R.string.button_allow))
            }
        }
    }
}
