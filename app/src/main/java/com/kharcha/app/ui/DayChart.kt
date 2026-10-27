package com.kharcha.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kharcha.app.ui.theme.Kharcha
import com.kharcha.app.ui.theme.PlexMono
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private val ChartLeft = 42.dp
private val ChartRight = 6.dp
private val ChartTop = 22.dp
private val ChartBottom = 24.dp

/**
 * Bars per day for the month in view, with the daily budget as a dashed line.
 * One huge day (rent) would flatten every other bar, so the scale is capped and the outlier
 * is drawn broken with its value on top. Tap or drag across the chart to read a day.
 */
@Composable
fun DayChart(state: HomeState, modifier: Modifier = Modifier) {
    val c = Kharcha.colors
    val measurer = rememberTextMeasurer()
    val days = state.daysInMonth
    var selected by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.month) { selected = null }

    val axisStyle = TextStyle(fontFamily = PlexMono, fontSize = 10.5.sp, color = c.faint)
    val outlierStyle = axisStyle.copy(color = c.ink)
    val tipStyle = TextStyle(fontSize = 12.sp, color = c.bg)
    val tipBold = TextStyle(fontFamily = PlexMono, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.bg)

    Canvas(
        modifier
            .fillMaxWidth()
            .height(196.dp)
            .semantics { contentDescription = "Spending per day in " + state.month.format(Fmt.monthYear) }
            .pointerInput(days) {
                detectTapGestures { pos ->
                    val hit = hitDay(pos.x, size.width.toFloat(), ChartLeft.toPx(), ChartRight.toPx(), days)
                    selected = if (hit == null || hit == selected) null else hit
                }
            }
            .pointerInput(days) {
                detectHorizontalDragGestures(
                    onDragStart = { pos -> selected = hitDay(pos.x, size.width.toFloat(), ChartLeft.toPx(), ChartRight.toPx(), days) },
                    onHorizontalDrag = { change, _ ->
                        hitDay(change.position.x, size.width.toFloat(), ChartLeft.toPx(), ChartRight.toPx(), days)?.let { selected = it }
                    },
                )
            },
    ) {
        val w = size.width
        val h = size.height
        val left = ChartLeft.toPx()
        val right = ChartRight.toPx()
        val top = ChartTop.toPx()
        val bottom = ChartBottom.toPx()
        val perDay = state.perDayPaise.map { it / 100.0 }
        val daily = if (state.budget > 0) state.budget.toDouble() / days else 0.0

        val sorted = perDay.filter { it > 0 }.sortedDescending()
        var maxV = max(sorted.firstOrNull() ?: 0.0, daily * 1.2)
        if (sorted.size > 2 && sorted[0] > 3 * sorted[1]) maxV = max(sorted[1] * 1.5, daily * 1.2)
        val step = niceStep(maxV)
        val scaleTop = max(step, ceil(maxV / step) * step)
        fun y(v: Double): Float = (top + (h - top - bottom) * (1 - v / scaleTop)).toFloat()
        val base = y(0.0)

        // Grid and y-axis labels
        var v = step
        while (v <= scaleTop + 1e-6) {
            drawLine(c.line, Offset(left, y(v)), Offset(w - right, y(v)), strokeWidth = 1.dp.toPx())
            label(measurer, Fmt.short(v), axisStyle, left - 8.dp.toPx(), y(v) + 3.5.dp.toPx(), align = 1f)
            v += step
        }
        drawLine(c.line, Offset(left, base), Offset(w - right, base), strokeWidth = 1.5.dp.toPx())
        label(measurer, "0", axisStyle, left - 8.dp.toPx(), base + 3.5.dp.toPx(), align = 1f)

        val slot = (w - left - right) / days
        val barW = (slot * 0.62f).coerceIn(3.dp.toPx(), 18.dp.toPx())
        val corner = CornerRadius(min(4.dp.toPx(), barW / 2))
        val labelEvery = if (slot < 11.dp.toPx()) 10 else 5
        var tipAnchorY = base

        for (i in 0 until days) {
            val day = i + 1
            val cx = left + i * slot + slot / 2
            val x = cx - barW / 2
            val value = perDay.getOrElse(i) { 0.0 }
            val future = state.isCurrentMonth && day > state.dayNow
            val emphasised = (state.isCurrentMonth && day == state.dayNow) || selected == i
            val barColor = c.accent.copy(alpha = if (emphasised) 1f else 0.72f)
            when {
                value > scaleTop -> {
                    val yTop = y(scaleTop) - 4.dp.toPx()
                    drawRoundRect(barColor, Offset(x, yTop), Size(barW, base - yTop), corner)
                    val yb = y(scaleTop * 0.62)
                    val gap = 2.dp.toPx()
                    val rise = 5.dp.toPx()
                    val cut = Path().apply {
                        moveTo(x - gap, yb + gap); relativeLineTo(barW + 2 * gap, -rise)
                        moveTo(x - gap, yb + 3 * gap); relativeLineTo(barW + 2 * gap, -rise)
                    }
                    drawPath(cut, c.surface, style = Stroke(2.5.dp.toPx()))
                    val lx = cx.coerceIn(left + 14.dp.toPx(), w - right - 14.dp.toPx())
                    label(measurer, Fmt.short(value), outlierStyle, lx, yTop - 5.dp.toPx(), align = 0.5f)
                    if (selected == i) tipAnchorY = yTop - 14.dp.toPx()
                }
                value > 0 -> {
                    val bh = max(2.dp.toPx(), base - y(value))
                    drawRoundRect(barColor, Offset(x, base - bh), Size(barW, bh), corner)
                    if (selected == i) tipAnchorY = base - bh
                }
                future -> drawRoundRect(c.line, Offset(x, base - 3.dp.toPx()), Size(barW, 3.dp.toPx()), CornerRadius(1.5.dp.toPx()))
            }
            if (day == 1 || day % labelEvery == 0) {
                label(measurer, day.toString(), axisStyle, cx, h - 6.dp.toPx(), align = 0.5f)
            }
        }

        if (daily > 0) {
            val dash = 4.dp.toPx()
            drawLine(
                c.warn, Offset(left, y(daily)), Offset(w - right, y(daily)),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
            )
        }

        selected?.takeIf { it < days }?.let { i ->
            val day = i + 1
            val future = state.isCurrentMonth && day > state.dayNow
            val whenText = state.month.atDay(day).format(Fmt.dayShort)
            val amount = if (future) "—" else Fmt.money(state.perDayPaise.getOrElse(i) { 0L })
            val count = state.perDayCount.getOrElse(i) { 0 }
            val sub = if (future) "Still to come" else Fmt.plural(count, "entry", "entries")
            val lines = listOf(
                measurer.measure(AnnotatedString(whenText), tipStyle),
                measurer.measure(AnnotatedString(amount), tipBold),
                measurer.measure(AnnotatedString(sub), tipStyle),
            )
            val padX = 10.dp.toPx()
            val padY = 7.dp.toPx()
            val boxW = lines.maxOf { it.size.width } + 2 * padX
            val boxH = lines.sumOf { it.size.height } + 2 * padY
            val cx = left + i * slot + slot / 2
            val bx = (cx - boxW / 2).coerceIn(0f, max(0f, w - boxW))
            val by = (tipAnchorY - 8.dp.toPx() - boxH).coerceAtLeast(0f)
            drawRoundRect(c.ink, Offset(bx, by), Size(boxW, boxH), CornerRadius(10.dp.toPx()))
            var ty = by + padY
            lines.forEach { line ->
                drawText(line, topLeft = Offset(bx + padX, ty))
                ty += line.size.height
            }
        }
    }
}

/** Day index under an x position, or null outside the plot area. */
private fun hitDay(x: Float, width: Float, left: Float, right: Float, days: Int): Int? {
    if (x < left || x > width - right || days <= 0) return null
    val slot = (width - left - right) / days
    return floor((x - left) / slot).toInt().coerceIn(0, days - 1)
}

/** Draws [text] with its baseline at [baseline]; [align] 0 = left, 0.5 = centre, 1 = right of [x]. */
private fun DrawScope.label(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    x: Float,
    baseline: Float,
    align: Float,
) {
    val layout = measurer.measure(AnnotatedString(text), style)
    drawText(layout, topLeft = Offset(x - layout.size.width * align, baseline - layout.firstBaseline))
}

private fun niceStep(max: Double): Double {
    if (max <= 0) return 500.0
    val raw = max / 4
    val p = 10.0.pow(floor(log10(raw)))
    val n = raw / p
    val m = when {
        n <= 1 -> 1.0
        n <= 2 -> 2.0
        n <= 2.5 -> 2.5
        n <= 5 -> 5.0
        else -> 10.0
    }
    return m * p
}
