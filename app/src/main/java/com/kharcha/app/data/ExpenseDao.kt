package com.kharcha.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<Expense>>

    @Upsert
    suspend fun upsert(expense: Expense)

    @Insert
    suspend fun insertAll(expenses: List<Expense>)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE sample = 1")
    suspend fun deleteSamples()

    @Query("SELECT value FROM settings WHERE name = :name")
    fun observeSetting(name: String): Flow<String?>

    @Upsert
    suspend fun putSetting(setting: Setting)

    @Query("DELETE FROM settings WHERE name = :name")
    suspend fun deleteSetting(name: String)
}
