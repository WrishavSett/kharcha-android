package com.kharcha.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

class ExpenseRepository(private val dao: ExpenseDao) {

    val expenses: Flow<List<Expense>> = dao.observeAll()

    /** Monthly budget in whole rupees; 0 means no budget. */
    val budget: Flow<Long> = dao.observeSetting(BUDGET).map { it?.toLongOrNull() ?: 0L }

    suspend fun save(expense: Expense) = dao.upsert(expense)

    suspend fun delete(expense: Expense) = dao.delete(expense)

    suspend fun setBudget(rupees: Long) {
        if (rupees > 0) dao.putSetting(Setting(BUDGET, rupees.toString())) else dao.deleteSetting(BUDGET)
    }

    suspend fun addSamples(today: LocalDate) = dao.insertAll(SampleData.build(today))

    suspend fun clearSamples() = dao.deleteSamples()

    private companion object {
        const val BUDGET = "monthly_budget"
    }
}

/** The same example book as the web version: this month up to today, plus a few entries last month. */
object SampleData {

    private class Row(val day: Int, val rupees: Long, val category: String, val note: String, val method: String)

    private val thisMonth = listOf(
        Row(1, 18000, "bills", "Flat rent", "UPI"), Row(1, 1249, "bills", "Electricity bill", "UPI"),
        Row(2, 642, "groceries", "Vegetables and fruit", "UPI"), Row(3, 200, "transport", "Metro card recharge", "UPI"),
        Row(4, 340, "food", "Lunch with friends", "Cash"), Row(5, 499, "fun", "Streaming subscription", "Card"),
        Row(6, 1890, "shopping", "New kurta", "Card"), Row(7, 260, "transport", "Cab to office", "UPI"),
        Row(8, 90, "food", "Chai and samosa", "Cash"), Row(9, 410, "health", "Pharmacy", "UPI"),
        Row(10, 1120, "groceries", "Monthly staples", "UPI"), Row(12, 650, "food", "Dinner out", "Card"),
        Row(14, 1500, "other", "Birthday gift", "UPI"), Row(15, 120, "transport", "Auto rickshaw", "Cash"),
    )

    private val lastMonth = listOf(
        Row(1, 18000, "bills", "Flat rent", "UPI"), Row(3, 2380, "groceries", "Monthly staples", "UPI"),
        Row(6, 1650, "food", "Weekend brunch", "Card"), Row(9, 3200, "shopping", "Running shoes", "Card"),
        Row(12, 540, "transport", "Cabs this week", "UPI"), Row(20, 899, "fun", "Concert ticket", "UPI"),
    )

    fun build(today: LocalDate): List<Expense> {
        val now = System.currentTimeMillis()
        val current = YearMonth.from(today)
        val previous = current.minusMonths(1)
        val dated = thisMonth.filter { it.day <= today.dayOfMonth }.map { it to current.atDay(it.day) } +
            lastMonth.map { it to previous.atDay(minOf(it.day, previous.lengthOfMonth())) }
        return dated.mapIndexed { i, (row, date) ->
            Expense(
                amountPaise = row.rupees * 100,
                category = row.category,
                note = row.note,
                date = date.toString(),
                method = row.method,
                createdAt = now + i,
                sample = true,
            )
        }
    }
}
