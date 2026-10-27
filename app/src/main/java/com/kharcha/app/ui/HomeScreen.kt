package com.kharcha.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.app.data.Expense
import com.kharcha.app.model.Category
import com.kharcha.app.model.PayMethod
import com.kharcha.app.ui.theme.Bricolage
import com.kharcha.app.ui.theme.KType
import com.kharcha.app.ui.theme.Kharcha
import com.kharcha.app.ui.theme.PlexMono
import kotlinx.coroutines.launch

private sealed interface SheetTarget {
    data object Add : SheetTarget
    data class Edit(val expense: Expense) : SheetTarget
}

private val ContentMaxWidth = 640.dp

@Composable
fun HomeScreen(vm: KharchaViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val c = Kharcha.colors
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var sheet by remember { mutableStateOf<SheetTarget?>(null) }
    var budgetOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var lastCategory by rememberSaveable { mutableStateOf(Category.Food) }
    var lastMethod by rememberSaveable { mutableStateOf(PayMethod.Upi) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshToday() }

    // A new message replaces whatever is showing, like the web toast.
    LaunchedEffect(vm) {
        vm.events.collect { event ->
            snackbar.currentSnackbarData?.dismiss()
            scope.launch {
                when (event) {
                    is UiEvent.Message -> snackbar.showSnackbar(event.text, duration = SnackbarDuration.Short)
                    is UiEvent.Deleted -> {
                        val result = snackbar.showSnackbar(
                            message = "Deleted “${event.expense.note}”",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Long,
                        )
                        if (result == SnackbarResult.ActionPerformed) vm.restore(event.expense)
                    }
                }
            }
        }
    }

    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        containerColor = c.bg,
        contentColor = c.ink,
        topBar = {
            TopBar(
                state = state,
                onPrevious = vm::previousMonth,
                onNext = vm::nextMonth,
                onThisMonth = vm::thisMonth,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add expense", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(KIcons.Plus, contentDescription = null, modifier = Modifier.size(22.dp)) },
                onClick = { sheet = SheetTarget.Add },
                expanded = fabExpanded,
                containerColor = c.accent,
                contentColor = c.accentInk,
            )
        },
    ) { padding ->
        val direction = LocalLayoutDirection.current
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(direction) + 16.dp,
                    end = padding.calculateEndPadding(direction) + 16.dp,
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                ),
            ) {
                item(key = "hero") {
                    HeroCard(state, onEditBudget = { budgetOpen = true })
                }
                item(key = "gap-1") { Spacer(Modifier.height(14.dp)) }
                item(key = "insights") {
                    InsightsCard(state, onToggleFilter = vm::toggleFilter)
                }
                item(key = "gap-2") { Spacer(Modifier.height(14.dp)) }
                transactionBook(
                    state = state,
                    query = query,
                    onQuery = {
                        query = it
                        vm.setQuery(it)
                    },
                    onEdit = { sheet = SheetTarget.Edit(it) },
                    onClearFilter = vm::clearFilter,
                    onLoadSamples = vm::addSamples,
                    onClearSamples = vm::clearSamples,
                )
            }
        }
    }

    sheet?.let { target ->
        val editing = (target as? SheetTarget.Edit)?.expense
        ExpenseSheet(
            editing = editing,
            initialCategory = lastCategory,
            initialMethod = lastMethod,
            today = state.today,
            onDismiss = { sheet = null },
            onSave = { draft ->
                if (editing == null) {
                    lastCategory = draft.category
                    lastMethod = draft.method
                }
                vm.save(draft, editing)
            },
            onDelete = { vm.delete(it) },
        )
    }

    if (budgetOpen) {
        BudgetDialog(
            current = state.budget,
            onDismiss = { budgetOpen = false },
            onSave = {
                budgetOpen = false
                vm.setBudget(it)
            },
        )
    }

    // The search box text lives here; keep the view model in step after process recreation.
    LaunchedEffect(Unit) { if (query.isNotEmpty()) vm.setQuery(query) }
}

@Composable
private fun TopBar(
    state: HomeState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onThisMonth: () -> Unit,
) {
    val c = Kharcha.colors
    Box(
        Modifier
            .fillMaxWidth()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark()
                Spacer(Modifier.width(8.dp))
                Text("Kharcha", style = KType.brand, color = c.ink, modifier = Modifier.weight(1f))
                MonthNav(state, onPrevious, onNext)
            }
            if (!state.isCurrentMonth) {
                Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Back to this month",
                        color = c.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(role = Role.Button, onClick = onThisMonth)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandMark() {
    val c = Kharcha.colors
    Box(
        Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(c.heroBg),
        contentAlignment = Alignment.Center,
    ) {
        Text("₹", color = c.heroFill, fontFamily = PlexMono, fontSize = 17.sp)
    }
}

@Composable
private fun MonthNav(state: HomeState, onPrevious: () -> Unit, onNext: () -> Unit) {
    val c = Kharcha.colors
    Row(
        Modifier
            .clip(CircleShape)
            .background(c.surface)
            .border(1.dp, c.line, CircleShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(KIcons.ChevronLeft, "Previous month", enabled = true, onClick = onPrevious)
        Text(
            state.month.format(Fmt.monthShortYear),
            fontFamily = Bricolage,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 76.dp),
        )
        RoundButton(KIcons.ChevronRight, "Next month", enabled = !state.isCurrentMonth, onClick = onNext)
    }
}

@Composable
private fun RoundButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val c = Kharcha.colors
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClickLabel = label, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.3f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = c.ink, modifier = Modifier.size(18.dp))
    }
}
