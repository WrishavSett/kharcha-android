package com.kharcha.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.kharcha.app.model.Category

/**
 * The web version's hand-drawn 24px stroke icons, ported path for path.
 * Each is drawn in one colour and tinted where it's used.
 */
object KIcons {
    val Food = stroke("food", "M4 11h16a8 8 0 0 1-16 0Z", "M9.5 3.5c0 1.5 1.5 1.5 1.5 3.5M13.5 3.5c0 1.5 1.5 1.5 1.5 3.5")
    val Groceries = stroke(
        "groceries",
        "M3 9h18l-2.2 10.2a1 1 0 0 1-1 .8H6.2a1 1 0 0 1-1-.8L3 9Z",
        "M8 9l3.5-5.5M16 9l-3.5-5.5M9.5 13v3.5M14.5 13v3.5",
    )
    val Transport = stroke(
        "transport",
        "M7 3.5h10a2.5 2.5 0 0 1 2.5 2.5v9a2.5 2.5 0 0 1-2.5 2.5H7a2.5 2.5 0 0 1-2.5-2.5V6A2.5 2.5 0 0 1 7 3.5Z",
        "M4.5 11h15M7.5 17.5V20M16.5 17.5V20M8 14.3h.01M16 14.3h.01",
    )
    val Bills = stroke("bills", "M6 3h12v18l-3-2-3 2-3-2-3 2V3Z", "M9 8h6M9 12h6M9 16h3")
    val Shopping = stroke("shopping", "M5 8h14l-1.1 11.1a1 1 0 0 1-1 .9H7.1a1 1 0 0 1-1-.9L5 8Z", "M9 10V7a3 3 0 0 1 6 0v3")
    val Health = stroke(
        "health",
        "M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10Z",
        "M8.5 12h2l1-2 1.5 4 1-2h1.5",
    )
    val Fun = stroke("fun", "M4 7h16v3a2 2 0 0 0 0 4v3H4v-3a2 2 0 0 0 0-4V7Z", "M14.5 7.5v1.5M14.5 11.2v1.5M14.5 14.9v1.5")
    val Other = stroke("other", 3f, "M6.5 12h.01M12 12h.01M17.5 12h.01")

    val ChevronLeft = stroke("chevron-left", 2f, "M14.5 6l-6 6 6 6")
    val ChevronRight = stroke("chevron-right", 2f, "M9.5 6l6 6-6 6")
    val Book = stroke("book", "M5 4.5h11a3 3 0 0 1 3 3V20H8a3 3 0 0 1-3-3V4.5Z", "M5 17a3 3 0 0 1 3-3h11M9 8.5h6")
    val Search = stroke("search", 2f, "M17.5 11a6.5 6.5 0 1 1-13 0a6.5 6.5 0 1 1 13 0Z", "M20 20l-4.2-4.2")
    val Plus = stroke("plus", 2.2f, "M12 5v14M5 12h14")
    val Close = stroke("close", 2f, "M7 7l10 10M17 7L7 17")
    val Calendar = stroke("calendar", "M6.5 5.5h11a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2h-11a2 2 0 0 1-2-2v-11a2 2 0 0 1 2-2Z", "M4.5 10h15M8.5 3.5v4M15.5 3.5v4")
    val Trash = stroke("trash", "M4 7h16M10 7V4.5h4V7M6.5 7l1 13h9l1-13")

    fun forCategory(c: Category): ImageVector = when (c) {
        Category.Food -> Food
        Category.Groceries -> Groceries
        Category.Transport -> Transport
        Category.Bills -> Bills
        Category.Shopping -> Shopping
        Category.Health -> Health
        Category.Fun -> Fun
        Category.Other -> Other
    }
}

private fun stroke(name: String, vararg paths: String): ImageVector = stroke(name, 1.8f, *paths)

private fun stroke(name: String, width: Float, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()
