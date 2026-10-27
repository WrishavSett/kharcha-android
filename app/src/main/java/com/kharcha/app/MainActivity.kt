package com.kharcha.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kharcha.app.ui.HomeScreen
import com.kharcha.app.ui.KharchaViewModel
import com.kharcha.app.ui.theme.KharchaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as KharchaApp).repository
        setContent {
            KharchaTheme {
                val vm: KharchaViewModel = viewModel { KharchaViewModel(repository) }
                HomeScreen(vm)
            }
        }
    }
}
