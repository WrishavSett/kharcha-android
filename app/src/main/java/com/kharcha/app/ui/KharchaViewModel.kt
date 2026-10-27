package com.kharcha.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kharcha.app.data.Expense
import com.kharcha.app.data.ExpenseRepository
import com.kharcha.app.model.Category
import com.kharcha.app.model.PayMethod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/** What the add/edit sheet hands back once the input is valid. */
data class ExpenseDraft(
    val amountPaise: Long,
    val category: Category,
    val note: String,
    val date: LocalDate,
    val method: PayMethod,
)

sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data class Deleted(val expense: Expense) : UiEvent
}

class KharchaViewModel(private val repo: ExpenseRepository) : ViewModel() {

    private data class Options(
        val month: YearMonth,
        val filter: Category?,
        val query: String,
        val today: LocalDate,
    )

    private val options = MutableStateFlow(
        LocalDate.now().let { Options(YearMonth.from(it), null, "", it) },
    )

    val state: StateFlow<HomeState> =
        combine(repo.expenses, repo.budget, options) { all, budget, o ->
            buildHomeState(all, budget, o.month, o.filter, o.query, o.today)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    fun previousMonth() = options.update { it.copy(month = it.month.minusMonths(1), filter = null) }

    fun nextMonth() = options.update {
        if (it.month < YearMonth.from(it.today)) it.copy(month = it.month.plusMonths(1), filter = null) else it
    }

    fun thisMonth() = options.update { it.copy(month = YearMonth.from(it.today), filter = null) }

    fun toggleFilter(category: Category) =
        options.update { it.copy(filter = if (it.filter == category) null else category) }

    fun clearFilter() = options.update { it.copy(filter = null) }

    fun setQuery(query: String) = options.update { it.copy(query = query) }

    /** Called on resume, so an app left open overnight rolls over to the new day (and month). */
    fun refreshToday() {
        val now = LocalDate.now()
        options.update {
            if (it.today == now) it
            else {
                val wasOnCurrent = it.month == YearMonth.from(it.today)
                it.copy(today = now, month = if (wasOnCurrent) YearMonth.from(now) else it.month)
            }
        }
    }

    fun save(draft: ExpenseDraft, editing: Expense?) {
        val expense = Expense(
            id = editing?.id ?: 0,
            amountPaise = draft.amountPaise,
            category = draft.category.id,
            note = draft.note.ifBlank { draft.category.title },
            date = draft.date.toString(),
            method = draft.method.label,
            createdAt = editing?.createdAt ?: System.currentTimeMillis(),
        )
        launchSafely("Could not save. Try again.") {
            repo.save(expense)
            options.update { it.copy(month = YearMonth.from(draft.date)) }
            val message = if (editing != null) "Changes saved"
            else "Added ${Fmt.money(expense.amountPaise)} · ${draft.category.title}"
            _events.send(UiEvent.Message(message))
        }
    }

    fun delete(expense: Expense) = launchSafely("Could not delete. Try again.") {
        repo.delete(expense)
        _events.send(UiEvent.Deleted(expense))
    }

    fun restore(expense: Expense) = launchSafely("Could not restore. Add it again from the form.") {
        repo.save(expense)
        _events.send(UiEvent.Message("Restored"))
    }

    fun setBudget(rupees: Long) = launchSafely("Could not save the budget. Try again.") {
        repo.setBudget(rupees)
        _events.send(
            UiEvent.Message(if (rupees > 0) "Budget set to ${Fmt.money0(rupees.toDouble())} a month" else "Budget removed"),
        )
    }

    fun addSamples() = launchSafely("Could not add examples. Try again.") {
        repo.addSamples(options.value.today)
        options.update { it.copy(month = YearMonth.from(it.today), filter = null) }
        _events.send(UiEvent.Message("Example entries added"))
    }

    fun clearSamples() = launchSafely("Could not clear examples. Try again.") {
        repo.clearSamples()
        _events.send(UiEvent.Message("Examples cleared"))
    }

    private fun launchSafely(failure: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(UiEvent.Message(failure))
            }
        }
    }
}
