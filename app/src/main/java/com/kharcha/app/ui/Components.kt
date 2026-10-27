package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kharcha.app.model.Category
import com.kharcha.app.model.PayMethod
import com.kharcha.app.ui.theme.KType
import com.kharcha.app.ui.theme.Kharcha
import com.kharcha.app.ui.theme.KharchaColors

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Kharcha.colors.muted) {
    Text(text.uppercase(), style = KType.eyebrow, color = color, modifier = modifier)
}

/** Category glyph on a soft tile of its own colour. */
@Composable
fun CategoryIcon(category: Category, size: Dp = 34.dp, corner: Dp = 10.dp, iconSize: Dp = 19.dp) {
    val color = Kharcha.colors.category(category)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(KIcons.forCategory(category), contentDescription = null, tint = color, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = Kharcha.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.line, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = if (enabled) c.ink else c.faint)
    }
}

fun methodColor(method: PayMethod, c: KharchaColors): Color = when (method) {
    PayMethod.Upi -> c.accent
    PayMethod.Card -> c.transport
    PayMethod.Cash -> c.food
}

/**
 * Draws a bordered, rounded panel behind the content. A panel can be split across several
 * list items: middle pieces draw only the side borders, so the pieces join seamlessly.
 */
fun Modifier.panel(
    colors: KharchaColors,
    top: Boolean = true,
    bottom: Boolean = true,
    radius: Dp = 18.dp,
): Modifier = drawBehind {
    val r = radius.toPx()
    val stroke = 1.dp.toPx()
    val y0 = if (top) 0f else -3 * r
    val y1 = if (bottom) size.height else size.height + 3 * r
    clipRect {
        drawRoundRect(
            color = colors.surface,
            topLeft = Offset(0f, y0),
            size = Size(size.width, y1 - y0),
            cornerRadius = CornerRadius(r),
        )
        drawRoundRect(
            color = colors.line,
            topLeft = Offset(stroke / 2, y0 + stroke / 2),
            size = Size(size.width - stroke, y1 - y0 - stroke),
            cornerRadius = CornerRadius(r),
            style = Stroke(stroke),
        )
    }
}
