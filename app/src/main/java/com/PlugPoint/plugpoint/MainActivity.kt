package com.PlugPoint.plugpoint

import AppNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import com.PlugPoint.plugpoint.data.DarkModeViewModel
import com.PlugPoint.plugpoint.ui.theme.PlugPointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlugPointTheme {
                val darkModeViewModel: DarkModeViewModel = viewModel(
                    factory = DarkModeViewModelFactory(applicationContext)
                )
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        darkModeViewModel = darkModeViewModel,
                        imgurViewModel = viewModel()
                    )
                }
            }
        }
    }
}
