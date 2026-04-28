package com.huanchengfly.tieba.post.ui.page.settings.theme

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.compose.rememberAsyncImageState
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.sketch.resize.Scale
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.LiteApi
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.transformations.SketchBlurTransformation
import com.huanchengfly.tieba.post.findActivity
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.CacheUtil
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.ImageCacheUtil
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import com.huanchengfly.tieba.post.utils.PickMediasRequest.ImageOnly
import com.huanchengfly.tieba.post.utils.PickMediasContract
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil.TRANSLUCENT_THEME_DARK
import com.huanchengfly.tieba.post.utils.ThemeUtil.TRANSLUCENT_THEME_LIGHT
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.extension.toArgbHexString
import com.huanchengfly.tieba.post.utils.requestPermission
import com.huanchengfly.tieba.post.utils.shouldUsePhotoPicker
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val RECOMMEND_WALLPAPERS_CACHE_ID = "recommend_wallpapers"
private const val ORIGIN_BACKGROUND_FILE_NAME = "origin_background.jpg"
private const val CROPPED_BACKGROUND_FILE_NAME = "cropped_background.jpg"
private const val SAVED_BACKGROUND_MAX_SIZE_KB = 512
private const val SAVED_BACKGROUND_INITIAL_QUALITY = 97
private const val ALPHA_MAX = 255
private const val BLUR_MAX = 100

