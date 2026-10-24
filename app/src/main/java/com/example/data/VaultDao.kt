package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_items WHERE isArchived = 0 ORDER BY purchaseDate DESC")
    fun getAllActive(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE isArchived = 1 ORDER BY updatedTimestamp DESC")
    fun getAllArchived(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    fun getItemById(id: Long): Flow<VaultItem?>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemByIdDirect(id: Long): VaultItem?

    @Query("SELECT * FROM vault_items ORDER BY id ASC")
    suspend fun getAllItemsDirect(): List<VaultItem>

    @Query("""
        SELECT * FROM vault_items 
        WHERE isArchived = 0 
          AND (title LIKE '%' || :query || '%' 
               OR storeName LIKE '%' || :query || '%' 
               OR notes LIKE '%' || :query || '%' 
               OR serialNumber LIKE '%' || :query || '%')
        ORDER BY purchaseDate DESC
    """)
    fun searchItems(query: String): Flow<List<VaultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VaultItem>)

    @Update
    suspend fun update(item: VaultItem)

    @Delete
    suspend fun delete(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vault_items")
    suspend fun deleteAll()

    @Query("DELETE FROM vault_items WHERE isDemo = 1")
    suspend fun deleteDemoItems()

    @Query("SELECT COUNT(*) FROM vault_items WHERE isArchived = 0")
    fun countActive(): Flow<Int>
}
