package com.kharcha.app.ui

import com.kharcha.app.data.Expense
import com.kharcha.app.model.Category
import com.kharcha.app.model.PayMethod
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

enum class PaceLevel { Good, Warn, Bad }

data class Pace(val level: PaceLevel, val text: String)

/** "₹1,200 less than Sep by this date": [amount] is shown bold, [text] follows it. */
data class Comparison(val amount: String?, val text: String)

data class CategoryTotal(val category: Category, val paise: Long, val count: Int)

data class MethodTotal(val method: PayMethod, val paise: Long)

data class DayGroup(val date: String, val label: String, val totalPaise: Long, val items: List<Expense>)

/** Everything the home screen draws, worked out from the raw expenses for the month in view. */
data class HomeState(
    val loaded: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val month: YearMonth = YearMonth.now(),
    val isCurrentMonth: Boolean = true,
    val daysInMonth: Int = YearMonth.now().lengthOfMonth(),
    /** Days elapsed in the month in view: today's date for this month, the full length for past months. */
    val dayNow: Int = LocalDate.now().dayOfMonth,
    val totalPaise: Long = 0,
    val entryCount: Int = 0,
    val comparison: Comparison? = null,
    /** Monthly budget in whole rupees; 0 means none. */
    val budget: Long = 0,
    val pace: Pace? = null,
    val perDayPaise: List<Long> = emptyList(),
    val perDayCount: List<Int> = emptyList(),
    val categories: List<CategoryTotal> = emptyList(),
    val methods: List<MethodTotal> = emptyList(),
    val topCategory: Category? = null,
    val filter: Category? = null,
    val query: String = "",
    val groups: List<DayGroup> = emptyList(),
    val visibleCount: Int = 0,
    val hasAnyExpenses: Boolean = false,
    val hasSamples: Boolean = false,
)

fun buildHomeState(
    all: List<Expense>,
    budget: Long,
    month: YearMonth,
    filter: Category?,
    query: String,
    today: LocalDate,
): HomeState {
    val isCurrent = month == YearMonth.from(today)
    val daysInMonth = month.lengthOfMonth()
    val dayNow = if (isCurrent) today.dayOfMonth else daysInMonth
    val key = month.toString()
    val inMonth = all.filter { it.date.startsWith(key) }
    val total = inMonth.sumOf { it.amountPaise }

    // Compare with the previous month, up to the same day of the month.
    val previous = month.minusMonths(1)
    val previousKey = previous.toString()
    val previousTotal = all
        .filter { it.date.startsWith(previousKey) && (!isCurrent || dayOf(it) <= dayNow) }
        .sumOf { it.amountPaise }
    val previousName = previous.format(Fmt.monthShort)
    val byDate = if (isCurrent) " by this date" else ""
    val comparison = when {
        total == 0L -> Comparison(null, if (isCurrent) "Nothing logged yet this month" else "Nothing logged this month")
        previousTotal == 0L -> Comparison(null, "Across ${Fmt.plural(inMonth.size, "entry", "entries")}")
        else -> {
            val diff = total - previousTotal
            if (abs(diff) < 100) Comparison(null, "Same as $previousName$byDate")
            else Comparison(
                Fmt.money0(abs(diff) / 100.0),
                " ${if (diff < 0) "less" else "more"} than $previousName$byDate",
            )
        }
    }

    val budgetPaise = budget * 100
    val pace = if (budget > 0) {
        val spentFraction = total.toDouble() / budgetPaise
        val timeFraction = dayNow.toDouble() / daysInMonth
        when {
            total > budgetPaise -> Pace(PaceLevel.Bad, "Over budget")
            isCurrent && spentFraction > timeFraction + 0.05 -> Pace(PaceLevel.Warn, "Spending ahead of pace")
            else -> Pace(PaceLevel.Good, if (isCurrent) "On track" else "Within budget")
        }
    } else null

    val perDay = LongArray(daysInMonth)
    val perCount = IntArray(daysInMonth)
    inMonth.forEach {
        val index = dayOf(it) - 1
        if (index in 0 until daysInMonth) {
            perDay[index] += it.amountPaise
            perCount[index]++
        }
    }

    val categories = inMonth
        .groupBy { Category.of(it.category) }
        .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amountPaise }, items.size) }
        .sortedByDescending { it.paise }

    val methods = PayMethod.entries.mapNotNull { method ->
        val sum = inMonth.filter { PayMethod.of(it.method) == method }.sumOf { it.amountPaise }
        if (sum > 0) MethodTotal(method, sum) else null
    }

    val q = query.trim().lowercase()
    val visible = inMonth
        .filter { filter == null || Category.of(it.category) == filter }
        .filter { q.isEmpty() || it.note.lowercase().contains(q) || Category.of(it.category).title.lowercase().contains(q) }
        .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.createdAt })

    val todayKey = today.toString()
    val yesterdayKey = today.minusDays(1).toString()
    val groups = visible.groupBy { it.date }.map { (date, items) ->
        val label = when (date) {
            todayKey -> "Today"
            yesterdayKey -> "Yesterday"
            else -> runCatching { LocalDate.parse(date).format(Fmt.dayLong) }.getOrDefault(date)
        }
        DayGroup(date, label, items.sumOf { it.amountPaise }, items)
    }

    return HomeState(
        loaded = true,
        today = today,
        month = month,
        isCurrentMonth = isCurrent,
        daysInMonth = daysInMonth,
        dayNow = dayNow,
        totalPaise = total,
        entryCount = inMonth.size,
        comparison = comparison,
        budget = budget,
        pace = pace,
        perDayPaise = perDay.toList(),
        perDayCount = perCount.toList(),
        categories = categories,
        methods = methods,
        topCategory = categories.firstOrNull()?.category,
        filter = filter,
        query = query,
        groups = groups,
        visibleCount = visible.size,
        hasAnyExpenses = all.isNotEmpty(),
        hasSamples = all.any { it.sample },
    )
}

private fun dayOf(e: Expense): Int = e.date.substring(8, 10).toIntOrNull() ?: 0
