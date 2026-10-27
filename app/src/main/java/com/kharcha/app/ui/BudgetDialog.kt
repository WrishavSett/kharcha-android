package com.kharcha.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kharcha.app.ui.theme.KType
import com.kharcha.app.ui.theme.Kharcha
import com.kharcha.app.ui.theme.PlexMono

/** Set, change or remove the monthly budget (whole rupees). */
@Composable
fun BudgetDialog(current: Long, onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    val c = Kharcha.colors
    val initial = if (current > 0) Fmt.number0(current.toDouble()) else ""
    var field by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    val focus = remember { FocusRequester() }

    fun save() {
        val rupees = field.text.filter { it.isDigit() }.toLongOrNull() ?: 0L
        onSave(rupees)
    }

    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text("Monthly budget", style = KType.h2, color = c.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "How much do you want to spend each month? The bar on the summary card tracks it day by day.",
                    style = KType.small,
                    color = c.muted,
                )
                OutlinedTextField(
                    value = field,
                    onValueChange = { new ->
                        if (new.text.all { it.isDigit() || it == ',' } && new.text.length <= 12) field = new
                    },
                    prefix = { Text("₹ ", color = c.muted) },
                    placeholder = { Text("e.g. 30,000", color = c.faint) },
                    singleLine = true,
                    textStyle = KType.mono.copy(color = c.ink, fontFamily = PlexMono),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { save() }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (current > 0) {
                    TextButton(onClick = { onSave(0) }) { Text("Remove", color = c.danger) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
