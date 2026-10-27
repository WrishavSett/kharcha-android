package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kharcha.app.data.Expense
import com.kharcha.app.model.Category
import com.kharcha.app.model.PayMethod
import com.kharcha.app.ui.theme.Bricolage
import com.kharcha.app.ui.theme.KType
import com.kharcha.app.ui.theme.Kharcha
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToLong

private val AmountPattern = Regex("^[0-9,]*(\\.[0-9]{0,2})?$")

/** Add or edit an expense. Mirrors the web form: amount, category, note, date, paid by. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSheet(
    editing: Expense?,
    initialCategory: Category,
    initialMethod: PayMethod,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: (ExpenseDraft) -> Unit,
    onDelete: (Expense) -> Unit,
) {
    val c = Kharcha.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val amountFocus = remember { FocusRequester() }

    var amount by remember { mutableStateOf(editing?.let { Fmt.plain(it.amountPaise) } ?: "") }
    var category by remember { mutableStateOf(editing?.let { Category.of(it.category) } ?: initialCategory) }
    var note by remember {
        mutableStateOf(editing?.let { if (it.note == Category.of(it.category).title) "" else it.note } ?: "")
    }
    var date by remember { mutableStateOf(editing?.let { runCatching { LocalDate.parse(it.date) }.getOrNull() } ?: today) }
    var method by remember { mutableStateOf(editing?.let { PayMethod.of(it.method) } ?: initialMethod) }
    var error by remember { mutableStateOf<String?>(null) }
    var pickingDate by remember { mutableStateOf(false) }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    fun submit() {
        val value = amount.replace(",", "").trim().toDoubleOrNull()
        when {
            value == null || value <= 0.0 -> {
                error = "Enter an amount above ₹0."
                amountFocus.requestFocus()
            }
            value > 10_000_000 -> {
                error = "That amount is over ₹1 crore. Check the number and try again."
                amountFocus.requestFocus()
            }
            date.isAfter(today) -> error = "Pick today or an earlier date."
            else -> {
                error = null
                focusManager.clearFocus()
                onSave(ExpenseDraft((value * 100).roundToLong(), category, note.trim(), date, method))
                close()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (editing == null) {
            delay(250) // let the sheet settle before the keyboard comes up
            runCatching { amountFocus.requestFocus() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surface,
        contentColor = c.ink,
        dragHandle = { BottomSheetDefaults.DragHandle(color = c.line) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (editing == null) "Add expense" else "Edit expense",
                    style = KType.h2,
                    fontSize = 20.sp,
                    color = c.ink,
                    modifier = Modifier.weight(1f),
                )
                if (editing != null) {
                    TextButton(onClick = {
                        onDelete(editing)
                        close()
                    }) {
                        Icon(KIcons.Trash, contentDescription = null, tint = c.danger, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", color = c.danger)
                    }
                }
            }

            Field("Amount") {
                AmountInput(
                    value = amount,
                    onValueChange = { new -> if (AmountPattern.matches(new) && new.length <= 14) amount = new },
                    focusRequester = amountFocus,
                )
            }

            Field("Category") {
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.chunked(4).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { cat ->
                                CategoryTile(
                                    category = cat,
                                    selected = cat == category,
                                    onClick = { category = cat },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            Field("Note") {
                InputBox {
                    if (note.isEmpty()) Text("What was it for?", style = KType.body, color = c.faint)
                    BasicTextField(
                        value = note,
                        onValueChange = { if (it.length <= 80) note = it },
                        singleLine = true,
                        textStyle = KType.body.copy(color = c.ink),
                        cursorBrush = SolidColor(c.accent),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("Date", modifier = Modifier.weight(1f))
                    DateChip("Today", selected = date == today) { date = today }
                    Spacer(Modifier.width(4.dp))
                    DateChip("Yesterday", selected = date == today.minusDays(1)) { date = today.minusDays(1) }
                }
                InputBox(onClick = { pickingDate = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(date.format(Fmt.dateField), style = KType.body, color = c.ink, modifier = Modifier.weight(1f))
                        Icon(KIcons.Calendar, contentDescription = "Pick a date", tint = c.muted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Field("Paid by") {
                MethodSegments(method) { method = it }
            }

            error?.let { Text(it, color = c.danger, fontSize = 13.sp) }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.accent)
                    .clickable(role = Role.Button) { submit() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (editing == null) "Add expense" else "Save changes",
                    color = c.accentInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (pickingDate) {
        val todayMillis = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    pickingDate = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pickingDate = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Eyebrow(label)
        content()
    }
}

@Composable
private fun InputBox(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val c = Kharcha.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface2)
            .border(1.dp, c.line, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) { content() }
}

@Composable
private fun AmountInput(value: String, onValueChange: (String) -> Unit, focusRequester: FocusRequester) {
    val c = Kharcha.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(14.dp)
    val big = TextStyle(
        fontFamily = Bricolage,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = "tnum",
        color = c.ink,
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface2)
            .border(if (focused) 2.dp else 1.dp, if (focused) c.accent else c.line, shape)
            .clickable(indication = null, interactionSource = null) { runCatching { focusRequester.requestFocus() } }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("₹", fontFamily = Bricolage, fontSize = 28.sp, color = c.faint)
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("0", style = big.copy(color = c.faint))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = big,
                cursorBrush = SolidColor(c.accent),
                interactionSource = interaction,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }
    }
}

@Composable
private fun CategoryTile(category: Category, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Kharcha.colors
    val color = c.category(category)
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (selected) color.copy(alpha = 0.08f).compositeOver(c.surface) else c.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) color else c.line, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(start = 2.dp, end = 2.dp, top = 10.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CategoryIcon(category, size = 34.dp, iconSize = 19.dp)
        Text(
            category.label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) c.ink else c.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DateChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = Kharcha.colors
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (selected) c.accentSoft else c.surface)
            .border(1.dp, if (selected) Color.Transparent else c.line, CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) c.accent else c.muted,
        )
    }
}

@Composable
private fun MethodSegments(selected: PayMethod, onSelect: (PayMethod) -> Unit) {
    val c = Kharcha.colors
    val outer = RoundedCornerShape(10.dp)
    val inner = RoundedCornerShape(7.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(outer)
            .background(c.surface2)
            .border(1.dp, c.line, outer)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PayMethod.entries.forEach { m ->
            val on = m == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(inner)
                    .background(if (on) c.surface else Color.Transparent)
                    .border(1.dp, if (on) c.line else Color.Transparent, inner)
                    .selectable(selected = on, role = Role.RadioButton) { onSelect(m) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    m.label,
                    fontSize = 13.5.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (on) c.ink else c.muted,
                )
            }
        }
    }
}
