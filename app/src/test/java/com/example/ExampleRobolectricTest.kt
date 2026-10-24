package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Category
import com.example.data.VaultDatabase
import com.example.data.VaultItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: VaultDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("LifeVault", appName)
    }

    @Test
    fun testRoomDaoInsertAndQuery() = runBlocking {
        val dao = database.vaultDao()
        val item = VaultItem(
            title = "iPad Air M2",
            category = Category.ELECTRONICS.name,
            storeName = "Apple Store",
            price = 599.00
        )

        val id = dao.insert(item)
        assertTrue(id > 0)

        val retrieved = dao.getItemByIdDirect(id)
        assertNotNull(retrieved)
        assertEquals("iPad Air M2", retrieved?.title)
        assertEquals("Apple Store", retrieved?.storeName)
        assertEquals(599.00, retrieved?.price ?: 0.0, 0.001)

        // Test update
        val updated = retrieved!!.copy(notes = "Includes Apple Pencil Pro")
        dao.update(updated)
        val refreshed = dao.getItemByIdDirect(id)
        assertEquals("Includes Apple Pencil Pro", refreshed?.notes)

        // Test delete
        dao.deleteById(id)
        val deleted = dao.getItemByIdDirect(id)
        assertEquals(null, deleted)
    }
}
