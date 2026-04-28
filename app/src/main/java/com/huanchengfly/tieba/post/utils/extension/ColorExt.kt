package com.huanchengfly.tieba.post.utils.extension

import android.graphics.Color as AndroidColor
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun Color.toHexString(): String {
    return "#${Integer.toHexString(toArgb()).substring(2)}"
}

fun @receiver:ColorInt Int.toArgbHexString(): String = buildString {
    append('#')
    append(AndroidColor.alpha(this@toArgbHexString).toTwoDigitHex())
    append(AndroidColor.red(this@toArgbHexString).toTwoDigitHex())
    append(AndroidColor.green(this@toArgbHexString).toTwoDigitHex())
    append(AndroidColor.blue(this@toArgbHexString).toTwoDigitHex())
}

private fun Int.toTwoDigitHex(): String =
    toString(16).padStart(2, '0').take(2)
