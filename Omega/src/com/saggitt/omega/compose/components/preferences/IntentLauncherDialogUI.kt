package com.saggitt.omega.compose.components.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.Utilities
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.preferences.IntentLauncherPref

@Composable
fun IntentLauncherDialogUI(
    pref: IntentLauncherPref,
    openDialogCustom: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val prefs = Utilities.getNeoPrefs(context)

    var radius = 16.dp
    if (prefs.profileWindowCornerRadius.getValue() > -1) {
        radius = prefs.profileWindowCornerRadius.getValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(pref.titleId), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(id = pref.summaryId),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    textId = pref.positiveAnswerId,
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        context.startActivity(pref.intent())
                        openDialogCustom.value = false
                    }
                )
            }
        }
    }
}