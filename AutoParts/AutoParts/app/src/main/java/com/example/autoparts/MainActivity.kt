package com.example.autoparts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autoparts.navigation.NavManager
import com.example.autoparts.ui.theme.AutoPartsTheme
import com.example.autoparts.viewmodel.RepuestosViewModel
import com.example.autoparts.viewmodel.SesionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoPartsTheme {
                val viewModel: RepuestosViewModel = viewModel()
                val sesionViewModel: SesionViewModel = viewModel()      // NUEVO
                NavManager(viewModel, sesionViewModel)                  // CAMBIO
            }
        }
    }
}
