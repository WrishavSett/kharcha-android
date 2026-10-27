package com.kharcha.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One logged expense. Money is kept in paise so totals never pick up floating-point drift.
 */
@Entity(tableName = "expenses", indices = [Index("date")])
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long,
    /** A [com.kharcha.app.model.Category] id, e.g. "food". */
    val category: String,
    val note: String,
    /** ISO date, yyyy-MM-dd. Sorting and month filtering work directly on the string. */
    val date: String,
    /** "UPI", "Card" or "Cash". */
    val method: String,
    val createdAt: Long,
    /** True for the example entries, so they can be cleared in one go. */
    val sample: Boolean = false,
)

/** Small key/value table; holds the monthly budget. */
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val name: String,
    val value: String,
)
