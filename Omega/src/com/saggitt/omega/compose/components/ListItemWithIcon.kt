/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saggitt.omega.compose.icons.Phosphor
import com.saggitt.omega.compose.icons.phosphor.BracketsCurly
import com.saggitt.omega.util.addIf

@Composable
fun ListItemWithIcon(
    title: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    startIcon: (@Composable () -> Unit)? = null,
    endCheckbox: (@Composable () -> Unit)? = null,
    isEnabled: Boolean = true,
    showDivider: Boolean = false,
    applyPaddings: Boolean = true,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    Column {
        if (showDivider) {
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }
        Row(
            verticalAlignment = verticalAlignment,
            modifier = modifier
                .fillMaxWidth()
                .addIf(applyPaddings) {
                    padding(
                        start = 16.dp,
                        end = horizontalPadding,
                        top = verticalPadding,
                        bottom = verticalPadding
                    )
                }
        ) {
            startIcon?.let {
                startIcon()
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .addIf(!isEnabled) {
                        alpha(0.3f)
                    }
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
                if (summary.isNotEmpty()) {
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6F),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp
                    )
                }
            }

            endCheckbox?.let {
                if (applyPaddings) {
                    Spacer(modifier = Modifier.requiredWidth(16.dp))
                }
                endCheckbox()
            }
        }
    }
}

@Preview
@Composable
fun PreviewListItemWithIcon() {
    ListItemWithIcon(
        title = "System Iconpack",
        modifier = Modifier.clickable { },
        summary = "com.saggitt.iconpack",
        startIcon = {
            Image(
                Phosphor.BracketsCurly,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12F)
                    )
            )

        },
        endCheckbox = {
            RadioButton(
                selected = false,
                onClick = null
            )
        }
    )
}