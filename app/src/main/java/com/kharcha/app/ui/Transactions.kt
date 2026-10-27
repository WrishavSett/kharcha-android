package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kharcha.app.data.Expense
import com.kharcha.app.model.Category
import com.kharcha.app.model.PayMethod
import com.kharcha.app.ui.theme.KType
import com.kharcha.app.ui.theme.Kharcha

/**
 * The transaction book as list items, so long months scroll lazily. The panel is drawn in
 * pieces: a top cap with the header, middle pieces for days and rows, and a bottom cap.
 */
fun LazyListScope.transactionBook(
    state: HomeState,
    query: String,
    onQuery: (String) -> Unit,
    onEdit: (Expense) -> Unit,
    onClearFilter: () -> Unit,
    onLoadSamples: () -> Unit,
    onClearSamples: () -> Unit,
) {
    item(key = "book-head") {
        BookHeader(state, query, onQuery, onClearFilter, onClearSamples)
    }
    when {
        !state.loaded -> item(key = "book-loading") { Segment { LoadingRows() } }
        state.groups.isEmpty() -> item(key = "book-empty") {
            Segment { EmptyBook(state, query, onLoadSamples) }
        }
        else -> state.groups.forEach { group ->
            item(key = "day-${group.date}") { Segment { DayHeader(group) } }
            itemsIndexed(group.items, key = { _, e -> "tx-${e.id}" }) { index, e ->
                Segment { TxRow(e, showDivider = index < group.items.lastIndex, onClick = { onEdit(e) }) }
            }
        }
    }
    item(key = "book-foot") {
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .panel(Kharcha.colors, top = false, bottom = true),
        )
    }
}

@Composable
private fun Segment(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .panel(Kharcha.colors, top = false, bottom = false),
    ) { content() }
}

@Composable
private fun BookHeader(
    state: HomeState,
    query: String,
    onQuery: (String) -> Unit,
    onClearFilter: () -> Unit,
    onClearSamples: () -> Unit,
) {
    val c = Kharcha.colors
    Column(
        Modifier
            .fillMaxWidth()
            .panel(c, top = true, bottom = false)
            .padding(start = 16.dp, end = 16.dp, top = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Transactions", style = KType.h2, color = c.ink)
            Spacer(Modifier.width(8.dp))
            val count = when {
                !state.loaded || state.entryCount == 0 -> ""
                state.visibleCount == state.entryCount -> "${state.entryCount}"
                else -> "${state.visibleCount} of ${state.entryCount}"
            }
            Text(count, style = KType.mono, fontSize = 13.sp, color = c.faint, modifier = Modifier.padding(bottom = 1.dp))
        }
        SearchField(query, onQuery)
        state.filter?.let { ActiveFilter(it, onClearFilter) }
        if (state.hasSamples) SampleNotice(onClearSamples)
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit) {
    val c = Kharcha.colors
    val focus = LocalFocusManager.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface2)
            .border(1.dp, if (focused) c.accent else c.line, shape)
            .padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(KIcons.Search, contentDescription = null, tint = c.faint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).padding(vertical = 10.dp)) {
            if (query.isEmpty()) Text("Search notes", style = KType.body, color = c.faint)
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = KType.body.copy(color = c.ink),
                cursorBrush = SolidColor(c.accent),
                interactionSource = interaction,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Clear search") { onQuery("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(KIcons.Close, contentDescription = "Clear search", tint = c.faint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ActiveFilter(category: Category, onClear: () -> Unit) {
    val c = Kharcha.colors
    Row(
        Modifier
            .clip(CircleShape)
            .background(c.accentSoft)
            .clickable(onClickLabel = "Remove filter ${category.title}", onClick = onClear)
            .padding(start = 12.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(category.title, color = c.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Icon(KIcons.Close, contentDescription = null, tint = c.accent, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SampleNotice(onClear: () -> Unit) {
    val c = Kharcha.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surface2)
            .border(1.dp, c.line, shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            buildAnnotatedString {
                append("Entries marked ")
                withStyle(SpanStyle(color = c.warn, fontWeight = FontWeight.SemiBold)) { append("Example") }
                append(" are sample data to show how Kharcha works.")
            },
            fontSize = 13.sp,
            color = c.muted,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(10.dp))
        GhostButton("Clear examples", onClear)
    }
}

@Composable
private fun DayHeader(group: DayGroup) {
    val c = Kharcha.colors
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(group.label, color = c.ink, modifier = Modifier.weight(1f))
            Text(Fmt.money(group.totalPaise), style = KType.mono, fontSize = 13.sp, color = c.muted)
        }
        HorizontalDivider(color = c.line, thickness = 1.dp)
    }
}

@Composable
private fun TxRow(e: Expense, showDivider: Boolean, onClick: () -> Unit) {
    val c = Kharcha.colors
    val category = Category.of(e.category)
    Column(Modifier.fillMaxWidth().clickable(onClickLabel = "Edit", onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(category, size = 38.dp, corner = 11.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    e.note,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildAnnotatedString {
                        append("${category.title} · ${PayMethod.of(e.method).label}")
                        if (e.sample) {
                            append("  ")
                            withStyle(SpanStyle(color = c.warn, fontWeight = FontWeight.SemiBold)) { append("Example") }
                        }
                    },
                    fontSize = 12.5.sp,
                    color = c.faint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(Fmt.money(e.amountPaise), style = KType.mono, fontWeight = FontWeight.Medium, color = c.ink)
        }
        if (showDivider) {
            HorizontalDivider(color = c.line, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun EmptyBook(state: HomeState, query: String, onLoadSamples: () -> Unit) {
    val c = Kharcha.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 34.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val filtering = query.isNotBlank() || state.filter != null
        val (title, body) = when {
            !state.hasAnyExpenses -> "Start your book" to "Tap Add expense to log one. Each entry lands here, grouped by day."
            filtering -> "No matches" to
                (if (query.isNotBlank()) "Nothing this month matches “${query.trim()}”." else "Nothing this month matches that category.")
            else -> "A clean month" to "Nothing logged for these dates yet."
        }
        if (!filtering) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(c.accentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(KIcons.Book, contentDescription = null, tint = c.accent, modifier = Modifier.size(26.dp))
            }
        }
        Text(title, style = KType.h2, color = c.ink)
        Text(body, style = KType.small, fontSize = 14.sp, color = c.muted, textAlign = TextAlign.Center)
        if (!state.hasAnyExpenses) GhostButton("Load example entries", onLoadSamples)
    }
}

@Composable
private fun LoadingRows() {
    val c = Kharcha.colors
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(3) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(c.line))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(0.55f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(c.line))
                    Box(Modifier.fillMaxWidth(0.3f).height(10.dp).clip(RoundedCornerShape(6.dp)).background(c.line))
                }
            }
        }
    }
}
