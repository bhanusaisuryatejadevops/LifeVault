package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class Category(val displayName: String, val iconName: String) {
    ELECTRONICS("Electronics", "devices"),
    APPLIANCES("Appliances", "kitchen"),
    HOME_FURNITURE("Home & Furniture", "chair"),
    VEHICLES("Vehicles & Auto", "directions_car"),
    CLOTHING_ACCESSORIES("Clothing & Jewelry", "watch"),
    DOCUMENTS_ID("Documents & IDs", "badge"),
    HEALTH_FITNESS("Health & Fitness", "fitness_center"),
    SUBSCRIPTIONS_SERVICE("Services & Contracts", "handshake"),
    OTHER("Other", "inventory_2");

    companion object {
        fun fromString(name: String): Category {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class ItemStatus(val label: String) {
    RETURN_URGENT("Return Window Closing"),
    EXPIRING_SOON("Expiring Soon"),
    ACTIVE("Active Coverage"),
    EXPIRED("Expired"),
    PERMANENT("No Expiry Set")
}

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = Category.OTHER.name,
    val storeName: String = "",
    val price: Double = 0.0,
    val currency: String = "USD",
    val purchaseDate: Long = System.currentTimeMillis(),
    val warrantyEndDate: Long? = null,
    val returnDeadlineDate: Long? = null,
    val documentExpiryDate: Long? = null,
    val customReminderDate: Long? = null,
    val reminderEnabled: Boolean = true,
    val receiptImagePath: String? = null,
    val notes: String = "",
    val serialNumber: String = "",
    val isArchived: Boolean = false,
    val isDemo: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedPrice(): String {
        if (price <= 0.0) return ""
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            if (currency == "EUR") format.currency = java.util.Currency.getInstance("EUR")
            else if (currency == "GBP") format.currency = java.util.Currency.getInstance("GBP")
            else if (currency == "CAD") format.currency = java.util.Currency.getInstance("CAD")
            format.format(price)
        } catch (_: Exception) {
            String.format(Locale.US, "%.2f %s", price, currency)
        }
    }

    fun getCategoryEnum(): Category = Category.fromString(category)

    /**
     * Primary upcoming deadline date in milliseconds, or null if none set.
     */
    fun getNearestUpcomingDeadline(now: Long = System.currentTimeMillis()): Pair<String, Long>? {
        val deadlines = mutableListOf<Pair<String, Long>>()
        returnDeadlineDate?.let { if (it >= now) deadlines.add("Return Deadline" to it) }
        warrantyEndDate?.let { if (it >= now) deadlines.add("Warranty Expiry" to it) }
        documentExpiryDate?.let { if (it >= now) deadlines.add("Document Expiry" to it) }
        customReminderDate?.let { if (it >= now) deadlines.add("Reminder Date" to it) }

        if (deadlines.isNotEmpty()) {
            return deadlines.minByOrNull { it.second }
        }

        // If none are in the future, return the latest expired deadline
        val expired = mutableListOf<Pair<String, Long>>()
        warrantyEndDate?.let { expired.add("Warranty Expired" to it) }
        documentExpiryDate?.let { expired.add("Document Expired" to it) }
        returnDeadlineDate?.let { expired.add("Return Window Closed" to it) }
        return expired.maxByOrNull { it.second }
    }

    fun getStatus(now: Long = System.currentTimeMillis()): ItemStatus {
        // 1. Check return deadline (most urgent if within 7 days)
        returnDeadlineDate?.let {
            if (it >= now && it - now <= TimeUnit.DAYS.toMillis(7)) {
                return ItemStatus.RETURN_URGENT
            }
        }

        // 2. Check warranty or document expiry
        val criticalDeadline = warrantyEndDate ?: documentExpiryDate
        if (criticalDeadline != null) {
            if (criticalDeadline < now) {
                return ItemStatus.EXPIRED
            }
            if (criticalDeadline - now <= TimeUnit.DAYS.toMillis(30)) {
                return ItemStatus.EXPIRING_SOON
            }
            return ItemStatus.ACTIVE
        }

        if (returnDeadlineDate != null) {
            return if (returnDeadlineDate < now) ItemStatus.EXPIRED else ItemStatus.ACTIVE
        }

        return ItemStatus.PERMANENT
    }

    fun getDaysRemaining(targetDate: Long?, now: Long = System.currentTimeMillis()): Long? {
        if (targetDate == null) return null
        val diff = targetDate - now
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    companion object {
        fun formatDate(millis: Long?): String {
            if (millis == null) return "None"
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}
