package com.example.carspotter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(
    label: String,
    isSelected: Boolean,
    options: List<Pair<String?, String>>, // id to displayName, null id = "ALL"
    onOptionSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        FilterChip(
            modifier=Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            selected = isSelected,
            onClick = { expanded = !expanded },
            label = {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedLabelColor = Color.White,
                containerColor = Color.Transparent,
                labelColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) Color.Red
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (id == null && !isSelected || /* aktywna opcja */ false)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onOptionSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}