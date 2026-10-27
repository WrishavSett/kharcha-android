package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kharcha.app.model.Category
import com.kharcha.app.ui.theme.KType
import com.kharcha.app.ui.theme.Kharcha
import com.kharcha.app.ui.theme.PlexMono
import kotlin.math.roundToInt

/** "Day by day" chart, "Where it went" by category, and the UPI / Card / Cash split. */
@Composable
fun InsightsCard(state: HomeState, onToggleFilter: (Category) -> Unit, modifier: Modifier = Modifier) {
    val c = Kharcha.colors
    Column(
        modifier
            .fillMaxWidth()
            .panel(c)
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Day by day", style = KType.h2, color = c.ink, modifier = Modifier.weight(1f))
            Legend(showBudget = state.budget > 0)
        }
        Spacer(Modifier.height(10.dp))
        DayChart(state)

        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text("Where it went", style = KType.h2, color = c.ink, modifier = Modifier.weight(1f))
            if (state.categories.isNotEmpty()) Text("Tap to filter", fontSize = 12.sp, color = c.faint)
        }
        Spacer(Modifier.height(8.dp))
        if (state.categories.isEmpty()) {
            Text(
                if (state.loaded) "Category totals appear once you log something this month." else "",
                style = KType.small,
                color = c.muted,
            )
        } else {
            val max = state.categories.first().paise.coerceAtLeast(1)
            state.categories.forEach { row ->
                CategoryRow(
                    row = row,
                    fraction = row.paise.toFloat() / max,
                    share = row.paise.toFloat() / state.totalPaise.coerceAtLeast(1),
                    selected = state.filter == row.category,
                    onClick = { onToggleFilter(row.category) },
                )
            }
        }

        if (state.methods.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            MethodSplit(state)
        }
    }
}

@Composable
private fun Legend(showBudget: Boolean) {
    val c = Kharcha.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(c.accent))
            Spacer(Modifier.width(6.dp))
            Text("Spent", fontSize = 12.sp, color = c.muted)
        }
        if (showBudget) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(14.dp)
                        .height(2.dp)
                        .drawBehind {
                            drawLine(
                                c.warn, Offset(0f, size.height / 2), Offset(size.width, size.height / 2),
                                strokeWidth = size.height,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
                            )
                        },
                )
                Spacer(Modifier.width(6.dp))
                Text("Daily budget", fontSize = 12.sp, color = c.muted)
            }
        }
    }
}

@Composable
private fun CategoryRow(row: CategoryTotal, fraction: Float, share: Float, selected: Boolean, onClick: () -> Unit) {
    val c = Kharcha.colors
    val color = c.category(row.category)
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) color.copy(alpha = 0.08f) else Color.Transparent)
            .border(1.dp, if (selected) color else Color.Transparent, shape)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(row.category, size = 34.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.category.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(Fmt.money(row.paise), style = KType.mono, fontSize = 14.sp, color = c.ink)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(c.surface2),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color),
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                "${Fmt.plural(row.count, "entry", "entries")} · ${(share * 100).roundToInt()}% of spending",
                fontSize = 12.sp,
                color = c.faint,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MethodSplit(state: HomeState) {
    val c = Kharcha.colors
    val total = state.totalPaise.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Eyebrow("Paid by")
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            state.methods.forEach { m ->
                Box(
                    Modifier
                        .weight(m.paise.toFloat())
                        .fillMaxHeight()
                        .background(methodColor(m.method, c)),
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            state.methods.forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(methodColor(m.method, c)))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        buildAnnotatedString {
                            append(m.method.label + " ")
                            withStyle(SpanStyle(fontFamily = PlexMono, fontWeight = FontWeight.Medium, color = c.ink)) {
                                append(Fmt.money0(m.paise / 100.0))
                            }
                            append(" · ${(m.paise * 100f / total).roundToInt()}%")
                        },
                        fontSize = 12.sp,
                        color = c.muted,
                    )
                }
            }
        }
    }
}
