package com.kharcha.app

import android.app.Application
import com.kharcha.app.data.ExpenseRepository
import com.kharcha.app.data.KharchaDatabase

class KharchaApp : Application() {
    val repository: ExpenseRepository by lazy {
        ExpenseRepository(KharchaDatabase.create(this).dao())
    }
}
