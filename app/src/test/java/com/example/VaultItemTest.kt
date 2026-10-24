package com.example

import com.example.data.Category
import com.example.data.ItemStatus
import com.example.data.VaultItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class VaultItemTest {

    @Test
    fun testPriceFormatting() {
        val item = VaultItem(
            title = "Test Phone",
            price = 999.99,
            currency = "USD"
        )
        val formatted = item.getFormattedPrice()
        assertTrue(formatted.contains("999.99"))
    }

    @Test
    fun testReturnUrgentStatus() {
        val now = System.currentTimeMillis()
        val item = VaultItem(
            title = "Headphones",
            purchaseDate = now - TimeUnit.DAYS.toMillis(5),
            returnDeadlineDate = now + TimeUnit.DAYS.toMillis(3) // 3 days remaining (< 7 days)
        )
        assertEquals(ItemStatus.RETURN_URGENT, item.getStatus(now))
    }

    @Test
    fun testExpiringSoonStatus() {
        val now = System.currentTimeMillis()
        val item = VaultItem(
            title = "Vacuum Cleaner",
            warrantyEndDate = now + TimeUnit.DAYS.toMillis(15) // 15 days remaining (< 30 days)
        )
        assertEquals(ItemStatus.EXPIRING_SOON, item.getStatus(now))
    }

    @Test
    fun testExpiredStatus() {
        val now = System.currentTimeMillis()
        val item = VaultItem(
            title = "Old Laptop",
            warrantyEndDate = now - TimeUnit.DAYS.toMillis(10) // 10 days in the past
        )
        assertEquals(ItemStatus.EXPIRED, item.getStatus(now))
    }

    @Test
    fun testActiveStatus() {
        val now = System.currentTimeMillis()
        val item = VaultItem(
            title = "New TV",
            warrantyEndDate = now + TimeUnit.DAYS.toMillis(200) // 200 days left
        )
        assertEquals(ItemStatus.ACTIVE, item.getStatus(now))
    }

    @Test
    fun testNearestUpcomingDeadline() {
        val now = System.currentTimeMillis()
        val returnTime = now + TimeUnit.DAYS.toMillis(5)
        val warrantyTime = now + TimeUnit.DAYS.toMillis(180)

        val item = VaultItem(
            title = "Smart Watch",
            returnDeadlineDate = returnTime,
            warrantyEndDate = warrantyTime
        )

        val nearest = item.getNearestUpcomingDeadline(now)
        assertNotNull(nearest)
        assertEquals("Return Deadline", nearest?.first)
        assertEquals(returnTime, nearest?.second)
    }

    @Test
    fun testCategoryFromString() {
        assertEquals(Category.ELECTRONICS, Category.fromString("ELECTRONICS"))
        assertEquals(Category.APPLIANCES, Category.fromString("Appliances"))
        assertEquals(Category.OTHER, Category.fromString("non_existent_category"))
    }
}
