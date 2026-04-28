package com.huanchengfly.tieba.post.ui.page.settings.custom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.appPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val FONT_SCALE_MIN = 0.8f
private const val FONT_SCALE_MAX = 1.3f
private const val FONT_SCALE_STEP = 0.05f
private const val FONT_SCALE_STEPS = 9

private val SIZE_TEXT_MAPPING = mapOf(
    R.string.text_size_small to 0..1,
    R.string.text_size_little_small to 2..3,
    R.string.text_size_default to 4..4,
    R.string.text_size_little_large to 5..6,
    R.string.text_size_large to 7..8,
    R.string.text_size_very_large to 9..10
)

@Destination
@Composable
fun AppFontSizePage(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val appPreferences = context.appPreferences
    val initialFontScale = remember { appPreferences.fontScale }
    var fontScale by remember { mutableFloatStateOf(initialFontScale) }
    var progress by remember { mutableIntStateOf(fontScaleToProgress(initialFontScale)) }
    val scope = rememberCoroutineScope()

    fun finishPage() {
        if (initialFontScale != appPreferences.fontScale) {
            App.INSTANCE.toastShort(R.string.toast_after_change_will_restart)
            App.INSTANCE.removeAllActivity()
            App.INSTANCE.packageManager.getLaunchIntentForPackage(App.INSTANCE.packageName)?.let {
                App.INSTANCE.startActivity(it)
            }
        }
        navigator.navigateUp()
    }

    BackHandler(onBack = ::finishPage)

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_custom_font_size),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = ::finishPage)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ChatBubblePreview(
                        text = stringResource(id = R.string.bubble_want_change_font_size),
                        isRight = true,
                        fontScale = fontScale
                    )
                }
                item {
                    ChatBubblePreview(
                        text = stringResource(id = R.string.bubble_change_font_size),
                        isRight = false,
                        fontScale = fontScale
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                backgroundColor = ExtendedTheme.colors.card,
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = sizeTextResId(progress)),
                        color = ExtendedTheme.colors.primary,
                        fontSize = (16f * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Slider(
                        value = progress.toFloat(),
                        onValueChange = { value ->
                            val newProgress = value.roundToInt().coerceIn(0, FONT_SCALE_STEPS + 1)
                            progress = newProgress
                            fontScale = progressToFontScale(newProgress)
                            scope.launch {
                                appPreferences.fontScale = fontScale
                            }
                        },
                        valueRange = 0f..(FONT_SCALE_STEPS + 1).toFloat(),
                        steps = FONT_SCALE_STEPS,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubblePreview(
    text: String,
    isRight: Boolean,
    fontScale: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isRight) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isRight) ExtendedTheme.colors.accent else ExtendedTheme.colors.card,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = text,
                color = if (isRight) ExtendedTheme.colors.onAccent else ExtendedTheme.colors.text,
                fontSize = (15f * fontScale).sp,
                modifier = Modifier.widthIn(max = 250.dp)
            )
        }
    }
}

private fun fontScaleToProgress(fontScale: Float): Int =
    ((fontScale * 1000L - FONT_SCALE_MIN * 1000L).toInt()) / ((FONT_SCALE_STEP * 1000L).toInt())

private fun progressToFontScale(progress: Int): Float =
    FONT_SCALE_MIN + progress * FONT_SCALE_STEP

private fun sizeTextResId(progress: Int): Int =
    SIZE_TEXT_MAPPING.entries.firstOrNull { progress in it.value }?.key ?: R.string.text_size_default
