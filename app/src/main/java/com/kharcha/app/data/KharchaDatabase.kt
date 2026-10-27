package com.kharcha.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Expense::class, Setting::class], version = 1, exportSchema = false)
abstract class KharchaDatabase : RoomDatabase() {

    abstract fun dao(): ExpenseDao

    companion object {
        fun create(context: Context): KharchaDatabase =
            Room.databaseBuilder(context.applicationContext, KharchaDatabase::class.java, "kharcha.db")
                .build()
    }
}
