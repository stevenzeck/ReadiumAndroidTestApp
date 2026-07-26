package com.example.readiumandroidtestapp.features.reader.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import org.readium.r2.navigator.preferences.EnumPreference
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.RangePreference

@Composable
fun SwitchPreference(
    title: String,
    preference: Preference<Boolean>,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!preference.isEffective) return

    val checked = preference.value ?: preference.effectiveValue

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun <T : Comparable<T>> StepperPreference(
    modifier: Modifier = Modifier,
    title: String,
    preference: RangePreference<T>,
    onCommit: () -> Unit,
    formatValue: (T) -> String = { it.toString() },
) {
    if (!preference.isEffective) return

    var currentValue by remember(preference) {
        mutableStateOf(preference.value ?: preference.effectiveValue)
    }
    val range = preference.supportedRange

    val canDecrement = currentValue > range.start
    val canIncrement = currentValue < range.endInclusive

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = {
                    preference.decrement()
                    currentValue = preference.value ?: preference.effectiveValue
                    onCommit()
                },
                enabled = canDecrement,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_previous),
                    contentDescription = stringResource(id = R.string.decrease),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = formatValue(currentValue),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            IconButton(
                onClick = {
                    preference.increment()
                    currentValue = preference.value ?: preference.effectiveValue
                    onCommit()
                },
                enabled = canIncrement,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = stringResource(id = R.string.increase),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun <T> EnumPreference(
    modifier: Modifier = Modifier,
    title: String,
    preference: EnumPreference<T>,
    onValueChange: (T) -> Unit,
    formatValue: (T) -> String = { it.toString() },
) {
    if (!preference.isEffective) return

    val value = preference.value ?: preference.effectiveValue
    var expanded by remember { mutableStateOf(value = false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(text = formatValue(value))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                preference.supportedValues.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = formatValue(option)) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun <T> ChoicePreference(
    modifier: Modifier = Modifier,
    title: String,
    preference: Preference<T>,
    choices: List<T>,
    onValueChange: (T) -> Unit,
    formatValue: (T) -> String = { it.toString() },
) {
    if (!preference.isEffective) return

    val value = preference.value ?: preference.effectiveValue
    var expanded by remember { mutableStateOf(value = false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(text = formatValue(value))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                choices.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = formatValue(option)) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
