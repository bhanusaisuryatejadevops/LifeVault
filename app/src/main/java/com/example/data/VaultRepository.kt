package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VaultRepository(private val vaultDao: VaultDao) {

    val activeItems: Flow<List<VaultItem>> = vaultDao.getAllActive()
    val archivedItems: Flow<List<VaultItem>> = vaultDao.getAllArchived()
    val activeCount: Flow<Int> = vaultDao.countActive()

    fun getItem(id: Long): Flow<VaultItem?> = vaultDao.getItemById(id)

    suspend fun getItemDirect(id: Long): VaultItem? = vaultDao.getItemByIdDirect(id)

    fun search(query: String): Flow<List<VaultItem>> {
        return if (query.isBlank()) {
            vaultDao.getAllActive()
        } else {
            vaultDao.searchItems(query.trim())
        }
    }

    suspend fun insertItem(item: VaultItem): Long = vaultDao.insert(item)

    suspend fun updateItem(item: VaultItem) = vaultDao.update(item)

    suspend fun deleteItem(item: VaultItem) = vaultDao.delete(item)

    suspend fun deleteById(id: Long) = vaultDao.deleteById(id)

    suspend fun clearAll() = vaultDao.deleteAll()

    suspend fun clearDemoData() = vaultDao.deleteDemoItems()

    suspend fun getAllItemsDirect(): List<VaultItem> = vaultDao.getAllItemsDirect()

    /**
     * Seeds realistic demo data into the local database for showcase or testing.
     * All items are explicitly marked with `isDemo = true`.
     */
    suspend fun loadDemoData() {
        val now = System.currentTimeMillis()
        val oneDay = TimeUnit.DAYS.toMillis(1)

        val demoItems = listOf(
            VaultItem(
                title = "MacBook Pro 16\" M3 Max",
                category = Category.ELECTRONICS.name,
                storeName = "Apple Store Fifth Ave",
                price = 3499.00,
                currency = "USD",
                purchaseDate = now - (90 * oneDay),
                warrantyEndDate = now + (275 * oneDay), // Active (approx 9 months left)
                returnDeadlineDate = now - (76 * oneDay), // Past return window
                serialNumber = "C02G99XXMD6R",
                notes = "AppleCare+ active through warranty expiration. Includes charger and MagSafe cable.",
                isDemo = true
            ),
            VaultItem(
                title = "Sony WH-1000XM5 Headphones",
                category = Category.ELECTRONICS.name,
                storeName = "Best Buy",
                price = 398.00,
                currency = "USD",
                purchaseDate = now - (23 * oneDay),
                warrantyEndDate = now + (342 * oneDay),
                returnDeadlineDate = now + (7 * oneDay), // Return window closes in 7 days!
                serialNumber = "SN-948102384",
                notes = "Keep original packaging if deciding to exchange for silver color.",
                isDemo = true
            ),
            VaultItem(
                title = "Dyson V15 Detect Vacuum",
                category = Category.APPLIANCES.name,
                storeName = "Dyson Direct",
                price = 749.99,
                currency = "USD",
                purchaseDate = now - (350 * oneDay),
                warrantyEndDate = now + (15 * oneDay), // Warranty expires in 15 days!
                serialNumber = "DY-V15-2023-99",
                notes = "2-year manufacturer warranty. Contact support before expiration for filter replacement.",
                isDemo = true
            ),
            VaultItem(
                title = "International Passport",
                category = Category.DOCUMENTS_ID.name,
                storeName = "National Passport Agency",
                price = 165.00,
                currency = "USD",
                purchaseDate = now - (1800 * oneDay),
                documentExpiryDate = now + (180 * oneDay), // Expiring in 6 months
                customReminderDate = now + (150 * oneDay),
                serialNumber = "P88392019",
                notes = "Renew at least 6 months prior to international travel.",
                isDemo = true
            ),
            VaultItem(
                title = "Breville Barista Touch Espresso",
                category = Category.APPLIANCES.name,
                storeName = "Williams Sonoma",
                price = 999.95,
                currency = "USD",
                purchaseDate = now - (740 * oneDay),
                warrantyEndDate = now - (10 * oneDay), // Expired 10 days ago!
                serialNumber = "BES880BSS",
                notes = "Descaling kit used every 3 months. Standard 2-year warranty expired.",
                isDemo = true
            ),
            VaultItem(
                title = "Patagonia Tres 3-in-1 Parka",
                category = Category.CLOTHING_ACCESSORIES.name,
                storeName = "Patagonia Soho",
                price = 599.00,
                currency = "USD",
                purchaseDate = now - (120 * oneDay),
                warrantyEndDate = now + (3650 * oneDay), // Lifetime Ironclad Guarantee
                notes = "Covered under Patagonia Ironclad Guarantee for repairs.",
                isDemo = true
            )
        )
        vaultDao.insertAll(demoItems)
    }

    /**
     * Serializes all user records to structured JSON for backup export.
     */
    suspend fun exportJson(): String {
        val items = vaultDao.getAllItemsDirect()
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "LifeVault")
        root.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("storeName", item.storeName)
            obj.put("price", item.price)
            obj.put("currency", item.currency)
            obj.put("purchaseDate", item.purchaseDate)
            item.warrantyEndDate?.let { obj.put("warrantyEndDate", it) }
            item.returnDeadlineDate?.let { obj.put("returnDeadlineDate", it) }
            item.documentExpiryDate?.let { obj.put("documentExpiryDate", it) }
            item.customReminderDate?.let { obj.put("customReminderDate", it) }
            obj.put("reminderEnabled", item.reminderEnabled)
            obj.put("notes", item.notes)
            obj.put("serialNumber", item.serialNumber)
            obj.put("isArchived", item.isArchived)
            obj.put("isDemo", item.isDemo)
            array.put(obj)
        }
        root.put("items", array)
        return root.toString(2)
    }

    /**
     * Imports structured JSON into the local database safely.
     */
    suspend fun importJson(jsonContent: String): Int {
        val root = JSONObject(jsonContent)
        val array = root.optJSONArray("items") ?: return 0
        val items = mutableListOf<VaultItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(
                VaultItem(
                    title = obj.optString("title", "Imported Item"),
                    category = obj.optString("category", Category.OTHER.name),
                    storeName = obj.optString("storeName", ""),
                    price = obj.optDouble("price", 0.0),
                    currency = obj.optString("currency", "USD"),
                    purchaseDate = obj.optLong("purchaseDate", System.currentTimeMillis()),
                    warrantyEndDate = if (obj.has("warrantyEndDate")) obj.getLong("warrantyEndDate") else null,
                    returnDeadlineDate = if (obj.has("returnDeadlineDate")) obj.getLong("returnDeadlineDate") else null,
                    documentExpiryDate = if (obj.has("documentExpiryDate")) obj.getLong("documentExpiryDate") else null,
                    customReminderDate = if (obj.has("customReminderDate")) obj.getLong("customReminderDate") else null,
                    reminderEnabled = obj.optBoolean("reminderEnabled", true),
                    notes = obj.optString("notes", ""),
                    serialNumber = obj.optString("serialNumber", ""),
                    isArchived = obj.optBoolean("isArchived", false),
                    isDemo = false
                )
            )
        }
        if (items.isNotEmpty()) {
            vaultDao.insertAll(items)
        }
        return items.size
    }
}
