package com.kharcha.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kharcha.app.ui.theme.Bricolage
import com.kharcha.app.ui.theme.Kharcha
import com.kharcha.app.ui.theme.PlexMono
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** The dark green summary card: month total, comparison, budget bar and quick stats. */
@Composable
fun HeroCard(state: HomeState, onEditBudget: () -> Unit, modifier: Modifier = Modifier) {
    val c = Kharcha.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(c.heroBg)
            .drawBehind {
                // Decorative rings in the top-right corner, as on the web version.
                val center = Offset(size.width - 70.dp.toPx(), 50.dp.toPx())
                drawCircle(c.heroField, radius = 150.dp.toPx(), center = center, style = Stroke(40.dp.toPx()))
                drawCircle(c.heroLine, radius = 130.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
            }
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(
                        if (state.isCurrentMonth) "Spent this month" else "Spent in " + state.month.format(Fmt.monthLong),
                        color = c.heroMuted,
                        modifier = Modifier.weight(1f),
                    )
                    state.pace?.let { PacePill(it) }
                }
                Spacer(Modifier.height(4.dp))
                HeroTotal(state)
                state.comparison?.let { cmp ->
                    Text(
                        buildAnnotatedString {
                            cmp.amount?.let {
                                withStyle(SpanStyle(color = c.heroInk, fontFamily = PlexMono, fontWeight = FontWeight.Medium)) { append(it) }
                            }
                            append(cmp.text)
                        },
                        color = c.heroMuted,
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            BudgetBlock(state, onEditBudget)
            HorizontalDivider(color = c.heroLine, thickness = 1.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val avg = if (state.entryCount > 0) Fmt.money0(state.totalPaise / 100.0 / state.dayNow) else "—"
                Stat("Daily average", avg, mono = true, modifier = Modifier.weight(1f))
                Stat("Entries", if (state.loaded) state.entryCount.toString() else "—", mono = true, modifier = Modifier.weight(1f))
                Stat("Top category", state.topCategory?.title ?: "—", mono = false, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroTotal(state: HomeState) {
    val c = Kharcha.colors
    val style = TextStyle(
        fontFamily = Bricolage,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.035).em,
        fontFeatureSettings = "tnum",
        color = c.heroInk,
    )
    if (!state.loaded) {
        Text("—", style = style, fontSize = 42.sp)
        return
    }
    val target = state.totalPaise / 100.0
    val animated = remember { Animatable(target.toFloat()) }
    LaunchedEffect(target) {
        animated.animateTo(target.toFloat(), tween(520, easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)))
    }
    // Count up in whole rupees while moving; settle on the exact paise value.
    val number = if (animated.isRunning) Fmt.number0(animated.value.toDouble()) else Fmt.number2(target)
    val size = when {
        number.length <= 9 -> 42.sp
        number.length <= 12 -> 36.sp
        else -> 30.sp
    }
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 0.5.em, color = c.heroMuted, fontWeight = FontWeight.Normal, baselineShift = BaselineShift(0.42f))) {
                append("₹")
            }
            append(number)
        },
        style = style,
        fontSize = size,
        lineHeight = size * 1.1f,
        maxLines = 1,
        modifier = Modifier.semantics { contentDescription = "Spent " + Fmt.money(state.totalPaise) },
    )
}

@Composable
private fun PacePill(pace: Pace) {
    val c = Kharcha.colors
    val color = when (pace.level) {
        PaceLevel.Good -> c.heroFill
        PaceLevel.Warn -> c.heroWarn
        PaceLevel.Bad -> c.heroBad
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(c.heroField)
            .border(1.dp, c.heroLine, CircleShape)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(7.dp))
        Text(pace.text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun BudgetBlock(state: HomeState, onEditBudget: () -> Unit) {
    val c = Kharcha.colors
    if (state.budget <= 0) {
        HeroButton("Set a monthly budget", onEditBudget)
        return
    }
    val budgetPaise = state.budget * 100
    val fraction = state.totalPaise.toFloat() / budgetPaise
    val left = budgetPaise - state.totalPaise
    val daysLeft = state.daysInMonth - state.dayNow + 1
    val fill = when {
        fraction > 1f -> c.heroBad
        fraction > 0.85f -> c.heroWarn
        else -> c.heroFill
    }
    val shown by animateFloatAsState(min(fraction, 1f), tween(500), label = "budget")
    val bold = SpanStyle(color = c.heroInk, fontFamily = PlexMono, fontWeight = FontWeight.Medium)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(18.dp)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(min(fraction, 1f), 0f..1f) },
        ) {
            val h = 8.dp.toPx()
            val top = (size.height - h) / 2
            drawRoundRect(c.heroLine, Offset(0f, top), Size(size.width, h), CornerRadius(h / 2))
            if (shown > 0f) {
                drawRoundRect(fill, Offset(0f, top), Size(max(h, size.width * shown), h), CornerRadius(h / 2))
            }
            if (state.isCurrentMonth) {
                // Where you'd be if spending evenly through the month.
                val x = size.width * state.dayNow / state.daysInMonth
                val w = 2.dp.toPx()
                drawRoundRect(c.heroInk.copy(alpha = 0.8f), Offset(x - w / 2, 0f), Size(w, size.height), CornerRadius(w / 2))
            }
        }
        Text(
            buildAnnotatedString {
                if (left >= 0) {
                    withStyle(bold) { append(Fmt.money0(left / 100.0)) }
                    append(" left of ${Fmt.money0(state.budget.toDouble())}")
                } else {
                    withStyle(bold) { append(Fmt.money0(abs(left) / 100.0)) }
                    append(" over ${Fmt.money0(state.budget.toDouble())} budget")
                }
            },
            color = c.heroMuted,
            fontSize = 13.sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val perDay: AnnotatedString? = if (state.isCurrentMonth && left > 0) buildAnnotatedString {
                withStyle(bold) { append(Fmt.money0(left / 100.0 / daysLeft)) }
                append(" a day for ${Fmt.plural(daysLeft, "day", "days")}")
            } else null
            Text(perDay ?: AnnotatedString(""), color = c.heroMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(
                "Edit budget",
                color = c.heroInk,
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onEditBudget)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun HeroButton(text: String, onClick: () -> Unit) {
    val c = Kharcha.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .clip(shape)
            .background(c.heroField)
            .border(1.dp, c.heroLine, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(text, color = c.heroInk, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Stat(label: String, value: String, mono: Boolean, modifier: Modifier = Modifier) {
    val c = Kharcha.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = c.heroMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            value,
            color = c.heroInk,
            fontSize = 15.sp,
            fontFamily = if (mono) PlexMono else null,
            fontWeight = if (mono) FontWeight.Medium else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
