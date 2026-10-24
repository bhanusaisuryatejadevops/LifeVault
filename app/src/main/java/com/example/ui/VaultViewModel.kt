package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backup.BackupManager
import com.example.data.Category
import com.example.data.ItemStatus
import com.example.data.VaultItem
import com.example.data.VaultRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SortOrder(val label: String) {
    DEADLINE_SOONEST("Deadline (Soonest)"),
    PURCHASE_NEWEST("Purchase Date (Newest)"),
    PURCHASE_OLDEST("Purchase Date (Oldest)"),
    PRICE_HIGHEST("Price (Highest)"),
    TITLE_AZ("Name (A to Z)")
}

data class DashboardMetrics(
    val activeCoverageCount: Int = 0,
    val urgentReturnCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val totalProtectedValue: Double = 0.0,
    val upcomingItems: List<VaultItem> = emptyList(),
    val recentItems: List<VaultItem> = emptyList()
)

class VaultViewModel(
    application: Application,
    private val repository: VaultRepository
) : AndroidViewModel(application) {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<Category?>(null)
    val selectedStatusFilter = MutableStateFlow<ItemStatus?>(null)
    val sortOrder = MutableStateFlow(SortOrder.DEADLINE_SOONEST)

    val activeItems: StateFlow<List<VaultItem>> = repository.activeItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val archivedItems: StateFlow<List<VaultItem>> = repository.archivedItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getItem(id: Long) = repository.getItem(id)

    val filteredItems: StateFlow<List<VaultItem>> = combine(
        activeItems,
        searchQuery,
        selectedCategory,
        selectedStatusFilter,
        sortOrder
    ) { items, query, category, status, sort ->
        val now = System.currentTimeMillis()
        var result = items

        // 1. Text Search Filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.storeName.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.serialNumber.lowercase().contains(q)
            }
        }

        // 2. Category Filter
        if (category != null) {
            result = result.filter { it.getCategoryEnum() == category }
        }

        // 3. Status Filter
        if (status != null) {
            result = result.filter { it.getStatus(now) == status }
        }

        // 4. Sorting
        when (sort) {
            SortOrder.DEADLINE_SOONEST -> {
                result.sortedWith(compareBy(
                    { it.getNearestUpcomingDeadline(now)?.second ?: Long.MAX_VALUE },
                    { -it.purchaseDate }
                ))
            }
            SortOrder.PURCHASE_NEWEST -> result.sortedByDescending { it.purchaseDate }
            SortOrder.PURCHASE_OLDEST -> result.sortedBy { it.purchaseDate }
            SortOrder.PRICE_HIGHEST -> result.sortedByDescending { it.price }
            SortOrder.TITLE_AZ -> result.sortedBy { it.title.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dashboardMetrics: StateFlow<DashboardMetrics> = activeItems.combine(activeItems) { items, _ ->
        val now = System.currentTimeMillis()
        var activeCoverage = 0
        var urgentReturns = 0
        var expiringSoon = 0
        var expired = 0
        var totalValue = 0.0

        val upcomingList = mutableListOf<Pair<Long, VaultItem>>()

        for (item in items) {
            totalValue += item.price
            when (item.getStatus(now)) {
                ItemStatus.RETURN_URGENT -> urgentReturns++
                ItemStatus.EXPIRING_SOON -> expiringSoon++
                ItemStatus.EXPIRED -> expired++
                ItemStatus.ACTIVE -> activeCoverage++
                ItemStatus.PERMANENT -> activeCoverage++
            }

            val nearest = item.getNearestUpcomingDeadline(now)
            if (nearest != null && nearest.second >= now) {
                upcomingList.add(nearest.second to item)
            }
        }

        DashboardMetrics(
            activeCoverageCount = activeCoverage,
            urgentReturnCount = urgentReturns,
            expiringSoonCount = expiringSoon,
            expiredCount = expired,
            totalProtectedValue = totalValue,
            upcomingItems = upcomingList.sortedBy { it.first }.map { it.second }.take(5),
            recentItems = items.sortedByDescending { it.createdTimestamp }.take(4)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    fun saveItem(item: VaultItem, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (item.id == 0L) {
                repository.insertItem(item)
            } else {
                repository.updateItem(item)
                item.id
            }

            // Schedule reminder alarms on local device
            val updated = item.copy(id = id)
            AlarmScheduler.scheduleItemReminders(getApplication(), updated)
            onDone(id)
        }
    }

    fun deleteItem(item: VaultItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelItemReminders(getApplication(), item.id)
            repository.deleteItem(item)
        }
    }

    fun archiveItem(item: VaultItem) {
        viewModelScope.launch {
            val archived = item.copy(isArchived = true, updatedTimestamp = System.currentTimeMillis())
            AlarmScheduler.cancelItemReminders(getApplication(), item.id)
            repository.updateItem(archived)
        }
    }

    fun unarchiveItem(item: VaultItem) {
        viewModelScope.launch {
            val active = item.copy(isArchived = false, updatedTimestamp = System.currentTimeMillis())
            repository.updateItem(active)
            if (active.reminderEnabled) {
                AlarmScheduler.scheduleItemReminders(getApplication(), active)
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            repository.clearDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val items = repository.getAllItemsDirect()
            for (item in items) {
                AlarmScheduler.cancelItemReminders(getApplication(), item.id)
            }
            repository.clearAll()
        }
    }

    suspend fun exportBackup(password: String?): String = withContext(Dispatchers.IO) {
        val json = repository.exportJson()
        if (!password.isNullOrBlank()) {
            BackupManager.encrypt(json, password)
        } else {
            json
        }
    }

    suspend fun importBackup(content: String, password: String?): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonToImport = if (BackupManager.isEncryptedPayload(content)) {
                if (password.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Password required for encrypted backup."))
                }
                BackupManager.decrypt(content, password)
            } else {
                content
            }
            val count = repository.importJson(jsonToImport)
            AlarmScheduler.rescheduleAll(getApplication())
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
