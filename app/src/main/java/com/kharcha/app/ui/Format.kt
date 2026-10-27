package com.kharcha.app.ui

import android.icu.text.NumberFormat
import android.icu.util.ULocale
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

/** Indian-grouped rupee formatting (₹1,23,456.5) and the date patterns the app uses. */
object Fmt {
    private val locale: Locale = Locale.forLanguageTag("en-IN")
    private val uLocale: ULocale = ULocale.forLanguageTag("en-IN")

    private val upToTwo = NumberFormat.getInstance(uLocale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    private val whole = NumberFormat.getInstance(uLocale).apply {
        maximumFractionDigits = 0
    }

    fun number2(rupees: Double): String = upToTwo.format(rupees)
    fun number0(rupees: Double): String = whole.format(rupees.roundToLong())

    fun money(paise: Long): String = "₹" + number2(paise / 100.0)
    fun money0(rupees: Double): String = "₹" + number0(rupees)

    /** Amount as typed into the form when editing: "1249" or "12.5". */
    fun plain(paise: Long): String =
        if (paise % 100 == 0L) (paise / 100).toString()
        else BigDecimal.valueOf(paise).movePointLeft(2).stripTrailingZeros().toPlainString()

    /** Axis labels: 950, 1.5k, 2.4L. */
    fun short(rupees: Double): String = when {
        rupees >= 100_000 -> oneDecimal(rupees / 100_000) + "L"
        rupees >= 1_000 -> oneDecimal(rupees / 1_000) + "k"
        else -> rupees.roundToLong().toString()
    }

    private fun oneDecimal(x: Double): String {
        val r = (x * 10).roundToLong()
        return if (r % 10 == 0L) (r / 10).toString() else "${r / 10}.${r % 10}"
    }

    val monthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    val monthShortYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy", locale)
    val monthLong: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", locale)
    val monthShort: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)
    val dayLong: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", locale)
    val dayShort: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", locale)
    val dateField: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale)

    fun plural(n: Int, one: String, many: String) = "$n ${if (n == 1) one else many}"
}
