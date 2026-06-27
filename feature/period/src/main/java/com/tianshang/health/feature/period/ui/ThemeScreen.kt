package com.tianshang.health.feature.period.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.theme.HslColorEngine
import com.tianshang.health.core.common.ui.theme.WallpaperManager
import com.tianshang.health.feature.period.viewmodel.ThemeViewModel

private val GRADIENT_PRESET_COLORS = listOf(
    Color(0xFFB3E5FC) to Color(0xFFE1BEE7),
    Color(0xFFFFF3E0) to Color(0xFFFFE0B2),
    Color(0xFFE3F2FD) to Color(0xFFE8F5E9),
    Color(0xFFF1F8E9) to Color(0xFFE0F2F1),
    Color(0xFFFCE4EC) to Color(0xFFF3E5F5),
    Color(0xFFFFF8E1) to Color(0xFFE0F7FA)
)

@Composable
fun ThemeScreen(
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val hue by viewModel.hue.collectAsState()
    val saturation by viewModel.saturation.collectAsState()
    val lightness by viewModel.lightness.collectAsState()
    val useCustomTheme by viewModel.useCustomTheme.collectAsState()
    val wallpaperConfig by viewModel.wallpaperConfig.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.saveError.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val previewColor = HslColorEngine.toColor(hue, saturation, lightness)

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                viewModel.setWallpaperImage(uri)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            cropImageLauncher.launch(
                CropImageContractOptions(
                    uri = it,
                    cropImageOptions = CropImageOptions()
                )
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_customization),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.custom_theme),
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = useCustomTheme,
                    onCheckedChange = { viewModel.toggleCustomTheme(it) }
                )
            }

            if (useCustomTheme) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.color_preview),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(previewColor)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.hsl_format,
                                hue.toInt(),
                                (saturation * 100).toInt(),
                                (lightness * 100).toInt()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HslSlider(stringResource(R.string.hue), hue, 0f..360f) { viewModel.setHue(it) }
                Spacer(modifier = Modifier.height(12.dp))
                HslSlider(stringResource(R.string.saturation), saturation, 0f..1f) { viewModel.setSaturation(it) }
                Spacer(modifier = Modifier.height(12.dp))
                HslSlider(stringResource(R.string.lightness), lightness, 0f..1f) { viewModel.setLightness(it) }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.preset_colors),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                HslColorEngine.PRESETS.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { preset ->
                            Button(
                                onClick = { viewModel.applyPreset(preset.hue, preset.saturation, preset.lightness) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(preset.labelResId))
                            }
                        }
                        if (row.size < 3) {
                            Spacer(modifier = Modifier.weight(3 - row.size.toFloat()))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.wallpaper_customize),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.wallpaper_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WallpaperTypeButton(
                    label = stringResource(R.string.wallpaper_none),
                    selected = wallpaperConfig.type == WallpaperManager.WallpaperType.NONE,
                    onClick = { viewModel.setWallpaperType(WallpaperManager.WallpaperType.NONE) },
                    modifier = Modifier.weight(1f)
                )
                WallpaperTypeButton(
                    label = stringResource(R.string.wallpaper_presets),
                    selected = wallpaperConfig.type == WallpaperManager.WallpaperType.PRESET,
                    onClick = { viewModel.setWallpaperType(WallpaperManager.WallpaperType.PRESET) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WallpaperTypeButton(
                    label = stringResource(R.string.wallpaper_gradient),
                    selected = wallpaperConfig.type == WallpaperManager.WallpaperType.GRADIENT,
                    onClick = { viewModel.setWallpaperType(WallpaperManager.WallpaperType.GRADIENT) },
                    modifier = Modifier.weight(1f)
                )
                WallpaperTypeButton(
                    label = stringResource(R.string.wallpaper_from_gallery),
                    selected = wallpaperConfig.type == WallpaperManager.WallpaperType.IMAGE,
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (wallpaperConfig.type) {
                WallpaperManager.WallpaperType.PRESET -> {
                    WallpaperPresetGrid(viewModel = viewModel)
                }
                WallpaperManager.WallpaperType.GRADIENT -> {
                    WallpaperGradientEditor(viewModel = viewModel)
                }
                WallpaperManager.WallpaperType.IMAGE -> {
                    WallpaperImageInfo(viewModel = viewModel)
                }
                WallpaperManager.WallpaperType.NONE -> { }
            }

            if (wallpaperConfig.type != WallpaperManager.WallpaperType.NONE) {
                Spacer(modifier = Modifier.height(16.dp))

                WallpaperSlider(
                    label = stringResource(R.string.wallpaper_opacity),
                    value = wallpaperConfig.opacity,
                    valueRange = 0.0f..1.0f,
                    displaySuffix = "%",
                    displayMultiplier = 100f
                ) { viewModel.setWallpaperOpacity(it) }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.removeWallpaper() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.wallpaper_remove))
                }
            }
        }
    }
}

@Composable
private fun WallpaperTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = if (selected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WallpaperPresetGrid(viewModel: ThemeViewModel) {
    Text(
        text = stringResource(R.string.wallpaper_presets),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    WallpaperManager.PRESETS.chunked(3).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { preset ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            brush = WallpaperManager.createPresetBrush(preset),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { viewModel.setWallpaperPreset(WallpaperManager.PRESETS.indexOf(preset)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(preset.nameResId),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            if (row.size < 3) {
                Spacer(modifier = Modifier.weight(3 - row.size.toFloat()))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WallpaperGradientEditor(viewModel: ThemeViewModel) {
    val config by viewModel.wallpaperConfig.collectAsState()

    Text(
        text = stringResource(R.string.wallpaper_gradient),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.wallpaper_gradient_start),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        GradientColorRow(
            selectedColor = config.gradientStartColor,
            onColorSelected = { viewModel.setGradientStartColor(it) }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.wallpaper_gradient_end),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        GradientColorRow(
            selectedColor = config.gradientEndColor,
            onColorSelected = { viewModel.setGradientEndColor(it) }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    WallpaperSlider(
        label = stringResource(R.string.wallpaper_gradient_angle),
        value = config.gradientAngle,
        valueRange = 0f..360f,
        displaySuffix = "\u00B0",
        displayMultiplier = 1f
    ) { viewModel.setGradientAngle(it) }
}

@Composable
private fun GradientColorRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GRADIENT_PRESET_COLORS.flatMap { listOf(it.first, it.second) }
            .distinct()
            .take(6)
            .forEach { color ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (color == selectedColor) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else {
                                Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                            }
                        )
                        .clickable { onColorSelected(color) }
                )
            }
    }
}

@Composable
private fun WallpaperImageInfo(viewModel: ThemeViewModel) {
    val config by viewModel.wallpaperConfig.collectAsState()

    if (config.imageUri.isNotEmpty()) {
        Text(
            text = stringResource(R.string.wallpaper_image_selected),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        Text(
            text = stringResource(R.string.wallpaper_select_image),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WallpaperSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displaySuffix: String = "%",
    displayMultiplier: Float = 100f,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${(value * displayMultiplier).toInt()}$displaySuffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun HslSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val displayValue = if (valueRange == 0f..360f) "${value.toInt()}\u00B0" else "${(value * 100).toInt()}%"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