private val presetThemeColors = listOf(
    AndroidColor.parseColor("#FF4477E0"),
    AndroidColor.parseColor("#FFFF9A9E"),
    AndroidColor.parseColor("#FFC51100"),
    AndroidColor.parseColor("#FF000000"),
    AndroidColor.parseColor("#FF512DA8"),
)

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun TranslucentThemePage(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val scope = rememberCoroutineScope()
    val preferences = context.appPreferences
    val systemUiController = rememberSystemUiController()
    val experimentalTipDialogState = rememberDialogState()
    val customColorDialogState = rememberDialogState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = androidx.compose.material.rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    var croppedImageUri by remember {
        mutableStateOf(
            File(context.filesDir, CROPPED_BACKGROUND_FILE_NAME)
                .takeIf { it.exists() }
                ?.let { Uri.fromFile(it) }
        )
    }
    var previewImageUri by remember { mutableStateOf(croppedImageUri) }
    var palette by remember { mutableStateOf<Palette?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var alpha by remember { mutableIntStateOf(preferences.translucentBackgroundAlpha) }
    var blur by remember { mutableIntStateOf(preferences.translucentBackgroundBlur) }
    var selectedTheme by remember { mutableIntStateOf(preferences.translucentBackgroundTheme) }
    var selectedPrimaryColor by remember {
        mutableIntStateOf(
            preferences.translucentPrimaryColor
                ?.runCatching { AndroidColor.parseColor(this) }
                ?.getOrNull()
                ?: AndroidColor.TRANSPARENT
        )
    }
    var customPrimaryColor by remember {
        mutableStateOf(
            Color(
                ThemeUtils.getColorById(
                    context,
                    R.color.default_color_primary
                )
            )
        )
    }
    var wallpapers by remember {
        mutableStateOf(readCachedWallpapers(context))
    }
    val colorChoices by remember {
        derivedStateOf { palette.toThemeColors() }
    }
    val hasPreviewImage by remember {
        derivedStateOf { croppedImageUri != null }
    }

    SideEffect {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = false)
        systemUiController.setNavigationBarColor(
            Color.Transparent,
            darkIcons = false,
            navigationBarContrastEnforced = false
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            ImageCacheUtil.clearImageMemoryCache(context)
            systemUiController.setStatusBarColor(
                Color.Transparent,
                darkIcons = ThemeUtil.isStatusBarFontDark()
            )
            systemUiController.setNavigationBarColor(
                Color.Transparent,
                darkIcons = ThemeUtil.isNavigationBarFontDark(),
                navigationBarContrastEnforced = false
            )
        }
    }

    fun refreshPreview(uri: Uri?) {
        previewImageUri = uri?.buildUpon()
            ?.appendQueryParameter("alpha", alpha.toString())
            ?.appendQueryParameter("blur", blur.toString())
            ?.appendQueryParameter("time", System.currentTimeMillis().toString())
            ?.build()
    }

    val uCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                croppedImageUri = UCrop.getOutput(result.data!!)
                refreshPreview(croppedImageUri)
            }
            UCrop.RESULT_ERROR -> UCrop.getError(result.data!!)?.printStackTrace()
        }
    }

    fun launchUCrop(sourceUri: Uri) {
        val currentActivity = activity ?: return
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                LoadRequest(context, sourceUri.toString()).execute()
            }
            if (result is LoadResult.Success) {
                val sourceFile = withContext(Dispatchers.IO) {
                    ImageUtil.bitmapToFile(
                        result.bitmap,
                        File(context.cacheDir, ORIGIN_BACKGROUND_FILE_NAME)
                    )
                }
                val destUri = Uri.fromFile(File(context.filesDir, CROPPED_BACKGROUND_FILE_NAME))
                val cropIntent = UCrop.of(Uri.fromFile(sourceFile), destUri)
                    .withAspectRatio(
                        App.ScreenInfo.EXACT_SCREEN_WIDTH.toFloat() / App.ScreenInfo.EXACT_SCREEN_HEIGHT.toFloat(),
                        1f
                    )
                    .withOptions(
                        UCrop.Options().apply {
                            setShowCropFrame(true)
                            setShowCropGrid(true)
                            setStatusBarColor(
                                ColorUtils.getDarkerColor(
                                    ThemeUtils.getColorByAttr(
                                        context,
                                        R.attr.colorPrimary
                                    )
                                )
                            )
                            setToolbarColor(ThemeUtils.getColorByAttr(context, R.attr.colorPrimary))
                            setToolbarWidgetColor(
                                ThemeUtils.getColorByAttr(
                                    context,
                                    R.attr.colorTextOnPrimary
                                )
                            )
                            setActiveControlsWidgetColor(
                                ThemeUtils.getColorByAttr(
                                    context,
                                    R.attr.colorAccent
                                )
                            )
                            setLogoColor(ThemeUtils.getColorByAttr(context, R.attr.colorPrimary))
                            setCompressionFormat(Bitmap.CompressFormat.JPEG)
                        }
                    )
                    .getIntent(currentActivity)
                isLoading = false
                uCropLauncher.launch(cropIntent)
            } else {
                isLoading = false
                context.toastShort(R.string.text_load_failed)
            }
        }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(PickMediasContract) { result ->
        result.uris.firstOrNull()?.let(::launchUCrop)
    }

    fun selectPicture() {
        if (shouldUsePhotoPicker()) {
            selectImageLauncher.launch(PickMediasRequest(mediaType = ImageOnly))
            return
        }
        context.requestPermission {
            unchecked = true
            permissions = when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> listOf(
                    PermissionUtils.READ_EXTERNAL_STORAGE,
                    PermissionUtils.WRITE_EXTERNAL_STORAGE
                )
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> listOf(
                    PermissionUtils.READ_EXTERNAL_STORAGE
                )
                else -> listOf(PermissionUtils.READ_MEDIA_IMAGES)
            }
            description = context.getString(R.string.tip_permission_storage)
            onGranted = {
                selectImageLauncher.launch(PickMediasRequest(mediaType = ImageOnly))
            }
            onDenied = {
                context.toastShort(R.string.toast_no_permission_insert_photo)
            }
        }
    }

    fun saveTheme() {
        val uri = croppedImageUri ?: return
        preferences.translucentBackgroundAlpha = alpha
        preferences.translucentBackgroundBlur = blur
        isLoading = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    preferences.translucentThemeBackgroundPath?.let { File(it).delete() }
                }
                DisplayRequest(context, uri.toString()) {
                    resizeScale(Scale.CENTER_CROP)
                    if (blur > 0) {
                        transformations(SketchBlurTransformation(blur))
                    }
                }.execute()
            }
            if (result is DisplayResult.Success) {
                val bitmap = ImageUtil.drawableToBitmap(result.drawable.apply { this.alpha = alpha })
                val file = withContext(Dispatchers.IO) {
                    ImageUtil.compressImage(
                        bitmap,
                        File(context.filesDir, "background_${System.currentTimeMillis()}.jpg"),
                        maxSizeKb = SAVED_BACKGROUND_MAX_SIZE_KB,
                        initialQuality = SAVED_BACKGROUND_INITIAL_QUALITY
                    )
                }
                preferences.translucentThemeBackgroundPath = file.absolutePath
                ThemeUtil.switchTheme(ThemeUtil.THEME_TRANSLUCENT, false)
                App.translucentBackground = null
                isLoading = false
                context.toastShort(R.string.toast_save_pic_success)
                navigator.navigateUp()
            } else {
                isLoading = false
                context.toastShort(R.string.text_load_failed)
            }
        }
    }

    LaunchedEffect(previewImageUri, alpha, blur) {
        if (previewImageUri == null) {
            palette = null
            return@LaunchedEffect
        }
        val result = withContext(Dispatchers.IO) {
            DisplayRequest(context, previewImageUri.toString()) {
                resizeScale(Scale.CENTER_CROP)
                if (blur > 0) {
                    transformations(SketchBlurTransformation(blur))
                }
            }.execute()
        }
        if (result is DisplayResult.Success) {
            result.drawable.alpha = alpha
            palette = Palette.from(ImageUtil.drawableToBitmap(result.drawable)).generate()
        }
    }

    LaunchedEffect(Unit) {
        refreshPreview(croppedImageUri)
        withContext(Dispatchers.IO) {
            LiteApi.instance.wallpapersAsync()
        }.doIfSuccess {
            CacheUtil.putCache(context, RECOMMEND_WALLPAPERS_CACHE_ID, it)
            wallpapers = it
        }
    }

    Dialog(
        dialogState = experimentalTipDialogState,
        title = { Text(text = stringResource(id = R.string.title_translucent_theme_experimental_feature)) },
        buttons = {
            DialogNegativeButton(text = stringResource(id = R.string.btn_close))
        }
    ) {
        Text(
            text = stringResource(id = R.string.tip_translucent_theme),
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }

    Dialog(
        dialogState = customColorDialogState,
        title = { Text(text = stringResource(id = R.string.title_color_picker_primary)) },
        buttons = {
            DialogPositiveButton(
                text = stringResource(id = R.string.button_finish),
                onClick = {
                    selectedPrimaryColor = customPrimaryColor.toArgb()
                    preferences.translucentPrimaryColor = customPrimaryColor.toArgb().toArgbHexString()
                    ThemeUtils.refreshUI(context)
                }
            )
            DialogNegativeButton(text = stringResource(id = R.string.button_cancel))
        }
    ) {
        ClassicColorPicker(
            color = HsvColor.from(customPrimaryColor),
            showAlphaBar = true,
            onColorChanged = { customPrimaryColor = it.toColor() },
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .height(280.dp)
                .fillMaxWidth()
        )
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 120.dp,
        sheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        sheetBackgroundColor = ExtendedTheme.colors.card,
        backgroundColor = Color.Black,
        sheetContent = {
            TranslucentThemeControls(
                selectedTheme = selectedTheme,
                colorChoices = colorChoices,
                selectedPrimaryColor = selectedPrimaryColor,
                alpha = alpha,
                blur = blur,
                wallpapers = wallpapers,
                hasPreviewImage = hasPreviewImage,
                onSelectPicture = ::selectPicture,
                onThemeChange = {
                    selectedTheme = it
                    preferences.translucentBackgroundTheme = it
                },
                onCustomColorClick = { customColorDialogState.show() },
                onPrimaryColorChange = {
                    selectedPrimaryColor = it
                    preferences.translucentPrimaryColor = it.toArgbHexString()
                    ThemeUtils.refreshUI(context)
                },
                onAlphaChangeFinished = {
                    alpha = it
                    refreshPreview(croppedImageUri)
                },
                onBlurChangeFinished = {
                    blur = it
                    refreshPreview(croppedImageUri)
                },
                onWallpaperClick = { launchUCrop(Uri.parse(it)) },
                onExperimentalTipClick = { experimentalTipDialogState.show() },
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TranslucentThemeBackground(
                imageUri = previewImageUri,
                alpha = alpha,
                blur = blur,
                modifier = Modifier.fillMaxSize()
            )
            if (!bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bottomSheetScaffoldState.bottomSheetState.progress)
                        .background(Color.Black.copy(alpha = 0.38f))
                        .clickable {
                            scope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
                        }
                )
            }
            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable {
                            scope.launch { bottomSheetScaffoldState.bottomSheetState.expand() }
                        }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            ) {}
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TranslucentIconButton(onClick = { navigator.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = ExtendedTheme.colors.text,
                    )
                }
                AnimatedVisibility(visible = hasPreviewImage) {
                    TranslucentIconButton(onClick = ::saveTheme) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(id = R.string.button_finish),
                            tint = ExtendedTheme.colors.text,
                        )
                    }
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun TranslucentThemeBackground(
    imageUri: Uri?,
    alpha: Int,
    blur: Int,
    modifier: Modifier = Modifier,
) {
    if (imageUri == null) {
        Box(modifier = modifier.background(Color.Black))
        return
    }
    val context = LocalContext.current
    val request = remember(imageUri, blur) {
        DisplayRequest(context, imageUri.toString()) {
            resizeScale(Scale.CENTER_CROP)
            if (blur > 0) {
                transformations(SketchBlurTransformation(blur))
            }
        }
    }
    val state = rememberAsyncImageState()
    AsyncImage(
        request = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        state = state,
        modifier = modifier.alpha(alpha / ALPHA_MAX.toFloat())
    )
}

