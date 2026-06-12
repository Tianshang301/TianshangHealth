package com.tianshang.health.feature.period.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tianshang.health.core.common.R
import com.tianshang.health.core.common.ui.theme.HslColorEngine
import com.tianshang.health.feature.period.viewmodel.ThemeViewModel

@Composable
fun ThemeScreen(
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val hue by viewModel.hue.collectAsState()
    val saturation by viewModel.saturation.collectAsState()
    val lightness by viewModel.lightness.collectAsState()
    val useCustomTheme by viewModel.useCustomTheme.collectAsState()

    val previewColor = HslColorEngine.toColor(hue, saturation, lightness)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

            // Color preview
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

            // HSL sliders
            HslSlider(stringResource(R.string.hue), hue, 0f..360f) { viewModel.setHue(it) }
            Spacer(modifier = Modifier.height(12.dp))
            HslSlider(stringResource(R.string.saturation), saturation, 0f..1f) { viewModel.setSaturation(it) }
            Spacer(modifier = Modifier.height(12.dp))
            HslSlider(stringResource(R.string.lightness), lightness, 0f..1f) { viewModel.setLightness(it) }

            Spacer(modifier = Modifier.height(24.dp))

            // Presets
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
    }
}

@Composable
private fun HslSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val displayValue = if (valueRange == 0f..360f) "${value.toInt()}°" else "${(value * 100).toInt()}%"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
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