@Composable
private fun TranslucentThemeControls(
    selectedTheme: Int,
    colorChoices: List<Int>,
    selectedPrimaryColor: Int,
    alpha: Int,
    blur: Int,
    wallpapers: List<String>,
    hasPreviewImage: Boolean,
    onSelectPicture: () -> Unit,
    onThemeChange: (Int) -> Unit,
    onCustomColorClick: () -> Unit,
    onPrimaryColorChange: (Int) -> Unit,
    onAlphaChangeFinished: (Int) -> Unit,
    onBlurChangeFinished: (Int) -> Unit,
    onWallpaperClick: (String) -> Unit,
    onExperimentalTipClick: () -> Unit,
) {
    var alphaValue by remember(alpha) { mutableStateOf(alpha.toFloat()) }
    var blurValue by remember(blur) { mutableStateOf(blur.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 132.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(ExtendedTheme.colors.text.copy(alpha = 0.2f))
                .align(Alignment.CenterHorizontally)
        )
        SectionTitle(
            text = stringResource(id = R.string.title_ui_settings),
            modifier = Modifier.padding(top = 16.dp)
        )
        TextButton(
            onClick = onSelectPicture,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = ExtendedTheme.colors.text.copy(alpha = 0.08f),
                contentColor = ExtendedTheme.colors.textSecondary,
            )
        ) {
            Text(text = stringResource(id = R.string.title_select_pic), fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThemeSelectButton(
                text = stringResource(id = R.string.dark_color),
                selected = selectedTheme == TRANSLUCENT_THEME_DARK,
                onClick = { onThemeChange(TRANSLUCENT_THEME_DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeSelectButton(
                text = stringResource(id = R.string.light_color),
                selected = selectedTheme == TRANSLUCENT_THEME_LIGHT,
                onClick = { onThemeChange(TRANSLUCENT_THEME_LIGHT) },
                modifier = Modifier.weight(1f)
            )
        }
        AnimatedVisibility(visible = hasPreviewImage) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                SectionTitle(text = stringResource(id = R.string.title_select_color))
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        CustomColorItem(onClick = onCustomColorClick)
                    }
                    items(colorChoices) { color ->
                        ThemeColorItem(
                            color = color,
                            selected = selectedPrimaryColor == color,
                            onClick = { onPrimaryColorChange(color) }
                        )
                    }
                }
            }
        }
        SectionTitle(
            text = stringResource(id = R.string.title_translucent_theme_alpha),
            modifier = Modifier.padding(top = 16.dp)
        )
        Slider(
            value = alphaValue,
            onValueChange = { alphaValue = it },
            onValueChangeFinished = { onAlphaChangeFinished(alphaValue.toInt().coerceIn(0, ALPHA_MAX)) },
            valueRange = 0f..ALPHA_MAX.toFloat(),
        )
        SectionTitle(
            text = stringResource(id = R.string.title_translucent_theme_blur),
            modifier = Modifier.padding(top = 16.dp)
        )
        Slider(
            value = blurValue,
            onValueChange = { blurValue = it },
            onValueChangeFinished = { onBlurChangeFinished(blurValue.toInt().coerceIn(0, BLUR_MAX)) },
            valueRange = 0f..BLUR_MAX.toFloat(),
        )
        if (wallpapers.isNotEmpty()) {
            SectionTitle(
                text = stringResource(id = R.string.title_translucent_theme_wallpapers),
                modifier = Modifier.padding(top = 16.dp)
            )
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(wallpapers) { wallpaper ->
                    WallpaperItem(
                        wallpaper = wallpaper,
                        onClick = { onWallpaperClick(wallpaper) }
                    )
                }
            }
        }
        ExperimentalTip(
            onClick = onExperimentalTipClick,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = ExtendedTheme.colors.text,
        style = MaterialTheme.typography.body2,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ThemeSelectButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = if (selected) ExtendedTheme.colors.accent else ExtendedTheme.colors.text.copy(alpha = 0.08f),
            contentColor = if (selected) ExtendedTheme.colors.onAccent else ExtendedTheme.colors.textSecondary,
        )
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
            )
        }
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CustomColorItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ExtendedTheme.colors.text.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Create,
            contentDescription = stringResource(id = R.string.title_color_picker_primary),
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ThemeColorItem(
    color: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .size(52.dp)
            .clickable(onClick = onClick)
    ) {
        Card(
            backgroundColor = Color(color),
            shape = RoundedCornerShape(10.dp),
            elevation = 0.dp,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopStart)
        ) {}
        if (selected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(ExtendedTheme.colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperItem(
    wallpaper: String,
    onClick: () -> Unit,
) {
    AsyncImage(
        imageUri = wallpaper,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(width = 60.dp, height = 120.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ExperimentalTip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ExtendedTheme.colors.text.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = stringResource(id = R.string.title_translucent_theme_experimental_feature),
            color = ExtendedTheme.colors.textSecondary,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        )
        Icon(
            imageVector = Icons.Rounded.Help,
            contentDescription = null,
            tint = ExtendedTheme.colors.textSecondary,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(12.dp)
        )
    }
}

@Composable
private fun TranslucentIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(4.dp)
    ) {
        content()
    }
}

private fun readCachedWallpapers(context: android.content.Context): List<String> {
    val cache = CacheUtil.getCache(context, RECOMMEND_WALLPAPERS_CACHE_ID, List::class.java)
    return cache?.filterIsInstance<String>().orEmpty()
}

private fun Palette?.toThemeColors(): List<Int> {
    val paletteColors = this?.let {
        listOf(
            it.getVibrantColor(AndroidColor.TRANSPARENT),
            it.getMutedColor(AndroidColor.TRANSPARENT),
            it.getDominantColor(AndroidColor.TRANSPARENT),
        ).filter { color -> color != AndroidColor.TRANSPARENT }
    }.orEmpty()
    return paletteColors + presetThemeColors
}
